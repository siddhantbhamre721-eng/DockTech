package com.projectui.controller;

import java.util.List;

import com.projectui.dao.ShipDAO;
import com.projectui.model.Ship;

public class ShipController {

    private ShipDAO shipDAO = new ShipDAO();

    // =====================================================
    // SAVE SHIP
    // =====================================================

    public void saveShip(Ship ship) {
        shipDAO.saveShip(ship);
    }

    // =====================================================
    // GET SHIP
    // =====================================================

    public Ship getShip(String shipId) {
        return shipDAO.getShip(shipId);
    }

    // =====================================================
    // UPDATE SHIP
    // =====================================================

    public void updateShip(Ship ship) {
        shipDAO.updateShip(ship);
    }

    // =====================================================
    // DELETE SHIP
    // =====================================================

    public void deleteShip(String shipId) {
        shipDAO.deleteShip(shipId);
    }

    // =====================================================
    // GET ALL SHIPS
    // =====================================================

    public List<Ship> getShips() {
        return shipDAO.getShips();
    }
}
