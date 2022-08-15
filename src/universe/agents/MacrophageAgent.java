package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import universe.Universe;
import universe.containers.AuxiliaryContainer;
import universe.laws.Constants;
import universe.laws.Movement;

import java.io.Console;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static universe.laws.Constants.*;

public class MacrophageAgent extends Agent {

    private String dnaToBeVerified = null;
    private Boolean cellPresentInContainer = true;
    ArrayList<Location> possiblePlacesToMove = new ArrayList<>();

    private void setPossiblePlacesToMove(ArrayList<Location> locs) {
        this.possiblePlacesToMove = locs;
    }

    @Override
    protected void setup() {

        addBehaviour(new GoingToInitialPosition());

        // Putting Behaviours in a queue
        SequentialBehaviour afterLandingBehaviour = new SequentialBehaviour() {
            public int onEnd() {
                reset();
                myAgent.addBehaviour(this);
                return super.onEnd();
            }
        };
        afterLandingBehaviour.addSubBehaviour(new AskCellForNeighbours());
        afterLandingBehaviour.addSubBehaviour(new AskingCellForIdentity());
        /* afterLandingBehaviour.addSubBehaviour(new ConsultingWithMemoryAgent()); */
        afterLandingBehaviour.addSubBehaviour(new DetectingAndKillingVirus());
        afterLandingBehaviour.addSubBehaviour(new MovingToNewCell());

        // Launching the Behaviours
        addBehaviour(afterLandingBehaviour);
    }

    private class GoingToInitialPosition extends OneShotBehaviour {
        @Override
        public void action() {

            try {
                doWait(PHAGOCYTE_SLEEP_TIME);
                ContainerController destinationContainer = Universe.CONTAINER_CONTROLLER_HASH_MAP.get("Container-0");
                try {
                    ContainerID dest = new ContainerID();
                    dest.setName(destinationContainer.getContainerName());
                    myAgent.doMove(dest);
                } catch (Exception exception) {
                    exception.getStackTrace();
                }
            } catch (Exception exception) {
                exception.getStackTrace();
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
        }
    }

    private class AskingCellForIdentity extends OneShotBehaviour {
        @Override
        public void action() {

            System.out.println(this);
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
                cellPresentInContainer = false;
            }
        }
    }

    private class DetectingAndKillingVirus extends OneShotBehaviour {

        @Override
        public void action() {
            
            System.out.println(this);
            ArrayList<Integer> updateSet = new ArrayList<>();
            for (Character s : dnaToBeVerified.toCharArray()) {
                updateSet.add(Character.getNumericValue(s));
            }
            
            int[] cellDNA = updateSet.stream().mapToInt(i -> i).toArray();
            
            if(!Arrays.equals(cellDNA, Constants.CELL_IDENTIFYING_DNA)) {
                repairDNA();
                /* killTheVirus(myAgent); */
            }
        }

    }

    /*
     * private class ConsultingWithMemoryAgent extends OneShotBehaviour{
     * 
     * @Override
     * public void action() {
     * if(dnaToBeVerified != null && cellPresentInContainer){
     * ACLMessage messageToMemory = new ACLMessage(ACLMessage.INFORM);
     * messageToMemory.addReceiver(new AID("memory", AID.ISLOCALNAME));
     * messageToMemory.setConversationId("DNA_Verification_Channel");
     * messageToMemory.setContent(dnaToBeVerified);
     * send(messageToMemory);
     * }
     * doWait(PHAGOCYTE_MEMORY_COMMUNICATION_TIME);
     * MessageTemplate messageTemplate =
     * MessageTemplate.MatchConversationId("DNA_Verification_Channel");
     * ACLMessage message = receive(messageTemplate);
     * if (message != null){
     * String decisionFromMemoryAgent = message.getContent();
     * if(decisionFromMemoryAgent.equals("virusPresent")) {
     * repairDNA();
     * if(AuxiliaryContainer.isThereAVirus(this.getAgent().getContainerController())
     * ){
     * killTheVirus();
     * System.out.println(ANSI_GREEN + "Macrophage" + ANSI_RESET +
     * ": \tMemory is right. Virus Present");
     * }else{
     * // System.out.println(ANSI_GREEN + "Macrophage" + ANSI_RESET +
     * ": \tMemory is wrong. No virus Here");
     * }
     * }else if(decisionFromMemoryAgent.equals("LooksGood")){
     * if(AuxiliaryContainer.isThereAVirus(this.getAgent().getContainerController())
     * ){
     * //System.out.println(ANSI_GREEN + "Macrophage" + ANSI_RESET +
     * ": \tMemory is wrong. Virus Present");
     * }
     * }
     * }
     * }
     * }
     */
    private void killTheVirus(Agent myAgent) {
        ContainerController thisContainer = myAgent.getContainerController();
        /* String targetVirus = "virus.".concat(thisContainer.getContainerName()); */
        System.out.println(thisContainer);
        /* AgentController virusAgentController = thisContainer.getAgent(targetVirus);
        System.out.println(virusAgentController);
        System.out
                .println(ANSI_GREEN + "Macrophage" + ANSI_RESET + ": \tKilled " + ANSI_RED + "virus" + ANSI_RESET);
        virusAgentController.kill(); */
    }

    private void repairDNA() {
        ACLMessage messageToCell = new ACLMessage(ACLMessage.INFORM); // Message type

        try {
            String targetCell = "cell.".concat(String.valueOf(this.getContainerController().getContainerName()));
            // System.out.println(targetCell);
            messageToCell.addReceiver(new AID(targetCell, AID.ISLOCALNAME)); // receiver
            messageToCell.setConversationId("DNA_Repair_Channel"); // conversation id
            String messageContent = "repair";
            messageToCell.setContent(messageContent);
            send(messageToCell);

        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
}
