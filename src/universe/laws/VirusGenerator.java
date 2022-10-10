package universe.laws;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class VirusGenerator {

    ContainerController virusContainerController;
    AgentController virusAgentController;
    

    int virus_replication_factor;
    int virus_cell_communication_time = 2000 * Constants.SIMULATION_TIME_SCALE;
    int virus_replication_time = 20000 * Constants.SIMULATION_TIME_SCALE;
    int time_to_kill_the_cell = 100000 * Constants.SIMULATION_TIME_SCALE; // Original Value 10000

    public VirusGenerator(ContainerController virusContainerController, int virus_replication_factor) {
        this.virusContainerController = virusContainerController;
        this.virus_replication_factor = virus_replication_factor;
    }

    public int[] generateVirus() {

        int[] virus_signature = generateVirusSignature();

        try {
            this.virusAgentController = virusContainerController.createNewAgent(
                    "virus.".concat(virusContainerController.getContainerName()),
                    "universe.agents.VirusAgent",
                    new Object[] {
                            virus_signature,
                            virus_replication_factor,
                            virus_cell_communication_time,
                            virus_replication_time,
                            time_to_kill_the_cell
                    });
            System.out.println("First " + Constants.ANSI_RED + "Virus " +
                    Constants.ANSI_RESET +
                    "dropped at ".concat(virusContainerController.getContainerName()));
        } catch (Exception e) {
            e.getStackTrace();
        }

        return virus_signature;
    }

    public void activateVirus() {
        try {
            virusAgentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private int[] generateVirusSignature() {

        Set<Integer> uniqueList = new HashSet<>();
    
        while(uniqueList.size() != Constants.VIRUS_SIGNATURE_LENGTH) {
            Random random = new Random();
            int random_int = random.nextInt( Constants.CELL_IDENTIFYING_DNA.length - 1 );
            uniqueList.add(random_int);
        }
        
        int[] virus_signature = uniqueList.stream().mapToInt(i -> i).toArray();
        Arrays.sort(virus_signature);
        
        return virus_signature;
    }
}
