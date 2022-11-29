import jade.wrapper.ControllerException;
import monitoring.Monitor;
import universe.Universe;

public class Experiment {static int grid_size = 10;
    static Boolean need_for_jade_gui = false;
    
    static int re_infection_immunity_strength_percentage = 100;
    static int pathogens_replication_factor = 2;

    public static void main(String[] args) throws ControllerException {
        Universe universe = new Universe( 
                grid_size, 
                re_infection_immunity_strength_percentage, 
                pathogens_replication_factor,
                need_for_jade_gui
            );
        universe.startUniverse();

        Monitor monitor = new Monitor();
        monitor.run();
        universe.stop();
    }
}
