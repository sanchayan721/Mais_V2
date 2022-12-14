package universe.containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class AuxiliaryContainer {

    ContainerController auxiliaryContainerController;
    String id;

    public ContainerController CreateAuxiliaryContainer(int index) {

        id = String.valueOf(index);

        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl(false);
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.CONTAINER_NAME, "Container-".concat(id));
        auxiliaryContainerController = runtime.createAgentContainer(profile);

        return auxiliaryContainerController;
    }

    public void createCell(ContainerController containerController) {
        try {

            containerController.createNewAgent(
                    "cell.".concat(containerController.getContainerName()),
                    "universe.agents.CellAgent",
                    new Object[] {});

        } catch (Exception exception) {
            exception.getStackTrace();
        }
    }

    public AgentController createDendriticCell(ContainerController containerController, int id) {
        try {

            AgentController dendriticCellController = containerController.createNewAgent(
                    "dendriticcell-" + Integer.toString(id),
                    "universe.agents.DendriticCellAgent",
                    new Object[] {});

            return dendriticCellController;

        } catch (Exception exception) {
            exception.getStackTrace();
            return null;
        }
    }

    public void createLymphVessel(ContainerController containerController) {
        try {

            containerController.createNewAgent(
                    "lymphVessel.".concat(containerController.getContainerName()),
                    "universe.agents.LymphVesselAgent",
                    new Object[] {});

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public AgentController createCD8TCell(ContainerController containerController, int[] virus_signature, int id) {
        try {
            AgentController cd8TcellController = containerController.createNewAgent(
                    "cd8TCell-" + Integer.toString(id),
                    "universe.agents.CD8TCellAgent",
                    new Object[] { virus_signature });

            return cd8TcellController;

        } catch (Exception e) {
            e.getStackTrace();
            return null;
        }
    }

    public static Boolean isCellAlive(ContainerController auxiliaryContainerController) {
        try {
            auxiliaryContainerController.getAgent("cell.".concat(auxiliaryContainerController.getContainerName()));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static Boolean isThereAVirus(ContainerController auxiliaryContainerController) {
        try {
            auxiliaryContainerController.getAgent("virus".concat(auxiliaryContainerController.getContainerName()));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

}
