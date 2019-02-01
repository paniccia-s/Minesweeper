import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.OverlayOffsetImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.StarImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;
import tester.Tester;

//hello there
class ExamplesLEU {

  Tile t1;
  Tile t2;
  Tile t3;
  Tile t4;

  ArrayList<Tile> lot;

  LightEmAll l;

  void testMe(Tester t) {
    /*
     * LightEmAll l = new LightEmAll(2, 2, 27); l.part3Init();
     * l.bigBang(l.width, l.height, 0.01);
     */
  }

  void initData() {
    /*
     * +---+---+ | 1 | 2 | +---+---+ | 3 | 4 | +---+---+
     *
     */

    t1 = new Tile();
    t2 = new Tile();
    t3 = new Tile();
    t4 = new Tile();

    t1.setR(t2);
    t1.setD(t3);

    t2.setD(t4);

    t3.setR(t4);

    lot = new ArrayList<Tile>(Arrays.asList(t1, t2, t3, t4));
  }

  void initLinks() {
    t1.right = true;
    t2.left = true;

    t1.bottom = true;
    t3.top = true;
  }

  // random seed 27 is used
  void init2x2Game() {
    l = new LightEmAll(2, 2, 27);
  }

  void testComparators(Tester t) {
    int one = 1;
    int two = 2;
    IComparator<Integer> intC = new IntComparator();

    t.checkExpect(intC.compare(one, two), -1);
    t.checkExpect(intC.compare(two, one), 1);
    t.checkExpect(intC.compare(one, one), 0);

    Edge<Integer> e1 = new Edge<Integer>(1, 1, 0.4);
    Edge<Integer> e2 = new Edge<Integer>(1, 1, 0.8);
    IComparator<Edge<Integer>> edgeC = new EdgeComparator<Integer>();

    // edge comparator returns which is smaller
    t.checkExpect(edgeC.compare(e1, e2), 1);
    t.checkExpect(edgeC.compare(e2, e1), -1);
    t.checkExpect(edgeC.compare(e1, e1), 0);
  }

  void testRadiusSearchPred(Tester t) {
    IPred<Tile> rsp = new RadiusSearchPred();

    // given anything, return false
    t.checkExpect(rsp.apply(new Tile()), false);
    t.checkExpect(rsp.apply(null), false);
  }

  void testTileGraphSearchManager(Tester t) {
    // testing visited, setVisited, getNeighbors

    // see initData for grid layout
    this.initData();
    GraphSearchManager<Tile> m = new TileGraphSearchManager();

    // All Tiles start unvisited
    for (Tile T : this.lot) {
      t.checkExpect(m.visited(T), false);
    }

    // set a Tile to visited
    m.setVisited(t1, true);
    t.checkExpect(m.visited(t1), true);
    t.checkExpect(t1.cycleVisited, true);
    // reset
    m.setVisited(t1, false);
    t.checkExpect(m.visited(t1), false);
    t.checkExpect(t1.cycleVisited, false);

    // none linked yet: no neighbors
    ArrayList<Tile> neighbors = m.getNeighbors(t1);
    t.checkExpect(neighbors.size(), 0);

    // now link and redo
    this.initLinks();

    neighbors = m.getNeighbors(t1);

    t.checkExpect(neighbors.get(0), t2);
    t.checkExpect(neighbors.get(1), t3);
  }

  void testHeapSorting(Tester t) {
    // generic method: if it works on ints, it works on any T
    // testing on ints to make everything easier

    // new heap in reverse order
    ArrayList<Integer> toHeap = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4));

    GraphUtils<Integer> gu = new GraphUtils<Integer>();
    IComparator<Integer> c = new IntComparator();

    // heapsort the list
    gu.heapSort(toHeap, c);

    // new list should be [4,3,2,1,0]
    for (int i = 0; i < 5; i++) {
      t.checkExpect(toHeap.get(i), 4 - i);
    }

    // reset list and heapify it
    toHeap = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4));
    gu.heapify(toHeap, c, toHeap.size() - 1);

    int top = toHeap.get(0);
    int left = toHeap.get(1);
    int right = toHeap.get(2);
    int downleft = toHeap.get(3);
    int downright = toHeap.get(4);

    // heap invariant checks
    t.checkExpect(top > left, true);
    t.checkExpect(top > right, true);
    t.checkExpect(left > downleft, true);
    t.checkExpect(left > downright, true);

    // turn this heap (not necessarily a sorted list!) to a sorted list
    gu.heapToSortedList(toHeap, c);

    // same checks as with direct call to heapSort()
    for (int i = 0; i < 5; i++) {
      t.checkExpect(toHeap.get(i), 4 - i);
    }
  }

  void testBFS(Tester t) {
    this.initData();

    // first, search for something in an unlinked list
    IPred<Tile> search3 = new TileSearchPred(t3);
    GraphUtils<Tile> gu = new GraphUtils<Tile>();
    GraphSearchManager<Tile> m = new TileGraphSearchManager();

    GraphSearchReturnPackage<Tile> pack = gu.breadthFirstSearch(t1, search3, m);
    t.checkExpect(pack.found, t1);
    t.checkExpect(pack.wasFound, false);

    // now link
    this.initLinks();
    pack = gu.breadthFirstSearch(t1, search3, m);
    t.checkExpect(pack.found, t3);
    t.checkExpect(pack.wasFound, true);

  }

  void testMST(Tester t) {
    this.initData();

    Edge<Tile> e1 = new Edge<Tile>(t1, t2, 3);
    Edge<Tile> e2 = new Edge<Tile>(t1, t3, 2);
    Edge<Tile> e3 = new Edge<Tile>(t2, t4, 1);
    Edge<Tile> e4 = new Edge<Tile>(t3, t4, 4);
    ArrayList<Edge<Tile>> loe = new ArrayList<Edge<Tile>>(Arrays.asList(e1, e2, e3, e4));
    // heaviest edge e4 will be discarded by mst

    GraphUtils<Tile> gu = new GraphUtils<Tile>();
    IComparator<Edge<Tile>> c = new EdgeComparator<Tile>();
    GraphSearchManager<Tile> m = new TileGraphSearchManager();

    loe = gu.mst(4, loe, c, m);

    // e4 is removed and the rest are sorted by weight (lightest first)
    t.checkExpect(loe.size(), 3);
    t.checkExpect(loe.get(0), e3);
    t.checkExpect(loe.get(1), e2);
    t.checkExpect(loe.get(2), e1);
  }

  void testNoCycle(Tester t) {
    this.initData();

    Edge<Tile> e1 = new Edge<Tile>(t1, t2, 3);
    Edge<Tile> e2 = new Edge<Tile>(t1, t3, 2);
    Edge<Tile> e3 = new Edge<Tile>(t2, t4, 1);
    Edge<Tile> e4 = new Edge<Tile>(t3, t4, 4);
    ArrayList<Edge<Tile>> mst = new ArrayList<Edge<Tile>>();

    GraphUtils<Tile> gu = new GraphUtils<Tile>();
    GraphSearchManager<Tile> m = new TileGraphSearchManager();

    // empty list within mst, noCycle is true
    t.checkExpect(gu.noCycle(e1, mst, m), true);

    // add to mst, continue with next edge
    mst.add(e1);
    t.checkExpect(gu.noCycle(e2, mst, m), true);

    mst.add(e2);
    t.checkExpect(gu.noCycle(e3, mst, m), true);

    // the fourth edge will introduce a cycle
    mst.add(e3);
    t.checkExpect(gu.noCycle(e4, mst, m), false);
  }

  void testNoCycleHelp(Tester t) {
    this.initData();

    Edge<Tile> e1 = new Edge<Tile>(t1, t2, 3);
    Edge<Tile> e2 = new Edge<Tile>(t1, t3, 2);
    Edge<Tile> e3 = new Edge<Tile>(t2, t4, 1);
    ArrayList<Edge<Tile>> mst = new ArrayList<Edge<Tile>>();

    GraphUtils<Tile> gu = new GraphUtils<Tile>();
    GraphSearchManager<Tile> m = new TileGraphSearchManager();

    // empty list within mst, noCycle is true
    // for each call, use the two Tiles within the edge in question
    // test e1
    t.checkExpect(gu.noCycleHelp(mst, t1, t2, m), true);

    // add to mst, continue with next edge
    mst.add(e1);
    // test e2
    t.checkExpect(gu.noCycleHelp(mst, t1, t3, m), true);

    mst.add(e2);
    // test e3
    t.checkExpect(gu.noCycleHelp(mst, t2, t4, m), true);

    // the fourth edge will introduce a cycle
    mst.add(e3);
    t.checkExpect(gu.noCycleHelp(mst, t3, t4, m), false);
  }

  void testInitEdges(Tester t) {
    LightEmAll l = new LightEmAll(2, 2);
    l.initEdgeList();

    Tile ul = l.board.get(0);
    Tile ur = l.board.get(1);
    Tile dl = l.board.get(2);
    Tile dr = l.board.get(3);

    Edge<Tile> ulToUr = l.edges.get(0);
    Edge<Tile> ulToDl = l.edges.get(1);
    Edge<Tile> urToDr = l.edges.get(2);
    Edge<Tile> dlToDr = l.edges.get(3);

    t.checkExpect(ulToUr.fromNode, ul);
    t.checkExpect(ulToUr.toNode, ur);

    t.checkExpect(ulToDl.fromNode, ul);
    t.checkExpect(ulToDl.toNode, dl);

    t.checkExpect(urToDr.fromNode, ur);
    t.checkExpect(urToDr.toNode, dr);

    t.checkExpect(dlToDr.fromNode, dl);
    t.checkExpect(dlToDr.toNode, dr);
  }

  void testInitTiles(Tester t) {
    this.init2x2Game();

    ArrayList<Tile> tileList = new ArrayList<Tile>();

    // fill the list with new Tiles
    for (int i = 0; i < l.numTiles; i++) {
      tileList.add(new Tile());
    }

    // For each Tile...
    for (int i = 0; i < l.numTiles; i++) {
      // Get it
      Tile toAssign = tileList.get(i);

      // calculate the indices of the cells to its right and bottom
      int indexD = i + l.cols;
      int indexR = i + 1;

      // Only assign the right piece if toAssign is not on the rightmost
      // side of
      // the board
      // in this case, the right reference should be null
      if ((i + 1) % l.cols != 0) {
        Tile r = tileList.get(indexR);
        toAssign.setR(r);
      }
      // Only assign the below piece if toAssign is not on the bottom row
      // of the
      // board
      // in this case, the bottom reference should be null
      if (indexD < l.numTiles) {
        Tile d = tileList.get(indexD);
        toAssign.setD(d);
      }
    }
    t.checkExpect(l.initTiles(), tileList);

  }

  void testPart3Init(Tester t) {
    this.init2x2Game();
    l.initEdgeList();

    // In part3Init():
    // 1 initEdgeList
    // 2 toMST()
    // 3 connectTile()
    // 4 determineRadius()
    // 5 movePowerStation()
    // 6 rotateRandomly()

    // 4 sneak-peak:
    // unlinked board: radius should be 0/2 + 1 = 1
    l.determineRadius();
    t.checkExpect(l.radius, 1);

    // 1:
    Edge<Tile> e1 = l.edges.get(0);
    Edge<Tile> e2 = l.edges.get(1);
    Edge<Tile> e3 = l.edges.get(2);
    Edge<Tile> e4 = l.edges.get(3);

    Tile t1 = l.board.get(0);
    Tile t2 = l.board.get(1);
    Tile t3 = l.board.get(2);
    Tile t4 = l.board.get(3);
    // each Tile should be linked to those around it
    t.checkExpect(l.edges.size(), 4);

    // Random, same seed
    Random r = new Random(27);
    double weight1 = r.nextDouble();
    double weight2 = r.nextDouble();
    double weight3 = r.nextDouble();
    double weight4 = r.nextDouble();

    t.checkExpect(e1, new Edge<Tile>(t1, t2, weight1));
    t.checkExpect(e2, new Edge<Tile>(t1, t3, weight2));
    t.checkExpect(e3, new Edge<Tile>(t2, t4, weight3));
    t.checkExpect(e4, new Edge<Tile>(t3, t4, weight4));

    // 2:
    l.toMST();

    // heaviest edge is out
    t.checkExpect(l.edges.size(), 3);

    e1 = l.edges.get(0);
    e2 = l.edges.get(1);
    e3 = l.edges.get(2);

    t.checkExpect(e1, new Edge<Tile>(t2, t4, weight3));
    t.checkExpect(e2, new Edge<Tile>(t3, t4, weight4));
    t.checkExpect(e3, new Edge<Tile>(t1, t3, weight2));

    // 3:
    // connect each Edge
    for (Edge<Tile> e : l.edges) {
      l.connectTiles(e.fromNode, e.toNode);
    }

    /*
     * +---+---+ | | | | | +-|-+-|-+ | +---+ | +---+---+
     * 
     * if you can interpret that, this is how it's all connected (a U)
     */

    t.checkExpect(t1.bottom, true);
    t.checkExpect(t2.bottom, true);
    t.checkExpect(t3.top, true);
    t.checkExpect(t3.right, true);
    t.checkExpect(t4.left, true);
    t.checkExpect(t4.top, true);

    // everything else should be false

    // 4:
    l.determineRadius();

    // on a 2x2 board, the diameter should be 4, so radius should be 3
    t.checkExpect(l.radius, 3);

    // 5:
    l.movePowerStation(0);
    t.checkExpect(l.board.get(0).hasPowerStation, true);

    // 6:

    ArrayList<Tile> rot = new ArrayList<Tile>(
        Arrays.asList(new Tile(), new Tile(), new Tile(), new Tile()));

    ArrayList<Integer> toRotateTiles = new ArrayList<Integer>();

    for (int i = 0; i < l.board.size(); i++) {
      int rand = r.nextInt(4);
      toRotateTiles.add(rand);
    }

    for (int i = 0; i < rot.size(); i++) {
      Tile T = rot.get(i);
      Tile from = l.board.get(i);

      T.top = from.top;
      T.right = from.right;
      T.bottom = from.bottom;
      T.left = from.left;

      int rotate = toRotateTiles.get(i);
      for (int j = 0; j < rotate; j++) {
        T.leftClick();
      }
    }

    l.rotateRandomly();

    for (int i = 0; i < rot.size(); i++) {
      Tile rotTile = rot.get(i);
      Tile fromBoard = l.board.get(i);
      t.checkExpect(rotTile.top, fromBoard.top);
      t.checkExpect(rotTile.right, fromBoard.right);
      t.checkExpect(rotTile.bottom, fromBoard.bottom);
      t.checkExpect(rotTile.left, fromBoard.left);
    }

  }

  void testRecalcPower(Tester t) {
    this.init2x2Game();
    l.part3Init();

    // change one tile, then recalc
    l.board.get(0).isLit = false;
    l.recalcPower();

    t.checkExpect(l.board.get(0).isLit, true);
  }

  void testCheckGameWon(Tester t) {
    this.init2x2Game();
    l.part3Init();

    t.checkExpect(l.checkGameWon(), false);

    for (Tile T : l.board) {
      T.isLit = true;
    }

    t.checkExpect(l.checkGameWon(), true);
  }

  void testSetNeighbor(Tester t) {
    Tile t1 = new Tile();
    Tile t2 = new Tile();

    t.checkExpect(t1.d, null);
    t.checkExpect(t2.u, null);

    t1.setD(t2);
    t.checkExpect(t1.d, t2);
    t.checkExpect(t2.u, t1);

    t.checkExpect(t1.r, null);
    t.checkExpect(t2.l, null);

    t1.setR(t2);
    t.checkExpect(t1.r, t2);
    t.checkExpect(t2.l, t1);
  }

  void testConnected(Tester t) {
    Tile t1 = new Tile();
    Tile t2 = new Tile();

    t.checkExpect(t1.connectedAtTop(), false);
    t.checkExpect(t1.connectedAtRight(), false);
    t.checkExpect(t1.connectedAtBottom(), false);
    t.checkExpect(t1.connectedAtLeft(), false);

    t1.setD(t2);
    t1.setR(t2);
    t1.bottom = true;
    t1.right = true;
    t2.left = true;
    t2.top = true;

    t.checkExpect(t1.connectedAtTop(), false);
    t.checkExpect(t1.connectedAtRight(), true);
    t.checkExpect(t1.connectedAtBottom(), true);
    t.checkExpect(t1.connectedAtLeft(), false);

  }

  void testGetNeighbors(Tester t) {
    Tile t1 = new Tile();
    Tile t2 = new Tile();
    Tile t3 = new Tile();

    t.checkExpect(t1.getNeighbors(), new ArrayList<Tile>());
    t1.setR(t2);
    t1.setD(t3);
    t1.bottom = true;
    t1.right = true;
    t2.left = true;
    t3.top = true;

    ArrayList<Tile> n = new ArrayList<Tile>(Arrays.asList(t2, t3));
    t.checkExpect(t1.getNeighbors(), n);
  }

  void testSpreadPowerState(Tester t) {
    Tile t1 = new Tile();
    Tile t2 = new Tile();
    Tile t3 = new Tile();

    t1.setR(t2);
    t2.setR(t3);
    t1.right = true;
    t2.left = true;
    t2.right = true;
    t3.left = true;

    t1.spreadPowerState(0);
    t.checkExpect(t2.isLit, false);
    t1.spreadPowerState(1);
    t.checkExpect(t2.isLit, false);
    t1.spreadPowerState(2);
    t.checkExpect(t2.isLit, true);
    t.checkExpect(t3.isLit, false);
    t1.spreadPowerState(3);
    t.checkExpect(t3.isLit, true);
  }

  void testTileClicks(Tester t) {
    Tile T = new Tile();
    T.bottom = true;
    T.leftClick();
    t.checkExpect(T.right, true);
    t.checkExpect(T.bottom, false);
    T.rightClick();
    t.checkExpect(T.right, false);
    t.checkExpect(T.bottom, true);
  }

  void testDrawATile(Tester t) {
    Tile t1 = new Tile();
    WorldImage im = t1.drawTile();
    WorldImage check = new RectangleImage(t1.pieceSize, t1.pieceSize, OutlineMode.SOLID,
        t1.cBackground);
    WorldImage outline = new RectangleImage(t1.pieceSize + 3, t1.pieceSize + 3, OutlineMode.SOLID,
        Color.BLACK);
    check = new OverlayImage(check, outline);
    t.checkExpect(im, check);

    // draw wire
    t1.bottom = true;
    t1.left = true;
    t1.top = true;
    t1.right = true;
    im = t1.drawTile();

    WorldImage ver = new RectangleImage(4, (t1.pieceSize / 2) + 2, OutlineMode.SOLID, t1.cWireDim);
    WorldImage hor = new RectangleImage((t1.pieceSize / 2) + 2, 5, OutlineMode.SOLID, t1.cWireDim);

    check = new OverlayOffsetImage(ver, 0, (t1.pieceSize / 4), check);
    check = new OverlayOffsetImage(hor, -(t1.pieceSize / 4), 0, check);
    check = new OverlayOffsetImage(ver, 0, -(t1.pieceSize / 4), check);
    check = new OverlayOffsetImage(hor, (t1.pieceSize / 4), 0, check);
    t.checkExpect(im, check);

    t1.hasPowerStation = true;
    im = t1.drawTile();

    WorldImage stationStar = new StarImage(20, 7, OutlineMode.SOLID, Color.CYAN);
    WorldImage stationOutline = new StarImage(20, 7, OutlineMode.OUTLINE, Color.ORANGE);

    check = new OverlayImage(stationOutline, new OverlayImage(stationStar, check));
    t.checkExpect(im, check);
  }

  void testOnMouseClick(Tester t) {
    this.init2x2Game();

    // test left click
    boolean beforeClickTop = l.board.get(0).top;
    l.onMouseClicked(new Posn(1, 1), "left");
    t.checkExpect(beforeClickTop, l.board.get(0).right);

    // test right click
    l.onMouseClicked(new Posn(1, 1), "right");
    t.checkExpect(beforeClickTop, l.board.get(0).top);
  }

  void testMakeScene(Tester t) {
    this.init2x2Game();
    WorldScene im = l.makeScene();

    WorldScene check = new WorldScene(l.width, l.height);
    ArrayList<WorldImage> tiles = new ArrayList<WorldImage>();
    for (int i = 0; i < l.numTiles; i++) {
      tiles.add(l.board.get(i).drawTile());
    }
    check.placeImageXY(tiles.get(0), 30, 30);
    check.placeImageXY(tiles.get(1), 90, 30);
    check.placeImageXY(tiles.get(2), 30, 90);
    check.placeImageXY(tiles.get(3), 90, 90);

    t.checkExpect(im, check);

    im = l.lastScene("You Win");

    WorldImage text = new TextImage("You Win", 30, Color.RED);
    check.placeImageXY(text, l.width / 2, l.height / 2);

    t.checkExpect(im, check);

    // all other drawing is already tested in other methods
  }

  void testDetermineTileFromClick(Tester t) {
    this.init2x2Game();
    // Clicking at (0,0) is cell 0 for a 3x3
    int zero = l.determineTileFromClick(new Posn(0, 0));
    int one = l.determineTileFromClick(new Posn(l.pieceSize, 0));
    int two = l.determineTileFromClick(new Posn(0, l.pieceSize));
    int three = l.determineTileFromClick(new Posn(l.pieceSize, l.pieceSize));

    t.checkExpect(zero, 0);
    t.checkExpect(one, 1);
    t.checkExpect(two, 2);
    t.checkExpect(three, 3);
  }

  void testOnKeyEvent(Tester t) {
    this.init2x2Game();
    l.part3Init();
    // right click 0 and 2 to make movable
    l.onRightClick(new Posn(1, 1));
    l.onRightClick(new Posn(1, 70));

    // garbage key
    int stationPre = l.stationIndex;
    l.onKeyEvent("g");
    // no change
    t.checkExpect(stationPre, l.stationIndex);

    // up, right, left do nothing
    l.onKeyEvent("left");
    t.checkExpect(stationPre, l.stationIndex);
    l.onKeyEvent("up");
    t.checkExpect(stationPre, l.stationIndex);
    l.onKeyEvent("right");
    t.checkExpect(stationPre, l.stationIndex);

    // down should move it one down
    l.onKeyEvent("down");
    t.checkExpect(2, l.stationIndex);
    t.checkExpect(l.board.get(0).hasPowerStation, false);
    t.checkExpect(l.board.get(2).hasPowerStation, true);
  }

}

// The game board and all of its required information
class LightEmAll extends World {
  // A list of Tiles
  ArrayList<Tile> board;

  // A list of Nodes
  ArrayList<Tile> nodes;

  // A list of Edges
  ArrayList<Edge<Tile>> edges;

  // The width of the board, in pixels
  int width;
  // The height of the board, in pixels
  int height;

  // The number of rows and columns of the board
  int rows;
  int cols;
  // The number of total Tiles on the board
  int numTiles;

  // The size of each Tile, in pixels
  int pieceSize = 60;

  // the current location of the power station,
  // as well as its effective radius
  int stationIndex;
  int radius;

  // The random seed with which to generate the board, tile rotation, etc
  int seed;

  // The Random to use (given seed as its seed)
  Random rand;

  // Specify a seed to use: used to test
  LightEmAll(int r, int c, int seed) {
    this.cols = r;
    this.rows = c;
    this.numTiles = rows * cols;

    this.width = this.cols * this.pieceSize;
    this.height = this.rows * this.pieceSize;

    this.board = this.initTiles();

    this.seed = seed;
    this.rand = new Random(seed);
  }

  // random seed
  LightEmAll(int r, int c) {
    this(r, c, 0);

    this.rand = new Random();
  }

  // convenience constructor
  LightEmAll() {
    this(8, 8);
  }

  // To breadth-first search the graph twice to determine the proper radius of
  // play
  // EFFECT: sets the game radius to twice the longest distance between Tiles
  void determineRadius() {
    // search first from any Tile (first is used here) and remember the
    // last-seen Tile
    GraphSearchReturnPackage<Tile> bfs1 = new GraphUtils<Tile>().breadthFirstSearch(
        this.board.get(0), new RadiusSearchPred(), new TileGraphSearchManager());
    Tile farthestFromStart = bfs1.found;

    // search again from that last-seen Tile
    GraphSearchReturnPackage<Tile> bfs2 = new GraphUtils<Tile>().breadthFirstSearch(
        farthestFromStart, new RadiusSearchPred(), new TileGraphSearchManager());
    // these method calls are not pretty

    // radius is diameter/2 + 1
    int diameter = bfs2.depthAt;
    this.radius = diameter / 2 + 1;

  }

  // To initialize the board, linking neighboring Tiles appropriately
  ArrayList<Tile> initTiles() {
    ArrayList<Tile> tileList = new ArrayList<Tile>();

    // fill the list with new Tiles
    for (int i = 0; i < numTiles; i++) {
      tileList.add(new Tile());
    }

    // For each Tile...
    for (int i = 0; i < numTiles; i++) {
      // Get it
      Tile toAssign = tileList.get(i);

      // calculate the indices of the cells to its right and bottom
      int indexD = i + cols;
      int indexR = i + 1;

      // Only assign the right piece if toAssign is not on the rightmost side of
      // the board
      // in this case, the right reference should be null
      if ((i + 1) % cols != 0) {
        Tile r = tileList.get(indexR);
        toAssign.setR(r);
      }
      // Only assign the below piece if toAssign is not on the bottom row of the
      // board
      // in this case, the bottom reference should be null
      if (indexD < numTiles) {
        Tile d = tileList.get(indexD);
        toAssign.setD(d);
      }
    }

    // return when done
    return tileList;
  }

  // To randomly generate the board through Kruskal's Minimal-Spanning Tree
  // algorithm
  // EFFECT: generates a randomly-weighted set of edges between neighboring
  // Tiles,
  // creates a minimal spanning tree from that graph, and visually connects each
  // edge
  void part3Init() {
    // establish Edges between neighboring Tiles
    this.initEdgeList();
    // make a minimal spanning tree of the edge network
    this.toMST();

    // connect each Edge
    for (Edge<Tile> e : this.edges) {
      this.connectTiles(e.fromNode, e.toNode);
    }

    // determine the radius afterwards
    this.determineRadius();
    this.rotateRandomly();
    this.stationIndex = 0;
    this.movePowerStation(0);

  }

  // To visually connect two connecting Tiles
  // EFFECT: toggles the Tiles' appropriate boolean directional values based
  // on where they are connected
  void connectTiles(Tile one, Tile two) {
    int indexOne = this.board.indexOf(one);
    int indexTwo = this.board.indexOf(two);

    // four possibilities: left, right, up, down
    // indices must be next to each other

    if (indexOne - 1 == indexTwo) {
      // one is right of two
      one.left = true;
      two.right = true;
    }
    else if (indexTwo - 1 == indexOne) {
      // one is left of two
      one.right = true;
      two.left = true;
    }
    else if (indexOne + this.cols == indexTwo) {
      // one is above two
      one.bottom = true;
      two.top = true;
    }
    else {
      // one is below two
      one.top = true;
      two.bottom = true;
    }
  }

  // To initialize a list of Edges between neighboring Tiles
  // EFFECT: links each Tile to those around it and gives each Edge a random
  // weight
  // between 0 and 1
  void initEdgeList() {
    this.edges = new ArrayList<Edge<Tile>>();

    for (Tile t : this.board) {
      // for each tile: make a new Edge of the Cells to its right and bottom
      Tile toLink;
      double rand;
      // first check right
      if (t.r != null) {
        toLink = t.r;
        rand = this.rand.nextDouble();
        Edge<Tile> newEdge = new Edge<Tile>(t, toLink, rand);
        edges.add(newEdge);
      }

      if (t.d != null) {
        toLink = t.d;
        rand = this.rand.nextDouble();
        Edge<Tile> newEdge = new Edge<Tile>(t, toLink, rand);
        edges.add(newEdge);
      }
    }
  }

  // To convert the Edge network into a minimal spanning tree
  // EFFECT: removes any unnecessary Edges from the list of Edges
  void toMST() {
    this.edges = new GraphUtils<Tile>().mst(this.numTiles, this.edges, new EdgeComparator<Tile>(),
        new TileGraphSearchManager());
  }

  // To rotate each Tile randomly by a number of clicks [0,3]
  // EFFECT: rotates each Tile on the board no more than three times
  void rotateRandomly() {
    for (Tile t : this.board) {
      int rand = this.rand.nextInt(4);
      for (int i = 0; i < rand; i++) {
        t.leftClick();
      }
    }
  }

  // To move the power station to the given index
  // EFFECT: sets hasPowerStation of the old station Tile to false,
  // sets hasPowerStation of the new station Tile to true,
  // and recalculates the power
  void movePowerStation(int index) {
    Tile oldStation = board.get(stationIndex);
    oldStation.hasPowerStation = false;

    this.stationIndex = index;
    Tile newStation = board.get(stationIndex);
    newStation.hasPowerStation = true;

    this.recalcPower();
  }

  // To recalculate which Tiles have power
  // EFFECT: wipes the board of power, sets the station Tile
  // to powered, recursively spread power from the station Tile
  // Note: The power station tile does NOT count toward the radius of reach
  void recalcPower() {
    Tile station = board.get(this.stationIndex);

    for (Tile t : board) {
      t.isLit = false;
    }

    station.isLit = true;
    station.spreadPowerState(this.radius);
  }

  // To render the World
  @Override
  public WorldScene makeScene() {
    WorldScene w = new WorldScene(this.width, this.height);
    this.drawTiles(w);
    return w;
  }

  // To render each Tile where it belongs
  public void drawTiles(WorldScene w) {
    for (int i = 0; i < this.numTiles; i++) {
      Tile toDraw = this.board.get(i);
      // determine the x and y to draw
      // offset by size/2 because placeXY puts
      // images by their centers
      int xPos = ((i % cols) * pieceSize) + (this.pieceSize / 2);
      int yPos = ((i / cols) * pieceSize) + (this.pieceSize / 2);

      WorldImage pieceImage = toDraw.drawTile();
      w.placeImageXY(pieceImage, xPos, yPos);
    }
  }

  // To properly respond to a mouse click
  // EFFECT: rotates the appropriate Tile if one is clicked
  @Override
  public void onMouseClicked(Posn p, String buttonName) {
    if (p.x > this.width || p.y > this.height) {
      return;
    }
    if (buttonName.equals("LeftButton")) {
      this.onLeftClick(p);
    }
    else if (buttonName.equals("RightButton")) {
      this.onRightClick(p);
    }

    if (this.checkGameWon()) {
      this.endOfWorld("You Win");
    }
  }

  // To determine if the game is won
  // EFFECT: if every Tile on the board is lit, call endOfWorld with "You win"
  boolean checkGameWon() {
    // if the game is over, all tiles are lit
    for (Tile t : this.board) {
      if (!t.isLit) {
        return false;
      }
    }

    return true;
  }

  // To render the victory scene
  @Override
  public WorldScene lastScene(String msg) {
    WorldScene ws = this.makeScene();

    WorldImage temp = new TextImage(msg, 30, Color.RED);
    ws.placeImageXY(temp, this.width / 2, this.height / 2);
    return ws;
  }

  // To respond to a left click.
  // EFFECT: rotates the clicked Tile counterclockwise
  void onLeftClick(Posn p) {
    int pieceIndex = this.determineTileFromClick(p);
    Tile clicked = this.board.get(pieceIndex);
    clicked.leftClick();
    this.recalcPower();
  }

  // To respond to a left click.
  // EFFECT: rotates the clicked Tile clockwise
  void onRightClick(Posn p) {
    int pieceIndex = this.determineTileFromClick(p);
    Tile clicked = this.board.get(pieceIndex);
    clicked.rightClick();
    this.recalcPower();
  }

  // To determine which Tile was clicked, given the coordinates of the click
  int determineTileFromClick(Posn click) {
    int row = click.x / this.pieceSize;
    // col number is posn's y / cellSize
    int col = click.y / this.pieceSize;

    // cell index number is: (the column of the click) * (the number in each
    // row)
    // + (the row of the click)
    // aka (the column) * (how many rows before this mine) + (the row)
    int pieceNum = (col * this.cols) + row;
    return pieceNum;
  }

  // To react to a pressed key.
  // EFFECT: moves the station in the correct direction, if appropriate to do so
  public void onKeyEvent(String key) {
    Tile station = this.board.get(stationIndex);
    // move only if there is a valid connection in the appropriate direction
    if (key.equals("up") && station.connectedAtTop()) {
      this.movePowerStation(stationIndex - this.cols);
    }
    else if (key.equals("right") && station.connectedAtRight()) {
      this.movePowerStation(stationIndex + 1);
    }
    else if (key.equals("down") && station.connectedAtBottom()) {
      this.movePowerStation(stationIndex + this.cols);
    }
    else if (key.equals("left") && station.connectedAtLeft()) {
      this.movePowerStation(stationIndex - 1);
    }

    if (this.checkGameWon()) {
      this.endOfWorld("You win");
    }
  }

}

// Represents a weighted edge between two Ts
class Edge<T> {
  T fromNode;
  T toNode;
  double weight;

  public Edge(T fromNode, T toNode, double weight) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
  }

}

// Represents a Tile on the board
class Tile {

  // its neighbors
  // - (u = up, l = left, d = down, r = right)
  Tile u;
  Tile l;
  Tile d;
  Tile r;

  // The size of this square Tile, in pixels
  int pieceSize = 60;

  // The colors of the Tile, including background and lit/dim wires
  Color cBackground = new Color(0x404040);
  Color cWireLit = new Color(0xf8f200);
  Color cWireDim = new Color(0x808080);

  // whether this Tile is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;

  // whether the power station is on this piece
  boolean hasPowerStation;

  // whether this Tile is lit
  boolean isLit;

  // whether this Tile has been visited by a cyclic method
  boolean cycleVisited;

  // To set the bottom neighbor of this Tile
  // EFFECT: links the given Tile to this Tile's bottom
  void setD(Tile d) {
    this.d = d;
    d.u = this;
  }

  // To set the right neighbor of this Tile
  // EFFECT: links the given Tile to this Tile's right
  void setR(Tile r) {
    this.r = r;
    r.l = this;
  }

  Tile() {
    // this.randomBranches();
  }

  // To determine if this Tile and the Tile to its top are connected
  boolean connectedAtTop() {
    return this.top && this.u != null && this.u.bottom;
  }

  // To determine if this Tile and the Tile to its right are connected
  boolean connectedAtRight() {
    return this.right && this.r != null && this.r.left;
  }

  // To determine if this Tile and the Tile to its bottom are connected
  boolean connectedAtBottom() {
    return this.bottom && this.d != null && this.d.top;
  }

  // To determine if this Tile and the Tile to its left are connected
  boolean connectedAtLeft() {
    return this.left && this.l != null && this.l.right;
  }

  // To get a list of this Tile's neighboring Tiles
  ArrayList<Tile> getNeighbors() {
    // four possible neighbors
    ArrayList<Tile> neighbors = new ArrayList<Tile>();
    // check top
    if (connectedAtTop()) {
      neighbors.add(this.u);
    }
    // check right
    if (connectedAtRight()) {
      neighbors.add(this.r);
    }
    // check bottom
    if (connectedAtBottom()) {
      neighbors.add(this.d);
    }
    // check left
    if (connectedAtLeft()) {
      neighbors.add(this.l);
    }
    return neighbors;
  }

  // To spread power from this Tile to all connected Tiles
  // EFFECT: Sets isLit for every connected Tile to true
  void spreadPowerState(int rad) {
    this.cycleVisited = true;
    rad--;
    if (rad > 0) {
      if (connectedAtTop() && !this.u.cycleVisited) {
        this.u.isLit = true;
        this.u.spreadPowerState(rad);
        this.u.cycleVisited = false;
      }
      if (connectedAtRight() && !this.r.cycleVisited) {
        this.r.isLit = true;
        this.r.spreadPowerState(rad);
        this.r.cycleVisited = false;
      }
      if (connectedAtBottom() && !this.d.cycleVisited) {
        this.d.isLit = true;
        this.d.spreadPowerState(rad);
        this.d.cycleVisited = false;
      }
      if (connectedAtLeft() && !this.l.cycleVisited) {
        this.l.isLit = true;
        this.l.spreadPowerState(rad);
        this.l.cycleVisited = false;
      }
    }

    this.cycleVisited = false;
  }

  // To handle a left click on this Tile
  // EFFECT: rotates each connection of this Tile counterclockwise
  // To implement a left click
  // EFFECT: toggles each of the four branch states based on the one on
  // its counterclockwise side
  void leftClick() {
    boolean temp = this.top;
    this.top = this.right;
    this.right = this.bottom;
    this.bottom = this.left;
    this.left = temp;
  }

  // To handle a right click on this Tile
  // EFFECT: rotates each connection of this Tile clockwise
  void rightClick() {
    boolean temp = this.top;
    this.top = this.left;
    this.left = this.bottom;
    this.bottom = this.right;
    this.right = temp;
  }

  // To render this Tile
  WorldImage drawTile() {
    WorldImage tile = new RectangleImage(this.pieceSize, this.pieceSize, OutlineMode.SOLID,
        this.cBackground);
    WorldImage outline = new RectangleImage(this.pieceSize + 3, this.pieceSize + 3,
        OutlineMode.SOLID, Color.BLACK);

    tile = new OverlayImage(tile, outline);
    tile = this.drawWires(tile);

    if (this.hasPowerStation) {
      tile = this.drawStation(tile);
    }

    return tile;
  }

  // To render the wires on this Tile
  WorldImage drawWires(WorldImage tile) {
    Color wireColor;

    if (this.isLit) {
      wireColor = this.cWireLit;
    }
    else {
      wireColor = this.cWireDim;
    }

    WorldImage ver = new RectangleImage(4, (this.pieceSize / 2) + 2, OutlineMode.SOLID, wireColor);
    WorldImage hor = new RectangleImage((this.pieceSize / 2) + 2, 5, OutlineMode.SOLID, wireColor);

    if (this.top) {
      tile = new OverlayOffsetImage(ver, 0, (this.pieceSize / 4), tile);
    }
    if (this.right) {
      tile = new OverlayOffsetImage(hor, -(this.pieceSize / 4), 0, tile);
    }
    if (this.bottom) {
      tile = new OverlayOffsetImage(ver, 0, -(this.pieceSize / 4), tile);
    }
    if (this.left) {
      tile = new OverlayOffsetImage(hor, (this.pieceSize / 4), 0, tile);
    }

    return tile;
  }

  // To render the power station on this Tile
  WorldImage drawStation(WorldImage tile) {
    WorldImage stationStar = new StarImage(20, 7, OutlineMode.SOLID, Color.CYAN);
    WorldImage stationOutline = new StarImage(20, 7, OutlineMode.OUTLINE, Color.ORANGE);

    tile = new OverlayImage(stationOutline, new OverlayImage(stationStar, tile));

    return tile;
  }

}
