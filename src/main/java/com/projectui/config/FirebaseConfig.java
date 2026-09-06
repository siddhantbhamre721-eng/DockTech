package com.projectui.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

/**
 * Firebase & Firestore access helper.
 * Safely initializes FirebaseApp and Firestore client.
 */
public class FirebaseConfig {

    public static final String PROJECT_ID = System.getenv("FIREBASE_PROJECT_ID") != null && !System.getenv("FIREBASE_PROJECT_ID").isBlank()
            ? System.getenv("FIREBASE_PROJECT_ID")
            : "docktech-1f257";

    public static final String API_KEY = System.getenv("FIREBASE_API_KEY") != null && !System.getenv("FIREBASE_API_KEY").isBlank()
            ? System.getenv("FIREBASE_API_KEY")
            : "AIzaSyBOzGx3auPqLh9npVmQvCW73lw5nnFe4No";

    public static final String AUTH_DOMAIN = PROJECT_ID + ".firebaseapp.com";

    private static Firestore firestore;
    private static boolean firebaseAppInitialized = false;

    public static synchronized boolean hasAdminAuth() {
        return firebaseAppInitialized && !FirebaseApp.getApps().isEmpty();
    }

    public static synchronized Firestore getFirestore() {
        if (firestore != null) return firestore;

        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                firebaseAppInitialized = true;
                firestore = FirestoreClient.getFirestore();
                return firestore;
            }

            InputStream serviceAccount = null;
            File keyFile = findServiceAccountFile();
            if (keyFile != null && keyFile.exists()) {
                serviceAccount = new FileInputStream(keyFile);
                System.out.println("[FirebaseConfig] Loading service account credentials from: " + keyFile.getAbsolutePath());
            } else {
                InputStream resourceStream = FirebaseConfig.class.getResourceAsStream("/serviceAccountKey.json");
                if (resourceStream != null) {
                    serviceAccount = resourceStream;
                    System.out.println("[FirebaseConfig] Loading service account credentials from classpath /serviceAccountKey.json");
                }
            }

            if (serviceAccount != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(PROJECT_ID)
                        .build();
                FirebaseApp.initializeApp(options);
                firebaseAppInitialized = true;
                firestore = FirestoreClient.getFirestore();
                return firestore;
            }

            try {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId(PROJECT_ID)
                        .build();
                FirebaseApp.initializeApp(options);
                firestore = FirestoreClient.getFirestore();
                return firestore;
            } catch (Throwable t) {
                try {
                    FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                            .setProjectId(PROJECT_ID)
                            .build();
                    if (firestoreOptions != null) {
                        firestore = firestoreOptions.getService();
                        return firestore;
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ex) {
            System.err.println("Warning: Firestore not initialized: " + ex.getMessage());
        }

        return null;
    }

    private static File findServiceAccountFile() {
        String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (envPath != null && !envPath.isBlank()) {
            File f = new File(envPath);
            if (f.exists() && f.isFile()) return f;
        }

        String customPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_KEY");
        if (customPath != null && !customPath.isBlank()) {
            File f = new File(customPath);
            if (f.exists() && f.isFile()) return f;
        }

        File[] candidates = new File[] {
            new File("serviceAccountKey.json"),
            new File("src/main/resources/serviceAccountKey.json"),
            new File(System.getProperty("user.dir"), "serviceAccountKey.json"),
            new File(System.getProperty("user.home"), "serviceAccountKey.json"),
            new File(System.getProperty("user.home") + "/Downloads", "serviceAccountKey.json")
        };

        for (File f : candidates) {
            if (f.exists() && f.isFile()) return f;
        }

        // Search for any *.json in root starting with serviceAccount or firebase
        File rootDir = new File(".");
        File[] jsonFiles = rootDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json") && (name.toLowerCase().contains("serviceaccount") || name.toLowerCase().contains("firebase") || name.toLowerCase().contains("docktech")));
        if (jsonFiles != null && jsonFiles.length > 0) {
            return jsonFiles[0];
        }

        return null;
    }

}
