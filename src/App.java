import jade.wrapper.ControllerException;
import universe.Universe;

public class App {

    public static void main(String[] args) throws ControllerException {
        Universe universe = new Universe(5);
        universe.startUniverse();
    }
}
