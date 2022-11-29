package universe.laws;

import universe.Universe;

public class Constants {

    public static double SIMULATION_TIME_SCALE = 0.5;    

    public static int GRID_SIZE;
    public static final int MACROPHAGE_SLEEP_TIME = (int) (3000 * SIMULATION_TIME_SCALE); //Seconds
    public static final int MACROPHAGE_CELL_COMMUNICATION_TIME = (int) (100 * SIMULATION_TIME_SCALE);
    public static final int MACROPHAGE_MEMORY_COMMUNICATION_TIME = (int) (100 * SIMULATION_TIME_SCALE);

    /* Cell's Constants */
    public static final int[] CELL_IDENTIFYING_DNA = new int[]
    {0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0};
    public static final int CELL_MUTATION_PERIOD = (int) (5000 * SIMULATION_TIME_SCALE); //Seconds
    public static final int CELL_REGENERATION_TIME_AVG = (int) (4000 * SIMULATION_TIME_SCALE);
    
    /* CD48TCell Manager Constants */
    public static final int TIME_TO_FIND_A_MATCHING_NAIEVE_CD4TCELL = (int) (5000 * SIMULATION_TIME_SCALE); //Seconds

    /* Amount of Macrophage; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_MACROPHAGE = 20;
    public static final int NUMBER_OF_VIRUS_MACROPHAGE_CAN_KILL = 5;

    /* Amount of CD4TCell; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_CD4TCell = 30;
    public static final int CD4TCell_ACTIVATION_TIME = (int) (5000 * SIMULATION_TIME_SCALE); //Seconds
    public static final int CD4TCell_SLEEP_TIME = (int) (8000 * SIMULATION_TIME_SCALE); //Seconds

    /* Amount of Dendritic Cells; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_DENDRITIC_CELLS = 5;

    /* Amount of Dendritic Cells; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_CD8T_CELLS = 20;

    /* Virus Constants */
    public static final int[] GENERIC_VIRUS_SIGNATURE = new int[] {10, 25, 20, 22, 15};
    public static final int VIRUS_SIGNATURE_LENGTH = CELL_IDENTIFYING_DNA.length * 30 / 100; // 30% of Cell DNA Length
    
    
    
    /* CD8TCell Constants */
    public static final int CD8T_CELL_SLEEP_TIME = (int) (4000 * SIMULATION_TIME_SCALE);
    public static final int CD8T_CELL_COMMUNICATION_TIME = (int) (200 * SIMULATION_TIME_SCALE);
    
    /* Dendritic Cell Constants */
    public static final int DENDRITIC_CELL_COMMUNICATION_TIME = (int) (300 * SIMULATION_TIME_SCALE);
    public static final int DENDRITIC_CELL_SLEEP_TIME = (int) (3000 * SIMULATION_TIME_SCALE);
    public static final int DENDRITIC_VESSEL_SLEEP_TIME = (int) (1000 * SIMULATION_TIME_SCALE);
    
    /* Lymph Vessel Topology */
    public static Boolean LYMPH_TOPOLOGY_CYCLIC = false;
    
    /* System Color Settings */
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BRED = "\u001B[91m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_BBLUE = "\u001B[94m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RESET = "\u001B[0m";
    
    public void setGridSize (Universe universe){
        GRID_SIZE = universe.getGRID_SIZE();
    }
    
}
