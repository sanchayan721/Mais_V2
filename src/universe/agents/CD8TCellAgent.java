package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import universe.containers.AuxiliaryContainer;
import universe.helper.ArrLocSerializable;
import universe.laws.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CD8TCellAgent extends Agent {

    int[] virus_signature = new int[] {};

    private int[] cellDNAToBeVerified = new int[] {};

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

                        ArrLocSerializable serializable = (ArrLocSerializable) receivedMessage.getContentObject();
                        ArrayList<Location> locations = serializable.locationArray;
                        
                        if (possiblePlacesToMove.size() >= 0) {
                            setPossiblePlacesToMove(locations);
                        }
                    }
                } catch (ControllerException | UnreadableException ignored) {
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
                } catch (ControllerException ignored) {
                }
                doWait(Constants.CD8T_CELL_COMMUNICATION_TIME);
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
                ACLMessage messageFromCell = receive(messageTemplate);
                if (messageFromCell != null) {
                    try {
                        cellDNAToBeVerified = (int[]) messageFromCell.getContentObject();
                    } catch (UnreadableException blocked) {
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

                if (Arrays.equals(differences, virus_signature)) {
                    try {
                        killTheVirus(myAgent);
                        myAgent.addBehaviour(new MovingToNewCell());
                    } catch (ControllerException blocked) {
                    }
                }
            }
            myAgent.addBehaviour(new MovingToNewCell());
        }

        protected void killTheVirus(Agent mAgent) throws ControllerException {

            ContainerController currentContainerController = mAgent.getContainerController();
            String currentContainerName = currentContainerController.getContainerName();
            String targetContaminant = "virus.".concat(currentContainerName);

            AgentController targetContaminantController = currentContainerController.getAgent(targetContaminant);
            targetContaminantController.kill();
            System.out.println(Constants.ANSI_GREEN +
                    "CD8T" +
                    Constants.ANSI_RESET +
                    ": \tKilled " +
                    Constants.ANSI_RED +
                    "virus" +
                    Constants.ANSI_RESET);
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
                    doWait(Constants.CD8T_CELL_SLEEP_TIME);
                    doMove(locationToMove);
                }
            }
            myAgent.addBehaviour(new AskCellForNeighbours());
        }
    }

}
