package universe.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import universe.containers.AuxiliaryContainer;
import universe.laws.Constants;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.ControllerException;

public class DendriticCellAgent extends Agent {

    private int[] dnaToBeVerified = new int[]{};

    Boolean virusFound = false;

    public void setVirusFound(Boolean status) {
        this.virusFound = status;
    }

    int[] VIRUS_IDENTIFYING_CODON = null;

    public void setVirusIdentifynigCodon(int[] differences) {
        this.VIRUS_IDENTIFYING_CODON = differences;
    }

    Boolean cellPresentInContainer = true;
    ArrayList<Location> possiblePlacesToMove = new ArrayList<>();

    private void setPossiblePlacesToMove(ArrayList<Location> locs) {
        this.possiblePlacesToMove = locs;
    }

    Boolean reachedVessel = false;

    public void setReachedVessel(Boolean status) {
        this.reachedVessel = status;
    }

    Boolean reachedLymphNode = false;

    public void setReachedLymphNode(Boolean status) {
        this.reachedLymphNode = status;
    }

    @Override
    protected void setup() {

        /*
         * SequentialBehaviour dendriticCellBehaviour = new SequentialBehaviour() {
         * public int onEnd() {
         * reset();
         * myAgent.addBehaviour(this);
         * return super.onEnd();
         * }
         * };
         * 
         * dendriticCellBehaviour.addSubBehaviour(new AskCellForNeighbours());
         * dendriticCellBehaviour.addSubBehaviour(new AskingCellForIdentity());
         * dendriticCellBehaviour.addSubBehaviour(new DetectingVirus());
         * dendriticCellBehaviour.addSubBehaviour(new MovingToNewCell());
         * 
         * addBehaviour(dendriticCellBehaviour);
         */

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
            } else {
                cellPresentInContainer = false;
            }

            if (virusFound) {
                myAgent.addBehaviour(new AskingCellAboutLymphVessel());
            } else {
                myAgent.addBehaviour(new AskingCellForIdentity());
            }
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
                doWait(200);
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
                ACLMessage messageFromCell = receive(messageTemplate);
                if (messageFromCell != null) {
                    try {
                        dnaToBeVerified = (int[]) messageFromCell.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                cellPresentInContainer = false;
            }

            myAgent.addBehaviour(new DetectingVirus());
        }
    }

    private class DetectingVirus extends OneShotBehaviour {

        @Override
        public void action() {

            if (!Arrays.equals(dnaToBeVerified, Constants.CELL_IDENTIFYING_DNA)) {
                setVirusFound(true);

                ArrayList<Integer> differenceList = new ArrayList<>();
                for (int index = 0; index < dnaToBeVerified.length; index++) {
                    if (dnaToBeVerified[index] != Constants.CELL_IDENTIFYING_DNA[index]) {
                        differenceList.add(index);
                    }
                }

                int[] differences = differenceList.stream().mapToInt(i -> i).toArray();
                setVirusIdentifynigCodon(differences);
                System.out.println(Arrays.toString(differences));
                myAgent.addBehaviour(new AskingCellAboutLymphVessel());

            } else {
                myAgent.addBehaviour(new MovingToNewCell());
            }
        }

    }

    private class AskingCellAboutLymphVessel extends OneShotBehaviour {

        String conversationID = "vessel_confirmation_channel";
        String questionForCell = "tell_if_you_are_a_vessel";

        @Override
        public void action() {

            try {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setConversationId(conversationID);
                String targetCell = "cell."
                        .concat(String.valueOf(myAgent.getContainerController().getContainerName()));
                message.addReceiver(new AID(targetCell, AID.ISLOCALNAME));
                message.setContent(questionForCell);
                send(message);
                Boolean replyReceived = false;

                while (!replyReceived) {
                    MessageTemplate reply = MessageTemplate.MatchConversationId(conversationID);
                    ACLMessage receivedMessage = receive(reply);
                    if (receivedMessage != null) {
                        replyReceived = true;
                        Boolean status = Boolean.parseBoolean(receivedMessage.getContent());
                        if (status) {
                            setReachedVessel(status);
                            myAgent.addBehaviour(new ContactVesselInCell());
                        } else {
                            myAgent.addBehaviour(new MovingToNewCell());
                        }
                    }
                }

            } catch (ControllerException e) {
                e.printStackTrace();
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
                    doWait(500);
                    doMove(locationToMove);
                }
            }
            myAgent.addBehaviour(new AskCellForNeighbours());
        }
    }

    private class ContactVesselInCell extends OneShotBehaviour {

        @Override
        public void action() {
            String conversationID = "Vessel_Dendritic_Communication_Channel";
            String questionForVessel = "give_me_next_vessel";

            try {
                String targetVessel = "lymphVessel.".concat(myAgent.getContainerController().getContainerName());
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setConversationId(conversationID);
                message.addReceiver(new AID(targetVessel, AID.ISLOCALNAME));
                message.setContent(questionForVessel);
                myAgent.send(message);

                Boolean gotReply = false;

                while (!gotReply) {
                    MessageTemplate reply = MessageTemplate.MatchConversationId(conversationID);
                    ACLMessage receivedMessage = receive(reply);

                    if (receivedMessage != null) {
                        ArrayList<Location> locations = (ArrayList<Location>) receivedMessage.getContentObject();
                        if (locations.size() > 0) {
                            setPossiblePlacesToMove(locations);
                        } else {
                            setReachedLymphNode(true);
                        }
                        gotReply = true;
                        break;
                    }
                }
                myAgent.addBehaviour(new MoveToNextVessel());

            } catch (ControllerException | UnreadableException e) {
                e.printStackTrace();
            }

        }
    }

    private class MoveToNextVessel extends OneShotBehaviour {

        @Override
        public void action() {
            if (possiblePlacesToMove.size() > 0) {
                Location currentLocation = myAgent.here();
                Random rand = new Random();
                Location locationToMove = possiblePlacesToMove.get(rand.nextInt(possiblePlacesToMove.size()));
                if (!locationToMove.equals(currentLocation)) {
                    doWait(500);
                    doMove(locationToMove);
                }
            }
            myAgent.addBehaviour(new ContactVesselInCell());
        }

    }
}
