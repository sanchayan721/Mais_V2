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
import universe.laws.VirusGenerator;

import java.util.HashMap;
import java.util.List;

public class MainContainer {

    private Boolean need_for_jade_gui = false;

    public MainContainer(Boolean need_for_jade_gui) {
        this.need_for_jade_gui = need_for_jade_gui;
    }

    public AgentContainer createMainContainer() {

        Runtime runtime = Runtime.instance();
        Properties properties = new ExtendedProperties();
        properties.setProperty(Profile.GUI, String.valueOf(need_for_jade_gui));
        Profile profile = new ProfileImpl(properties);
        return runtime.createMainContainer(profile);

    }

    public AgentController createMacrophageAgent(ContainerController containerController, int id,
            ContainerController iContainerController) {
        try {
            String macrophageName = "Macrophage-" + String.valueOf(id);
            AgentController MacrophageAgentController = containerController.createNewAgent(
                    macrophageName, "universe.agents.MacrophageAgent", new Object[] { iContainerController });
            return MacrophageAgentController;

        } catch (StaleProxyException exception) {
            exception.printStackTrace();
            return null;
        }

    }

    public void createInitiatorAgent(
            ContainerController containerController,
            HashMap<ContainerController, int[]> containerControllerGridMap,
            HashMap<String, ContainerController> lymphPathMap,
            HashMap<String, int[]> lymphCoordinateMap,
            AgentController dendriticCellController,
            List<AgentController> CD8CellControllerList,
            List<AgentController> MacrophageControllerList,
            VirusGenerator virusGenerator) {
        try {
            AgentController initiatorAgentController = containerController.createNewAgent(
                    "initiator",
                    "universe.agents.InitiatorAgent",
                    new Object[] { 
                            containerControllerGridMap, 
                            lymphPathMap, 
                            lymphCoordinateMap,
                            dendriticCellController,
                            CD8CellControllerList,
                            MacrophageControllerList,
                            virusGenerator
                         });
            initiatorAgentController.start();
        } catch (StaleProxyException exception) {
            exception.printStackTrace();
        }
    }

    public void createCD4TCellManager(ContainerController containerController, int UNIVERSE_SIZE) {
        try {
            String agentName = "CD4TCellManager";
            AgentController CD4TCellManagerController = containerController.createNewAgent(
                    agentName, "universe.agents.CD4TCellManager", new Object[] { UNIVERSE_SIZE });

            CD4TCellManagerController.start();

        } catch (StaleProxyException exception) {
            exception.printStackTrace();
        }
    }
}
