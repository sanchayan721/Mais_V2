package universe.containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

import java.awt.*;

public class PrimaryContainer extends Container {

    /* Container ID */
    String id = "primaryContainer";

    public void setContainerID(String containerID) {
        this.id = containerID;
    }

    public String getContainerID(){
        return this.id;
    }

    /* Own Container Controller */
    ContainerController ownContainerController;
    protected ContainerController createOwnController(){

        Runtime runtime = Runtime.instance();
        Properties properties = new ExtendedProperties();
        properties.setProperty(Profile.GUI, "true");
        Profile profile = new ProfileImpl(properties);
        return runtime.createMainContainer(profile);
    }

    /* Creation of Memory Agent */
    AgentController memoryAgentController;
    public void createMemoryAgent () throws ControllerException {
        String memoryAgentName = "memory";
        AgentController memoryAgentController = this.ownContainerController.createNewAgent(
                memoryAgentName,
                "universe.agents.MemoryAgent",
                new Object[]{}
        );
        memoryAgentController.start();
    }

    public AgentController getMemoryAgentController(){
        return this.memoryAgentController;
    }


    /* Creation of Memory Agent */
    AgentController TCellAgentController;
    public void createTCellAgent () throws ControllerException {
        String memoryAgentName = "t-cell";
        AgentController tCellAgentController = this.ownContainerController.createNewAgent(
                memoryAgentName,
                "universe.agents.TCellAgent",
                new Object[]{}
        );
        tCellAgentController.start();
    }

    public AgentController getTCellAgentController(){
        return this.TCellAgentController;
    }

    /* Instantiation */
    public PrimaryContainer(){
        this.ownContainerController = createOwnController();
    }
}
