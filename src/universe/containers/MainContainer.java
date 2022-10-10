package universe.containers;

import jade.wrapper.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.HashMap;

public class MainContainer {

    public AgentContainer createMainContainer() {

        Runtime runtime = Runtime.instance();
        Properties properties = new ExtendedProperties();
        properties.setProperty(Profile.GUI, "true");
        Profile profile = new ProfileImpl(properties);
        return runtime.createMainContainer(profile);

    }

    public void createMacrophageAgent(ContainerController containerController, int id,
            ContainerController iContainerController) {
        try {
            String macrophageName = "Macrophage-" + String.valueOf(id);
            AgentController MacrophageAgentController = containerController.createNewAgent(
                    macrophageName, "universe.agents.MacrophageAgent", new Object[] { iContainerController });
            MacrophageAgentController.start();

        } catch (StaleProxyException exception) {
            exception.printStackTrace();
        }

    }

    public void createInitiatorAgent(
            ContainerController containerController,
            HashMap<ContainerController, int[]> containerControllerGridMap,
            HashMap<String, ContainerController> lymphPathMap,
            HashMap<String, int[]> lymphCoordinateMap) {
        try {
            AgentController initiatorAgentController = containerController.createNewAgent(
                    "initiator",
                    "universe.agents.InitiatorAgent",
                    new Object[] { containerControllerGridMap, lymphPathMap, lymphCoordinateMap });
            initiatorAgentController.start();
        } catch (StaleProxyException exception) {
            exception.printStackTrace();
        }
    }

    public void createCD4TCellManager(ContainerController containerController) {
        try {
            String agentName = "CD4TCellManager";
            AgentController CD4TCellManagerController = containerController.createNewAgent(
                    agentName, "universe.agents.CD4TCellManager", new Object[] { });

            CD4TCellManagerController.start();

        } catch (StaleProxyException exception) {
            exception.printStackTrace();
        }
    }
}
