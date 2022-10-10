package universe.agents;

import java.util.ArrayList;
import java.util.Arrays;

import jade.core.Agent;
import jade.core.AID;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CD4TCellManager extends Agent{
    
    Boolean contactedByDendriticCell = false;
    
    int[] virus_signature = new int[]{};

    ArrayList<Location> path = new ArrayList<>();

    AID senderAid = new AID();

    @Override
    public void setup () {
        addBehaviour(new ListeningToDendriticCell());
    }

    protected class ListeningToDendriticCell extends CyclicBehaviour {

        String conversationID = "dendritic_cell_c4t_communication_channel";
        String done = "done";

        @Override
        public void action() {
            if(!contactedByDendriticCell) {

                MessageTemplate mTemplate = MessageTemplate.MatchConversationId(conversationID);
                ACLMessage message = receive(mTemplate);

                if (message != null) {
                    senderAid = message.getSender();
                    try {
                        virus_signature = (int[]) message.getContentObject();
                        
                        contactedByDendriticCell = true;
                    } catch (Exception e) {}    
                }

            } else {
                myAgent.addBehaviour(new AskingDendriticCellForPath());
                myAgent.removeBehaviour(this);
            }         
        }
        
    }

    protected class AskingDendriticCellForPath extends OneShotBehaviour {

        String conversationID = "path_query_channel";
        String query = "tell_me_how_you_reach_here";

        @Override
        public void action() {
            
            System.out.println(Arrays.toString(virus_signature));
        }
        
    }
}
