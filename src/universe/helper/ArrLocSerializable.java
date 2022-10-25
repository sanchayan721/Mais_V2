package universe.helper;

import java.io.Serializable;
import java.util.ArrayList;

import jade.core.Location;

public class ArrLocSerializable implements Serializable{
    public ArrayList<Location> locationArray;

    public ArrLocSerializable(ArrayList<Location> arrLocation) {
        this.locationArray = arrLocation;
    }
}
