package universe.helper;

import java.io.Serializable;

public class VirusInformation implements Serializable {
    public int[] virus_signature;
    public int virus_replication_factor;
    public int virus_cell_communication_time;
    public int virus_replication_time;
    public int time_to_kill_the_cell;
}
