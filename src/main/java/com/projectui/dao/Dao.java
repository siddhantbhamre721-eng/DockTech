package com.projectui.dao;

import com.google.cloud.firestore.Firestore;
import com.projectui.config.FirebaseConfig;

/**
 * Base DAO providing access to Firestore. Kept intentionally small — child DAOs
 * call `db()` to access the Firestore instance.
 */
public abstract class Dao {

	protected Firestore db() {
		return FirebaseConfig.getFirestore();
	}

}
