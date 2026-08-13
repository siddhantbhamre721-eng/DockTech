package com.projectui.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

/**
 * Minimal Firestore access helper. Attempts to construct a Firestore client
 * via default credentials. If not available at runtime, callers should handle
 * a null return value. This preserves compilation and keeps runtime behaviour
 * unchanged if proper credentials/config are present on the host.
 */
public class FirebaseConfig {

	private static Firestore firestore;

	public static synchronized Firestore getFirestore() {
		if (firestore != null) return firestore;
		try {
			firestore = FirestoreOptions.getDefaultInstance().getService();
			return firestore;
		} catch (Exception ex) {
			// Preserve non-failing compile-time behaviour; runtime will suffer if credentials missing.
			System.err.println("Warning: Firestore not initialized: " + ex.getMessage());
			return null;
		}
	}

}
