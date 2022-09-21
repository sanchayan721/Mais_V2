package universe.agents;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import universe.laws.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CellAgent extends Agent {

    private final int[] myDNA = copyDNAFromSourceCodeOfUniverse();

    // Intrinsic Properties of a Cell Agent

    /* Setting up the neighbour location List */
    ArrayList<Location> neighboursLocations = new ArrayList<>();

    public void setNeighboursLocations(ArrayList<Location> locations) throws ControllerException {
        this.neighboursLocations = locations;
    }

    private ArrayList<Location> getNeighboursLocations() {
        return this.neighboursLocations;
    }

    private int[] copyDNAFromSourceCodeOfUniverse() {
        int[] replicaDNA = new int[Constants.CELL_IDENTIFYING_DNA.length];
        System.arraycopy(Constants.CELL_IDENTIFYING_DNA, 0, replicaDNA, 0, Constants.CELL_IDENTIFYING_DNA.length);
        return replicaDNA;
    }

    /* Setup method */
    @Override
    protected void setup() {
        // Adding the behaviours
        addBehaviour(new sendingOwnLocationToInitiator());
        addBehaviour(new settingNeighboursLocationWithInitiator());

        addBehaviour(new ListenToMacrophage());
        addBehaviour(new TellNeighboursLocation());
        addBehaviour(new ListenToVirus());
        addBehaviour(new SpawningVirus());
        addBehaviour(new DNARepairBehaviour());
    }

    private class sendingOwnLocationToInitiator extends CyclicBehaviour {
        String conversationID = "tell_initiator_about_location";
        String query = "what_is_your_location";

        @Override
        public void action() {
            if (neighboursLocations.size() > 0) {
                myAgent.removeBehaviour(this);
            } else {
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
                ACLMessage message = receive(messageTemplate);
                Location myLocation = myAgent.here();
                String myContainerName = myAgent.getContainerController().getName();
                if (message != null) {
                    String receivedQuery = message.getContent();
                    if (receivedQuery.equals(query)) {
                        try {
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setConversationId(conversationID);
                            reply.addReceiver(message.getSender());
                            reply.setContentObject(myLocation);
                            send(reply);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class settingNeighboursLocationWithInitiator extends CyclicBehaviour {
        String conversationID = "neighbour_allocation_channel";

        @Override
        public void action() {
            if (neighboursLocations.size() > 0) {
                myAgent.removeBehaviour(this);
            } else {
                try {
                    MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
                    ACLMessage messageReceived = myAgent.receive(messageTemplate);
                    if (messageReceived != null) {
                        ArrayList<Location> neighbourLocation = (ArrayList<Location>) messageReceived.getContentObject();
                        setNeighboursLocations(neighbourLocation);
                    }
                } catch (UnreadableException | ControllerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ListenToMacrophage extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
            ACLMessage msg = receive(messageTemplate);

            if (msg != null) {
                String messageContent = msg.getContent();
                if (messageContent.equals("Verify_Identity")) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setConversationId("Signature_Verification_Channel");
                    reply.setSender(msg.getSender());

                    StringBuilder myDNACode = new StringBuilder();
                    for (int codon : myDNA) {
                        myDNACode.append(codon);
                    }

                    reply.setContent(myDNACode.toString());
                    send(reply);

                }
            }
        }
    }

    private class TellNeighboursLocation extends CyclicBehaviour {
        String conversationID = "Tell_About_Neighbours";
        String query = "neighbour_list";

        @Override
        public void action() {
            if (neighboursLocations.size() != 0) {
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
                ACLMessage message = receive(messageTemplate);
                if (message != null) {
                    String messageContent = message.getContent();
                    if (messageContent.equals(query)) {
                        try {
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setSender(message.getSender());
                            reply.setConversationId(conversationID);
                            reply.setContentObject(neighboursLocations);
                            send(reply);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class ListenToVirus extends CyclicBehaviour {

        @Override
        public void action() {

            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Update_DNA_Message_From_Virus");
            ACLMessage msg = receive(messageTemplate);

            if (msg != null) {

                String messageContent = msg.getContent();
                int[] updatePoints = getPointUpdates(messageContent);
                this.getAgent().removeBehaviour(this);
                updateDNA(updatePoints);
            }
        }

        private int[] getPointUpdates(String messageStream) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(messageStream);
            ArrayList<Integer> updateSet = new ArrayList<>();
            while (matcher.find()) {
                updateSet.add(Integer.valueOf(matcher.group()));
            }
            return updateSet.stream().mapToInt(i -> i).toArray();
        }
    }

    private class DNARepairBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("DNA_Repair_Channel");
            ACLMessage msg = receive(messageTemplate);

            if (msg != null) {
                String messageContent = msg.getContent();
                if (messageContent.equals("repair")) {
                    System.arraycopy(Constants.CELL_IDENTIFYING_DNA, 0, myDNA, 0,
                            Constants.CELL_IDENTIFYING_DNA.length);
                    this.getAgent().addBehaviour(new ListenToVirus());
                    this.getAgent().addBehaviour(new SpawningVirus());
                }
            }
        }
    }

    private class SpawningVirus extends CyclicBehaviour {
        @Override
        public void action() {

            String expectedMessage = "Spwan_A_New_Virus";
            String conversationID = "Spwan_A_New_Virus_Channel";
            
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
            ACLMessage msg = receive(messageTemplate);
            
            if (msg != null) {
                
                String messageContent = msg.getContent();
                if (messageContent.equals(expectedMessage)) {

                    try {
                        if(!isVirusAlreadyPresent()) {
                            spwanAVirus();
                            /* myAgent.removeBehaviour(this); */
                        } else {
                            block();
                        }
                    } catch (StaleProxyException e) {
                    } catch (ControllerException e) {
                    }
                }
                this.getAgent().removeBehaviour(this);
            }
        }

        private Boolean isVirusAlreadyPresent () {
            Boolean virusPresent = false;
            ContainerController thisContainerController = myAgent.getContainerController();
            try {
                thisContainerController.getAgent("virus.".concat(thisContainerController.getContainerName()));
                virusPresent = true;
            } catch (ControllerException e) {
            }
            return virusPresent;
        }

        private void spwanAVirus() throws StaleProxyException, ControllerException {
            ContainerController myContainerController = myAgent.getContainerController();
            AgentController virusAgentController = myContainerController.createNewAgent(
                    "virus.".concat(myContainerController.getContainerName()), "universe.agents.VirusAgent",
                    new Object[] {});

            virusAgentController.start();
        }
    }

    private void updateDNA(int[] flipLocations) {

        for (int flipLocation : flipLocations) {
            if (myDNA[flipLocation] == 0) {
                this.myDNA[flipLocation] = 1;
            } else if (myDNA[flipLocation] == 1) {
                this.myDNA[flipLocation] = 0;
            }
        }
    }
}
