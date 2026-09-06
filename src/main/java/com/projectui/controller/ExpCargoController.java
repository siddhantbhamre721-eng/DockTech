package com.projectui.controller;

import com.projectui.dao.CargoDao;

public class ExpCargoController {
    private final CargoDao cargoDao = new CargoDao();

    public String registerCargo(String cargoName, String cargoType, String quantity, String weight, String origin, String destination) {
        String cargoId = cargoDao.generateCargoId();
        cargoDao.saveCargo(cargoId, cargoName, cargoType, quantity, weight, origin, destination, "Registered").join();
        return cargoId;
    }

    public String addCargo(String cargoName, String cargoType, int quantity, double weight, double cargoValue, String originPort, String destinationPort, String priority) {
        return addCargo(cargoName, cargoType, quantity, weight, cargoValue, originPort, destinationPort, priority, null, null, null, null);
    }

    public String addCargo(String cargoName, String cargoType, int quantity, double weight, double cargoValue,
            String originPort, String destinationPort, String priority,
            String importerName, String importerEmail, String importerId, String importerCompany) {
        if (!Double.isFinite(cargoValue) || cargoValue <= 0.0 || cargoValue >= 50000.0) {
            throw new IllegalArgumentException("Estimated Value must be greater than ₹0 and less than ₹50,000.");
        }
        String cargoId = cargoDao.generateCargoId();
        cargoDao.saveCargo(cargoId, cargoName, cargoType, String.valueOf(quantity), String.valueOf(weight),
                String.valueOf(cargoValue), originPort, destinationPort, priority,
                importerName, importerEmail, importerId, importerCompany, "Registered").join();
        return cargoId;
    }
}
