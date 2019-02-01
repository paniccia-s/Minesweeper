import java.util.ArrayList;

// A Utils class for sorting and managing graphs of type T
class GraphUtils<T> {

  // To heapsort the given list according to the given IComparator
  void heapSort(ArrayList<T> list, IComparator<T> comp) {
    // first, downheap
    this.heapify(list, comp, list.size() - 1);
    // then, heapify
    this.heapToSortedList(list, comp);
  }

  // To convert the given list into a heap, according to the given IComparator
  // starting at position whereToStart
  void heapify(ArrayList<T> list, IComparator<T> comp, int whereToStart) {
    // Sort every element
    for (int i = whereToStart; i >= 0; i--) {
      // The element to concern now
      T toMove = list.get(i);

      // left and right indices of its children
      int leftIndex = 2 * i + 1;
      int rightIndex = 2 * i + 2;

      // declare children objects
      T childLeft;
      T childRight;

      // If the left index is too big, we're in the last row and we stop
      if (leftIndex < list.size()) {
        // otherwise, get the left child and compare it to the parent
        childLeft = list.get(2 * i + 1);
        int compLeft = comp.compare(toMove, childLeft);
        // If the right index is too big, we only compare one child
        if (rightIndex < list.size()) {
          // otherwise, get the right child and compare it to the parent
          childRight = list.get(2 * i + 2);

          int compRight = comp.compare(toMove, childRight);

          // compare the kids to see which is bigger
          int compareKids = comp.compare(childLeft, childRight);

          // If either child is bigger...
          if ((compLeft < 0) || (compRight < 0)) {
            // declare which element to swap and its index
            T toSwap;
            int indexToSwap;

            // If the left child is bigger than the right...
            if (compareKids > 0) {
              // we're swapping left
              toSwap = childLeft;
              indexToSwap = leftIndex;
            }
            else {
              // otherwise we're swapping right
              toSwap = childRight;
              indexToSwap = rightIndex;
            }

            // swap toMove and the child to swap
            T temp = toMove;
            list.set(i, toSwap);
            list.set(indexToSwap, temp);

            // recur on the new list post-swap
            heapify(list, comp, indexToSwap);
          }
          // If neither child is bigger, then we're good for this one
        }
        // If there is no right child...
        else {
          // and if the left child is bigger than the parent...
          if (compLeft < 0) {
            // swap left and parent, recur
            T temp = toMove;
            list.set(i, childLeft);
            list.set(leftIndex, temp);

            heapify(list, comp, leftIndex);
          }
        }
      }
    }
  }

  // To convert the given heap into a sorted list, according to the given
  // IComparator, heapifying when necessary
  void heapToSortedList(ArrayList<T> list, IComparator<T> comp) {
    ArrayList<T> sorted = new ArrayList<T>();
    while (list.size() != 0) {
      // remove the top, heapify to maintain heap after removing
      T max = list.remove(0);
      sorted.add(max);
      heapify(list, comp, list.size() - 1);
    }

    // mutate list to contain the elements of sorted, in order
    for (T t : sorted) {
      list.add(t);
    }
  }

  // Conducts a breadth-first search through a graph
  // - The search begins at T start
  // - A match is determined by the given IPred<T> checkFound
  // - Managing retrieving neighbors and checking for cycles is done by manager
  // and returns the found element, its depth in the graph, and whether or not
  // it was
  // returned as the last-seen element because the true element was not found
  GraphSearchReturnPackage<T> breadthFirstSearch(T start, IPred<T> checkFound,
      GraphSearchManager<T> manager) {
    // Worklist and what's been seen
    Deque<T> worklist = new Deque<T>();
    ArrayList<T> seen = new ArrayList<T>();

    // Always add at the tail for BFS
    worklist.addAtTail(start);

    // The current depth of the search
    int depth = 0;

    // How many Tiles have been added to the current depth level
    int thisLevel = 1;
    // How many Tiles have been added to the next depth level
    int nextLevel = 0;

    // Work until there are no more Tiles in the graph
    while (worklist.size() != 0) {

      T next = worklist.removeFromHead();

      // Apply predicate: if true, we've found what we're looking for
      if (checkFound.apply(next)) {
        return new GraphSearchReturnPackage<T>(true, depth, next);
      }

      // Otherwise, get all of the neighbors of the current Tile
      ArrayList<T> neighbors = manager.getNeighbors(next);

      // Set this Tile's cycleVisited to true to avoid infinite cycle recursion
      manager.setVisited(next, true);
      // For each neighbor...
      for (T t : neighbors) {
        // ... if it has not yet been visited...
        if (!manager.visited(t)) {
          // add the neighbor to the worklist
          worklist.addAtTail(t);
          // and increment the number of Tiles to work through on the next level
          // by one (because any *new* neighbors of the current Tile must be on
          // the next depth level)
          nextLevel++;
        }
      }

      // Done with this one, decrement thisLevel by one: we've finished a Tile
      // on this level
      thisLevel--;

      // If we've finished this depth level...
      if (thisLevel == 0) {
        // move down a level and increment the depth by one
        thisLevel = nextLevel;
        nextLevel = 0;
        depth++;
      }

      // Finally, add this one to the list of seen
      seen.add(next);
    }

    // Clean-up: reset cycleVisited for each Tile
    for (T t : seen) {
      manager.setVisited(t, false);
    }

    // Return what was seen last
    T last = seen.get(seen.size() - 1);
    return new GraphSearchReturnPackage<T>(false, depth, last);
  }

  // To produce a minimal spanning tree of the given graph, according to the
  // given IComparator
  ArrayList<Edge<T>> mst(int numNodes, ArrayList<Edge<T>> l, IComparator<Edge<T>> comp,
      GraphSearchManager<T> manager) {

    // loe is an unsorted List of Edges. first, sort it
    new GraphUtils<Edge<T>>().heapSort(l, comp);

    // the minimal spanning tree
    ArrayList<Edge<T>> mst = new ArrayList<Edge<T>>();

    // number of edges in mst is exactly equal to number of nodes - 1
    int numEdges = numNodes - 1;

    // keep going until mst is established
    while (mst.size() != numEdges) {
      // only add the lowest if it does not create a cycle
      Edge<T> toConsider = l.remove(0);
      if (noCycle(toConsider, mst, manager)) {
        mst.add(toConsider);
      }
    }

    return mst;
  }

  // To determine whether to add the given Edge to the given graph without
  // introducing a cycle
  boolean noCycle(Edge<T> edge, ArrayList<Edge<T>> l, GraphSearchManager<T> manager) {
    T one = edge.fromNode;
    T two = edge.toNode;

    // Strategy: try to reach two from one. If it's possible to do so, then
    // adding edge must
    // introduce a cycle
    return this.noCycleHelp(l, one, two, manager);
  }

  // To try to reach T toFind starting at T from
  // - returns whether or not a cycle *would* exist if the relevant Edge<T> were
  // added
  boolean noCycleHelp(ArrayList<Edge<T>> l, T from, T toFind, GraphSearchManager<T> manager) {
    ArrayList<T> linkedToFrom = new ArrayList<T>();

    // for each edge: if it has from as one of its nodes, add to list
    for (Edge<T> e : l) {
      if (e.fromNode == from) {
        linkedToFrom.add(e.toNode);
      }
      else if (e.toNode == from) {
        linkedToFrom.add(e.fromNode);
      }
    }

    // Whether or not it is safe to add the Edge<T> in question
    boolean shouldAddEdge = true;

    // this is a list of every connection including from
    for (T t : linkedToFrom) {
      // if t is equal to toFind, there is a cycle
      if (t.equals(toFind)) {
        return false;
      }
      else {
        // otherwise, we must recur
        // from has been visited
        manager.setVisited(from, true);
        if (!manager.visited(t)) {
          // a single false should be of importance: some paths may return true,
          // but
          // any path that returns false is dominant
          shouldAddEdge = shouldAddEdge && this.noCycleHelp(l, t, toFind, manager);
        }
        // cleanup: return from's visited state to false
        manager.setVisited(from, false);
      }
    }
    return shouldAddEdge;
  }
}

// A manager for searching through generic-type graphs. Used for generic
// graph searching algorithms
interface GraphSearchManager<T> {

  // To retrieve all neighbors of the given T
  ArrayList<T> getNeighbors(T t);

  // To determine if this T has already been visited by the cyclic algorithm
  boolean visited(T t);

  // To set the given T's cycle-visited state to the given boolean
  void setVisited(T t, boolean val);
}

// The graph manager for a graph of Tiles
class TileGraphSearchManager implements GraphSearchManager<Tile> {

  // To retrieve all neighbors of the given Tile
  @Override
  public ArrayList<Tile> getNeighbors(Tile t) {
    // This functionality is already in the Tile class
    return t.getNeighbors();
  }

  // To determine if this Tile has already been visited by the cyclic algorithm
  @Override
  public boolean visited(Tile t) {
    return t.cycleVisited;
  }

  // To set the given Tile's cycleVisited to val
  @Override
  public void setVisited(Tile t, boolean val) {
    t.cycleVisited = val;
  }
}

// A package containing the T returned by a graph searching algorithm,
// along with the depth at which that element was and whether the element
// was actually found or returned at the end of the algorithm as the last-seen
// element
class GraphSearchReturnPackage<T> {
  // Was the desired T found, or did the algorithm return the last-seen T?
  boolean wasFound;
  // The depth of the returned T
  int depthAt;
  // The returned T
  T found;

  public GraphSearchReturnPackage(boolean wasFound, int depthAt, T found) {
    this.wasFound = wasFound;
    this.depthAt = depthAt;
    this.found = found;
  }
}
