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

public class MacrophageAgent extends Agent {

    private int[] cellDNAToBeVerified = new int[] {};

    ArrayList<Location> possiblePlacesToMove = new ArrayList<>();

    private void setPossiblePlacesToMove(ArrayList<Location> locs) {
        this.possiblePlacesToMove = locs;
    }

    int number_of_virus_killed = 0;

    Boolean stimulated = false;

    @Override
    protected void setup() {
        System.out.println(this.getAID().getName());
        addBehaviour(new GoingToInitialPosition());
    }

    private class GoingToInitialPosition extends OneShotBehaviour {
        @Override
        public void action() {

            Object[] arguments = getArguments();
            ContainerController initialContainerController = (ContainerController) arguments[0];
            try {
                doWait(Constants.MACROPHAGE_SLEEP_TIME);
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

                        if (locations.size() >= 0) {
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
                } catch (ControllerException ignored) {
                }
                doWait(Constants.MACROPHAGE_CELL_COMMUNICATION_TIME);
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
                ACLMessage messageFromCell = receive(messageTemplate);
                if (messageFromCell != null) {
                    try {
                        cellDNAToBeVerified = (int[]) messageFromCell.getContentObject();
                    } catch (UnreadableException blocked) {
                    }
                }
            }
            myAgent.addBehaviour(new DetectingAndKillingVirus());
        }
    }

    private class DetectingAndKillingVirus extends OneShotBehaviour {

        @Override
        public void action() {

            if (!Arrays.equals(cellDNAToBeVerified, Constants.CELL_IDENTIFYING_DNA)) {
                repairDNA();
                killTheVirus(myAgent);
            }
            myAgent.addBehaviour(new CheckIfQuotaComplete());
        }

    }

    private void killTheVirus(Agent myAgent) {
        try {
            String targetVirus = "virus.".concat(String.valueOf(this.getContainerController().getContainerName()));
            ContainerController thisContainer = myAgent.getContainerController();
            AgentController virusAgentController = thisContainer.getAgent(targetVirus);
            number_of_virus_killed++;
            virusAgentController.kill();
            System.out.println(Constants.ANSI_CYAN +
                    this.getAID().getName() +
                    Constants.ANSI_RESET +
                    ": \tKilled " +
                    Constants.ANSI_RED +
                    "virus" +
                    Constants.ANSI_RESET);
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

    private class CheckIfQuotaComplete extends OneShotBehaviour {

        @Override
        public void action() {
            if (number_of_virus_killed <= Constants.NUMBER_OF_VIRUS_MACROPHAGE_CAN_KILL || stimulated) {
                addBehaviour(new MovingToNewCell());
            } else {
                addBehaviour(new TellCellIamExhausted());
                addBehaviour(new WaitingForStimulation());
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

    private class TellCellIamExhausted extends OneShotBehaviour {

        String conversationID = "exhausted_macrophage_cell_connection_channel";
        String query = "i_am_exhausted";

        @Override
        public void action() {
            try {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setConversationId(conversationID);
                String targetCell = "cell."
                        .concat(String.valueOf(myAgent.getContainerController().getContainerName()));
                message.addReceiver(new AID(targetCell, AID.ISLOCALNAME));
                message.setContent(query);
                send(message);
            } catch (ControllerException e) {
            }
        }

    }

    private class WaitingForStimulation extends OneShotBehaviour {

        String conversationID = "cd4t_macrophage_communication_channel";
        String instruction = "stimulate_macrophage";

        @Override
        public void action() {

            Boolean messageReceivedFromCD4T = false;

            while (!messageReceivedFromCD4T) {
                MessageTemplate mTemplate = MessageTemplate.MatchConversationId(conversationID);
                ACLMessage message = receive(mTemplate);

                if (message != null) {
                    try {
                        messageReceivedFromCD4T = true;
                        String messageContent = message.getContent();

                        if (messageContent == instruction) {
                            stimulated = true;
                        }

                    } catch (Exception e) {
                    }
                }
            }

            addBehaviour(new MovingToNewCell());
        }

    }

}
