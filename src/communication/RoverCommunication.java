package communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Coord;
import common.MapTile;

/** Used to send and or receive locations of sciences to the team.
 * 
 * @author Shay */
public class RoverCommunication implements Runnable, Detector {

    private Map<Group, DataOutputStream> group_output_map;
    private List<Coord> discovered_science;
    private List<Group> blue_corp_groups;
    private RoverChatSender sender;
    private String rover_name;

    public RoverCommunication(String rover_name, List<Group> groups) {
        group_output_map = new HashMap<Group, DataOutputStream>();
        discovered_science = new ArrayList<Coord>();
        sender = new RoverChatSender();
        this.rover_name = rover_name;

        blue_corp_groups = removeSelfFromGroups(groups);
    }

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

    public void displayAllDiscoveries() {
        System.out.println(rover_name + " SCIENCE-DISCOVERED-BY-ME: " + toProtocolString(discovered_science));
        System.out.println(rover_name + " TOTAL-NUMBER-OF-SCIENCE-DISCOVERED-BY-ME: " + discovered_science.size());
    }

    public void displayConnections() {
        System.out.println(rover_name + " CONNECTED TO:" + group_output_map.size() + " ROVERS");
        System.out.println(rover_name + " CONNECTIONS: " + group_output_map.keySet());
    }

    public List<Group> getGroups() {
        return blue_corp_groups;
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

        for (Group group : blue_corp_groups) {

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

    public void setBlue_corp_groups(List<Group> blue_corp_groups) {
        this.blue_corp_groups = blue_corp_groups;
    }

    public void shareScience(Coord coord) throws IOException {
        List<DataOutputStream> output_streams = new ArrayList<DataOutputStream>();
        for (DataOutputStream dos : group_output_map.values()) {
            output_streams.add(dos);
        }
        sender.shareScience(output_streams, coord);
    }

    public String toProtocolString(List<Coord> coords) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = coords.size() - 1; i >= 0; i--) {
            sb.append(coords.get(i).toProtocol() + " ");
        }
        sb.append("]");
        return sb.toString();
    }

    public List<Coord> updateDiscoveries(List<Coord> detected_sciences) {
        List<Coord> new_sciences = new ArrayList<Coord>();
        for (Coord c : detected_sciences) {
            if (!discovered_science.contains(c)) {
                discovered_science.add(c);
                new_sciences.add(c);
            }
        }
        return new_sciences;
    }
}
