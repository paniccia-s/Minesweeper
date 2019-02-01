// *** Predicates and Comparators *** \\

// Predicate interface 
interface IPred<T> {
  boolean apply(T t);
}

// An IPred to help determine the radius of a graph
class RadiusSearchPred implements IPred<Tile> {

  /*
   * In order to properly determine the radius during a breadth-first search,
   * the algorithm must run through every element in the graph. This IPred
   * always returns false to ensure that the search doesn't stop until the last
   * element
   */
  @Override
  public boolean apply(Tile t) {
    return false;
  }
}

class TileSearchPred implements IPred<Tile> {

  Tile toFind;

  public TileSearchPred(Tile toFind) {
    this.toFind = toFind;
  }

  @Override
  public boolean apply(Tile i) {
    return i == toFind;
  }
}

// To compare two Ts
// - not sure if we can use Java's Comparator<T> interface instead
interface IComparator<T> {

  // To compare the given elements: return positive if one is greater than two,
  // negative if one is less than two, zero if equal
  int compare(T one, T two);
}

// To compare two integers
class IntComparator implements IComparator<Integer> {

  // To compare the given Integers: return positive if one is greater than two,
  // negative if one is less than two, zero if equal
  @Override
  public int compare(Integer one, Integer two) {
    return one - two;
  }

}

// To compare two Edge<T>s
class EdgeComparator<T> implements IComparator<Edge<T>> {

  // To compare the given Edge<T>s's weights: return positive if one is less
  // than two,
  // negative if one is greater than two, zero if equal
  public int compare(Edge<T> one, Edge<T> two) {
    if (one.weight > two.weight) {
      return -1;
    }
    else if (two.weight > one.weight) {
      return 1;
    }
    return 0;
  }
}
