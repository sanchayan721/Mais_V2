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
import universe.laws.Movement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class InitiatorAgent extends Agent {

    HashMap<ContainerController, int[]> containerControllerGridMap;
    public void setContainerControllerGridMap(HashMap<ContainerController, int[]> gridMap){
        this.containerControllerGridMap = gridMap;
    }

    HashMap<ContainerController, Location> containerControllerLocationHashMap = new HashMap<>();
    public void addContainerControllerLocationHashMap(ContainerController cellContainerController, Location cellLocation){
        containerControllerLocationHashMap.put(cellContainerController, cellLocation);
    }

    @Override
    public void setup(){
        Object[] arguments = getArguments();
        setContainerControllerGridMap((HashMap<ContainerController, int[]>) arguments[0]);
        addBehaviour( new InitiatingCell() );
    }

    private class InitiatingCell extends CyclicBehaviour {
        String conversationID = "tell_initiator_about_location";
        String query = "what_is_your_location";
        @Override
        public void action(){
            if (containerControllerLocationHashMap.size() == containerControllerGridMap.size()) {
                myAgent.addBehaviour(new AssigningNeighbours());
                myAgent.removeBehaviour(this);
            } else {
                for(ContainerController cellContainerController: containerControllerGridMap.keySet()){
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

                        if(reply != null && reply.getSender().equals(agentID)){
                            Location cellLocation = (Location) reply.getContentObject();
                            if(containerControllerLocationHashMap.get(cellContainerController) == null){
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
        public void action () {
            for(ContainerController cellContainerController: containerControllerGridMap.keySet()) {
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
            myAgent.removeBehaviour(this);
        }

        private ArrayList<Location> findNeighbourLocation(ContainerController containerController) throws ControllerException {
            ArrayList<Location> neighbourLocationList = new ArrayList<>();
            ArrayList<ContainerController> neighbouringContainerController;
            Movement movement = new Movement();
            neighbouringContainerController = movement.getAdjacentContainerControllers(containerController);
            for (ContainerController eachNeighbourController : neighbouringContainerController){
                neighbourLocationList.add(containerControllerLocationHashMap.get(eachNeighbourController));
            }
            return neighbourLocationList;
        }
    }
}
