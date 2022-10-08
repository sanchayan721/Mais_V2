package universe.laws;

import universe.Universe;

public class Constants {

    public static int GRID_SIZE;
    public static final int MACROPHAGE_SLEEP_TIME = 2000; //Seconds
    public static final int MACROPHAGE_CELL_COMMUNICATION_TIME = 100;
    public static final int PHAGOCYTE_MEMORY_COMMUNICATION_TIME = 100;
    public static final int[] CELL_IDENTIFYING_DNA = new int[]
    {0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0};
    public static final int CELL_MUTATION_PERIOD = 5000; //Seconds
    

    /* Amount of Macrophage; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_MACROPHAGE = 30;

    /* Amount of Dendritic Cells; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_DENDRITIC_CELLS = 20;

    /* Amount of Dendritic Cells; A percentage of total number of cells */
    public static final int PERCENTAGE_OF_CD8T_CELLS = 40;

    /* Virus Constants */
    public static final int VIRUS_CELL_COMMUNICATION_TIME = 1000;
    public static final int VIRUS_REPLICATION_TIME = 10000; //Seconds
    public static final int VIRUS_REPLICATION_FACTOR = 3;
    public static final int KILL_THE_CELL_AFTERWARD = 2000;
    public static final int CELL_REGENERATION_TIME_AVG = 4000;
    public static final int[] VIRUS_SIGNATURE = new int[] {10, 25, 20, 22, 15};
    /* public static final int[] VIRUS_IDENTIFYING_CODON = new int[] {1, 0, 0, 1, 1, 0, 1, 1, 0, 0}; */
    
    /* System Settings */
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    /* Lymph Vessel Topology */

    public static Boolean LYMPH_TOPOLOGY_CYCLIC = false;

    public void setGridSize (Universe universe){
        GRID_SIZE = universe.getGRID_SIZE();
    }

}
