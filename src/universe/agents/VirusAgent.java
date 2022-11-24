package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.*;
import universe.helper.ArrLocSerializable;
import universe.helper.VirusInformation;
import universe.laws.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class VirusAgent extends Agent {

    Boolean cellPresentInContainer = true;

    // Neighbouring Locations
    ArrayList<Location> possiblePlacesToMove;

    private void setPossiblePlacesToMove(ArrayList<Location> locs) {
        this.possiblePlacesToMove = locs;
    }

    VirusInformation virusInformation;

    @Override
    protected void setup() {
        Object[] arguments = getArguments();
        VirusInformation information = new VirusInformation();
        
        information.virus_signature = (int[]) arguments[0];
        information.virus_replication_factor = (int) arguments[1];
        information.virus_cell_communication_time = (int) arguments[2];
        information.virus_replication_time = (int) arguments[3];
        information.time_to_kill_the_cell = (int) arguments[4];

        this.virusInformation = information;

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
                messageToCell.setConversationId("Update_DNA_Message_From_Virus"); // conversation id
                messageToCell.setContentObject(virusInformation.virus_signature);
                send(messageToCell); // sending method
            } catch (ControllerException | IOException e) {
                e.printStackTrace();
            }
            // Send message to Cell About Genetic Modification
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

                doWait(virusInformation.virus_cell_communication_time);
                MessageTemplate reply = MessageTemplate.MatchConversationId(conversationID);
                ACLMessage receivedMessage = receive(reply);
                if (receivedMessage != null) {

                    //ArrLocSerializable serializable = (ArrLocSerializable) receivedMessage.getContentObject();
                    //ArrayList<Location> locations = serializable.locationArray;
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
            doWait(virusInformation.virus_replication_time);
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

                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setConversationId(conversationID);
                message.addReceiver(new AID(remoteCellName, AID.ISLOCALNAME));
                try {
                    message.setContentObject(virusInformation);
                    send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

            if (virusInformation.virus_replication_factor > possiblePlaces.size()) {
                for (Location possibleLocation : possiblePlaces) {
                    if (!possibleLocation.equals(currentLocation)) {
                        cloningLocation.add(possibleLocation);
                    }
                }
            } else {
                for (int i = 0; i < virusInformation.virus_replication_factor; i++) {
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

            doWait(virusInformation.time_to_kill_the_cell);

            try {
                ContainerController currentContainerController = myAgent.getContainerController();
                String targetCell = "cell.".concat(myAgent.getContainerController().getContainerName());
                AgentController targetCellAgentController = currentContainerController.getAgent(targetCell);

                /*
                 * System.out.println(
                 * ANSI_RED + "VIRUS" + ANSI_RESET +
                 * ": \tKilled ".concat(targetCellAgentController.getName()));
                 */
                targetCellAgentController.kill();
                /* System.out.println(myAgent.getName()); */
                myAgent.doDelete();
            } catch (ControllerException ignored) {
            }
        }
    }
}
