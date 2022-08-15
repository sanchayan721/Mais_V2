package universe.containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ContainerProxy;
import jade.wrapper.ControllerException;

import java.util.*;

public class RemoteContainer extends AgentContainer{

    /* ID of the Container */
    String id;

    public String getId() {
        return this.id;
    }


    /* Container Controller of this Container */
    ContainerController ownContainerController;

    public ContainerController getOwnContainerController() {
        return this.ownContainerController;
    }

    protected ContainerController createOwnController(String id){
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl(false);
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.CONTAINER_NAME, "Container-".concat(id));
        return runtime.createAgentContainer(profile);
    }



    /* Coordinates of This Container */
    public int[] coordinates;
    public int[] getCoordinates(){
        return this.coordinates;
    }


    /* Neighbouring Containers */
    ArrayList<ContainerController> neighbouringContainers;

    public void setNeighbouringContainers(ArrayList<ContainerController> containerList){
        this.neighbouringContainers = containerList;
    }

    public ArrayList<ContainerController> getNeighbouringContainers() {
        return this.neighbouringContainers;
    }


    /* Agent Registry */
    HashMap<String, AgentController> agentRegistry = new HashMap<>();
    public void setAgentRegistry(String agentName, AgentController agentController){
        this.agentRegistry.put(agentName, agentController);
    }
    public HashMap<String, AgentController> getAgentRegistry (){
        return this.agentRegistry;
    }
    public AgentController findInRegistry(String agentName){
        return  this.agentRegistry.get(agentName);
    }



    /* Queries */
    public Boolean anyCellAlive(){
        boolean state = false;
        for (String agentName: agentRegistry.keySet()){
            state = agentName.toLowerCase().contains("cell".toLowerCase());
        }
        return state;
    }

    public int howManyCellsPresent(){
        int cellCount = 0;
        for (String agentName: agentRegistry.keySet()){
            if(agentName.toLowerCase().contains("cell".toLowerCase())){
                cellCount++;
            }
        }
        return cellCount;
    }

    public Boolean isVirusAlive(){
        boolean state = false;
        for (String agentName: agentRegistry.keySet()){
            state = agentName.toLowerCase().contains("virus".toLowerCase());
        }
        return state;
    }

    /* Instantiation */
    public RemoteContainer(ContainerProxy cp, jade.core.AgentContainer impl, String platformName) {
        super(cp, impl, platformName);

        /* this.id = id;
        this.coordinates = coordinates;
        this.ownContainerController = createOwnController(id); */

    }

}
