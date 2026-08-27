package com.projectui.controller.portauthority;

import java.util.List;

import com.projectui.dao.portauthority.ContainerDAO;
import com.projectui.model.portauthority.Container;

public class ContainerController {

    private ContainerDAO containerDAO =
            new ContainerDAO();


    // =====================================================
    // SAVE CONTAINER
    // =====================================================

    public void saveContainer(
            Container container
    ) {

        containerDAO.saveContainer(
                container
        );
    }


    // =====================================================
    // GET ONE CONTAINER
    // =====================================================

    public Container getContainer(
            String containerId
    ) {

        return containerDAO.getContainer(
                containerId
        );
    }


    // =====================================================
    // UPDATE CONTAINER
    // =====================================================

    public void updateContainer(
            Container container
    ) {

        containerDAO.updateContainer(
                container
        );
    }


    // =====================================================
    // DELETE CONTAINER
    // =====================================================

    public void deleteContainer(
            String containerId
    ) {

        containerDAO.deleteContainer(
                containerId
        );
    }


    // =====================================================
    // GET ALL CONTAINERS
    // =====================================================

    public List<Container> getContainers() {

        return containerDAO.getContainers();
    }
}