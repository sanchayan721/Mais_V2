package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import universe.containers.AuxiliaryContainer;
import universe.laws.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CD8TCellAgent extends Agent {

    int[] virus_signature = new int[] {};

    private int[] cellDNAToBeVerified = new int[]{};

    ArrayList<Location> possiblePlacesToMove = new ArrayList<>();

    private void setPossiblePlacesToMove(ArrayList<Location> locs) {
        this.possiblePlacesToMove = locs;
    }

    @Override
    protected void setup() {
        Object[] argumants = getArguments();
        this.virus_signature = (int[]) argumants[0];

        addBehaviour(new AskCellForNeighbours());
    }

    private class AskCellForNeighbours extends OneShotBehaviour {
        String conversationID = "Tell_About_Neighbours";
        String questionForCell = "neighbour_list";

        @Override
        public void action() {
            if (AuxiliaryContainer.isCellAlive(myAgent.getContainerController())) {
                try {
                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.setConversationId(conversationID);
                    String targetCell = "cell."
                            .concat(String.valueOf(myAgent.getContainerController().getContainerName()));
                    message.addReceiver(new AID(targetCell, AID.ISLOCALNAME));
                    message.setContent(questionForCell);
                    send(message);
                    MessageTemplate reply = MessageTemplate.MatchConversationId(conversationID);
                    ACLMessage receivedMessage = receive(reply);
                    if (receivedMessage != null) {
                        ArrayList<Location> locations = (ArrayList<Location>) receivedMessage.getContentObject();
                        setPossiblePlacesToMove(locations);
                    }
                } catch (ControllerException | UnreadableException e) {
                    e.printStackTrace();
                }
            }
            myAgent.addBehaviour(new AskingCellForIdentity());
        }

    }

    private class AskingCellForIdentity extends OneShotBehaviour {
        @Override
        public void action() {

            if (AuxiliaryContainer.isCellAlive(this.getAgent().getContainerController())) {

                ACLMessage messageToCell = new ACLMessage(ACLMessage.INFORM); // Message type
                try {
                    String targetCell = "cell."
                            .concat(String.valueOf(this.getAgent().getContainerController().getContainerName()));
                    messageToCell.addReceiver(new AID(targetCell, AID.ISLOCALNAME)); // receiver
                    messageToCell.setConversationId("Signature_Verification_Channel"); // conversation id
                    String messageContent = "Verify_Identity";
                    messageToCell.setContent(messageContent);
                    send(messageToCell);
                } catch (ControllerException e) {
                    e.printStackTrace();
                }
                doWait(Constants.MACROPHAGE_CELL_COMMUNICATION_TIME);
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
                ACLMessage messageFromCell = receive(messageTemplate);
                if (messageFromCell != null) {
                    try {
                        cellDNAToBeVerified = (int[]) messageFromCell.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
            }
            myAgent.addBehaviour(new DetectingAndKillingContaminant());
        }
    }

    private class DetectingAndKillingContaminant extends OneShotBehaviour {

        @Override
        public void action() {

            if (!Arrays.equals(cellDNAToBeVerified, Constants.CELL_IDENTIFYING_DNA)) {
                ArrayList<Integer> differenceList = new ArrayList<>();
                for (int index = 0; index < cellDNAToBeVerified.length; index++) {
                    if (cellDNAToBeVerified[index] != Constants.CELL_IDENTIFYING_DNA[index]) {
                        differenceList.add(index);
                    }
                }

                int[] differences = differenceList.stream().mapToInt(i -> i).toArray();
                
                if(!Arrays.equals(differences, virus_signature)) {
                    myAgent.addBehaviour(new MovingToNewCell());
                } else {
                    
                }
            } else {
                myAgent.addBehaviour(new MovingToNewCell());
            }
        }
    }

    private class MovingToNewCell extends OneShotBehaviour {
        @Override
        public void action() {
            if (possiblePlacesToMove.size() > 0) {
                Location currentLocation = myAgent.here();
                Random rand = new Random();
                Location locationToMove = possiblePlacesToMove.get(rand.nextInt(possiblePlacesToMove.size()));
                if (!locationToMove.equals(currentLocation)) {
                    doWait(Constants.MACROPHAGE_SLEEP_TIME);
                    doMove(locationToMove);
                }
            }
            myAgent.addBehaviour(new AskCellForNeighbours());
        }
    }

}
