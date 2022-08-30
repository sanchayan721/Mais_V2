package universe.agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class DendriticCellAgent extends Agent{
        
    private int[] viralCodon;
    
    public void setViralCodon(int[] codon) {
        this.viralCodon = codon;
    }


    /* Setup Method */

    @Override
    protected void setup() {
        /* Behaviours of Dendritic Cell */
    }

    private class StartSearchingForVirus extends OneShotBehaviour {

        @Override
        public void action() {
            
            
        }

    }

}
