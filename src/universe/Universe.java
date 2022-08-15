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
                    CONTROLLER_GRID_MAP.put(containerController, new int[] { index_x, index_y });
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

        // Initiator Agent
        mainContainer.createInitiatorAgent(mainContainerController, CONTROLLER_GRID_MAP);

        // Create Macrophage Agents Main Container

        for (int i = 0; i <= (int) GRID_SIZE * 50/100 ; i++) {
            int randInt = (int) (Math.random() * (UNIVERSE_SIZE));
            ContainerController iContainerController = CONTAINER_CONTROLLER_HASH_MAP.get("Container-".concat(String.valueOf(randInt)));
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

    public void stop() throws StaleProxyException {
        for (ContainerController auxiliaryContainerController : CONTROLLER_GRID_MAP.keySet()) {
            auxiliaryContainerController.kill();
        }
    }
}
