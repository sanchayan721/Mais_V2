package universe.laws;

import java.util.ArrayList;

public class LymphMap {

    int gridDimensions = Constants.GRID_SIZE;

    public ArrayList<int[]> getNextVessels(int[] currentCoordinate) {
        ArrayList<int[]> nextVessel = new ArrayList<>();

        int current_x = currentCoordinate[0];
        int current_y = currentCoordinate[1];

        /* vessel cordinate Equation x = y */
        int dx = 1;
        int dy = 1;

        int nextVessel_x = current_x + dx;
        int nextVessel_y = current_y + dy;

        int[] nextCoordinate = new int[] { nextVessel_x, nextVessel_y };
        
        nextVessel.add(nextCoordinate);

        return nextVessel;
    }
}
