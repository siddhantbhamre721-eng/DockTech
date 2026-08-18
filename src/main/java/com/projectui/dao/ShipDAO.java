package com.projectui.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.projectui.config.FirebaseConfig;
import com.projectui.model.Ship;

@SuppressWarnings({"null", "unchecked"})
public class ShipDAO extends Dao {

    private Firestore getDb() {
        Firestore dbInstance = db();
        return dbInstance != null ? dbInstance : FirebaseConfig.getFirestore();
    }

    // SAVE SHIP
    public void saveShip(Ship ship) {
        try {
            Firestore db = getDb();
            if (db != null) {
                db.collection("Ships")
                  .document(ship.getShipId())
                  .set(ship);
                System.out.println("Ship Data Saved");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET SHIP
    public Ship getShip(String shipId) {
        try {
            Firestore db = getDb();
            if (db != null) {
                ApiFuture<DocumentSnapshot> future =
                    db.collection("Ships")
                      .document(shipId)
                      .get();

                DocumentSnapshot document = future.get();
                if (document.exists()) {
                    return document.toObject(Ship.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE SHIP
    public void updateShip(Ship ship) {
        try {
            Firestore db = getDb();
            if (db != null) {
                db.collection("Ships")
                  .document(ship.getShipId())
                  .update(
                      "shipName", ship.getShipName(),
                      "shipType", ship.getShipType(),
                      "imoNumber", ship.getImoNumber(),
                      "company", ship.getCompany(),
                      "arrival", ship.getArrival(),
                      "departure", ship.getDeparture(),
                      "berth", ship.getBerth(),
                      "status", ship.getStatus()    
                  );
                System.out.println("Ship Data Updated");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE SHIP
    public void deleteShip(String shipId) {
        try {
            Firestore db = getDb();
            if (db != null) {
                db.collection("Ships")
                  .document(shipId)
                  .delete()
                  .get();
                System.out.println("Ship Data Deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET ALL SHIPS
    public List<Ship> getShips() {
        List<Ship> list = new ArrayList<>();
        try {
            Firestore db = getDb();
            if (db != null) {
                ApiFuture<QuerySnapshot> future =
                    db.collection("Ships").get();

                QuerySnapshot snapshot = future.get();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Ship ship = doc.toObject(Ship.class);
                    if (ship != null) {
                        list.add(ship);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
