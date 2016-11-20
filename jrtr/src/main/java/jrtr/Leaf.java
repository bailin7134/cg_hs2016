package jrtr;

import java.util.List;
import java.util.LinkedList;
import javax.vecmath.Matrix4f;

public abstract class Leaf implements Node {
	
	protected Matrix4f t;
	public Leaf() {
		this.t = new Matrix4f();
		t.setIdentity();
	}
	public Leaf(Matrix4f t) {
		this.t = t;
	}
	
	public Matrix4f getTransformation() {
		return t;
	}

	public void setTransformation(Matrix4f t) {
		this.t = t;
	}
	
	public LinkedList<Node> getChildren() {
		return null;
	}
}
