package com.projectui.dao.portauthority;

import java.util.ArrayList;
import java.util.List;

import com.projectui.config.portauthority.FirebaseConfig;
import com.projectui.model.portauthority.Container;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Firestore;

public class ContainerDAO {

    private Firestore db =
            FirebaseConfig.getFirestore();


    // =====================================================
    // SAVE CONTAINER
    // =====================================================

    public void saveContainer(
            Container container
    ) {

        try {

            db.collection("Containers")
              .document(container.getContainerId())
              .create(container);

            System.out.println(
                    "Container Data Inserted"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ONE CONTAINER
    // =====================================================

    public Container getContainer(
            String containerId
    ) {

        try {

            ApiFuture<DocumentSnapshot> future =
                    db.collection("Containers")
                      .document(containerId)
                      .get();

            DocumentSnapshot document =
                    future.get();


            if (document.exists()) {

                return document.toObject(
                        Container.class
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }


    // =====================================================
    // UPDATE CONTAINER
    // =====================================================

    public void updateContainer(
            Container container
    ) {

        try {

            db.collection("Containers")
              .document(container.getContainerId())
              .update(

                    "type",
                    container.getType(),

                    "cargoId",
                    container.getCargoId(),

                    "ship",
                    container.getShip(),

                    "location",
                    container.getLocation(),

                    "size",
                    container.getSize(),

                    "weight",
                    container.getWeight(),

                    "status",
                    container.getStatus(),

                    "allocationDate",
                    container.getAllocationDate()
              );

            System.out.println(
                    "Container Data Updated"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // DELETE CONTAINER
    // =====================================================

    public void deleteContainer(
            String containerId
    ) {

        try {

            db.collection("Containers")
              .document(containerId)
              .delete()
              .get();

            System.out.println(
                    "Container Data Deleted"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ALL CONTAINERS
    // =====================================================

    public List<Container> getContainers() {

        List<Container> list =
                new ArrayList<>();


        try {

            ApiFuture<QuerySnapshot> future =
                    db.collection("Containers")
                      .get();

            QuerySnapshot snapshot =
                    future.get();


            for (
                    DocumentSnapshot doc :
                    snapshot.getDocuments()
            ) {

                Container container =
                        doc.toObject(
                                Container.class
                        );

                list.add(
                        container
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        return list;
    }
}