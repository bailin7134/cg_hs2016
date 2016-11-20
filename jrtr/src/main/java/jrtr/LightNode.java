package jrtr;

import javax.vecmath.Matrix4f;

public class LightNode extends Leaf {

	private Light l;

	public LightNode(Light l, Matrix4f t) {
		super(t);
		this.l = l;
	}
	
	public LightNode(Light l) {
		super();
		this.l = l;
	}
	
	public Light getLight() {
		return l;
	}
	
}
