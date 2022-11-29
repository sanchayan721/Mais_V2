package universe;

import java.util.ArrayList;
import jade.wrapper.*;
import universe.containers.AuxiliaryContainer;
import universe.containers.MainContainer;
import universe.laws.Constants;
import universe.laws.VirusGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Universe {

    int GRID_SIZE;
    int UNIVERSE_SIZE;
    int IMMUNITY_STRENGTH_PERCENTAGE;
    int pathogens_replication_factor;
    Boolean need_for_jade_gui;

    public static HashMap<String, ContainerController> CONTAINER_CONTROLLER_HASH_MAP;
    public static HashMap<ContainerController, int[]> CONTROLLER_GRID_MAP;
    public static HashMap<String, ContainerController> LYMPH_PATH_MAP;
    public static HashMap<String, int[]> LYMPH_COORDINATE_MAP;

    public static AgentController dendriticCellController;
    public static List<AgentController> CD8CellControllerList = new ArrayList<>();
    public static List<AgentController> MacrophageControllerList = new ArrayList<>();

    public Universe(
            int grid_size, int immunity_strength_percentage,
            int pathogens_replication_factor,
            Boolean need_for_jade_gui) {
        GRID_SIZE = grid_size;
        UNIVERSE_SIZE = GRID_SIZE * GRID_SIZE;
        this.IMMUNITY_STRENGTH_PERCENTAGE = immunity_strength_percentage;
        this.pathogens_replication_factor = pathogens_replication_factor;
        this.need_for_jade_gui = need_for_jade_gui;
    }

    public int getGRID_SIZE() {
        return GRID_SIZE;
    }

    public void startUniverse() throws ControllerException {
        // Creating MainContainer
        CONTAINER_CONTROLLER_HASH_MAP = new HashMap<>();
        CONTROLLER_GRID_MAP = new HashMap<>();
        LYMPH_PATH_MAP = new HashMap<>();
        LYMPH_COORDINATE_MAP = new HashMap<>();

        MainContainer mainContainer = new MainContainer(need_for_jade_gui);
        ContainerController mainContainerController = mainContainer.createMainContainer();

        // Creating AuxiliaryContainer and HashMap
        AuxiliaryContainer auxiliaryContainer = new AuxiliaryContainer();

        int index = 0;
        for (int index_y = 0; index_y < GRID_SIZE; index_y++) {
            for (int index_x = 0; index_x < GRID_SIZE; index_x++) {

                ContainerController containerController = auxiliaryContainer.CreateAuxiliaryContainer(index);
                try {
                    CONTAINER_CONTROLLER_HASH_MAP.put(containerController.getContainerName(), containerController);

                    int[] coordinate = new int[] { index_x, index_y };
                    CONTROLLER_GRID_MAP.put(containerController, coordinate);

                    /* Diagonal grids are chosen to be lymph nodes */
                    if (index_x == index_y) {
                        String lymphContainerName = "lymphVesselContainer-"
                                .concat(String.valueOf(index_x) + String.valueOf(index_y));
                        LYMPH_PATH_MAP.put(lymphContainerName, containerController);
                        LYMPH_COORDINATE_MAP.put(lymphContainerName, new int[] { index_x, index_y });
                    }

                } catch (Exception exception) {
                    exception.getStackTrace();
                }
                index++;
            }
        }

        // Create Cell Agents in Auxiliary Containers
        for (ContainerController auxiliaryContainerController : CONTROLLER_GRID_MAP.keySet()) {
            auxiliaryContainer.createCell(auxiliaryContainerController);
        }

        /* Create the lymph vessel */
        for (String vesselContainer : LYMPH_PATH_MAP.keySet()) {
            ContainerController lymphVesselContainerController = LYMPH_PATH_MAP.get(vesselContainer);
            auxiliaryContainer.createLymphVessel(lymphVesselContainerController);
        }

        /* Create Dendritic Cell(s) */
        int randomInt = (int) (Math.random() * (UNIVERSE_SIZE));
        ContainerController randContainerController = CONTAINER_CONTROLLER_HASH_MAP
                .get("Container-".concat(String.valueOf(randomInt)));
        dendriticCellController = auxiliaryContainer.createDendriticCell(randContainerController, 0);

        /* Creating CD4TCell Manager Agent */
        mainContainer.createCD4TCellManager(mainContainerController, UNIVERSE_SIZE);

        // Create Macrophage Agents Main Container
        for (int i = 0; i < UNIVERSE_SIZE * Constants.PERCENTAGE_OF_MACROPHAGE / 100; i++) {

            int randInt = (int) (Math.random() * (UNIVERSE_SIZE));
            ContainerController iContainerController = CONTAINER_CONTROLLER_HASH_MAP
                    .get("Container-".concat(String.valueOf(randInt)));
            AgentController macrophageController = mainContainer.createMacrophageAgent(mainContainerController, i, iContainerController);
            MacrophageControllerList.add(macrophageController);
        }

        // Creating Virus Agents on Random Containers

        int rndInt = (int) (Math.random() * (UNIVERSE_SIZE));
        ContainerController randomContainerController = CONTAINER_CONTROLLER_HASH_MAP
                .get("Container-".concat(String.valueOf(rndInt)));

        VirusGenerator virusGenerator = new VirusGenerator(
                randomContainerController,
                pathogens_replication_factor);

        int[] virus_signature = virusGenerator.generateVirus();

        /* Creating CD8TCells */
        int totalAvailableCD8TCells = UNIVERSE_SIZE * Constants.PERCENTAGE_OF_CD8T_CELLS / 100;
        int numberOfTCellsWithSignature = totalAvailableCD8TCells * IMMUNITY_STRENGTH_PERCENTAGE / 100;

        Set<Integer> uniqueSetOfContainers = new HashSet<>();

        while (uniqueSetOfContainers.size() != totalAvailableCD8TCells) {
            Random random = new Random();
            int randomContainerNumber = random.nextInt(UNIVERSE_SIZE - 1);
            uniqueSetOfContainers.add(randomContainerNumber);
        }

        int cd8Counter = 0;
        for (int containerNumber : uniqueSetOfContainers) {

            ContainerController jContainerController = CONTAINER_CONTROLLER_HASH_MAP
                    .get("Container-".concat(String.valueOf(containerNumber)));

            if (cd8Counter <= numberOfTCellsWithSignature) {
                AgentController CD8Controller = auxiliaryContainer.createCD8TCell(
                        jContainerController,
                        virus_signature,
                        cd8Counter);
                CD8CellControllerList.add(CD8Controller);
            } else {
                AgentController CD8Controller = auxiliaryContainer.createCD8TCell(
                        jContainerController,
                        Constants.GENERIC_VIRUS_SIGNATURE,
                        cd8Counter);
                CD8CellControllerList.add(CD8Controller);
            }
            cd8Counter++;
        }

        // Initiator Agent
        mainContainer.createInitiatorAgent(
                mainContainerController,
                CONTROLLER_GRID_MAP,
                LYMPH_PATH_MAP,
                LYMPH_COORDINATE_MAP,
                dendriticCellController,
                CD8CellControllerList,
                MacrophageControllerList,
                virusGenerator);
    }

    /* Stopping the simulation */
    public void stop() throws StaleProxyException {
        for (ContainerController auxiliaryContainerController : CONTROLLER_GRID_MAP.keySet()) {
            auxiliaryContainerController.kill();
        }
    }
}
