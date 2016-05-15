package communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Coord;
import common.MapTile;

/** Used to send and or receive locations of sciences to the team.
 * 
 * @author Shay */
public class RoverCommunication implements Runnable, Detector, Sender {

    private Map<Group, DataOutputStream> group_output_map;
    private List<Coord> discovered_science;
    private List<Group> groups_list;
    private String rover_name;

    public RoverCommunication(String rover_name, List<Group> groups) {
        group_output_map = new HashMap<Group, DataOutputStream>();
        discovered_science = new ArrayList<Coord>();
        this.rover_name = rover_name;

        groups_list = removeSelfFromGroups(groups);
    }

    /** Scan the map for science. Update rover science list. Share the science
     * to all the ROVERS. Display result on console. Also display the list of
     * connected ROVER
     * 
     * @param map
     *            Result of scanMap.getScanMap(). Use to check for science
     * @param currentLoc
     *            ROVER current location. Use to calculate the science absolute
     *            location
     * @param sight_range
     *            Either 3, if your radius is 7x7, or 5, if your radius is 11x11
     * @throws IOException */
    public void detectAndShare(MapTile[][] map, Coord currentLoc, int sight_range) throws IOException {
        List<Coord> detected_sciences = detectScience(map, currentLoc, sight_range);
        List<Coord> new_sciences = updateDiscoveries(detected_sciences);
        for (Coord c : new_sciences) {
            shareScience(convertToList(group_output_map.values()), c);
        }
        displayAllDiscoveries();
        displayConnections();
    }

    private List<DataOutputStream> convertToList(Collection<DataOutputStream> values) {
        List<DataOutputStream> output_streams = new ArrayList<DataOutputStream>();
        for (DataOutputStream dos : values) {
            output_streams.add(dos);
        }
        return output_streams;
    }

    @Override
    public List<Coord> detectScience(MapTile[][] map, Coord rover_coord, int sight_range) {
        List<Coord> science_coords = new ArrayList<Coord>();

        /* iterate through every MapTile Object in the 2D Array. If the MapTile
         * contains science, calculate and save the coordinates of the tiles. */
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

    private void displayAllDiscoveries() {
        System.out.println(rover_name + " SCIENCE-DISCOVERED-BY-ME: " + toProtocolString(discovered_science));
        System.out.println(rover_name + " TOTAL-NUMBER-OF-SCIENCE-DISCOVERED-BY-ME: " + discovered_science.size());
    }

    private void displayConnections() {
        System.out.println(rover_name + " CONNECTED TO: " + group_output_map.size() + " ROVERS");
        System.out.println(rover_name + " CONNECTIONS: " + group_output_map.keySet());
    }

    private List<Group> removeSelfFromGroups(List<Group> groups) {
        List<Group> groups_without_me = new ArrayList<Group>();
        for (Group g : groups) {
            if (!g.getName().equals(rover_name)) {
                groups_without_me.add(g);
            }
        }
        return groups_without_me;
    }

    @Override

    public void run() {

        /* Will try to connect to all the ROVERs on a separate Thread. Add them
         * to a list if connection is successful. */
        for (Group group : groups_list) {

            new Thread(() -> {
                final int MAX_ATTEMPTS = 10;
                int attempts = 0;
                Socket socket = null;

                do {
                    try {
                        socket = new Socket(group.getIp(), group.getPort());
                        group_output_map.put(group, new DataOutputStream(socket.getOutputStream()));
                        System.out.println(rover_name + " CONNECTED TO " + group);
                    } catch (Exception e) {
                        /* Do nothing. */
                    }
                } while (socket == null && attempts <= MAX_ATTEMPTS);
            }).start();
        }
    }

    /** @param coords
     *            Coord with Science
     * @return A list of Coord.toProtocol(). For example (SOIL CRYSTAL 5 3, ROCK
     *         MINERAL 52 13) */
    private String toProtocolString(List<Coord> coords) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = coords.size() - 1; i >= 0; i--) {
            sb.append(coords.get(i).toProtocol() + " ");
        }
        sb.append("]");
        return sb.toString();
    }

    /** @param detected_sciences
     *            The science that your ROVER found on its scanned map
     * @return A list of Coordinates that are new. Will compare the
     *         detected_sciences list with the ALL the science the ROVER has
     *         discovered. The result , what this method is returning, is the
     *         difference between detected_sciences and all the sciences
     *         discovered so far by the ROVER */
    private List<Coord> updateDiscoveries(List<Coord> detected_sciences) {
        List<Coord> new_sciences = new ArrayList<Coord>();
        for (Coord c : detected_sciences) {
            if (!discovered_science.contains(c)) {
                discovered_science.add(c);
                new_sciences.add(c);
            }
        }
        return new_sciences;
    }

    @Override
    public void shareScience(List<DataOutputStream> output_streams, Coord coord) throws IOException {
        for (DataOutputStream dos : output_streams) {
            dos.writeBytes(coord.toProtocol() + "\n");
        }
    }

}
