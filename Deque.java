
import java.util.Iterator;
import tester.Tester;
/*
 * Design choice: 
 * Adding/removing/finding is done only through the Sentinel. Calling
 * adding/removing methods (such as add/remove at head/tail) 
 * will throw a RuntimeException. This is because the user should
 * not be operating through the Sentinel/Node(s) of the Deque; 
 * the user calls to the Deque, the Deque calls to the Sentinel,
 * and the Sentinel adds/removes as appropriate. This also applies
 * to contains(), a method that should never be called by the user. 
 * 
 * Also: during tests, fields of fields (sometimes of fields) are
 * directly referenced. This isn't done in functional code and 
 * only serves to facilitate testing.
 */

class ExamplesDeque {

  // Example nodes
  Node<String> one = new Node<String>("one");
  Node<String> two = new Node<String>("two");
  Node<String> three = new Node<String>("three");

  Node<String> four = new Node<String>("four");
  Node<String> five = new Node<String>("five");
  Node<String> six = new Node<String>("six");

  // OneTwoThree -> OTT, FourFiveSix 0> FFS
  Deque<String> ott;
  Deque<String> ffs;

  // three we needed
  Deque<String> deque1 = new Deque<String>();
  Deque<String> deque2;
  Deque<String> deque3;

  void initData() {
    one = new Node<String>("one");
    two = new Node<String>("two");
    three = new Node<String>("three");
    four = new Node<String>("four");
    five = new Node<String>("five");
    six = new Node<String>("six");

    ott = new Deque<String>();
    ffs = new Deque<String>();
    deque2 = new Deque<String>();

    deque2.addAtTail("abc");
    deque2.addAtTail("bcd");
    deque2.addAtTail("cde");

    deque3 = new Deque<String>();
    deque3.addAtTail("one");
    deque3.addAtTail("two");
    deque3.addAtTail("three");
    deque3.addAtTail("four");
    deque3.addAtTail("five");
    deque3.addAtTail("six");

  }

  void testT(Tester t) {
    initData();
    for (String s : this.deque2) {
      System.out.println(s);
    }
  }

  void initOtt() {
    ott.header.addAtTail(one);
    ott.header.addAtTail(two);
    ott.header.addAtTail(three);
  }

  void initFfs() {
    ffs.header.addAtTail(four);
    ffs.header.addAtTail(five);
    ffs.header.addAtTail(six);
  }

  void testPreds(Tester t) {
    initData();
    IPred<String> o = new PredFirstWithO();
    IPred<String> c = new PredNotThreeChars();
    IPred<String> m = new PredMtString();

    t.checkExpect(o.apply(""), false);
    t.checkExpect(o.apply("asdfasdfasdbpo"), true);
    t.checkExpect(o.apply("t.checkExpect"), false);

    t.checkExpect(c.apply(""), true);
    t.checkExpect(c.apply("123"), false);
    t.checkExpect(c.apply("asdfasdf"), true);

    t.checkExpect(m.apply(""), true);
    t.checkExpect(m.apply("im so tired"), false);
  }

  void testSizePostSentinel(Tester t) {
    initData();
    // size of a Sentinel should be 0
    t.checkExpect(ott.header.sizePostSentinel(), 0);
    initOtt();
    // size after visiting sentinel should still be 0
    t.checkExpect(ott.header.sizePostSentinel(), 0);
    // sizePostSentinel should function as a standard size for Nodes
    t.checkExpect(ott.header.next.sizePostSentinel(), 3);
    // really bad test but it's fine
    t.checkExpect(ott.header.next.next.sizePostSentinel(), 2);
  }

  void testSize(Tester t) {
    initData();
    // size of any empty deque is 0
    t.checkExpect(ott.size(), 0);
    t.checkExpect(ffs.size(), 0);
    t.checkExpect(deque1.size(), 0);
    t.checkExpect(ott.header.size(), 0);
    t.checkExpect(ffs.header.size(), 0);
    t.checkExpect(deque1.header.size(), 0);

    // add to ott
    ott.addAtHead("one");
    t.checkExpect(ott.size(), 1);
    t.checkExpect(ott.header.size(), 1);

    ott.addAtHead("two");
    t.checkExpect(ott.size(), 2);

    ott.addAtHead("three");
    t.checkExpect(ott.size(), 3);

    // size of others should still be 0
    t.checkExpect(ffs.size(), 0);
    t.checkExpect(deque1.size(), 0);
    t.checkExpect(ffs.header.size(), 0);
    t.checkExpect(deque1.header.size(), 0);

    this.initFfs();
    t.checkExpect(ffs.size(), 3);
    t.checkExpect(ffs.header.size(), 3);

    t.checkExpect(deque1.size(), 0);
    t.checkExpect(deque1.header.size(), 0);

    // throw error if called on a node
    t.checkException(new RuntimeException("Do not invoke size() on a non-Sentinel ANode!"), one,
        "size");

  }

  void testAddHead(Tester t) {
    initData();
    // make sure there's nothing in ott before beginning
    t.checkExpect(ott.size(), 0);
    // add two to ott
    ott.addAtHead("two");
    ANode<String> newTwo = new Node<String>("two", ott.header, ott.header);
    // size should be one
    t.checkExpect(ott.size(), 1);
    // the first element should be one (next + prev fields of ott.header)
    t.checkExpect(ott.header.next, newTwo);
    t.checkExpect(ott.header.prev, newTwo);
    // add one to ott.head (which should never be done by the user)
    ott.addAtHead(one.data);
    ANode<String> newOne = new Node<String>("one", newTwo, ott.header);
    // size should be two
    t.checkExpect(ott.size(), 2);
    // the first element should be one
    t.checkExpect(ott.header.next, newOne);
    // the last element should be two
    t.checkExpect(ott.header.prev, newTwo);
    // the second element should be two
    t.checkExpect(ott.header.next.next, newTwo);

    // the following should throw:
    Exception toThrow = new RuntimeException("Do not invoke add methods on a Node!");
    t.checkException(toThrow, two, "addAtHead", five);
  }

  void testAddTail(Tester t) {
    initData();
    // make sure there's nothing in ott in beginning
    t.checkExpect(ott.size(), 0);
    // add one to ott
    ott.addAtTail("one");
    ANode<String> newOne = new Node<String>("one", ott.header, ott.header);
    // size should be one
    t.checkExpect(ott.size(), 1);
    // the first element should be one (next + prev fields of ott.header)
    t.checkExpect(ott.header.next, newOne);
    t.checkExpect(ott.header.prev, newOne);
    // add two to ott
    ott.addAtTail("two");
    ANode<String> newTwo = new Node<String>("two", ott.header, newOne);
    // size should be two
    t.checkExpect(ott.size(), 2);
    // the first element should be one
    t.checkExpect(ott.header.next, newOne);
    // the last element should be two
    t.checkExpect(ott.header.prev, newTwo);
    // the second element should be two
    t.checkExpect(ott.header.next.next, newTwo);

    // the following should throw:
    Exception toThrow = new RuntimeException("Do not invoke add methods on a Node!");
    t.checkException(toThrow, two, "addAtTail", five);
  }

  void testRemoveAtHead(Tester t) {
    initData();
    initOtt();
    // keep track of what's last removed
    String lastRemoved = this.ott.removeFromHead();
    // new size is one less than before
    t.checkExpect(ott.size(), 2);
    // next should refer to the one after the last removed (before removal)
    // using these chained dot accessors is very bad practice but it's okay for
    // testing
    t.checkExpect(ott.header.next, two);
    // the second in the list should be the old third
    t.checkExpect(ott.header.next.next, three);
    // the last should also be the old third
    t.checkExpect(ott.header.prev, three);
    t.checkExpect(lastRemoved, one.data);

    lastRemoved = this.ott.removeFromHead();
    t.checkExpect(ott.size(), 1);

    t.checkExpect(ott.header.next, three);
    t.checkExpect(ott.header.prev, three);
    t.checkExpect(lastRemoved, two.data);

    lastRemoved = this.ott.removeFromHead();
    t.checkExpect(ott.size(), 0);

    t.checkExpect(ott.header.next, ott.header);
    t.checkExpect(ott.header.prev, ott.header);
    t.checkExpect(lastRemoved, three.data);

    // the following should throw:
    Exception toThrow = new RuntimeException("Cannot remove from an empty list!");
    t.checkException(toThrow, ott, "removeFromHead");
  }

  void testRemoveAtTail(Tester t) {
    initData();
    initOtt();

    String lastRemoved = this.ott.removeFromTail();

    t.checkExpect(ott.size(), 2);

    t.checkExpect(ott.header.next, one);
    t.checkExpect(ott.header.next.next, two);
    t.checkExpect(ott.header.prev, two);
    t.checkExpect(lastRemoved, three.data);

    lastRemoved = this.ott.removeFromTail();
    t.checkExpect(ott.size(), 1);

    t.checkExpect(ott.header.next, one);
    t.checkExpect(ott.header.prev, one);
    t.checkExpect(lastRemoved, two.data);

    lastRemoved = this.ott.removeFromTail();
    t.checkExpect(ott.size(), 0);

    t.checkExpect(ott.header.next, ott.header);
    t.checkExpect(ott.header.prev, ott.header);
    t.checkExpect(lastRemoved, one.data);

    // the following should throw:
    Exception toThrow = new RuntimeException("Cannot remove from an empty list!");
    t.checkException(toThrow, ott, "removeFromTail");
  }

  void testFind(Tester t) {
    initData();
    // finding anything on an empty list should return the sentinel
    t.checkExpect(ott.find(new PredFirstWithO()), ott.header);
    t.checkExpect(ott.header.find(new PredFirstWithO()), ott.header);
    t.checkExpect(ott.find(new PredNotThreeChars()), ott.header);
    t.checkExpect(ott.header.find(new PredNotThreeChars()), ott.header);
    t.checkExpect(ott.find(new PredMtString()), ott.header);
    t.checkExpect(ott.header.find(new PredMtString()), ott.header);
    t.checkExpect(ffs.find(new PredFirstWithO()), ffs.header);
    t.checkExpect(ffs.header.find(new PredFirstWithO()), ffs.header);
    t.checkExpect(ffs.find(new PredNotThreeChars()), ffs.header);
    t.checkExpect(ffs.header.find(new PredNotThreeChars()), ffs.header);
    t.checkExpect(ffs.find(new PredMtString()), ffs.header);
    t.checkExpect(ffs.header.find(new PredMtString()), ffs.header);

    initOtt();

    // now, ott should return results as appropriate; ffs should still return
    // the sentinel
    t.checkExpect(ott.find(new PredFirstWithO()), one);
    t.checkExpect(ott.header.find(new PredFirstWithO()), one);
    t.checkExpect(ott.find(new PredNotThreeChars()), three);
    t.checkExpect(ott.header.find(new PredNotThreeChars()), three);
    t.checkExpect(ott.find(new PredMtString()), ott.header);
    t.checkExpect(ott.header.find(new PredMtString()), ott.header);
    t.checkExpect(ffs.find(new PredFirstWithO()), ffs.header);
    t.checkExpect(ffs.header.find(new PredFirstWithO()), ffs.header);

    // calling find on a Node should throw:
    Exception toThrow = new RuntimeException("Do not invoke find() on a non-Sentinel ANode!");
    t.checkException(toThrow, one, "find", new PredFirstWithO());

  }

  void testFindPostSentinel(Tester t) {
    initData();
    initOtt();

    // calling on a Sentinel returns the Sentinel:
    t.checkExpect(ott.header.findPostSentinel(new PredFirstWithO()), ott.header);
    t.checkExpect(ott.header.findPostSentinel(new PredNotThreeChars()), ott.header);
    t.checkExpect(ott.header.findPostSentinel(new PredMtString()), ott.header);
    // calling on a Node returns as appropriate:

    t.checkExpect(one.findPostSentinel(new PredFirstWithO()), one);

  }

  void testRemoveNode(Tester t) {
    initData();
    initOtt();
    ott.removeNode(two);
    t.checkExpect(ott.size(), 2);
    t.checkExpect(ott.header.next, one);
    t.checkExpect(ott.header.prev, three);
    t.checkExpect(ott.header.next.next, three);

    ott.removeNode(one);
    t.checkExpect(ott.header.next, three);

    ott.removeNode(three);
    t.checkExpect(ott.header.next, ott.header);

    // Trying to remove something that is not in the list should do nothing
    initFfs();
    ffs.removeNode(one);
    t.checkExpect(ffs.size(), 3);
  }

  void testContains(Tester t) {
    initData();
    t.checkExpect(ott.header.contains(one), false);

    ott.addAtTail("one");
    ANode<String> newOne = new Node<String>("one", ott.header, ott.header);
    t.checkExpect(ott.header.contains(newOne), true);
    t.checkExpect(ott.header.contains(two), false);
    initOtt();
    newOne.next = two;
    t.checkExpect(ott.header.contains(newOne), true);
    t.checkExpect(ott.header.contains(five), false);
  }

  void testContainsPostSentinel(Tester t) {
    initData();
    initOtt();

    // calling on a Sentinel returns false
    t.checkExpect(ott.header.containsPostSentinel(one), false);

    // calling on a Node operates as desired
    t.checkExpect(one.containsPostSentinel(one), true);
    t.checkExpect(one.containsPostSentinel(five), false);
  }

}

class PredFirstWithO implements IPred<String> {

  @Override
  public boolean apply(String hasO) {
    return hasO.lastIndexOf("o") != -1;
  }

}

class PredNotThreeChars implements IPred<String> {

  @Override
  public boolean apply(String hasThree) {
    return hasThree.length() != 3;
  }

}

class PredMtString implements IPred<String> {

  @Override
  public boolean apply(String t) {
    return t.equals("");
  }

}

class DequeBackIterator<T> implements Iterator<T> {
  ANode<T> current;

  public DequeBackIterator(Deque<T> d) {
    this.current = d.header.prev;
  }

  @Override
  public boolean hasNext() {
    return this.current instanceof Node;
  }

  @Override
  public T next() {
    T next = ((Node<T>) this.current).data;
    this.current = this.current.prev;
    return next;
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub

  }

}

class DequeIterator<T> implements Iterator<T> {

  ANode<T> current;

  public DequeIterator(Deque<T> d) {
    this.current = d.header.next;
  }

  @Override
  public boolean hasNext() {

    return !(current instanceof Sentinel);
  }

  @Override
  public T next() {
    T next = ((Node<T>) this.current).data;
    this.current = this.current.next;
    return next;
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub

  }

}

class Deque<T> implements Iterable<T> {
  Sentinel<T> header;

  Deque() {
    this.header = new Sentinel<T>();
  }

  Deque(Sentinel<T> header) {
    this.header = header;
  }

  // To count the number of Nodes (NOT including Sentinel!) in this Deque.
  int size() {
    return this.header.size();
  }

  void addAtHead(T newHead) {
    this.header.addAtHead(new Node<T>(newHead));
  }

  void addAtTail(T newTail) {
    this.header.addAtTail(new Node<T>(newTail));
  }

  T removeFromHead() {
    return this.header.removeAtHead().data;
  }

  T removeFromTail() {
    return this.header.removeAtTail().data;
  }

  ANode<T> find(IPred<T> pred) {
    return this.header.find(pred);
  }

  void removeNode(ANode<T> toRemove) {
    // toRemove must be...
    // - not the Sentinel
    // - not unlinked
    // - within this Deque
    if ((this.header != toRemove) && toRemove.next != null && toRemove.prev != null
        && this.contains(toRemove)) {
      toRemove.remove();
    }
  }

  boolean contains(ANode<T> contains) {
    return this.header.contains(contains);
  }

  @Override
  public Iterator<T> iterator() {
    return new DequeBackIterator<T>(this);
  }

}

abstract class ANode<T> {
  ANode<T> next;
  ANode<T> prev;

  abstract int size();

  abstract int sizePostSentinel();

  abstract ANode<T> find(IPred<T> pred);

  abstract ANode<T> findPostSentinel(IPred<T> pred);

  abstract boolean contains(ANode<T> contains);

  abstract boolean containsPostSentinel(
      ANode<T> contains);/*
                          * 
                          * abstract <U> boolean perform(IPred<U> pred);
                          * 
                          * abstract <U> boolean performPostSentinel(IPred<U>
                          * pred);
                          */

  // removeAtHead and removeAtTail are abstracted into remove(), which runs on
  // Node and throws on Sentinel
  abstract Node<T> remove();

  // head and tail direct calls throw in Node and redirect to remove() on
  // Sentinel
  abstract Node<T> removeAtHead();

  abstract Node<T> removeAtTail();

  void addAtHead(ANode<T> newNext) {
    this.next.prev = newNext;
    newNext.next = this.next;
    newNext.prev = this;
    this.next = newNext;
  }

  void addAtTail(ANode<T> newPrev) {
    this.prev.next = newPrev;
    newPrev.prev = this.prev;
    newPrev.next = this;
    this.prev = newPrev;
  }

}

class Sentinel<T> extends ANode<T> {

  Sentinel() {
    this.next = this;
    this.prev = this;
  }

  // To count the number of Nodes (NOT including Sentinel!) in this Sentinel
  @Override
  int size() {
    return this.next.sizePostSentinel();
  }

  @Override
  int sizePostSentinel() {
    return 0;
  }

  @Override
  void addAtHead(ANode<T> newNext) {
    if (this.next == this) {
      // empty list
      this.next = newNext;
      this.next.prev = this;
      this.prev = newNext;
      this.next.next = this;
    }
    else {
      // not empty list
      super.addAtHead(newNext);
    }
  }

  @Override
  void addAtTail(ANode<T> newPrev) {
    if (this.prev == this) {
      // empty list
      this.prev = newPrev;
      this.prev.next = this;
      this.next = newPrev;
      this.next.prev = this;
    }
    else {
      // not empty list
      super.addAtTail(newPrev);
    }
  }

  @Override
  Node<T> removeAtHead() {
    // if list is empty, throw exception
    if (this.next == this) {
      throw new RuntimeException("Cannot remove from an empty list!");
    }
    else {
      // remove the first
      return this.next.remove();
    }
  }

  @Override
  Node<T> removeAtTail() {
    // if list is empty, throw exception
    if (this.prev == this) {
      throw new RuntimeException("Cannot remove from an empty list!");
    }
    else {
      // remove the last
      return this.prev.remove();
    }
  }

  @Override
  ANode<T> find(IPred<T> pred) {
    return this.next.findPostSentinel(pred);
  }

  @Override
  ANode<T> findPostSentinel(IPred<T> pred) {
    // if we've made it back to the Sentinel, we're finished; return this
    return this;
  }

  @Override
  boolean contains(ANode<T> contains) {
    return this.next.containsPostSentinel(contains);
  }

  @Override
  boolean containsPostSentinel(ANode<T> contains) {
    return contains == this;
  }

  @Override
  Node<T> remove() {
    throw new RuntimeException("Cannot remove a Sentinel!");
  }

}

class Node<T> extends ANode<T> {
  T data;

  Node(T data) {
    this.data = data;
  }

  Node(T data, ANode<T> next, ANode<T> prev) {
    if (next == null || prev == null) {
      throw new IllegalArgumentException(
          "A Node has been" + " given null input for its next/prev fields!");
    }

    this.data = data;
    this.next = next;
    this.prev = prev;

    this.next.prev = this;
    this.prev.next = this;
  }

  @Override
  int size() {
    throw new RuntimeException("Do not invoke size() on a non-Sentinel ANode!");
  }

  @Override
  int sizePostSentinel() {
    return 1 + this.next.sizePostSentinel();
  }

  @Override
  void addAtHead(ANode<T> toAdd) {
    throw new RuntimeException("Do not invoke add methods on a Node!");
  }

  @Override
  void addAtTail(ANode<T> toAdd) {
    throw new RuntimeException("Do not invoke add methods on a Node!");
  }

  @Override
  Node<T> removeAtHead() {
    throw new RuntimeException("Do not invoke remove methods on a Node!");
  }

  @Override
  Node<T> removeAtTail() {
    throw new RuntimeException("Do not invoke remove methods on a Node!");
  }

  @Override
  ANode<T> find(IPred<T> pred) {
    throw new RuntimeException("Do not invoke find() on a non-Sentinel ANode!");
  }

  @Override
  ANode<T> findPostSentinel(IPred<T> pred) {
    if (pred.apply(this.data)) {
      return this;
    }
    return this.next.findPostSentinel(pred);
  }

  @Override
  boolean contains(ANode<T> contains) {
    throw new RuntimeException("Do not invoke contains() on a non-Sentinel ANode!");
  }

  @Override
  boolean containsPostSentinel(ANode<T> contains) {
    return (this == contains) || this.next.containsPostSentinel(contains);
  }

  @Override
  Node<T> remove() {
    this.next.prev = this.prev;
    this.prev.next = this.next;
    return this;
  }

}
