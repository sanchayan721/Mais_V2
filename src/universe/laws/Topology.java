package universe.laws;

import jade.wrapper.ContainerController;

import java.util.ArrayList;

public class Topology {
    /* Topology type of the system */
    String topologyType = "SquareGrid";
    public void setTopologyType(String topologyType) {
        this.topologyType = topologyType;
    }

    public String getTopologyType() {
        return this.topologyType;
    }

    /*public ArrayList<ContainerController> getNeighbourhoodContainerControllers (ContainerController currentContainerController){
        int[] currentCoordinate = currentContainerController.
    }*/
}
