package communication;

import java.util.ArrayList;
import java.util.List;

/** Represent each Group or Team. Use to identify and communicates with the
 * ROVERS
 * 
 * @author Shay */
public class Group {

    private String ip;
    private int port;

    private String name;

    public Group(String name, String ip, int port) {
        this.name = name;
        this.port = port;
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String toString() {
        return "name=" + name + " ip=" + ip + " port=" + port;
    }

    /** @param ip
     *            I assume that all the ROVERS will be connected to the same ip
     *            address. This method will return a list of all the ROVERS with
     *            provided ip address. This ip address should be yours. Please
     *            note that this will only return ROVERS that can gather, as
     *            they are the only ones that expects communications.
     *            Furthermore the list will includes a dummy rover at port 53799
     *            which is use mostly for testing purposes.
     * @return */
    public final static List<Group> BLUE_GATHERERS(String ip) {

        List<Group> blueCorp = new ArrayList<Group>();

        /* For testing purpose. This server will display any message sent to
         * it */
        blueCorp.add(new Group("ROVER_DUMMY", ip, 53799));

        /* All the ROVERS that can gathers */
        blueCorp.add(new Group("ROVER_01", ip, 53701));
        blueCorp.add(new Group("ROVER_03", ip, 53703));
        blueCorp.add(new Group("ROVER_04", ip, 53704));
        blueCorp.add(new Group("ROVER_07", ip, 53707));
        blueCorp.add(new Group("ROVER_08", ip, 53708));
        blueCorp.add(new Group("ROVER_09", ip, 53709));

        return blueCorp;
    }
}
