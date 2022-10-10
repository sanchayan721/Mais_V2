package universe.laws;

import universe.Universe;

public class Constants {

    public static int SIMULATION_TIME_SCALE = 1;    

    public static int GRID_SIZE;
    public static final int MACROPHAGE_SLEEP_TIME = 3000 * SIMULATION_TIME_SCALE; //Seconds
    public static final int MACROPHAGE_CELL_COMMUNICATION_TIME = 100 * SIMULATION_TIME_SCALE;
    public static final int MACROPHAGE_MEMORY_COMMUNICATION_TIME = 100 * SIMULATION_TIME_SCALE;

    /* Cell's Constants */
    public static final int[] CELL_IDENTIFYING_DNA = new int[]
    {0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0};
    public static final int CELL_MUTATION_PERIOD = 5000 * SIMULATION_TIME_SCALE; //Seconds
    public static final int CELL_REGENERATION_TIME_AVG = 4000 * SIMULATION_TIME_SCALE;
    
    /* CD48TCell Manager Constants */
    public static final int TIME_TO_FIND_A_MATCHING_NAIEVE_CD4TCELL = 5000 * SIMULATION_TIME_SCALE; //Seconds

    /* Amount of Macrophage; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_MACROPHAGE = 30;
    public static final int NUMBER_OF_VIRUS_MACROPHAGE_CAN_KILL = 5;

    /* Amount of CD4TCell; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_CD4TCell = 30;
    public static final int CD4TCell_ACTIVATION_TIME = 5000 * SIMULATION_TIME_SCALE; //Seconds
    public static final int CD4TCell_SLEEP_TIME = 1000 * SIMULATION_TIME_SCALE; //Seconds

    /* Amount of Dendritic Cells; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_DENDRITIC_CELLS = 20;

    /* Amount of Dendritic Cells; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_CD8T_CELLS = 40;

    /* Virus Constants */
    public static final int[] GENERIC_VIRUS_SIGNATURE = new int[] {10, 25, 20, 22, 15};
    public static final int VIRUS_SIGNATURE_LENGTH = CELL_IDENTIFYING_DNA.length * 30 / 100; // 30% of Cell DNA Length
    /* public static final int VIRUS_REPLICATION_TIME = 10000; //Seconds
    public static final int VIRUS_CELL_COMMUNICATION_TIME = 1000 * SIMULATION_SPEED;
    public static final int VIRUS_REPLICATION_FACTOR = 3;
    public static final int KILL_THE_CELL_AFTERWARD = 2000;
    public static final int[] VIRUS_IDENTIFYING_CODON = new int[] {1, 0, 0, 1, 1, 0, 1, 1, 0, 0}; */
    
    /* System Color Settings */
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";


    /* CD8TCell Constants */
    public static final int CD8T_CELL_SLEEP_TIME = 4000 * SIMULATION_TIME_SCALE;
    public static final int CD8T_CELL_COMMUNICATION_TIME = 200 * SIMULATION_TIME_SCALE;

    /* Dendritic Cell Constants */
    public static final int DENDRITIC_CELL_COMMUNICATION_TIME = 300 * SIMULATION_TIME_SCALE;
    public static final int DENDRITIC_CELL_SLEEP_TIME = 3000 * SIMULATION_TIME_SCALE;
    public static final int DENDRITIC_VESSEL_SLEEP_TIME = 1000 * SIMULATION_TIME_SCALE;

    /* Lymph Vessel Topology */
    public static Boolean LYMPH_TOPOLOGY_CYCLIC = false;

    public void setGridSize (Universe universe){
        GRID_SIZE = universe.getGRID_SIZE();
    }

}
