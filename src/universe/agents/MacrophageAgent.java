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

import static universe.laws.Constants.*;

public class MacrophageAgent extends Agent {

    private String dnaToBeVerified = null;
    ArrayList<Location> possiblePlacesToMove = new ArrayList<>();

    private void setPossiblePlacesToMove(ArrayList<Location> locs) {
        this.possiblePlacesToMove = locs;
    }

    @Override
    protected void setup() {

        addBehaviour(new GoingToInitialPosition());
    }

    private class GoingToInitialPosition extends OneShotBehaviour {
        @Override
        public void action() {

            Object[] arguments = getArguments();
            ContainerController initialContainerController = (ContainerController) arguments[0];
            try {
                doWait(PHAGOCYTE_SLEEP_TIME);
                // ContainerController destinationContainer =
                // Universe.CONTAINER_CONTROLLER_HASH_MAP.get("Container-0");
                try {
                    ContainerID dest = new ContainerID();
                    dest.setName(initialContainerController.getContainerName());
                    myAgent.doMove(dest);
                } catch (Exception exception) {
                    exception.getStackTrace();
                }
            } catch (Exception exception) {
                exception.getStackTrace();
            }
            myAgent.addBehaviour(new AskCellForNeighbours());
        }
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

            /* System.out.println(this); */
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
                doWait(PHAGOCYTE_CELL_COMMUNICATION_TIME);
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
                ACLMessage messageFromCell = receive(messageTemplate);
                if (messageFromCell != null) {
                    dnaToBeVerified = messageFromCell.getContent();
                }
            } else {
            }
            myAgent.addBehaviour(new DetectingAndKillingVirus());
        }
    }

    private class DetectingAndKillingVirus extends OneShotBehaviour {

        @Override
        public void action() {

            /* System.out.println(this); */
            ArrayList<Integer> updateSet = new ArrayList<>();
            for (Character s : dnaToBeVerified.toCharArray()) {
                updateSet.add(Character.getNumericValue(s));
            }

            int[] cellDNA = updateSet.stream().mapToInt(i -> i).toArray();

            if (!Arrays.equals(cellDNA, Constants.CELL_IDENTIFYING_DNA)) {
                repairDNA();
                killTheVirus(myAgent);
            }
            myAgent.addBehaviour(new MovingToNewCell());
        }

    }

    private void killTheVirus(Agent myAgent) {
        try {
            String targetVirus = "virus.".concat(String.valueOf(this.getContainerController().getContainerName()));
            ContainerController thisContainer = myAgent.getContainerController();
            AgentController virusAgentController = thisContainer.getAgent(targetVirus);
            /* virusAgentController.kill(); */
            System.out
                    .println(ANSI_GREEN + "Macrophage" + ANSI_RESET + ": \tKilled " + ANSI_RED + "virus" + ANSI_RESET);
        } catch (ControllerException e) {
            return;
        }
    }

    private void repairDNA() {
        ACLMessage messageToCell = new ACLMessage(ACLMessage.INFORM); // Message type

        try {
            String targetCell = "cell.".concat(String.valueOf(this.getContainerController().getContainerName()));
            messageToCell.addReceiver(new AID(targetCell, AID.ISLOCALNAME)); // receiver
            messageToCell.setConversationId("DNA_Repair_Channel"); // conversation id
            String messageContent = "repair";
            messageToCell.setContent(messageContent);
            send(messageToCell);

        } catch (ControllerException e) {
            e.printStackTrace();
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
                    doWait(PHAGOCYTE_SLEEP_TIME);
                    doMove(locationToMove);
                }
            }
            myAgent.addBehaviour(new AskCellForNeighbours());
        }
    }

}
