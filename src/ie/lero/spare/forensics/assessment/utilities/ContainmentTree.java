package ie.lero.spare.forensics.assessment.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
  * @author ycoppel@google.com (Yohann Coppel)
  * 
  * @param <T>
  *          Object's type in the tree.
*/
public class ContainmentTree<T> {

  private T head;
  
  private T assetName;

  private ArrayList<ContainmentTree<T>> leafs = new ArrayList<ContainmentTree<T>>();

  private ContainmentTree<T> parent = null;

  private HashMap<T, ContainmentTree<T>> locate = new HashMap<T, ContainmentTree<T>>();

  public ContainmentTree(T head) {
    this.head = head;
    locate.put(head, this);
  }

  public void addLeaf(T root, T leaf) {
    if (locate.containsKey(root)) {
      locate.get(root).addLeaf(leaf);
    } else {
      addLeaf(root).addLeaf(leaf);
    }
  }

  public ContainmentTree<T> addLeaf(T leaf) {
    ContainmentTree<T> t = new ContainmentTree<T>(leaf);
    leafs.add(t);
    t.parent = this;
    t.locate = this.locate;
    locate.put(leaf, t);
    return t;
  }

  public ContainmentTree<T> setAsParent(T parentRoot) {
    ContainmentTree<T> t = new ContainmentTree<T>(parentRoot);
    t.leafs.add(this);
    this.parent = t;
    t.locate = this.locate;
    t.locate.put(head, this);
    t.locate.put(parentRoot, t);
    return t;
  }

  public T getHead() {
    return head;
  }

  public ContainmentTree<T> getTree(T element) {
    return locate.get(element);
  }

  public ContainmentTree<T> getParent() {
    return parent;
  }

  public Collection<T> getSuccessors(T root) {
    Collection<T> successors = new ArrayList<T>();
    ContainmentTree<T> tree = getTree(root);
    if (null != tree) {
      for (ContainmentTree<T> leaf : tree.leafs) {
        successors.add(leaf.head);
      }
    }
    return successors;
  }

  public Collection<ContainmentTree<T>> getSubTrees() {
    return leafs;
  }

  public static <T> Collection<T> getSuccessors(T of, Collection<ContainmentTree<T>> in) {
    for (ContainmentTree<T> tree : in) {
      if (tree.locate.containsKey(of)) {
        return tree.getSuccessors(of);
      }
    }
    return new ArrayList<T>();
  }

  
  public T getAssetName() {
    return this.assetName;
  }

  public void setAssetName(T assetName) {
    this.assetName = assetName;
  }

  @Override
  public String toString() {
    return printTree(0);
  }

  private static final int indent = 2;

  private String printTree(int increment) {
    String s = "";
    String inc = "";
    for (int i = 0; i < increment; ++i) {
      inc = inc + " ";
    }
    s = inc + head;
    for (ContainmentTree<T> child : leafs) {
      s += "\n" + child.printTree(increment + indent);
    }
    return s;
  }
}

