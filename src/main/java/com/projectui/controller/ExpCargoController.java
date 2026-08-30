package com.projectui.controller;

import com.projectui.dao.CargoDao;

public class ExpCargoController {
    private final CargoDao cargoDao = new CargoDao();

    public String registerCargo(String cargoName, String cargoType, String quantity, String weight, String origin, String destination) {
        String cargoId = cargoDao.generateCargoId();
        cargoDao.saveCargo(cargoId, cargoName, cargoType, quantity, weight, origin, destination, "Registered");
        return cargoId;
    }

    public String addCargo(String cargoName, String cargoType, int quantity, double weight, double cargoValue, String originPort, String destinationPort, String priority) {
        String cargoId = cargoDao.generateCargoId();
        cargoDao.saveCargo(cargoId, cargoName, cargoType, String.valueOf(quantity), String.valueOf(weight), String.valueOf(cargoValue), originPort, destinationPort, priority, "Registered");
        return cargoId;
    }
}
