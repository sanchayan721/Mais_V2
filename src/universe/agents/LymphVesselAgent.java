package universe.agents;

import java.io.IOException;
import java.util.ArrayList;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import universe.helper.ArrLocSerializable;

public class LymphVesselAgent extends Agent {

    /*
     * Lymph Vessel Agent is a unit of the lymphatic system.
     * A virus can not attack these types of agents.
     * 
     * It provides a direction towards the Lymph Node.
     * 
     * A Dendritic cell can access the lympathic pathway by communicating with the
     * Lymph
     * vessel agent.
     */

    ArrayList<Location> stepTowardsLymphNode = new ArrayList<>();

    public void setStepTowardsLymphNode(ArrayList<Location> nextLocations) {
        this.stepTowardsLymphNode = nextLocations;
    }

    Boolean isLymphNodeVessel = false;

    public void setIsLymphNodeVessel(Boolean statement) {
        this.isLymphNodeVessel = statement;
    }

    @Override
    protected void setup() {
        addBehaviour(new SendingOwnLocationToInitiator());
        addBehaviour(new SettingNextVeselLocations());

        addBehaviour(new TellIfIamLymphNode());
        addBehaviour(new TellNextLocationToDendriticCell());
    }

    private class SendingOwnLocationToInitiator extends CyclicBehaviour {
        String conversationID = "tell_initiator_about_location";
        String query = "what_is_your_location";

        @Override
        public void action() {
            if (stepTowardsLymphNode.size() > 0 || isLymphNodeVessel) {
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
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class SettingNextVeselLocations extends CyclicBehaviour {

        String conversationID = "next_vessel_allocation_channel";

        @Override
        public void action() {
            if (stepTowardsLymphNode.size() > 0 || isLymphNodeVessel) {
                myAgent.removeBehaviour(this);
            } else {
                try {
                    MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(conversationID);
                    ACLMessage messageReceived = myAgent.receive(messageTemplate);
                    if (messageReceived != null) {

                        ArrLocSerializable serializable = (ArrLocSerializable) messageReceived.getContentObject();
                        ArrayList<Location> nextVesselLocations = serializable.locationArray;
                        
                        if (nextVesselLocations.size() == 0) {
                            setIsLymphNodeVessel(true);
                        } else {
                            setStepTowardsLymphNode(nextVesselLocations);
                        }
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class TellIfIamLymphNode extends CyclicBehaviour {
        String conversationID = "lymph_node_verification_channel";
        String query = "tell_if_lymph_node";

        @Override
        public void action() {
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
                        reply.setContentObject(isLymphNodeVessel);
                        System.out.println(isLymphNodeVessel);
                        send(reply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class TellNextLocationToDendriticCell extends CyclicBehaviour {
        String conversationID = "Vessel_Dendritic_Communication_Channel";
        String query = "give_me_next_vessel";

        @Override
        public void action() {
            if (!isLymphNodeVessel) {
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
                            reply.setContentObject(stepTowardsLymphNode);
                            send(reply);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
