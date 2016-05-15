package communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.Coord;
import common.MapTile;
import enums.Science;

/**
 * Implement the Detector interface. Detects science.
 * @author Shay
 *
 */
public class ScienceDetector implements Detector {


    @Override
    public List<Coord> detectScience(MapTile[][] map, Coord rover_coord, int sight_range) {

        List<Coord> science_coords = new ArrayList<Coord>();

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                
                MapTile mapTile = map[x][y];
                
                if (Detector.DETECTABLE_SCIENCES.contains(mapTile.getScience())) {
                    int tileX = rover_coord.xpos + (x - sight_range);
                    int tileY = rover_coord.ypos + (y - sight_range);
                    Coord coord = new Coord(mapTile.getTerrain(), mapTile.getScience(), tileX, tileY);
                    science_coords.add(coord);
                }
            }
        }
        return science_coords;
    }

}
