package universe.laws;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class VirusGenerator {

    ContainerController virusContainerController;
    AgentController virusAgentController;
    int virus_replication_factor;

    public VirusGenerator(ContainerController virusContainerController, int virus_replication_factor) {
        this.virusContainerController = virusContainerController;
        this.virus_replication_factor = virus_replication_factor;
    }

    public void generateVirus() {

        int[] virus_signature = new int[] { 10, 25, 20, 22, 15 };
        int virus_cell_communication_time = 1000;
        int virus_replication_time = 10000;
        int time_to_kill_the_cell = 2000;

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
    }

    public void activateVirus() {
        try {
            virusAgentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
