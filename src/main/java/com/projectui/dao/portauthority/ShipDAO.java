package com.projectui.dao.portauthority;

import java.util.ArrayList;
import java.util.List;

import com.projectui.config.portauthority.FirebaseConfig;
import com.projectui.model.portauthority.Ship;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Firestore;

public class ShipDAO {

    private Firestore db = FirebaseConfig.getFirestore();

    // SAVE SHIP
    public void saveShip(Ship ship) {

        try {

            db.collection("Ships")
              .document(ship.getShipId())
              .create(ship);

            System.out.println("Data Inserted");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET SHIP
    public Ship getShip(String shipId) {

        try {

            ApiFuture<DocumentSnapshot> future =
                db.collection("Ships")
                  .document(shipId)
                  .get();

            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return document.toObject(Ship.class);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // UPDATE SHIP
    public void updateShip(Ship ship) {

        try {

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

            System.out.println("Data Updated");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE SHIP
    public void deleteShip(String shipId) {

        try {

            db.collection("Ships")
              .document(shipId)
              .delete()
              .get();

            System.out.println("Data Deleted");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET ALL SHIPS
    public List<Ship> getShips() {

        List<Ship> list = new ArrayList<>();

        try {

            ApiFuture<QuerySnapshot> future =
                db.collection("Ships").get();

            QuerySnapshot snapshot = future.get();

            for (DocumentSnapshot doc : snapshot.getDocuments()) {

                Ship ship = doc.toObject(Ship.class);

                list.add(ship);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}