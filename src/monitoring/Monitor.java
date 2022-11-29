package monitoring;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import universe.Universe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Monitor {

    private int number_of_viruses;
    private int number_of_cells;

    public int virusCount() {
        int virusCounter = 0;
        for (ContainerController auxiliaryContainerController : Universe.CONTROLLER_GRID_MAP.keySet()) {
            try {
                AgentController virus = auxiliaryContainerController.getAgent(
                        "virus.".concat(auxiliaryContainerController.getContainerName()));
                virusCounter++;
            } catch (ControllerException e) {
            }
        }
        if (virusCounter == 0) {
            System.out.println("End of the Simulation");
        }
        return virusCounter;
    }

    public int cellCount() {
        int cellCounter = 0;
        for (ContainerController auxiliaryContainerController : Universe.CONTROLLER_GRID_MAP.keySet()) {
            try {
                AgentController cell = auxiliaryContainerController.getAgent(
                        "cell.".concat(auxiliaryContainerController.getContainerName()));
                cellCounter++;
            } catch (ControllerException e) {
            }
        }
        return cellCounter;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            String FileName = String.valueOf(Universe.CONTAINER_CONTROLLER_HASH_MAP.size()).concat("-GridSize.csv");
            FileWriter csvWrite = new FileWriter(new File("data", FileName));
            csvWrite.append(String.join(",",
                    List.of("Timestamp", "number_of_viruses", "number_of_cells", "number_of_dead_cells")));
            csvWrite.append("\n");

            Boolean loop = true;
            
            while (loop) {
                try {
                    Thread.sleep(50);
                    number_of_viruses = virusCount();
                    number_of_cells = cellCount();
                    long endTime = System.currentTimeMillis();
                    long timeElapsed = endTime - startTime;

                    ArrayList<String> row = new ArrayList<String>(Arrays.asList(
                            String.valueOf(timeElapsed),
                            String.valueOf(number_of_viruses),
                            String.valueOf(number_of_cells),
                            String.valueOf(Universe.CONTAINER_CONTROLLER_HASH_MAP.size() - number_of_cells)));

                    csvWrite.append(String.join(",", row));
                    
                    if (number_of_viruses == 0) {
                        loop = false;
                    } else {
                        csvWrite.append("\n");
                    }

                } catch (Exception e) {}
                // hard breaking.
                if (System.currentTimeMillis() - startTime > 300000) {
                    break;
                }
            }
            csvWrite.flush();
            csvWrite.close();
        } catch (IOException ignored) {
        }
    }
}
