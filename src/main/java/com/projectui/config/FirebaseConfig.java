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

    private static Firestore firestore;

    public static synchronized Firestore getFirestore() {
        if (firestore != null) return firestore;

        try {
            if (!FirebaseApp.getApps().isEmpty()) {
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

            if (serviceAccount != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId("fir-demo-4dbd9")
                        .build();
                FirebaseApp.initializeApp(options);
                firestore = FirestoreClient.getFirestore();
                return firestore;
            }

            try {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId("fir-demo-4dbd9")
                        .build();
                FirebaseApp.initializeApp(options);
                firestore = FirestoreClient.getFirestore();
                return firestore;
            } catch (Throwable t) {
                try {
                    FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                            .setProjectId("fir-demo-4dbd9")
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
