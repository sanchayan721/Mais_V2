package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import universe.helper.CellMacrophageInformation;
import universe.helper.VirusInformation;
import universe.laws.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CellAgent extends Agent {

    private final int[] myDNA = copyDNAFromSourceCodeOfUniverse();
    private Boolean CELL_STATE = true; // Alive

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

    Boolean ifLymphVesselPresent = false;

    public void setLymphVesselPresent(Boolean status) {
        this.ifLymphVesselPresent = status;
    }

    Boolean exhaustedMacrophagePresent = false;
    AID exhaustedMacrophageAID = new AID();

    /* Setup method */
    @Override
    protected void setup() {
        // Adding the behaviours
        addBehaviour(new sendingOwnLocationToInitiator());
        addBehaviour(new settingNeighboursLocationWithInitiator());
        addBehaviour(new checkingIFaVessel());

        addBehaviour(new ManageCellStatus());
        addBehaviour(new SignatureVerificationBehaviour());
        addBehaviour(new TellDendriticCellAboutLymphVessel());
        addBehaviour(new TellNeighboursLocation());
        addBehaviour(new ListenToVirus());
        addBehaviour(new SpawningVirus());
        addBehaviour(new SettingExhaustedMacrophagePresence());
        addBehaviour(new TellingCD4TCellAboutExhaustedMacrophage());
        addBehaviour(new DNARepairBehaviour());
    }

    private class ManageCellStatus extends CyclicBehaviour {

        String conversationID = "manage_cell_ststus";

        @Override
        public void action() {

            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
            ACLMessage message = receive(messageTemplate);
            if (message != null) {
                String receivedQuery = message.getContent();

                switch (receivedQuery) {
                    case "check_cell_status":
                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setConversationId(conversationID);
                        reply.addReceiver(message.getSender());
                        reply.setContent(Boolean.toString(CELL_STATE));
                        send(reply);
                        break;

                    case "change_cell_status":
                        CELL_STATE = !CELL_STATE;
                        break;

                    default:
                        break;
                }
            }

        }

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
                        } catch (IOException ignored) {
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
                        ArrayList<Location> neighbourLocation = (ArrayList<Location>) messageReceived
                                .getContentObject();
                        setNeighboursLocations(neighbourLocation);
                    }
                } catch (UnreadableException | ControllerException ignored) {
                }
            }
        }
    }

    private class checkingIFaVessel extends CyclicBehaviour {
        String conversationID = "vessel_cell_connection_channel";

        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
            ACLMessage messageReceived = myAgent.receive(messageTemplate);

            if (messageReceived != null) {
                Boolean status = Boolean.parseBoolean(messageReceived.getContent());
                setLymphVesselPresent(status);
                myAgent.removeBehaviour(this);
            }
        }

    }

    private class SignatureVerificationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            if (CELL_STATE) {
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
                ACLMessage msg = receive(messageTemplate);

                if (msg != null) {
                    String messageContent = msg.getContent();
                    if (messageContent.equals("Verify_Identity")) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setConversationId("Signature_Verification_Channel");
                        reply.setSender(msg.getSender());
                        try {
                            reply.setContentObject(myDNA);
                            send(reply);
                        } catch (IOException blocked) {
                        }
                    }
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
                        } catch (IOException blocked) {
                        }
                    }
                }
            }
        }
    }

    private class TellDendriticCellAboutLymphVessel extends CyclicBehaviour {

        String conversationID = "vessel_confirmation_channel";
        String matchQuery = "tell_if_you_are_a_vessel";

        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
            ACLMessage msg = receive(messageTemplate);

            if (msg != null) {
                String messageContent = msg.getContent();
                if (messageContent.equals(matchQuery)) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setConversationId(conversationID);
                    reply.setSender(msg.getSender());
                    reply.setContent(String.valueOf(ifLymphVesselPresent));
                    send(reply);
                }
            }
        }

    }

    private class ListenToVirus extends CyclicBehaviour {

        @Override
        public void action() {
            if (CELL_STATE) {
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Update_DNA_Message_From_Virus");
                ACLMessage msg = receive(messageTemplate);

                if (msg != null) {

                    try {
                        Object messageContent = msg.getContentObject();
                        if (messageContent != null) {
                            int[] updatePoints = (int[]) messageContent;
                            updateDNA(updatePoints);
                        }
                        this.getAgent().removeBehaviour(this);
                    } catch (UnreadableException blocked) {
                    }
                }
            }
        }
    }

    private class DNARepairBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            if (CELL_STATE) {
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
    }

    private class SpawningVirus extends CyclicBehaviour {
        @Override
        public void action() {
            if (CELL_STATE) {
                String conversationID = "Spwan_A_New_Virus_Channel";

                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
                ACLMessage msg = receive(messageTemplate);

                if (msg != null) {

                    try {
                        if (!isVirusAlreadyPresent()) {
                            VirusInformation virusInformation = (VirusInformation) msg.getContentObject();
                            spwanAVirus(virusInformation);
                            /* myAgent.removeBehaviour(this); */
                        } else {
                            block();
                        }
                    } catch (ControllerException | UnreadableException ignored) {
                    }
                    this.getAgent().removeBehaviour(this);
                }
            }
        }

        private Boolean isVirusAlreadyPresent() {
            Boolean virusPresent = false;
            ContainerController thisContainerController = myAgent.getContainerController();
            try {
                thisContainerController.getAgent("virus.".concat(thisContainerController.getContainerName()));
                virusPresent = true;
            } catch (ControllerException e) {
            }
            return virusPresent;
        }

        private void spwanAVirus(VirusInformation virusInformation) throws StaleProxyException, ControllerException {
            ContainerController myContainerController = myAgent.getContainerController();
            AgentController virusAgentController = myContainerController.createNewAgent(
                    "virus.".concat(myContainerController.getContainerName()), "universe.agents.VirusAgent",
                    new Object[] {
                            virusInformation.virus_signature,
                            virusInformation.virus_replication_factor,
                            virusInformation.virus_cell_communication_time,
                            virusInformation.virus_replication_time,
                            virusInformation.time_to_kill_the_cell
                    });

            virusAgentController.start();
        }
    }

    private class SettingExhaustedMacrophagePresence extends CyclicBehaviour {

        String conversationID = "exhausted_macrophage_cell_connection_channel";
        String query = "i_am_exhausted";

        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
            ACLMessage messageReceived = myAgent.receive(messageTemplate);

            if (messageReceived != null) {

                String messageContent = messageReceived.getContent();

                if (messageContent == query) {
                    exhaustedMacrophagePresent = true;
                    exhaustedMacrophageAID = messageReceived.getSender();
                }
            }
        }
    }

    private class TellingCD4TCellAboutExhaustedMacrophage extends CyclicBehaviour {

        String conversationID = "asking_cell_exhausted_macrophage_connection_channel";
        String query = "is_there_exhausted_macrophage";

        @Override
        public void action() {

            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
            ACLMessage messageReceived = myAgent.receive(messageTemplate);

            if (messageReceived != null) {
                String messageContent = messageReceived.getContent();
                try {
                    if (messageContent == query) {
                        CellMacrophageInformation information = new CellMacrophageInformation(
                                exhaustedMacrophagePresent,
                                exhaustedMacrophageAID);
                        ACLMessage reply = messageReceived.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setSender(messageReceived.getSender());
                        reply.setConversationId(conversationID);
                        reply.setContentObject(information);
                    }
                } catch (Exception e) {
                }
            }

        }

    }

    private class ListeningAboutMacrophageStimulationFromCD4T extends CyclicBehaviour {

        String conversationID = "telling_cell_macrophage_channel_cd4t";
        String query = "macrophage_is_stimulated";

        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
            ACLMessage messageReceived = myAgent.receive(messageTemplate);

            if (messageReceived != null) {
                String messageContent = messageReceived.getContent();
                try {
                    if (messageContent == query) {
                        exhaustedMacrophagePresent = false;
                        exhaustedMacrophageAID = null;
                    }
                } catch (Exception e) {
                }
            }
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
