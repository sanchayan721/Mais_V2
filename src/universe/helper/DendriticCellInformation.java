package universe.helper;

import java.io.Serializable;
import java.util.ArrayList;
import jade.core.Location;

public class DendriticCellInformation implements Serializable{
    public int[] virus_signature;
    public ArrayList<Location> path; 

    public DendriticCellInformation(int[] virus_signature, ArrayList<Location> path) {
        this.virus_signature = virus_signature;
        this.path = path;
    }
}
