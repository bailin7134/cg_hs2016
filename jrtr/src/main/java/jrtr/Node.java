package jrtr;

import java.util.LinkedList;
import javax.vecmath.Matrix4f;

public interface Node {
	
	public Matrix4f getTransformation();
	public LinkedList<Node> getChildren();
//	public Shape getShape();
	
}
