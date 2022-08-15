package universe.laws;

import universe.Universe;

public class Constants {

    public static int GRID_SIZE;
    //public static int UNIVERSE_SIZE = GRID_SIZE * GRID_SIZE;
    public static final int PHAGOCYTE_SLEEP_TIME = 1000; //Seconds
    public static final int PHAGOCYTE_CELL_COMMUNICATION_TIME = 100;
    public static final int PHAGOCYTE_MEMORY_COMMUNICATION_TIME = 100;
    public static final int VIRUS_CELL_COMMUNICATION_TIME = 1000;
    public static final int VIRUS_REPLICATION_TIME = 10000; //Seconds
    public static final int VIRUS_REPLICATION_FACTOR = 2;
    public static final int[] CELL_IDENTIFYING_DNA = new int[]
            {0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0};
    public static final int CELL_MUTATION_PERIOD = 5000; //Seconds
    public static final int KILL_THE_CELL_AFTERWARD = 10000;
    public static final int CELL_REGENERATION_TIME_AVG = 4000;
    public static final String VIRUS_SIGNATURE = "10,25,20,22";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public void setGridSize (Universe universe){
        GRID_SIZE = universe.getGRID_SIZE();
    }
}
