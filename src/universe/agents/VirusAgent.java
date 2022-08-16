package universe.agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.*;
import universe.laws.Constants;

import java.util.ArrayList;
import java.util.Random;

import static universe.laws.Constants.*;

public class VirusAgent extends Agent {
    Boolean cellPresentInContainer = true;

    // Neighbouring Locations
    ArrayList<Location> possiblePlacesToMove;

    private void setPossiblePlacesToMove(ArrayList<Location> locs) {
        this.possiblePlacesToMove = locs;
    }

    @Override
    protected void setup() {
        addBehaviour(new CheckingIfCellAliveInContainer());
    }

    private class CheckingIfCellAliveInContainer extends OneShotBehaviour {
        @Override
        public void action() {
            try {
                ContainerController currentContainerController = myAgent.getContainerController();
                String targetCell = "cell.".concat(myAgent.getContainerController().getContainerName());
                currentContainerController.getAgent(targetCell);
                myAgent.addBehaviour(new CommunicateWithCell());
            } catch (ControllerException ignored) {
                cellPresentInContainer = false;
                myAgent.doDelete();
            }
        }
    }

    private class CommunicateWithCell extends OneShotBehaviour {
        @Override
        public void action() {

            ACLMessage messageToCell = new ACLMessage(ACLMessage.INFORM); // Message type

            try {
                String targetCell = "cell.".concat(String.valueOf(myAgent.getContainerController().getContainerName()));
                // System.out.println(targetCellID);
                messageToCell.addReceiver(new AID(targetCell, AID.ISLOCALNAME));
                ; // receiver
                messageToCell.setConversationId("Update_DNA_Message_From_Virus"); // conversation id
            } catch (ControllerException e) {
                e.printStackTrace();
            }
            // Send message to Cell About Genetic Modification
            messageToCell.setContent(VIRUS_SIGNATURE);
            send(messageToCell); // sending method
            myAgent.addBehaviour(new AskingCellForNeighbours());
        }
    }

    private class AskingCellForNeighbours extends OneShotBehaviour {
        String conversationID = "Tell_About_Neighbours";
        String questionForCell = "neighbour_list";

        @Override
        public void action() {
            try {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setConversationId(conversationID);
                String targetCell = "cell.".concat(String.valueOf(myAgent.getContainerController().getContainerName()));
                message.addReceiver(new AID(targetCell, AID.ISLOCALNAME));
                message.setContent(questionForCell);
                send(message);

                doWait(VIRUS_CELL_COMMUNICATION_TIME);
                MessageTemplate reply = MessageTemplate.MatchConversationId(conversationID);
                ACLMessage receivedMessage = receive(reply);
                if (receivedMessage != null) {
                    ArrayList<Location> locations = (ArrayList<Location>) receivedMessage.getContentObject();
                    setPossiblePlacesToMove(locations);
                    myAgent.addBehaviour(new CloningBehaviour());
                }
            } catch (ControllerException | UnreadableException e) {
            }

        }
    }

    private class CloningBehaviour extends OneShotBehaviour {
        public void action() {
            doWait(VIRUS_REPLICATION_TIME);
            ArrayList<Location> cloningLocations = getPlacesToClone(possiblePlacesToMove);

            System.out.println("\n**************************************************");
            try {
                System.out.println("Hi...! Virus at ".concat(
                        myAgent.getContainerController().getContainerName() + " and places I will Clone to are"));
            } catch (ControllerException ignored) {
            }
            /* System.out.println(cloningLocations); */
            System.out.println("**************************************************");
            System.out.println("Now Begining the cloning...");

            for (Location location : cloningLocations) {
                System.out.println("cloning to " + Constants.ANSI_RED + location.getName() + Constants.ANSI_RESET);

                String remoteCellName = "cell.".concat(location.getName());
                String conversationID = "Spwan_A_New_Virus_Channel";
                String messageContent = "Spwan_A_New_Virus";

                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setConversationId(conversationID);
                message.addReceiver(new AID(remoteCellName, AID.ISLOCALNAME));
                message.setContent(messageContent);
                send(message);
                doWait(200);
            }

            myAgent.addBehaviour(new KillingACell());

        }

        /*
         * This is the code to randomly choose places to clone from all available places
         * to clone
         */
        private ArrayList<Location> getPlacesToClone(ArrayList<Location> possiblePlaces) {
            ArrayList<Location> cloningLocation = new ArrayList<>();
            Location currentLocation = myAgent.here();
            Random random = new Random();

            if (universe.laws.Constants.VIRUS_REPLICATION_FACTOR > possiblePlaces.size()) {
                for (Location possibleLocation : possiblePlaces) {
                    if (!possibleLocation.equals(currentLocation)) {
                        cloningLocation.add(possibleLocation);
                    }
                }
            } else {
                for (int i = 0; i < universe.laws.Constants.VIRUS_REPLICATION_FACTOR; i++) {
                    int randomIndex = random.nextInt(possiblePlaces.size());
                    Location randomlyChosenLocation = possiblePlaces.get(randomIndex);
                    if (!randomlyChosenLocation.equals(currentLocation)
                            && !cloningLocation.contains(randomlyChosenLocation)) {
                        cloningLocation.add(randomlyChosenLocation);
                    } else {
                        i--;
                    }
                }
            }
            return cloningLocation;
        }
    }

    private class KillingACell extends OneShotBehaviour {
        @Override
        public void action() {

            doWait(KILL_THE_CELL_AFTERWARD);

            try {
                ContainerController currentContainerController = myAgent.getContainerController();
                String targetCell = "cell.".concat(myAgent.getContainerController().getContainerName());
                AgentController targetCellAgentController = currentContainerController.getAgent(targetCell);

                /* System.out.println(
                        ANSI_RED + "VIRUS" + ANSI_RESET + ": \tKilled ".concat(targetCellAgentController.getName())); */
                targetCellAgentController.kill();
                /* System.out.println(myAgent.getName()); */
                myAgent.doDelete();
            } catch (ControllerException ignored) {
            }
        }
    }
}
