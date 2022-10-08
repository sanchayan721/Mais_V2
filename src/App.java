import jade.wrapper.ControllerException;
import universe.Universe;

public class App {

    static int grid_size = 3;
    static int immunity_strength_percentage = 50;
    static int[] pathogens_replication_factor = new int[] { 3 };

    public static void main(String[] args) throws ControllerException {
        Universe universe = new Universe(
                grid_size,
                immunity_strength_percentage,
                pathogens_replication_factor);
        universe.startUniverse();
    }
}
