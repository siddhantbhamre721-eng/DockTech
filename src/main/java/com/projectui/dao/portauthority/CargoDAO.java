package com.projectui.dao.portauthority;

import java.util.ArrayList;
import java.util.List;

import com.projectui.config.portauthority.FirebaseConfig;
import com.projectui.model.portauthority.Cargo;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Firestore;

public class CargoDAO {

    private Firestore db = FirebaseConfig.getFirestore();

    // SAVE CARGO
    public void saveCargo(Cargo cargo) {

        try {

            db.collection("Cargo")
              .document(cargo.getCargoId())
              .create(cargo)
              .get();

            System.out.println("Cargo Inserted");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET CARGO
    public Cargo getCargo(String cargoId) {

        try {

            ApiFuture<DocumentSnapshot> future =
                db.collection("Cargo")
                  .document(cargoId)
                  .get();

            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return document.toObject(Cargo.class);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // UPDATE CARGO
    public void updateCargo(Cargo cargo) {

        try {

            db.collection("Cargo")
              .document(cargo.getCargoId())
              .update(
                  "ship", cargo.getShip(),
                  "cargoType", cargo.getCargoType(),
                  "weight", cargo.getWeight(),
                  "origin", cargo.getOrigin(),
                  "destination", cargo.getDestination(),
                  "containers", cargo.getContainers(),
                  "status", cargo.getStatus()
              )
              .get();

            System.out.println("Cargo Updated");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE CARGO
    public void deleteCargo(String cargoId) {

        try {

            db.collection("Cargo")
              .document(cargoId)
              .delete()
              .get();

            System.out.println("Cargo Deleted");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET ALL CARGO
    public List<Cargo> getCargos() {

        List<Cargo> list = new ArrayList<>();

        try {

            ApiFuture<QuerySnapshot> future =
                db.collection("Cargo").get();

            QuerySnapshot snapshot = future.get();

            for (DocumentSnapshot doc : snapshot.getDocuments()) {

                Cargo cargo = doc.toObject(Cargo.class);

                list.add(cargo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}