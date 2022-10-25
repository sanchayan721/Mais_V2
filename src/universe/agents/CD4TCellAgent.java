package universe.agents;

import java.util.ArrayList;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.ControllerException;
import universe.containers.AuxiliaryContainer;
import universe.helper.ArrLocSerializable;
import universe.helper.CellMacrophageInformation;
import universe.laws.Constants;

public class CD4TCellAgent extends Agent {

    int[] virus_signature = new int[] {};
    ArrayList<Location> path_to_site = new ArrayList<>();
    Location lymphNodeLocation;

    Boolean cellPresentInContainer = true;

    ArrayList<Location> possiblePlacesToMove = new ArrayList<>();

    private void setPossiblePlacesToMove(ArrayList<Location> locs) {
        this.possiblePlacesToMove = locs;
    }

    Boolean found_macrophage = false;
    AID exMacrophageAID = null;

    @Override
    public void setup() {
        Object[] arguments = getArguments();
        this.virus_signature = (int[]) arguments[0];
        this.lymphNodeLocation = (Location) arguments[1];
        this.path_to_site = (ArrayList<Location>) arguments[2];

        addBehaviour(new GoingToLymphNode());
    }

    private class GoingToLymphNode extends OneShotBehaviour {

        @Override
        public void action() {
            doMove(lymphNodeLocation);
            addBehaviour(new MoveToNextVessel());
        }

    }

    private class MoveToNextVessel extends CyclicBehaviour {

        @Override
        public void action() {
            if (path_to_site.size() > 0) {
                doWait(Constants.CD4TCell_SLEEP_TIME);
                doMove(path_to_site.get(0));
                path_to_site = new ArrayList<>(path_to_site.subList(1, path_to_site.size()));
            } else {
                removeBehaviour(this);
                addBehaviour(new CheckIfExhaustedMacrophagePresent());
            }
        }

    }

    private class CheckIfExhaustedMacrophagePresent extends OneShotBehaviour {

        String conversationID = "asking_cell_exhausted_macrophage_connection_channel";
        String query = "is_there_exhausted_macrophage";

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

                Boolean replyReieved = false;
                while (!replyReieved) {
                    MessageTemplate reply = MessageTemplate.MatchConversationId(conversationID);
                    ACLMessage receivedMessage = receive(reply);
                    if (receivedMessage != null) {
                        replyReieved = true;
                        try {
                            CellMacrophageInformation information = (CellMacrophageInformation) receivedMessage
                                    .getContentObject();

                            if (!information.exh_macrophage_present) {
                                addBehaviour(new AskCellForNeighbours());
                            } else {
                                found_macrophage = true;
                                exMacrophageAID = information.exh_macrophage_aid;
                                addBehaviour(new StimulatingMacrophage());
                            }

                        } catch (Exception e) {
                        }
                    }
                }
            } catch (ControllerException e) {
            }
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
                    doWait(Constants.DENDRITIC_CELL_COMMUNICATION_TIME);
                    MessageTemplate reply = MessageTemplate.MatchConversationId(conversationID);
                    ACLMessage receivedMessage = receive(reply);
                    
                    if (receivedMessage != null) {

                        ArrLocSerializable serializable = (ArrLocSerializable) receivedMessage.getContentObject();
                        ArrayList<Location> locations = serializable.locationArray;

                        if (locations.size() >= 0) {
                            setPossiblePlacesToMove(locations);
                        }
                    }
                } catch (ControllerException | UnreadableException ignored) {
                }
            } else {
                cellPresentInContainer = false;
            }
            addBehaviour(new MovingToNewCell());
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
                    doWait(Constants.DENDRITIC_CELL_SLEEP_TIME);
                    doMove(locationToMove);
                }
            }
            addBehaviour(new CheckIfExhaustedMacrophagePresent());
        }
    }

    private class StimulatingMacrophage extends OneShotBehaviour {

        String conversationID = "cd4t_macrophage_communication_channel";
        String instruction = "stimulate_macrophage";

        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setConversationId(conversationID);
            message.addReceiver(exMacrophageAID);
            message.setContent(instruction);
            send(message);

            addBehaviour(new TellingCellSimulatedAboutMacrophage());
        }

    }

    private class TellingCellSimulatedAboutMacrophage extends OneShotBehaviour {

        String conversationID = "telling_cell_macrophage_channel_cd4t";
        String query = "macrophage_is_stimulated";

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

                addBehaviour(new AskCellForNeighbours());
            } catch (ControllerException e) {
            }
        }
    }
}
