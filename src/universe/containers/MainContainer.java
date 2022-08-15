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

    public AgentContainer createMainContainer(){

        Runtime runtime = Runtime.instance();
        Properties properties = new ExtendedProperties();
        properties.setProperty(Profile.GUI, "true");
        Profile profile = new ProfileImpl(properties);
        return runtime.createMainContainer(profile);

    }

    public void createMacrophageAndMemoryAgents(ContainerController containerController){
        try {
            AgentController memoryAgentController = containerController.createNewAgent("memory", "universe.agents.MemoryAgent", new Object[]{});
            AgentController MacrophageAgentController = containerController.createNewAgent("Macrophage", "universe.agents.MacrophageAgent", new Object[]{});
            memoryAgentController.start();
            MacrophageAgentController.start();

        } catch (StaleProxyException exception) {
            exception.printStackTrace();
        }

    }
    public void createInitiatorAgent (ContainerController containerController, HashMap<ContainerController, int[]> containerControllerGridMap) {
        try {
            AgentController initiatorAgentController = containerController.createNewAgent(
                    "initiator",
                    "universe.agents.InitiatorAgent",
                    new Object[]{ containerControllerGridMap }
            );
            initiatorAgentController.start();
        } catch (StaleProxyException exception) {
            exception.printStackTrace();
        }
    }
}
