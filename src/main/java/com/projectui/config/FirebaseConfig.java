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
            : "fir-demo-4dbd9";

    public static final String API_KEY = System.getenv("FIREBASE_API_KEY") != null && !System.getenv("FIREBASE_API_KEY").isBlank()
            ? System.getenv("FIREBASE_API_KEY")
            : "AIzaSyDO3l4YIlfgO9qvU2XB54k2pBvayr_ID1U";

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
            File keyFile = new File("serviceAccountKey.json");
            if (keyFile.exists()) {
                serviceAccount = new FileInputStream(keyFile);
            } else {
                InputStream resourceStream = FirebaseConfig.class.getResourceAsStream("/serviceAccountKey.json");
                if (resourceStream != null) {
                    serviceAccount = resourceStream;
                }
            }

            String projectId = PROJECT_ID;

            if (serviceAccount != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();
                FirebaseApp.initializeApp(options);
                firebaseAppInitialized = true;
                firestore = FirestoreClient.getFirestore();
                return firestore;
            }

            try {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId(projectId)
                        .build();
                FirebaseApp.initializeApp(options);
                firestore = FirestoreClient.getFirestore();
                return firestore;
            } catch (Throwable t) {
                try {
                    FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                            .setProjectId(projectId)
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

}
