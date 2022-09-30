package universe;

import jade.wrapper.*;
import universe.containers.AuxiliaryContainer;
import universe.containers.MainContainer;
import universe.laws.Constants;
import java.util.HashMap;

public class Universe {

    int GRID_SIZE;
    int UNIVERSE_SIZE;
    public static HashMap<String, ContainerController> CONTAINER_CONTROLLER_HASH_MAP;
    public static HashMap<ContainerController, int[]> CONTROLLER_GRID_MAP;
    public static HashMap<String, ContainerController> LYMPH_PATH_MAP;
    public static HashMap<String, int[]> LYMPH_COORDINATE_MAP;

    public Universe(int grid_size) {
        GRID_SIZE = grid_size;
        UNIVERSE_SIZE = GRID_SIZE * GRID_SIZE;
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

        MainContainer mainContainer = new MainContainer();
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
        auxiliaryContainer.createDendriticCell(randContainerController);

        // Initiator Agent
        mainContainer.createInitiatorAgent(
                mainContainerController,
                CONTROLLER_GRID_MAP,
                LYMPH_PATH_MAP,
                LYMPH_COORDINATE_MAP);

        // Create Macrophage Agents Main Container
        for (int i = 0; i <= GRID_SIZE * Constants.PERCENTAGE_OF_MACROPHAGE; i++) {

            int randInt = (int) (Math.random() * (UNIVERSE_SIZE));
            ContainerController iContainerController = CONTAINER_CONTROLLER_HASH_MAP
                    .get("Container-".concat(String.valueOf(randInt)));
            mainContainer.createMacrophageAgent(mainContainerController, i, iContainerController);

        }

        // Creating First Virus Agent on A Random Container
        int randInt = (int) (Math.random() * (UNIVERSE_SIZE));
        ContainerController randomContainerController = CONTAINER_CONTROLLER_HASH_MAP
                .get("Container-".concat(String.valueOf(randInt)));

        try {
            AgentController virusController = randomContainerController.createNewAgent(
                    "virus.".concat(randomContainerController.getContainerName()),
                    "universe.agents.VirusAgent", new Object[] {});
            System.out.println("First " + Constants.ANSI_RED + "Virus " + Constants.ANSI_RESET +
                    "dropped at ".concat(randomContainerController.getContainerName()));
            virusController.start();
        } catch (Exception exception) {
            exception.getStackTrace();
        }
    }

    /* Stopping the simulation */
    public void stop() throws StaleProxyException {
        for (ContainerController auxiliaryContainerController : CONTROLLER_GRID_MAP.keySet()) {
            auxiliaryContainerController.kill();
        }
    }
}
