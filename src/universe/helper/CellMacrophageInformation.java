package universe.helper;

import java.io.Serializable;

import jade.core.AID;

public class CellMacrophageInformation implements Serializable{
    public Boolean exh_macrophage_present;
    public AID exh_macrophage_aid; 

    public CellMacrophageInformation(Boolean exh_macrophage_present, AID exh_macrophage_aid) {
        this.exh_macrophage_present = exh_macrophage_present;
        this.exh_macrophage_aid = exh_macrophage_aid;
    }
}
