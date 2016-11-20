package jrtr;

import java.util.LinkedList;

import jrtr.Shape;

public class ShapeNode extends Leaf {
	
	private Shape shape;

	public ShapeNode(Shape shape) {
		super();
		this.shape = shape;
	}
	
	public Shape getShape() {
		return shape;
	}

}
