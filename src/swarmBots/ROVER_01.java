package swarmBots;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import communication.Detector;
import communication.Group;
import communication.RoverCommunication;
import enums.RoverDriveType;
import enums.RoverToolType;
import enums.Science;
import enums.Terrain;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_01 {

	BufferedReader in; //Read from Stream
	PrintWriter out; // Write to Stream
	String rovername; // UNique identifier for Rover
	ScanMap scanMap; // Map Variable
	int sleepTime; // for timer
	String SERVER_ADDRESS = "localhost"; //192.168.1.106";
	static final int PORT_ADDRESS = 9537;
	int counter = 0;
	

	Coord currentLoc = null;
	Coord previousLoc = null;
	
	//this variables are used for moving the rover g



	// Direction Variables
	String east = "E"; 
	String west = "W";
	String north = "N";
	String south = "S";
	
	String direction = east; // Initial direction of the Rover
	int blockedCounter = 0;

	List<Coord> crystalCoordinates = new ArrayList<Coord>(); // Co-ordinates for crystals as the Rover has Spectral Scanner g

	Coord targetLocationCrystal = null; //targetLocation where Rover will move towards g

	Boolean blocked = false; // Rover in not blocked initially
	
	/* Communication Module*/
	RoverCommunication rocom;
    

	
	public ROVER_01() {	//Constructor for Rover class
		System.out.println("ROVER_01 rover object constructed");
		rovername = "ROVER_01";
		SERVER_ADDRESS = "localhost";
		sleepTime = 300; // in milliseconds - smaller is faster, but the server will cut connection if it is too small. This value should be a safe but slow timer value
	
	}
	
	public ROVER_01(String serverAddress) { // Constructor Overload
		System.out.println("ROVER_01 rover object constructed");
		rovername = "ROVER_01";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 200; // in milliseconds - smaller is faster, but the server will cut connection if it is too small
	}

	/**
	 * development of scanning 7*7 matrix ends here
	 * Connects to the server then enters the processing loop.
	 */

	// Detect crystal and add target locations of crystals
	 public void detectCrystalScience(MapTile[][] scanMapTiles, Coord currentLoc) 
	 {       
		 int centerIndex = (scanMap.getEdgeSize() - 1) / 2; // The position of Rover in 7*7 Scanner Map
		 int xPos = currentLoc.xpos - centerIndex; // X - Co-ordinate of Rover 
		 int yPos = currentLoc.ypos - centerIndex; // y - Co-ordinate of Rover
		 int scienceXPosition, scienceYPosition;
		 for (int x = 0; x < scanMapTiles.length; x++) // Scanning Map
	     	 {
	            for (int y = 0; y < scanMapTiles.length; y++) 
	            {
	                if (scanMapTiles[x][y].getScience() == Science.CRYSTAL)  // if the MapTile contains Crystals
	                {
                		//as we have only drill with spectral sensor, we will get only crystal in rock and gravel which is nearest to us
	                	if( scanMapTiles[x][y].getTerrain() == Terrain.ROCK || scanMapTiles[x][y].getTerrain() == Terrain.GRAVEL || scanMapTiles[x][y].getTerrain() == Terrain.SOIL)
	                 	{
	                		scienceXPosition = xPos + x;
		                    	scienceYPosition = yPos + y;
			                Coord coord = new Coord(scanMapTiles[x][y].getTerrain(), scanMapTiles[x][y].getScience(),
			                		scienceXPosition, scienceYPosition);
			                crystalCoordinates.add(coord); // Adding reachable Science in a list to collect 
	                	}
        		}
            	    }
	     	}
	 }
	
	// Moving the rover
	public void move(String direction)
	{
		out.println("GATHER");
		out.println("MOVE " + direction);
	}
	
	//change direction of rover is next move is a sand, wall or a rover
	//this function will move the rover randomly in the east,west,north or south direction.	
	public String changeRoverDirection(String direction)
	{
		ArrayList<String> directions = new ArrayList<String>();
		directions.add("E");directions.add("W");directions.add("N");directions.add("S");
		Random randomgenerator = new Random();
		switch(direction)
		{
		
		case "E": return directions.get(randomgenerator.nextInt(4));
		case "W": return directions.get(randomgenerator.nextInt(4));
		case "N": return directions.get(randomgenerator.nextInt(4));
		case "S": return directions.get(randomgenerator.nextInt(4));
		default: return null;
		}
	}
//validation of the rover is the next move.
	
//validty of rover next move
	public Boolean checkValidityOfMove(MapTile[][] scanMapTiles, String direction)
	{
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int xpos = centerIndex;
		int ypos = centerIndex;
		if(direction.equalsIgnoreCase(east))
		{
			xpos = xpos+1;
		}
		if(direction.equalsIgnoreCase(west))
		{
			xpos = xpos-1;
		}
		if(direction.equalsIgnoreCase(north))
		{
			ypos = ypos-1;
		}
		if(direction.equalsIgnoreCase(south))
		{
			ypos = ypos+1;
		}
		if(scanMapTiles[xpos][ypos].getTerrain() == Terrain.SAND || scanMapTiles[xpos][ypos].getTerrain() == Terrain.NONE
				|| scanMapTiles[xpos][ypos].getHasRover() == true)
		{
			return false;
		}
		else
			return true;
		
	}
//this function will scan the map tiles if there is a sand or not?	

	//move 10 times in a random direction when blocked
	public void moveRandomDirection(MapTile[][] scanMapTiles)
	{
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		while (!checkValidityOfMove(scanMapTiles, direction)) {

			direction = changeRoverDirection(direction);
			
		}

		move(direction);
	}
	
	//move towards target location
	public void moveTowardsTargetLocation(MapTile[][] scanMapTiles,  Coord currentLoc) throws IOException, InterruptedException
	{
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		
		if(checkValidityOfMove(scanMapTiles, direction))
		{

			detectCrystalScience(scanMapTiles, currentLoc);
			move(direction);
			
		}
		
		else
		{
			moveRandomDirection(scanMapTiles);
			blocked = true;
		
		}
	}
//movement of rover
	public void roverMovement(MapTile[][] scanMapTiles, Coord currentLoc, Coord targetLocation) throws IOException, InterruptedException
	{
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int currentXPosition = currentLoc.xpos;
		int currentYPosition = currentLoc.ypos;
		int targetXPosition,targetYPosition;
		targetXPosition = targetLocation.xpos;
		targetYPosition = targetLocation.ypos;
		
		if(currentXPosition == targetXPosition)//check less than x and y)
		{
			if(currentYPosition  < targetYPosition) direction = south;
			else direction = north;
		}
		else if (currentYPosition == targetYPosition)//x are equal and y is greater and lesser)
		{
			if(currentXPosition < targetXPosition) direction = east;
			else direction = west;
			//
		}
		else if (currentXPosition != targetXPosition)
		{
			if(currentXPosition > targetXPosition) direction = west;
			else direction = east;
		}
		else if(currentYPosition != targetYPosition)
		{
			if(currentYPosition > targetYPosition) direction = north;
			else direction = south;
		}

			
		moveTowardsTargetLocation(scanMapTiles, currentLoc);
		
		
	}
	
	public void Movement(MapTile[][] scanMapTiles, Coord currentLoc) throws IOException, InterruptedException
	{
		detectCrystalScience(scanMapTiles, currentLoc);
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		if(checkValidityOfMove(scanMapTiles, direction))
		{
			if (!scanMapTiles[centerIndex][centerIndex].getScience().getSciString().equals("N")) {
				System.out.println("ROVER_01 request GATHER");
				out.println("GATHER");
			}
			
			move(direction);
			
		}
		else
		{
			while (!checkValidityOfMove(scanMapTiles, direction)) {

				direction = changeRoverDirection(direction);
				counter=0;
			}
			if (!scanMapTiles[centerIndex][centerIndex].getScience().getSciString().equals("N")) {
				System.out.println("ROVER_01 request GATHER");
				out.println("GATHER");
			}
	
			
			move(direction);
		}
		
	}
		
	private void run() throws IOException, InterruptedException {

		// Make connection to SwarmServer and initialize streams
		Socket socket = null;
		try {
			socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			
			
            // ******************* SET UP COMMUNICATION MODULE by Shay *********************
            /* Your Group Info*/
            Group group = new Group(rovername, SERVER_ADDRESS, 53701, RoverDriveType.WALKER,
                    RoverToolType.DRILL, RoverToolType.SPECTRAL_SENSOR);

            /* Setup communication, only communicates with gatherers */
            rocom = new RoverCommunication(group);
            rocom.setGroupList(Group.getGatherers());

            /* Can't go on ROCK, thus ignore any SCIENCE COORDS that is on ROCK */
            rocom.ignoreTerrain(Terrain.ROCK);

            /* Start your server, receive incoming message from other ROVERS */
            rocom.startServer();
            // ******************************************************************
	
			// Process all messages from server, wait until server requests Rover ID
			// name - Return Rover Name to complete connection
			while (true) {
				String line = in.readLine();
				if (line.startsWith("SUBMITNAME")) {
					out.println(rovername); // This sets the name of this instance
											// of a swarmBot for identifying the
											// thread to the server
					break;
				}
			}
	
			
			// ********* Rover logic setup *********
			
			String line = "";
			Coord rovergroupStartPosition = null;
			Coord targetLocation = null;
			
			/**
			 *  Get initial values that won't change
			 */
			// **** get equipment listing ****			
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			System.out.println(rovername + " equipment list results " + equipment + "\n");
			
			
			// **** Request START_LOC Location from SwarmServer ****
			out.println("START_LOC");
			line = in.readLine();
            if (line == null) {
            	System.out.println(rovername + " check connection to server");
            	line = "";
            }
			if (line.startsWith("START_LOC")) {
				rovergroupStartPosition = extractLocationFromString(line);
			}
			System.out.println(rovername + " START_LOC " + rovergroupStartPosition);
			
			
			// **** Request TARGET_LOC Location from SwarmServer ****
			out.println("TARGET_LOC");
			line = in.readLine();
            if (line == null) {
            	System.out.println(rovername + " check connection to server");
            	line = "";
            }
			if (line.startsWith("TARGET_LOC")) {
				targetLocation = extractLocationFromString(line);
			}
			System.out.println(rovername + " TARGET_LOC " + targetLocation);

			boolean goingSouth = false;
			boolean stuck = false; // just means it did not change locations between requests,
									// could be velocity limit or obstruction etc.
			//boolean blocked = false;
	
			String[] cardinals = new String[4];
			cardinals[0] = "N";
			cardinals[1] = "E";
			cardinals[2] = "S";
			cardinals[3] = "W";
	
			String currentDir = cardinals[0];

	

			/**
			 *  ####  Rover controller process loop  ####
			 */
			while (true) {
	
				
				// **** Request Rover Location from SwarmServer ****
				out.println("LOC");
				line = in.readLine();
	            if (line == null) {
	            	System.out.println(rovername + " check connection to server");
	            	line = "";
	            }
				if (line.startsWith("LOC")) {
					// loc = line.substring(4);
					currentLoc = extractLocationFromString(line);
				}
				System.out.println(rovername + " currentLoc at start: " + currentLoc);
				
				// after getting location set previous equal current to be able to check for stuckness and blocked later
				previousLoc = currentLoc;		
				
				// ***** do a SCAN *****

				// gets the scanMap from the server based on the Rover current location
				doScan(); 
				// prints the scanMap to the Console output for debug purposes
				scanMap.debugPrintMap();
				

				// ***** get TIMER remaining *****
				out.println("TIMER");
				line = in.readLine();
				System.out.println(line);
	            if (line == null) {
	            	System.out.println(rovername + " check connection to server");
	            	line = "";
	            }
				if (line.startsWith("TIMER")) {
					String timeRemaining = line.substring(6);
					System.out.println(rovername + " timeRemaining: " + timeRemaining);
				}
				
				//getting the target locaiton of crystal if any
				if(crystalCoordinates.size() > 0)
				{
					for(int i = 0 ; i <= crystalCoordinates.size(); i++)
					{
						targetLocationCrystal = crystalCoordinates.get(i);
						crystalCoordinates.remove(i);
								
					}/*
					for(Coord crystalCoord : crystalCoordinates)
					{
						targetLocationCrystal = crystalCoord;
					}*/
				}
				else
					targetLocationCrystal = targetLocation; 
	
				
				// ***** MOVING *****
				// try moving east 5 block if blocked
				//roverMovement(MapTile[][] scanMapTiles, Coord currentLoc);
				MapTile[][] scanMapTiles = scanMap.getScanMap();
				
				//FUNCTION THAT CALL MOVEMENT OF THE ROVER
				if(blocked == true)
				{
					for(int i = 1; i <= 10; i++)
					{
						moveRandomDirection(scanMapTiles);
						
					}
					blocked = false;
				}
				else
					roverMovement(scanMapTiles, currentLoc,targetLocationCrystal);
				
				
				
				// another call for current location
				out.println("LOC");
				line = in.readLine();
				if(line == null){
					System.out.println("ROVER_01 check connection to server");
					line = "";
				}
				if (line.startsWith("LOC")) {
					currentLoc = extractLocationFromString(line);
					
				}
	
	
				// test for stuckness
				stuck = currentLoc.equals(previousLoc);
				 if(stuck==true)
				 {
					 Movement(scanMapTiles, currentLoc);
				 }
	
				//System.out.println("ROVER_01 stuck test " + stuck);
				System.out.println("ROVER_01 blocked test " + blocked);
	
				// logic to calculate where to move next
				
				
                /* ********* Detect and Share Science ***************/
                rocom.detectAndShare(scanMap.getScanMap(), currentLoc, 3);
                /* *************************************************/
				
				// this is the Rovers HeartBeat, it regulates how fast the Rover cycles through the control loop
				Thread.sleep(sleepTime);
				
				System.out.println("ROVER_01 ------------ bottom process control --------------"); 
			}
		
		// This catch block closes the open socket connection to the server
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
	        if (socket != null) {
	            try {
	            	socket.close();
	            } catch (IOException e) {
	            	System.out.println("ROVER_01 problem closing socket");
	            }
	        }
	    }

	} // END of Rover main control loop
	
	// ####################### Support Methods #############################
	
	private void clearReadLineBuffer() throws IOException{
		while(in.ready()){
			//System.out.println("ROVER_01 clearing readLine()");
			in.readLine();	
		}
	}
	
	public void avoid_side_obstacles(){
			// Make connection to SwarmServer and initialize streams
		Socket socket = null;
		try {
			socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			String line = in.readLine();
			if (line.startsWith("LOC")) {
				Coord currentLoc = extractLocationFromString(line);
				//check for obstacles on right, left, bottom and top
			}

		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	
	
	}

// method to retrieve a list of the rover's EQUIPMENT from the server
	@SuppressWarnings("unused")
	private ArrayList<String> getEquipment() throws IOException {
		
		ArrayList<String> returnList = null;		
		
		try{
			//System.out.println("ROVER_01 method getEquipment()");
			Gson gson = new GsonBuilder()
	    			.setPrettyPrinting()
	    			.enableComplexMapKeySerialization()
	    			.create();
			out.println("EQUIPMENT");
			
			String jsonEqListIn = in.readLine(); //grabs the string that was returned first
			if(jsonEqListIn == null){
				jsonEqListIn = "";
			}
			StringBuilder jsonEqList = new StringBuilder();
			//System.out.println("ROVER_01 incomming EQUIPMENT result - first readline: " + jsonEqListIn);
			
			if(jsonEqListIn.startsWith("EQUIPMENT")){
				while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
					if(jsonEqListIn == null){
						break;
					}
					//System.out.println("ROVER_01 incomming EQUIPMENT result: " + jsonEqListIn);
					jsonEqList.append(jsonEqListIn);
					jsonEqList.append("\n");
					//System.out.println("ROVER_01 doScan() bottom of while");
				}
			} else {
				// in case the server call gives unexpected results
				clearReadLineBuffer();
				return null; // server response did not start with "EQUIPMENT"
			}
			
			String jsonEqListString = jsonEqList.toString();		
			returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>(){}.getType());		
			//System.out.println("ROVER_01 returnList " + returnList);
			
		}
		catch(Exception ex){
			System.out.println("Error Message :- ");
			ex.printStackTrace();
		}

		return returnList;

	}
	

	

	// sends a SCAN request to the server and puts the result in the scanMap array
	public void doScan() throws IOException {
		//System.out.println("ROVER_01 method doScan()");
		Gson gson = new GsonBuilder()
    			.setPrettyPrinting()
    			.enableComplexMapKeySerialization()
    			.create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); //grabs the string that was returned first
		if(jsonScanMapIn == null){
			System.out.println("ROVER_01 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println("ROVER_01 incomming SCAN result - first readline: " + jsonScanMapIn);
		
		if(jsonScanMapIn.startsWith("SCAN")){	
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				//System.out.println("ROVER_01 incomming SCAN result: " + jsonScanMapIn);
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
				//System.out.println("ROVER_01 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}
		//System.out.println("ROVER_01 finished scan while");

		String jsonScanMapString = jsonScanMap.toString();
		// debug print json object to a file

		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);		
	}
	
	
	// this takes the server response string, parses out the x and x values and
	// returns a Coord object	
	public static Coord extractLocationFromString(String sStr) {
		int indexOf;
		indexOf = sStr.indexOf(" ");
		sStr = sStr.substring(indexOf +1);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			//System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			//System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}
	

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_01 client = new ROVER_01();//192.168.1.106");
		client.run();
	}
}
