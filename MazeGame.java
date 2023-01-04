import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/* Instructions to play the game:
 * 
 * Press the 'R' key to reset the maze game and design a new random maze
 * Press the 'B' key to find the path using the Breadth-First Search algorithm
 * Press the 'D' key to find the path using the Depth-First Search algorithm
 * Use the arrow keys to manually traverse through the maze
 * 
 */

class Cell {
  int x;
  int y;
  ArrayList<Edge> edges;

  Cell(int x, int y) {
    this.x = x;
    this.y = y;
    this.edges = new ArrayList<Edge>();
  }

  // draws the cell as a worldimage
  WorldImage drawCell(int cellSize) {
    return this.drawCell(cellSize, Color.LIGHT_GRAY);
  }

  // draws the cell as a worldimage
  WorldImage drawCell(int cellSize, Color c) {
    return new RectangleImage(cellSize - 2, cellSize - 2, OutlineMode.SOLID, c);
  }

  // is the given cell connected to this cell?
  public boolean containsNeighbor(Cell c) {
    for (Edge e : this.edges) {
      if (e.findNode(this).equals(c)) {
        return true;
      }
    }
    return false;
  }
}

// to represent a connection between two cells
class Edge implements Comparable<Edge> {
  Cell node1;
  Cell node2;
  int weight;

  Edge(Cell node1, Cell node2) {
    this.node1 = node1;
    this.node2 = node2;
    this.weight = new Random().nextInt(100);
  }

  // if you pass in a specific node it gives the other node
  Cell findNode(Cell node) {
    if (node == node1) {
      return node2;
    }
    else {
      return node1;
    }
  }

  // comparator method
  public int compareTo(Edge e) {
    return this.weight - e.weight;
  }

  // draw the edge
  public WorldImage drawEdge(int cellSize) {
    return new RectangleImage(cellSize - 2, cellSize - 2, OutlineMode.SOLID, Color.LIGHT_GRAY);
  }
}

// world class OwO
class MazeGame extends World {
  ArrayList<ArrayList<Cell>> board;
  int width;
  int height;
  ArrayList<Edge> edges;
  ArrayList<Cell> searched;
  ArrayList<Cell> playerSearched;
  int searchedCounter;
  ArrayList<Cell> correctPath;
  Cell currentCell;
  boolean solved;

  static int TOTAL_WIDTH = 1000;
  static int TOTAL_HEIGHT = 600;

  MazeGame(int width, int height) {
    this.width = width;
    this.height = height;
    this.board = this.makeBoard();
    this.edges = this.kruskal();
    this.connectEdgesToCells();
    this.searched = new ArrayList<Cell>();
    this.searchedCounter = 0;
    this.correctPath = new ArrayList<Cell>();
    this.currentCell = this.board.get(0).get(0);
    this.solved = false;
    this.playerSearched = new ArrayList<Cell>();
    this.playerSearched.add(this.currentCell);
  }

  // creates a board of cells
  public ArrayList<ArrayList<Cell>> makeBoard() {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    for (int col = 0; col < this.width; col++) {

      ArrayList<Cell> column = new ArrayList<Cell>();
      for (int row = 0; row < this.height; row++) {

        column.add(new Cell(col, row));
      }
      board.add(column);
    }
    return board;
  }

  // make all possible edges and sort them
  public ArrayList<Edge> makeEdges() {
    ArrayList<Edge> totalEdges = new ArrayList<Edge>();
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        if (i < this.width - 1) {
          totalEdges.add(new Edge(this.board.get(i).get(j), this.board.get(i + 1).get(j)));
        }
        if (j < this.height - 1) {
          totalEdges.add(new Edge(this.board.get(i).get(j), this.board.get(i).get(j + 1)));
        }
      }
    }
    Collections.sort(totalEdges);
    return totalEdges;
  }

  // connect edges to cells
  public void connectEdgesToCells() {
    for (Edge e : this.edges) {
      e.node1.edges.add(e);
      e.node2.edges.add(e);
    }
  }

  // make a minimum spanning tree using kruskal's algorithm
  public ArrayList<Edge> kruskal() {
    ArrayList<Edge> totalEdges = this.makeEdges();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    HashMap<Cell, Cell> cellMap = new HashMap<Cell, Cell>();

    for (ArrayList<Cell> column : this.board) {
      for (Cell c : column) {
        cellMap.put(c, c);
      }
    }
    while (edgesInTree.size() < this.width * this.height - 1) {
      Edge e = totalEdges.remove(0);
      // are these two connected already?
      if (this.unionFind(cellMap, e.node1).equals(this.unionFind(cellMap, e.node2))) {
        continue;
      }
      else {
        // they aren't connected so put them in the cell map and now they are connected
        edgesInTree.add(e);
        cellMap.put(this.unionFind(cellMap, e.node1), this.unionFind(cellMap, e.node2));
      }
    }
    return edgesInTree;
  }

  // finds the cell in a hashmap that refers to itself
  public Cell unionFind(HashMap<Cell, Cell> map, Cell key) {
    if (map.get(key).equals(key)) {
      return key;
    }
    else {
      return this.unionFind(map, map.get(key));
    }
  }

  // search method using either DFS or BFS
  void search(boolean isDfs) {
    HashMap<Cell, Cell> cameFromEdge = new HashMap<Cell, Cell>();
    Deque<Cell> worklist = new LinkedList<Cell>();
    ArrayList<Cell> seen = new ArrayList<Cell>();
    worklist.add(this.board.get(0).get(0));

    while (worklist.size() > 0) {
      Cell next = worklist.removeFirst();
      if (seen.contains(next)) {
        continue;
      }
      else if (next.equals(this.board.get(width - 1).get(height - 1))) {
        seen.add(next);
        this.searched = seen;
        this.correctPath = this.reconstruct(cameFromEdge, next);
        this.correctPath.add(this.board.get(0).get(0));
        return;
      }
      else {
        seen.add(next);
        for (Edge e : next.edges) {
          if (isDfs) {
            worklist.addFirst(e.findNode(next));
            if (!seen.contains(e.findNode(next))) {
              cameFromEdge.put(e.findNode(next), next);
            }
          }
          else {
            worklist.add(e.findNode(next));
            if (!seen.contains(e.findNode(next))) {
              cameFromEdge.put(e.findNode(next), next);
            }
          }
        }
      }
    }
  }

  // reconstruct the right path
  public ArrayList<Cell> reconstruct(HashMap<Cell, Cell> cameFromEdge, Cell c) {
    ArrayList<Cell> pathSoFar = new ArrayList<Cell>();
    while (!c.equals(this.board.get(0).get(0))) {
      pathSoFar.add(c);
      c = cameFromEdge.get(c);
    }
    return pathSoFar;
  }

  // updates the player's path
  public void updatePlayerPath(Cell n) {
    if (this.currentCell.containsNeighbor(n)) {
      this.playerSearched.add(n);
      this.currentCell = n;
    }
    if (this.currentCell.equals(this.board.get(this.width - 1).get(this.height - 1))) {
      this.solved = true;
    }
  }

  // display the current game message
  public String gameMessage() {
    if (this.solved) {
      return "The maze has been solved :)";
    }
    else {
      return "The maze has not been solved :(";
    }
  }

  // make scene
  public WorldScene makeScene() {
    WorldScene ws = this.getEmptyScene();
    int cellSize = MazeGame.TOTAL_WIDTH / this.width;
    int offset = cellSize / 2;
    ws.placeImageXY(new RectangleImage(MazeGame.TOTAL_WIDTH, MazeGame.TOTAL_HEIGHT,
        OutlineMode.SOLID, Color.DARK_GRAY), MazeGame.TOTAL_WIDTH / 2, MazeGame.TOTAL_HEIGHT / 2);

    // draw the blank board with just cells, no edges
    for (ArrayList<Cell> column : this.board) {
      for (Cell c : column) {
        ws.placeImageXY(c.drawCell(cellSize), (c.x * cellSize + offset), (c.y * cellSize + offset));
      }
    }

    // draw the edges into the board
    for (Edge e : this.edges) {
      ws.placeImageXY(e.drawEdge(cellSize), (e.node1.x + e.node2.x) * cellSize / 2 + offset,
          (e.node1.y + e.node2.y) * cellSize / 2 + offset);
    }

    // draws every single player searched cell so far
    for (int i = 0; i < this.playerSearched.size(); i++) {
      Cell c = this.playerSearched.get(i);
      ws.placeImageXY(c.drawCell(cellSize, Color.CYAN), (c.x * cellSize + offset),
          (c.y * cellSize + offset));
    }

    // draws every single DFS or BFS searched cell so far
    for (int i = 0; i < this.searchedCounter && i < this.searched.size(); i++) {
      Cell c = this.searched.get(i);
      ws.placeImageXY(c.drawCell(cellSize, Color.CYAN), (c.x * cellSize + offset),
          (c.y * cellSize + offset));
    }
    // draw the current cell that we are on
    ws.placeImageXY(this.currentCell.drawCell(cellSize, Color.BLUE),
        (this.currentCell.x * cellSize + offset), (this.currentCell.y * cellSize + offset));

    // draw the final correct path for when the user uses BFS or DFS and the answer
    // is found
    if (this.searchedCounter == this.searched.size()) {
      for (Cell c : this.correctPath) {
        ws.placeImageXY(c.drawCell(cellSize, Color.BLUE), (c.x * cellSize + offset),
            (c.y * cellSize + offset));
      }
    }
    // the player found the end of the maze so it displays the correctPath
    if (this.solved) {
      for (Cell c : this.correctPath) {
        ws.placeImageXY(c.drawCell(cellSize, Color.BLUE), (c.x * cellSize + offset),
            (c.y * cellSize + offset));
      }
    }
    ws.placeImageXY(new TextImage(this.gameMessage(), 20, Color.BLACK), cellSize * this.width / 2,
        cellSize * this.height + offset);
    return ws;
  }

  // key event handler
  public void onKeyEvent(String s) {
    if (s.equals("d")) {
      this.search(true);
      this.searchedCounter = 0;
    }
    else if (s.equals("b")) {
      this.search(false);
      this.searchedCounter = 0;
    }
    else if (s.equals("r")) {
      this.board = this.makeBoard();
      this.edges = this.kruskal();
      this.connectEdgesToCells();
      this.searched = new ArrayList<Cell>();
      this.searchedCounter = 0;
      this.correctPath = new ArrayList<Cell>();
      this.currentCell = this.board.get(0).get(0);
      this.solved = false;
      this.playerSearched = new ArrayList<Cell>();
      this.playerSearched.add(this.currentCell);
    }
    else if (!solved) {
      if (s.equals("up") && this.currentCell.y > 0) {
        Cell neighbor = this.board.get(this.currentCell.x).get(this.currentCell.y - 1);
        this.updatePlayerPath(neighbor);
      }
      else if (s.equals("down") && this.currentCell.y < this.height - 1) {
        Cell neighbor = this.board.get(this.currentCell.x).get(this.currentCell.y + 1);
        this.updatePlayerPath(neighbor);
      }
      else if (s.equals("left") && this.currentCell.x > 0) {
        Cell neighbor = this.board.get(this.currentCell.x - 1).get(this.currentCell.y);
        this.updatePlayerPath(neighbor);
      }
      else if (s.equals("right") && this.currentCell.x < this.width - 1) {
        Cell neighbor = this.board.get(this.currentCell.x + 1).get(this.currentCell.y);
        this.updatePlayerPath(neighbor);
      }
    }
  }

  // on tick handler
  public void onTick() {
    this.searchedCounter = Math.min(this.searchedCounter + 1, this.searched.size());
    if (this.searchedCounter == this.searched.size() && this.searchedCounter != 0) {
      this.solved = true;
    }
    else if (this.board.get(this.width - 1).get(this.height - 1).equals(this.currentCell)) {
      this.search(true);
      this.searched = new ArrayList<Cell>();
      this.solved = true;
    }
  }
}

// examples class
class ExamplesMaze {
  MazeGame exampleWorld;
  Cell cell1;
  Cell cell2;
  Cell exampleCell;
  Cell exampleCell2;
  Cell exampleCell3;
  Cell exampleCell4;
  Edge exampleEdge;
  Edge exampleEdge2;
  Edge exampleEdge3;
  MazeGame owo;
  MazeGame next;
  HashMap<Cell, Cell> exampleCameFromEdge;

  void initData() {
    this.cell1 = new Cell(4, 5);
    this.cell2 = new Cell(0, 3);
    this.exampleCell = new Cell(0, 0);
    this.exampleCell2 = new Cell(1, 0);
    this.exampleCell3 = new Cell(0, 1);
    this.exampleCell4 = new Cell(1, 1);
    this.exampleEdge = new Edge(this.exampleCell, this.exampleCell2);
    this.exampleEdge2 = new Edge(this.exampleCell2, this.exampleCell);
    this.exampleEdge3 = new Edge(this.exampleCell2, this.exampleCell4);
    this.exampleWorld = new MazeGame(2, 2);
    this.exampleWorld.edges = new ArrayList<Edge>(
        Arrays.asList(this.exampleEdge, this.exampleEdge3));
    this.exampleWorld.board = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(this.exampleCell, this.exampleCell3)),
            new ArrayList<Cell>(Arrays.asList(this.exampleCell2, this.exampleCell4))));
    this.owo = new MazeGame(20, 12);
    this.next = new MazeGame(30, 45);
    this.exampleCameFromEdge = new HashMap<Cell, Cell>();
    this.exampleCameFromEdge.put(this.exampleCell4, this.exampleCell2);
    this.exampleCameFromEdge.put(this.exampleCell2, this.exampleCell);
    this.exampleWorld.connectEdgesToCells();
  }

  // tests for makeBoard
  boolean testMakeBoard(Tester t) {
    initData();
    return t.checkExpect(this.owo.makeBoard().size(), 20)
        && t.checkExpect(this.next.makeBoard().size(), 30);
  }

  // tests for drawCell
  void testDrawCell(Tester t) {
    initData();
    t.checkExpect(this.exampleCell.drawCell(10),
        new RectangleImage(8, 8, OutlineMode.SOLID, Color.LIGHT_GRAY));
    t.checkExpect(this.exampleCell.drawCell(5),
        new RectangleImage(3, 3, OutlineMode.SOLID, Color.LIGHT_GRAY));
    t.checkExpect(this.exampleCell.drawCell(5, Color.red),
        new RectangleImage(3, 3, OutlineMode.SOLID, Color.red));
  }

  // tests for drawEdge
  boolean testDrawEdge(Tester t) {
    initData();
    return t.checkExpect(this.exampleEdge.drawEdge(20),
        new RectangleImage(18, 18, OutlineMode.SOLID, Color.LIGHT_GRAY))
        && t.checkExpect(this.exampleEdge.drawEdge(10),
            new RectangleImage(8, 8, OutlineMode.SOLID, Color.LIGHT_GRAY));
  }

  // tests for makeEdges
  void testMakeEdges(Tester t) {
    initData();
    t.checkExpect(this.owo.makeEdges().size(), 448);
    t.checkExpect(this.next.makeEdges().size(), 2625);
    t.checkExpect(this.owo.makeEdges().get(0).weight < this.owo.makeEdges().get(447).weight, true);
  }

  // tests for compareTo
  boolean testCompareTo(Tester t) {
    initData();
    exampleEdge.weight = 20;
    exampleEdge2.weight = 40;
    return t.checkExpect(exampleEdge.compareTo(exampleEdge2), -20)
        && t.checkExpect(exampleEdge2.compareTo(exampleEdge), 20)
        && t.checkExpect(exampleEdge.compareTo(exampleEdge), 0);
  }

  // test for containsNeighbor
  void testContainsNeighbor(Tester t) {
    initData();
    t.checkExpect(this.exampleCell2.containsNeighbor(this.cell1), false);
    t.checkExpect(this.exampleCell.containsNeighbor(this.cell2), false);
    this.exampleCell.edges.add(new Edge(this.exampleCell, this.cell2));
    this.exampleCell2.edges.add(new Edge(this.exampleCell2, this.cell1));
    t.checkExpect(this.exampleCell2.containsNeighbor(this.cell1), true);
    t.checkExpect(this.exampleCell.containsNeighbor(this.cell2), true);
  }

  // test for findNode
  void testFindNode(Tester t) {
    initData();
    t.checkExpect(this.exampleEdge.findNode(this.exampleCell), this.exampleCell2);
    t.checkExpect(this.exampleEdge.findNode(this.exampleCell2), this.exampleCell);
  }

  // tests for kruskal
  boolean testKruskal(Tester t) {
    initData();
    return t.checkExpect(this.owo.kruskal().size(), 239)
        && t.checkExpect(this.next.kruskal().size(), 1349);
  }

  // test for makeScene
  void testMakeScene(Tester t) {
    initData();
    WorldScene ws = this.exampleWorld.getEmptyScene();
    ws.placeImageXY(new RectangleImage(MazeGame.TOTAL_WIDTH, MazeGame.TOTAL_HEIGHT,
        OutlineMode.SOLID, Color.DARK_GRAY), MazeGame.TOTAL_WIDTH / 2, MazeGame.TOTAL_HEIGHT / 2);
    t.checkExpect(this.exampleWorld.makeScene(), ws);
  }

  // test for unionFind
  void testUnionFind(Tester t) {
    initData();
    HashMap<Cell, Cell> map = new HashMap<Cell, Cell>();
    map.put(this.exampleWorld.board.get(0).get(0), this.exampleWorld.board.get(0).get(0));
    map.put(this.exampleWorld.board.get(1).get(1), this.exampleWorld.board.get(0).get(0));
    t.checkExpect(this.exampleWorld.unionFind(map, this.exampleWorld.board.get(0).get(0)),
        this.exampleWorld.board.get(0).get(0));
    t.checkExpect(this.exampleWorld.unionFind(map, this.exampleWorld.board.get(1).get(1)),
        this.exampleWorld.board.get(0).get(0));
  }

  // test for connectEdgesToCell
  void testConnectEdgesToCell(Tester t) {
    initData();
    this.exampleWorld.edges = new ArrayList<Edge>(Arrays.asList(
        new Edge(this.exampleWorld.board.get(0).get(0), this.exampleWorld.board.get(1).get(0)),
        new Edge(this.exampleWorld.board.get(0).get(0), this.exampleWorld.board.get(0).get(1))));
    for (ArrayList<Cell> column : this.exampleWorld.board) {
      for (Cell c : column) {
        c.edges.clear();
      }
    }
    this.exampleWorld.connectEdgesToCells();
    t.checkExpect(this.exampleWorld.board.get(0).get(0).edges.size(), 2);
    t.checkExpect(this.exampleWorld.board.get(1).get(0).edges.size(), 1);
    t.checkExpect(this.exampleWorld.board.get(0).get(1).edges.size(), 1);
  }

  // test for search using dfs and bfs
  void testSearch(Tester t) {
    initData();
    t.checkExpect(this.owo.searched.size(), 0);
    t.checkExpect(this.owo.correctPath.size(), 0);
    this.owo.search(true);
    t.checkExpect(this.owo.searched.size() > 0, true);
    t.checkExpect(this.owo.correctPath.size() > 0, true);
    initData();
    t.checkExpect(this.owo.searched.size(), 0);
    this.owo.search(false);
    t.checkExpect(this.owo.searched.size() > 0, true);
    t.checkExpect(this.owo.correctPath.size() > 0, true);
  }

  // test for reconstruct
  void testReconstruct(Tester t) {
    initData();
    this.exampleWorld.reconstruct(this.exampleCameFromEdge, this.exampleCell);
    // the correct path for this example world is size 2
    t.checkExpect(this.exampleWorld.reconstruct(this.exampleCameFromEdge, this.exampleCell4).size(),
        2);
  }

  // test for updatePlayerPath
  void testUpdatePlayerPath(Tester t) {
    initData();
    this.exampleWorld.currentCell = this.exampleCell;
    t.checkExpect(this.exampleWorld.playerSearched.size(), 1);
    this.exampleWorld.updatePlayerPath(exampleCell2);
    t.checkExpect(this.exampleWorld.playerSearched.size(), 2);
    t.checkExpect(this.exampleWorld.solved, false);
    initData();
    this.exampleWorld.currentCell = this.exampleCell2;
    this.exampleWorld.updatePlayerPath(this.exampleCell4);
    t.checkExpect(this.exampleWorld.solved, true);
  }

  // test for gameMessage
  void testGameMessage(Tester t) {
    initData();
    t.checkExpect(this.exampleWorld.gameMessage(), "The maze has not been solved :(");
    this.exampleWorld.solved = true;
    t.checkExpect(this.exampleWorld.gameMessage(), "The maze has been solved :)");
  }

  // test for onKeyEvent
  void testOnKeyEvent(Tester t) {
    initData();
    t.checkExpect(this.owo.searched.size(), 0);
    t.checkExpect(this.owo.correctPath.size(), 0);
    this.owo.onKeyEvent("d");
    t.checkExpect(this.owo.searched.size() > 0, true);
    t.checkExpect(this.owo.correctPath.size() > 0, true);

    initData();
    t.checkExpect(this.owo.searched.size(), 0);
    t.checkExpect(this.owo.correctPath.size(), 0);
    this.owo.onKeyEvent("b");
    t.checkExpect(this.owo.searched.size() > 0, true);
    t.checkExpect(this.owo.correctPath.size() > 0, true);

    initData();
    t.checkExpect(this.owo.searched.size(), 0);
    t.checkExpect(this.owo.correctPath.size(), 0);
    t.checkExpect(this.owo.solved, false);
    this.owo.onKeyEvent("r");
    t.checkExpect(this.owo.searched.size(), 0);
    t.checkExpect(this.owo.correctPath.size(), 0);
    t.checkExpect(this.owo.solved, false);

    initData();
    t.checkExpect(this.exampleWorld.playerSearched.size(), 1);
    this.exampleWorld.currentCell = this.exampleCell4;
    this.exampleWorld.onKeyEvent("up");
    t.checkExpect(this.exampleWorld.playerSearched.size(), 2);

    initData();
    t.checkExpect(this.exampleWorld.playerSearched.size(), 1);
    this.exampleWorld.currentCell = this.exampleCell2;
    this.exampleWorld.onKeyEvent("down");
    t.checkExpect(this.exampleWorld.playerSearched.size(), 2);

    initData();
    t.checkExpect(this.exampleWorld.playerSearched.size(), 1);
    this.exampleWorld.currentCell = this.exampleCell2;
    this.exampleWorld.onKeyEvent("left");
    t.checkExpect(this.exampleWorld.playerSearched.size(), 2);

    initData();
    t.checkExpect(this.exampleWorld.playerSearched.size(), 1);
    this.exampleWorld.currentCell = this.exampleCell;
    this.exampleWorld.onKeyEvent("right");
    t.checkExpect(this.exampleWorld.playerSearched.size(), 2);

    initData();
    t.checkExpect(this.owo.searched.size(), 0);
    t.checkExpect(this.owo.correctPath.size(), 0);
    this.owo.onKeyEvent("OWO UWU");
    t.checkExpect(this.owo.searched.size(), 0);
    t.checkExpect(this.owo.correctPath.size(), 0);
  }

  // test for onTick
  void testOnTick(Tester t) {
    initData();
    t.checkExpect(this.exampleWorld.solved, false);
    this.exampleWorld.currentCell = this.exampleCell4;
    t.checkExpect(this.exampleWorld.solved, false);
    this.exampleWorld.onTick();
    t.checkExpect(this.exampleWorld.solved, true);
  }

  // test for BigBang
  void testBigBang(Tester t) {
    int width = 1100;
    int height = 700;

    MazeGame owo = new MazeGame(10, 6);
    owo.bigBang(width, height, .00001);
  }
}
