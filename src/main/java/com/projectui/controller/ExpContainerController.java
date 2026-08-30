package com.projectui.controller;

import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;

public class ExpContainerController {
    private final ContainerDao containerDao = new ContainerDao();
    private final CargoDao cargoDao = new CargoDao();

    public String bookContainer(String cargoId, String containerType, String shippingLine, String preferredPort,
            String destination) {
        return bookContainer(cargoId, "Cargo " + cargoId, containerType, "1", preferredPort, "Port of Mumbai",
                destination, shippingLine, 45000.0);
    }

    public String bookContainer(String cargoId, String cargoName, String containerType, String quantity,
            String preferredPort, String originPort, String destinationPort, String shippingLine, double totalAmount) {
        String bookingId = containerDao.generateBookingId();
        String customerType = "Exporter";
        String userName = "Global Logistics Ltd.";

        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Map<String, Object> u = com.projectui.view.login.LoginPage.loggedInUser;
            if (u.get("role") != null && !u.get("role").toString().isBlank()) {
                customerType = u.get("role").toString().trim();
            }
            if (u.get("company") != null && !u.get("company").toString().isBlank()) {
                userName = u.get("company").toString().trim();
            } else if (u.get("name") != null && !u.get("name").toString().isBlank()) {
                userName = u.get("name").toString().trim();
            } else if (u.get("email") != null && !u.get("email").toString().isBlank()) {
                userName = u.get("email").toString().trim();
            }
        }

        containerDao.saveBooking(bookingId, cargoId, cargoName, containerType, quantity, preferredPort, originPort,
                destinationPort, totalAmount, "Booked", customerType, userName);
        if (cargoId != null && !cargoId.isBlank()) {
            cargoDao.updateCargoStatus(cargoId, "Container Booked");
        }
        return bookingId;
    }
}
