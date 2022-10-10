package universe.agents;

import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import universe.laws.Constants;

public class CD4TCellAgent extends Agent {

    int[] virus_signature = new int[] {};
    ArrayList<Location> path_to_site = new ArrayList<>();
    Location lymphNodeLocation;


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
                addBehaviour(new CommunicateWithCellAgent());
            }
        }

    }

    private class CommunicateWithCellAgent extends OneShotBehaviour{

        @Override
        public void action() {
            
            
            
        }
        
    }
}
