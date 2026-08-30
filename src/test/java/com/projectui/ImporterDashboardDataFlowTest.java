package com.projectui;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.view.importer.ImporterDashboard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ImporterDashboardDataFlowTest {

    @Test
    public void testExporterToImporterShipmentDataFlow() {
        CargoDao cargoDao = new CargoDao();
        ContainerDao containerDao = new ContainerDao();

        // 1. Exporter registers a cargo
        String cargoId = cargoDao.generateCargoId();
        cargoDao.saveCargo(
                cargoId,
                "Precision Engineering Spares",
                "Machinery / Electronics",
                "250",
                "15000",
                "5000000",
                "Port of Mumbai",
                "Cochin Port",
                "High",
                "In Transit"
        );

        // 2. Verify cargo is saved in CargoDao
        Map<String, Object> saved = cargoDao.getCargo(cargoId);
        Assertions.assertNotNull(saved, "Cargo must be saved in CargoDao");
        Assertions.assertEquals("Precision Engineering Spares", saved.get("cargoName"));
        Assertions.assertEquals("In Transit", saved.get("status"));
        Assertions.assertEquals("Port of Mumbai", saved.get("originPort"));
        Assertions.assertEquals("Cochin Port", saved.get("destinationPort"));

        // 3. Exporter books a container for this cargo
        String bookingId = containerDao.generateBookingId();
        containerDao.saveBooking(
                bookingId,
                cargoId,
                "Precision Engineering Spares",
                "40 ft Standard Dry",
                "1",
                "Cochin Port",
                "Port of Mumbai",
                "Cochin Port",
                45000.0,
                "In Transit",
                "Exporter",
                "Nayara Energy Ltd."
        );

        Map<String, Object> booking = containerDao.getBookingByCargoId(cargoId);
        Assertions.assertNotNull(booking, "Booking must be linked to Cargo ID");
        Assertions.assertEquals("Nayara Energy Ltd.", booking.get("exporter"));

        // 4. Importer updates status to Delivered
        cargoDao.updateCargoStatus(cargoId, "Delivered");
        Map<String, Object> deliveredCargo = cargoDao.getCargo(cargoId);
        Assertions.assertNotNull(deliveredCargo);
        Assertions.assertEquals("Delivered", deliveredCargo.get("status"));
    }
}
