package jrtr;

import java.util.List;
import java.util.LinkedList;
import javax.vecmath.Matrix4f;

public abstract class Group implements Node {
	
	LinkedList<Node> children = new LinkedList<Node>();
	
	// add child
	public void addChildren(Node... nodes) {
		for (Node n: nodes)
			children.add(n);
	}
	
	// remove child
	public boolean removeChildren(Node n) {
		return children.remove(n);
	}
	
	// get child
	public LinkedList<Node> getChildren() {
		return children;
	}
}
