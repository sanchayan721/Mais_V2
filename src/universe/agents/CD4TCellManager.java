package universe.agents;

import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import universe.helper.DendriticCellInformation;
import universe.laws.Constants;

public class CD4TCellManager extends Agent {

    int UNIVERSE_SIZE;

    Boolean contactedByDendriticCell = false;

    int[] virus_signature = new int[] {};

    Location lymphLocation;
    ArrayList<Location> path_to_site = new ArrayList<>();

    @Override
    public void setup() {
        Object[] arguments = getArguments();
        this.UNIVERSE_SIZE = (int) arguments[0];
        addBehaviour(new CommunicateWithDendriticCell());
    }

    protected class CommunicateWithDendriticCell extends CyclicBehaviour {

        String conversationID = "dendritic_cell_c4t_communication_channel";

        @Override
        public void action() {
            if (!contactedByDendriticCell) {

                MessageTemplate mTemplate = MessageTemplate.MatchConversationId(conversationID);
                ACLMessage message = receive(mTemplate);

                if (message != null) {
                    try {
                        contactedByDendriticCell = true;
                        DendriticCellInformation information = (DendriticCellInformation) message.getContentObject();
                        virus_signature = information.virus_signature;
                        ArrayList<Location> path = information.path;
                        
                        Collections.reverse(path);
                        lymphLocation = path.get(0);
                        path_to_site = new ArrayList<>(path.subList(1, path.size()));

                        myAgent.addBehaviour(new MatchingCD4TCell());

                    } catch (Exception e) {
                    }
                }

            } else {
                myAgent.removeBehaviour(this);
            }
        }

    }

    private class MatchingCD4TCell extends OneShotBehaviour {

        @Override
        public void action() {

            ContainerController mainContainerController = this.getAgent().getContainerController();

            doWait(Constants.TIME_TO_FIND_A_MATCHING_NAIEVE_CD4TCELL);

            for (int id = 0; id <= UNIVERSE_SIZE * Constants.PERCENTAGE_OF_CD4TCell / 100; id++) {
                try {
                    String agentName = "CD4TCell-".concat(String.valueOf(id));
                    AgentController cd4TCellController = mainContainerController.createNewAgent(
                            agentName,
                            "universe.agents.CD4TCellAgent",
                            new Object[] {
                                    virus_signature,
                                    lymphLocation,
                                    path_to_site
                            });
                    
                    cd4TCellController.start();

                } catch (StaleProxyException e) {
                }
            }

        }

    }
}
