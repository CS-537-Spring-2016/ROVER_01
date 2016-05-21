package common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.Node;

public class AStar implements SearchStrategy {

	static String result = "";
	Node[][] nodes;
	private List<Node> openList;
	private List<Node> closedList;
	private boolean done = false;
	int row = -1, col = -1;
	List<Node> path = new ArrayList<Node>();
	int south = 0, north = 0, west = 0, east = 0;

	@Override
	public List<Edge> search(Graph graph, Node source, Node dist) {
		return null;
	}

	// Initialize the nodes and set the blocked nodes
	private void initEmptyNodes(int row, int col, int[][] blocked, int c) {
		nodes = new Node[row][col];
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				nodes[i][j] = new Node(i, j,0);
			}
		}
		for (int i = 0; i < c; ++i) {
			nodes[blocked[i][0]][blocked[i][1]].setWalkable(false);
			nodes[blocked[i][0]][blocked[i][1]].setWalkable(false);
		}
	}

	// Get the node in openlist with the lowest F value
	private Node lowestFInOpen() {
		// TODO currently, this is done by going through the whole openList!
		Node cheapest = openList.get(0);
		for (int i = 0; i < openList.size(); i++) {
			if (openList.get(i).getF() < cheapest.getF()) {
				cheapest = openList.get(i);
			}
		}
		return cheapest;
	}

	// Returns the path
	private List<Node> calcPath(Node start, Node goal) {
		LinkedList<Node> path = new LinkedList<Node>();
		Node curr = goal;
		boolean done = false;
		while (!done) {
			path.addFirst(curr);
			curr = (Node) curr.getParent();
			if (curr.equals(start)) {
				done = true;
			}
		}
		return path;
	}

	// Returns the list of walkable adjacent nodes
	private List<Node> getAdjacent(Node node) {
		int x = node.getxPosition();
		int y = node.getyPosition();
		List<Node> adj = new LinkedList<Node>();
		Node temp;

		// North
		if (x > 0) {
			temp = this.getNode((x - 1), y);
			if (temp.isWalkable() && !closedList.contains(temp)) {
				adj.add(temp);
			}
		}
		// West
		if (y > 0) {
			temp = this.getNode(x, (y - 1));
			if (temp.isWalkable() && !closedList.contains(temp)) {
				adj.add(temp);
			}
		}
		// East
		if (y < col - 1) {
			temp = this.getNode(x, (y + 1));
			if (temp.isWalkable() && !closedList.contains(temp)) {
				adj.add(temp);
			}
		}
		// South
		if (x < row - 1) {
			temp = this.getNode((x + 1), y);
			if (temp.isWalkable() && !closedList.contains(temp)) {
				adj.add(temp);
			}
		}
		return adj;
	}

	private Node getNode(int x, int y) {
		return nodes[x][y];
	}

	public final String findPath(int oldX, int oldY, int newX, int newY) {
		openList = new LinkedList<Node>();
		closedList = new LinkedList<Node>();
		openList.add(nodes[oldX][oldY]);
		result = "";
		done = false;
		Node current;
		while (!done) {

			// Get the cheapest node in the openlist
			current = lowestFInOpen();

			// add the node to closedlist and remove from openlist
			closedList.add(current);
			openList.remove(current);

			// If the destination is reached
			if ((current.getxPosition() == newX) && (current.getyPosition() == newY)) {

				path = calcPath(nodes[oldX][oldY], current);

				// Generate path from source
				if (0 < path.get(0).getxPosition()) {
					result = result + "S";
					south++;
				} else if (0 > path.get(0).getxPosition()) {
					result = result + "N";
					north++;
				} else if (3 < path.get(0).getyPosition()) {
					result = result + "E";
					east++;
				} else if (3 > path.get(0).getyPosition()) {
					result = result + "W";
					west++;
				}

				for (int i = 0; i < path.size() - 1; i++) {

					if (path.get(i).getxPosition() < path.get(i + 1).getxPosition()) {
						result = result + "S";
						south++;
					} else if (path.get(i).getxPosition() > path.get(i + 1).getxPosition()) {
						result = result + "N";
						north++;
					} else if (path.get(i).getyPosition() < path.get(i + 1).getyPosition()) {
						result = result + "E";
						east++;
					} else if (path.get(i).getyPosition() > path.get(i + 1).getyPosition()) {
						result = result + "W";
						west++;
					}

				}

				return result;

			}

			// The destination is not yet reached

			// Get the list of adjacent walkable nodes
			List<Node> adjacentNodes = getAdjacent(current);

			for (int i = 0; i < adjacentNodes.size(); i++) {

				Node currentAdj = adjacentNodes.get(i);

				// node is not in openList
				if (!openList.contains(currentAdj)) {

					// set current node as previous for this node
					currentAdj.setParent(current);

					// set H value for this node - from this node to destination
					currentAdj.setH(nodes[newX][newY]);
					// set G value - from source to this node
					currentAdj.setG(current);

					// add the node to openlist
					openList.add(currentAdj);
				}

				// node is in the openlist
				else {

					// check if cost from current node is cheaper than previous
					// costs
					if (currentAdj.getG() > currentAdj.calculategCosts(current)) {

						// set current node as parent for the previous node
						currentAdj.setParent(current);

						// set G value
						currentAdj.setG(current);
					}
				}
			}
		}
		return null;
	}

	public String searchFromGridFile(File file) {

		// TODO: read file and generate path using AStar algorithm

		try {

			int[][] blocked = new int[250][250];
			FileReader input = new FileReader(file);
			BufferedReader br = new BufferedReader(input);
			String txt = br.readLine();
			txt = br.readLine();
			col = txt.length() / 2 - 1;
			int si = 0, sj = 0, a = 0, b = 0, c = 0, di = 0, dj = 0;
			do {
				char[] txtary = txt.toCharArray();
				b = 0;
				for (int d = 2; d < txtary.length; d += 2)

				{
					// Source
					if (txtary[d] == '1') {
						si = a;
						sj = b;
					}
					// Destination
					else if (Character.isDigit(txtary[d])) {
						di = a;
						dj = b;
					}
					// Blocked
					if (txtary[d] == '#') {
						blocked[c][0] = a;
						blocked[c][1] = b;
						c++;
					}
					b++;
				}
				a++;

			} while ((txt = br.readLine()) != null && txt.toCharArray()[0] != '+');
			row = a;

			initEmptyNodes(row, col, blocked, c);
			String str = findPath(si, sj, di, dj);
			System.out.println(str);
			return (str);
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	void disp() {
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {

				if (nodes[i][j].isWalkable() == false)
					System.out.print("#  ");
				else
					System.out.print("(" + nodes[i][j].getxPosition() + "," + nodes[i][j].getyPosition() + ")  ");
			}
			System.out.println("\n");
		}

	}

	void dispopen() {
		System.out.println("open");
		for (Node n : openList)
			System.out.print(n.getxPosition() + "," + n.getyPosition() + "\t");
	}

	void dispclosed() {
		System.out.println("Closed");
		for (Node n : closedList) {
			System.out.print(n.getxPosition() + "," + n.getyPosition() + "\t");
		}
	}

	void dispadjacent(List<Node> c) {

		for (Node n : c)
			System.out.print(n.getxPosition() + "," + n.getyPosition() + "\t");
		System.out.println("\n");
	}

	void dispparent() {
		System.out.println("PARENTS");
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < col; j++) {
				System.out.print("P(" + i + "," + j + ") ->");
				// System.out.print("P(" + nodes[i][j].getxPosition() + "," +
				// nodes[i][j].getyPosition() + ") ->");
				if (nodes[i][j].getParent() != null)
					System.out.print("C(" + nodes[i][j].getParent().getxPosition() + ","
							+ nodes[i][j].getParent().getyPosition() + ")  ");
				else
					System.out.print("NULL  ");
			}
			System.out.println("\n");
		}

	}

}
