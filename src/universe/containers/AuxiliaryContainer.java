package universe.containers;

import jade.core.Location;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.ArrayList;

public class AuxiliaryContainer {

    ContainerController auxiliaryContainerController;
    String id;

    public ContainerController CreateAuxiliaryContainer(int index){

        id = String.valueOf(index);

        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl(false);
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.CONTAINER_NAME, "Container-".concat(id));
        auxiliaryContainerController = runtime.createAgentContainer(profile);

        return auxiliaryContainerController;
    }

    public void createCell(ContainerController containerController){
        try {

            AgentController cellController = containerController.createNewAgent(
                    "cell.".concat(containerController.getContainerName()),
                    "universe.agents.CellAgent",
                    new Object[]{}
            );

        }catch(Exception exception){
            exception.getStackTrace();
        }
    }

    public void createDendriticCell(ContainerController containerController){
        try {

            AgentController dendriticCellController = containerController.createNewAgent(
                    "dendriticcell.".concat(containerController.getContainerName()),
                    "universe.agents.DendriticCellAgent",
                    new Object[]{}
            );

            dendriticCellController.start();

        }catch(Exception exception){
            exception.getStackTrace();
        }
    }

    public static Boolean isCellAlive(ContainerController auxiliaryContainerController){
        try {
            auxiliaryContainerController.getAgent("cell.".concat(auxiliaryContainerController.getContainerName()));
            return true;
        } catch (Exception ignored){
            return false;
        }
    }

    public static Boolean isThereAVirus(ContainerController auxiliaryContainerController){
        try {
            auxiliaryContainerController.getAgent("virus");
            return true;
        }catch (Exception ignored){
            return false;
        }
    }

}
