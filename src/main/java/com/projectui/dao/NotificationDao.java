package com.projectui.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.QuerySnapshot;
import com.projectui.config.FirebaseConfig;
import com.projectui.model.Notification;

import javafx.application.Platform;

@SuppressWarnings({"null", "unchecked"})
public class NotificationDao extends Dao {

    private static final Map<String, Notification> localNotificationCache = new ConcurrentHashMap<>();

    private Firestore getDb() {
        Firestore dbInstance = db();
        return dbInstance != null ? dbInstance : FirebaseConfig.getFirestore();
    }

    public static String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    /**
     * Create and persist a notification into Firestore.
     */
    public void createNotification(Notification notif) {
        if (notif == null || notif.getUserId() == null || notif.getUserId().isBlank()) return;

        if (notif.getId() == null || notif.getId().isBlank()) {
            notif.setId("NOTIF-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 899 + 100));
        }
        if (notif.getCreatedAtEpoch() == 0) {
            notif.setCreatedAtEpoch(System.currentTimeMillis());
        }
        if (notif.getTimestamp() == null || notif.getTimestamp().isBlank()) {
            notif.setTimestamp(generateTimestamp());
        }

        localNotificationCache.put(notif.getId(), notif);

        try {
            Firestore db = getDb();
            if (db != null) {
                db.collection("notifications").document(notif.getId()).set(notif);
                System.out.println("[NOTIF CREATED] -> " + notif.getTitle() + " for user " + notif.getUserId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve all notifications for a specific user.
     */
    public List<Notification> getNotificationsForUser(String userId) {
        List<Notification> list = new ArrayList<>();
        if (userId == null || userId.isBlank()) return list;

        String uKey = userId.toLowerCase().trim();

        // Check local cache first
        for (Notification n : localNotificationCache.values()) {
            if (n != null && n.getUserId() != null && n.getUserId().toLowerCase().trim().equals(uKey)) {
                list.add(n);
            }
        }

        try {
            Firestore db = getDb();
            if (db != null) {
                ApiFuture<QuerySnapshot> future = db.collection("notifications")
                        .whereEqualTo("userId", userId)
                        .get();

                QuerySnapshot snap = future.get();
                for (DocumentSnapshot doc : snap.getDocuments()) {
                    Notification notif = doc.toObject(Notification.class);
                    if (notif != null) {
                        localNotificationCache.put(notif.getId(), notif);
                        if (list.stream().noneMatch(existing -> existing.getId().equals(notif.getId()))) {
                            list.add(notif);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Silently fall back to cached list
        }

        // Sort descending by epoch
        list.sort((a, b) -> Long.compare(b.getCreatedAtEpoch(), a.getCreatedAtEpoch()));
        return list;
    }

    /**
     * Count unread notifications for a user.
     */
    public int getUnreadCount(String userId) {
        if (userId == null || userId.isBlank()) return 0;
        List<Notification> all = getNotificationsForUser(userId);
        int unread = 0;
        for (Notification n : all) {
            if (!n.isRead()) unread++;
        }
        return unread;
    }

    /**
     * Mark a notification as read.
     */
    public void markAsRead(String notifId) {
        if (notifId == null || notifId.isBlank()) return;

        Notification cached = localNotificationCache.get(notifId);
        if (cached != null) {
            cached.setRead(true);
        }

        try {
            Firestore db = getDb();
            if (db != null) {
                db.collection("notifications").document(notifId).update("read", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mark all notifications for a user as read.
     */
    public void markAllAsRead(String userId) {
        if (userId == null || userId.isBlank()) return;
        List<Notification> userList = getNotificationsForUser(userId);
        for (Notification n : userList) {
            if (n != null && !n.isRead()) {
                markAsRead(n.getId());
            }
        }
    }

    /**
     * Attach a real-time Firestore listener for a specific user's notifications.
     */
    public ListenerRegistration listenToUserNotifications(String userId, Consumer<List<Notification>> listener) {
        if (userId == null || userId.isBlank()) return null;

        try {
            Firestore db = getDb();
            if (db != null) {
                return db.collection("notifications")
                        .whereEqualTo("userId", userId)
                        .addSnapshotListener((snapshots, error) -> {
                            if (error != null) {
                                return;
                            }
                            if (snapshots != null) {
                                List<Notification> updatedList = new ArrayList<>();
                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    Notification notif = doc.toObject(Notification.class);
                                    if (notif != null) {
                                        localNotificationCache.put(notif.getId(), notif);
                                        updatedList.add(notif);
                                    }
                                }
                                updatedList.sort((a, b) -> Long.compare(b.getCreatedAtEpoch(), a.getCreatedAtEpoch()));
                                if (listener != null) {
                                    Platform.runLater(() -> listener.accept(updatedList));
                                }
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
