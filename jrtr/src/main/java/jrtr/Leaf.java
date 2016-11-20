package jrtr;

import java.util.LinkedList;

public abstract class Leaf implements Node {
	public LinkedList<Node> getChildren() {
		return null;
	}
}
