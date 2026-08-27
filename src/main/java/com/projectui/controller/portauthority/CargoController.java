package com.projectui.controller.portauthority;

import java.util.List;

import com.projectui.dao.portauthority.CargoDAO;
import com.projectui.model.portauthority.Cargo;

public class CargoController {

    private CargoDAO cargoDAO;

    public CargoController() {

        cargoDAO = new CargoDAO();
    }

    // SAVE CARGO
    public void saveCargo(Cargo cargo) {

        cargoDAO.saveCargo(cargo);
    }

    // GET CARGO
    public Cargo getCargo(String cargoId) {

        return cargoDAO.getCargo(cargoId);
    }

    // UPDATE CARGO
    public void updateCargo(Cargo cargo) {

        cargoDAO.updateCargo(cargo);
    }

    // DELETE CARGO
    public void deleteCargo(String cargoId) {

        cargoDAO.deleteCargo(cargoId);
    }

    // GET ALL CARGO
    public List<Cargo> getCargos() {

        return cargoDAO.getCargos();
    }
}