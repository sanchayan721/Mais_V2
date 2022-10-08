package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import universe.containers.AuxiliaryContainer;
import universe.laws.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CD8TCellAgent extends Agent {

    @Override
    protected void setup() {
        Object[] argumants = getArguments();
        Boolean canDetectVirusSignature = (Boolean) argumants[0];
    }
}
