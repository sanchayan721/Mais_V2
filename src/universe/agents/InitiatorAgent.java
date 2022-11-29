package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import universe.laws.LymphMap;
import universe.laws.Movement;
import universe.laws.VirusGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InitiatorAgent extends Agent {

    /* Cell Neighbour allocation */
    HashMap<ContainerController, int[]> containerControllerGridMap;

    public void setContainerControllerGridMap(HashMap<ContainerController, int[]> gridMap) {
        this.containerControllerGridMap = gridMap;
    }

    HashMap<ContainerController, Location> containerControllerLocationHashMap = new HashMap<>();

    public void addContainerControllerLocationHashMap(ContainerController cellContainerController,
            Location cellLocation) {
        containerControllerLocationHashMap.put(cellContainerController, cellLocation);
    }

    /* Lymph Vessels path Allocation */
    HashMap<String, ContainerController> lymphVesselHashMap;

    public void setLymphVesselHashMap(HashMap<String, ContainerController> hashMap) {
        this.lymphVesselHashMap = hashMap;
    }

    /* Lymph Vessel Coordinate Map */
    HashMap<String, int[]> lymphVesselCoordinateMap;

    public void setLymphVesselCoordinateMap(HashMap<String, int[]> coordinateMap) {
        this.lymphVesselCoordinateMap = coordinateMap;
    }

    /* Vessel Location Map */
    HashMap<String, Location> vesselLocationHashmap = new HashMap<>();

    public void addOneVesselLocation(String vesselName, Location vesselLocation) {
        vesselLocationHashmap.put(vesselName, vesselLocation);
    }

    AgentController dendriticCellController;
    public void setDendriticCellController(AgentController dendriticCellController) {
        this.dendriticCellController = dendriticCellController;
    }

    List<AgentController> CD8CellControllerList;
    public void setCD8CellControllerList(List<AgentController> CD8CellControllerList) {
        this.CD8CellControllerList = CD8CellControllerList;
    }

    List<AgentController> MacrophageControllerList;
    public void setMacrophageControllerList (List<AgentController> MacrophageControllerList) {
        this.MacrophageControllerList = MacrophageControllerList;
    }

    VirusGenerator virusGenerator;
    public void setVirusGenerator (VirusGenerator virusGenerator) {
        this.virusGenerator = virusGenerator;
    }

    Boolean cellOperationsComplete = false;
    Boolean vesselOperationsComplete = false;

    @Override
    public void setup() {
        Object[] arguments = getArguments();
        setContainerControllerGridMap((HashMap<ContainerController, int[]>) arguments[0]);
        setLymphVesselHashMap((HashMap<String, ContainerController>) arguments[1]);
        setLymphVesselCoordinateMap((HashMap<String, int[]>) arguments[2]);
        setDendriticCellController((AgentController) arguments[3]);
        setCD8CellControllerList((List<AgentController>) arguments[4]);
        setMacrophageControllerList((List<AgentController>) arguments[5]);
        setVirusGenerator((VirusGenerator) arguments[6]);
        

        /* Adding the behaviours */
        addBehaviour(new InitiatingCell());
        addBehaviour(new InitializingLymphVessels());
        addBehaviour(new InitiatingDendriticCell());
        addBehaviour(new InitiatingCD4Cells());
        addBehaviour(new InitiatingMacrophage());
        addBehaviour(new InitiatingVirus());
    }

    /* Cell Initializing Behaviours */
    private class InitiatingCell extends CyclicBehaviour {
        String conversationID = "tell_initiator_about_location";
        String query = "what_is_your_location";

        @Override
        public void action() {
            if (containerControllerLocationHashMap.size() == containerControllerGridMap.size()) {
                myAgent.addBehaviour(new AssigningNeighbours());
                myAgent.removeBehaviour(this);
            } else {
                for (ContainerController cellContainerController : containerControllerGridMap.keySet()) {

                    try {
                        String agentName = "cell.".concat(cellContainerController.getContainerName());
                        AgentController cellAgentController = cellContainerController.getAgent(agentName);
                        cellAgentController.start();

                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        AID agentID = new AID(agentName, AID.ISLOCALNAME);
                        message.addReceiver(agentID);
                        message.setConversationId(conversationID);
                        message.setContent(query);
                        myAgent.send(message);

                        MessageTemplate replyTemplate = MessageTemplate.MatchConversationId(conversationID);
                        ACLMessage reply = receive(replyTemplate);

                        if (reply != null && reply.getSender().equals(agentID)) {
                            Location cellLocation = (Location) reply.getContentObject();
                            if (containerControllerLocationHashMap.get(cellContainerController) == null) {
                                addContainerControllerLocationHashMap(cellContainerController, cellLocation);
                            }
                        }
                    } catch (ControllerException | UnreadableException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class AssigningNeighbours extends OneShotBehaviour {
        String conversationID = "neighbour_allocation_channel";

        @Override
        public void action() {
            for (ContainerController cellContainerController : containerControllerGridMap.keySet()) {
                try {
                    String agentName = "cell.".concat(cellContainerController.getContainerName());
                    AID agentID = new AID(agentName, AID.ISLOCALNAME);
                    ArrayList<Location> neighboursLocation = findNeighbourLocation(cellContainerController);

                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.setConversationId(conversationID);
                    message.addReceiver(agentID);

                    message.setContentObject(neighboursLocation);
                    myAgent.send(message);

                } catch (ControllerException | IOException e) {
                    e.printStackTrace();
                }
            }
            myAgent.addBehaviour(new TellingCellTheyAreVessel());
            cellOperationsComplete = true;
            myAgent.removeBehaviour(this);
        }

        private ArrayList<Location> findNeighbourLocation(ContainerController containerController)
                throws ControllerException {
            ArrayList<Location> neighbourLocationList = new ArrayList<>();
            ArrayList<ContainerController> neighbouringContainerController;
            Movement movement = new Movement();
            neighbouringContainerController = movement.getAdjacentContainerControllers(containerController);
            for (ContainerController eachNeighbourController : neighbouringContainerController) {
                neighbourLocationList.add(containerControllerLocationHashMap.get(eachNeighbourController));
            }
            return neighbourLocationList;
        }
    }

    private class TellingCellTheyAreVessel extends OneShotBehaviour {

        String conversationID = "vessel_cell_connection_channel";

        @Override
        public void action() {
            for (ContainerController cellContainerController : containerControllerGridMap.keySet()) {

                String agentName;
                try {
                    agentName = "cell.".concat(cellContainerController.getContainerName());
                    AID agentID = new AID(agentName, AID.ISLOCALNAME);

                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.setConversationId(conversationID);
                    message.addReceiver(agentID);
                    if (checkIfCellisaVessel(cellContainerController)) {
                        message.setContent("true");
                    } else {
                        message.setContent("false");
                    }
                    myAgent.send(message);
                } catch (ControllerException e) {
                    e.printStackTrace();
                }
            }
        }

        private Boolean checkIfCellisaVessel(ContainerController targetContainerController) {
            Boolean isAVessel = false;

            for (String vessel : lymphVesselHashMap.keySet()) {
                if (!isAVessel && lymphVesselHashMap.get(vessel) == targetContainerController) {
                    isAVessel = true;
                }
            }

            return isAVessel;
        }

    }

    /* Lymph Vessel Initializing Behaviours */
    private class InitializingLymphVessels extends CyclicBehaviour {

        String conversationID = "tell_initiator_about_location";
        String query = "what_is_your_location";

        @Override
        public void action() {

            if (lymphVesselHashMap.size() == vesselLocationHashmap.size()) {
                myAgent.addBehaviour(new AssigningNextVesselLocation());
                myAgent.removeBehaviour(this);
            } else {
                for (String vesselName : lymphVesselHashMap.keySet()) {
                    try {
                        ContainerController vesselContainerController = lymphVesselHashMap.get(vesselName);
                        String vesselAgentName = "lymphVessel.".concat(vesselContainerController.getContainerName());
                        AgentController vessAgentController = vesselContainerController.getAgent(vesselAgentName);
                        vessAgentController.start();

                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        AID agentID = new AID(vesselAgentName, AID.ISLOCALNAME);
                        message.addReceiver(agentID);
                        message.setConversationId(conversationID);
                        message.setContent(query);
                        myAgent.send(message);

                        MessageTemplate replyTemplate = MessageTemplate.MatchConversationId(conversationID);
                        ACLMessage reply = receive(replyTemplate);

                        if (reply != null && reply.getSender().equals(agentID)) {
                            Location vesselLocation = (Location) reply.getContentObject();
                            if (vesselLocationHashmap.get(vesselName) == null) {
                                vesselLocationHashmap.put(vesselName, vesselLocation);
                            }
                        }

                    } catch (ControllerException | UnreadableException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    private class AssigningNextVesselLocation extends OneShotBehaviour {
        String conversationID = "next_vessel_allocation_channel";

        @Override
        public void action() {

            for (String vesselName : vesselLocationHashmap.keySet()) {

                ArrayList<Location> nextVessels = findNextVessels(vesselName);

                try {

                    ContainerController vesselContainerController = lymphVesselHashMap.get(vesselName);
                    String agentName = "lymphVessel.".concat(vesselContainerController.getContainerName());
                    AID agentID = new AID(agentName, AID.ISLOCALNAME);

                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.setConversationId(conversationID);
                    message.addReceiver(agentID);
                    message.setContentObject(nextVessels);
                    myAgent.send(message);

                } catch (ControllerException | IOException e) {
                    e.printStackTrace();
                }
            }

            vesselOperationsComplete = true;
        }

        private ArrayList<Location> findNextVessels(String vesselName) {
            ArrayList<Location> nextVesselLocations = new ArrayList<>();

            int[] currentVesselCoordinate = lymphVesselCoordinateMap.get(vesselName);
            LymphMap lymphMap = new LymphMap();
            ArrayList<int[]> coordinatesOfNextVessels = lymphMap.getNextVessels(currentVesselCoordinate);

            for (int[] coordinate : coordinatesOfNextVessels) {

                String nextVesselName = null;
                for (String vessel : lymphVesselCoordinateMap.keySet()) {

                    if (Arrays.equals(lymphVesselCoordinateMap.get(vessel), coordinate)) {
                        nextVesselName = vessel;
                    }
                }

                if (nextVesselName != null) {
                    Location nextLocation = vesselLocationHashmap.get(nextVesselName);
                    nextVesselLocations.add(nextLocation);
                }
            }

            return nextVesselLocations;
        }

    }

    private class InitiatingDendriticCell extends CyclicBehaviour {

        @Override
        public void action() {
            
            if(cellOperationsComplete && vesselOperationsComplete) {
                try {
                    dendriticCellController.start();
                    myAgent.removeBehaviour(this);
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
            
        }
        
    }

    private class InitiatingCD4Cells extends CyclicBehaviour {

        @Override
        public void action() {
            
            if(cellOperationsComplete && vesselOperationsComplete) {
                for (AgentController CD8CellController : CD8CellControllerList) {
                    try {
                        CD8CellController.start();
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                }
                myAgent.removeBehaviour(this);
            }
            
        }
        
    }

    private class InitiatingMacrophage extends CyclicBehaviour {

        @Override
        public void action() {
            
            if(cellOperationsComplete && vesselOperationsComplete) {
                for (AgentController MacrophageController : MacrophageControllerList) {
                    try {
                        MacrophageController.start();
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                }
                myAgent.removeBehaviour(this);
            }
            
        }
        
    }

    private class InitiatingVirus extends CyclicBehaviour {

        @Override
        public void action () {
            if(cellOperationsComplete && vesselOperationsComplete) {
                virusGenerator.activateVirus();
                myAgent.removeBehaviour(this);
            }
        }
    }
}
