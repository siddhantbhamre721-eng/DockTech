package com.projectui.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import com.projectui.dao.UserDao;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Centralized service managing profile picture uploading, caching,
 * Base64 encoding/decoding, Firestore synchronization, and avatar rendering
 * for all user roles across the DockTech platform.
 */
public class UserProfileImageManager {

    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
    private static final String AVATARS_DIR = System.getProperty("user.home") + File.separator + ".docktech" + File.separator + "avatars";

    static {
        try {
            File dir = new File(AVATARS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception ignored) {}
    }

    public static void addAvatarChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    public static void removeAvatarChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.remove(listener);
        }
    }

    public static void notifyAvatarChanged() {
        for (Runnable r : changeListeners) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    private static String cleanKey(String key) {
        if (key == null || key.isBlank()) return "unknown";
        return key.trim().toLowerCase().replaceAll("[^a-z0-9_@.-]", "_");
    }

    /**
     * Retrieve the current user's profile image or return null if not set.
     */
    public static Image getUserProfileImage(String emailOrUid) {
        if (emailOrUid == null || emailOrUid.isBlank()) {
            if (LoginPage.loggedInUser != null) {
                Object em = LoginPage.loggedInUser.get("email");
                Object ui = LoginPage.loggedInUser.get("uid");
                emailOrUid = em != null ? em.toString() : (ui != null ? ui.toString() : "");
            }
        }
        if (emailOrUid == null || emailOrUid.isBlank()) return null;

        String key = cleanKey(emailOrUid);
        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }

        // 1. Check loggedInUser map for profilePictureUrl / avatar
        String picData = null;
        if (LoginPage.loggedInUser != null) {
            Object obj = LoginPage.loggedInUser.get("profilePictureUrl");
            if (obj == null) obj = LoginPage.loggedInUser.get("avatarUrl");
            if (obj == null) obj = LoginPage.loggedInUser.get("profileImage");
            if (obj != null && !obj.toString().isBlank()) {
                picData = obj.toString().trim();
            }
        }

        // 2. Check localProfileStore
        if (picData == null && UserDao.localProfileStore != null) {
            Map<String, Object> prof = UserDao.localProfileStore.get(emailOrUid.toLowerCase().trim());
            if (prof == null) prof = UserDao.localProfileStore.get(emailOrUid.trim());
            if (prof != null) {
                Object obj = prof.get("profilePictureUrl");
                if (obj == null) obj = prof.get("avatarUrl");
                if (obj == null) obj = prof.get("profileImage");
                if (obj != null && !obj.toString().isBlank()) {
                    picData = obj.toString().trim();
                }
            }
        }

        // Parse Base64 data URI if present
        if (picData != null && picData.startsWith("data:image")) {
            try {
                int commaIdx = picData.indexOf(",");
                if (commaIdx != -1) {
                    byte[] bytes = Base64.getDecoder().decode(picData.substring(commaIdx + 1));
                    Image img = new Image(new ByteArrayInputStream(bytes));
                    if (!img.isError() && img.getWidth() > 0) {
                        imageCache.put(key, img);
                        return img;
                    }
                }
            } catch (Exception ignored) {}
        } else if (picData != null && (picData.startsWith("http://") || picData.startsWith("https://") || picData.startsWith("file:"))) {
            try {
                Image img = new Image(picData, true);
                imageCache.put(key, img);
                return img;
            } catch (Exception ignored) {}
        }

        // 3. Check local disk cache
        try {
            File localFile = new File(AVATARS_DIR, key + ".png");
            if (localFile.exists() && localFile.length() > 0) {
                Image img = new Image(localFile.toURI().toString());
                if (!img.isError() && img.getWidth() > 0) {
                    imageCache.put(key, img);
                    return img;
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Set/upload a user profile image from a selected File.
     */
    public static void setUserProfileImage(String email, String uid, File file, Runnable onSuccess, Consumer<String> onError) {
        if (file == null || !file.exists() || !file.isFile()) {
            if (onError != null) onError.accept("Selected file does not exist.");
            return;
        }

        long fileSize = file.length();
        if (fileSize > 5 * 1024 * 1024) {
            if (onError != null) onError.accept("Image file size exceeds 5MB limit. Please select a smaller photo.");
            return;
        }

        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".webp") && !fileName.endsWith(".gif")) {
            if (onError != null) onError.accept("Unsupported file format. Please upload a JPG, PNG, or WEBP image.");
            return;
        }

        new Thread(() -> {
            try {
                BufferedImage original = ImageIO.read(file);
                if (original == null) {
                    Platform.runLater(() -> {
                        if (onError != null) onError.accept("Could not decode image file.");
                    });
                    return;
                }

                // Square crop and scale to max 256x256 for crisp and lightweight storage
                int size = Math.min(original.getWidth(), original.getHeight());
                int cropX = (original.getWidth() - size) / 2;
                int cropY = (original.getHeight() - size) / 2;
                BufferedImage cropped = original.getSubimage(cropX, cropY, size, size);

                int targetSize = 256;
                BufferedImage scaled = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = scaled.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(cropped, 0, 0, targetSize, targetSize, null);
                g2.dispose();

                // Convert to PNG Base64 Data URI
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(scaled, "png", baos);
                byte[] bytes = baos.toByteArray();
                String base64Data = Base64.getEncoder().encodeToString(bytes);
                String dataUri = "data:image/png;base64," + base64Data;

                String primaryKey = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : (uid != null ? uid : "current_user");
                String cleanK = cleanKey(primaryKey);

                // Save to disk cache
                try {
                    File diskFile = new File(AVATARS_DIR, cleanK + ".png");
                    try (FileOutputStream fos = new FileOutputStream(diskFile)) {
                        fos.write(bytes);
                    }
                } catch (Exception ignored) {}

                // Create JavaFX Image
                Image fxImage = new Image(new ByteArrayInputStream(bytes));
                imageCache.put(cleanK, fxImage);
                if (email != null) imageCache.put(cleanKey(email), fxImage);
                if (uid != null) imageCache.put(cleanKey(uid), fxImage);

                // Update session state
                if (LoginPage.loggedInUser != null) {
                    LoginPage.loggedInUser.put("profilePictureUrl", dataUri);
                }

                // Update local profile store
                if (UserDao.localProfileStore != null) {
                    if (email != null) {
                        Map<String, Object> p = UserDao.localProfileStore.get(email.toLowerCase().trim());
                        if (p != null) p.put("profilePictureUrl", dataUri);
                    }
                    if (uid != null) {
                        Map<String, Object> p = UserDao.localProfileStore.get(uid.trim());
                        if (p != null) p.put("profilePictureUrl", dataUri);
                    }
                }

                // Asynchronously persist to Firestore user record (users/{email})
                String targetDocId = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : uid;
                if (targetDocId != null && !targetDocId.isBlank()) {
                    Map<String, Object> updateFields = new java.util.HashMap<>();
                    updateFields.put("profilePictureUrl", dataUri);
                    new UserDao().updateUserProfileFields(targetDocId, updateFields);
                }

                Platform.runLater(() -> {
                    notifyAvatarChanged();
                    if (onSuccess != null) onSuccess.run();
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    if (onError != null) onError.accept("Failed to process image: " + ex.getMessage());
                });
            }
        }).start();
    }

    /**
     * Opens FileChooser for selecting and uploading profile picture.
     */
    public static void chooseAndUploadProfilePicture(Window owner, String email, String uid, Runnable onComplete, Consumer<String> onError) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Picture");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg, *.jpeg, *.webp)", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.PNG", "*.JPG", "*.JPEG", "*.WEBP"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selected = chooser.showOpenDialog(owner);
        if (selected != null) {
            setUserProfileImage(email, uid, selected, onComplete, onError);
        }
    }

    /**
     * Creates a circular avatar node (image or initials fallback) of the given diameter.
     */
    public static Node createAvatarNode(double size, String initials, String emailOrUid) {
        Image img = getUserProfileImage(emailOrUid);
        if (img != null && !img.isError() && img.getWidth() > 0) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);

            Circle clip = new Circle(size / 2.0, size / 2.0, size / 2.0);
            iv.setClip(clip);

            StackPane sp = new StackPane(iv);
            sp.setAlignment(Pos.CENTER);
            sp.setMinSize(size, size);
            sp.setPrefSize(size, size);
            sp.setMaxSize(size, size);
            sp.setStyle("-fx-background-radius: 999; -fx-border-radius: 999; -fx-border-color: #BFDBFE; -fx-border-width: 1.5;");
            return sp;
        }

        // Initials Avatar
        String init = (initials != null && !initials.isBlank()) ? initials.trim().toUpperCase() : "DT";
        Label label = new Label(init);
        label.setAlignment(Pos.CENTER);
        label.setMinSize(size, size);
        label.setPrefSize(size, size);
        label.setMaxSize(size, size);

        double fontSize = Math.max(10, size * 0.38);
        label.setStyle(
                "-fx-background-color: #0b63ff;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: " + String.format(java.util.Locale.US, "%.1f", fontSize) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: center;" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: #BFDBFE;" +
                "-fx-border-width: 1.5;"
        );
        return label;
    }
}
