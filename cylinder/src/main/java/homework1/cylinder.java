package homework1;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

public class cylinder extends Shape
{
	public cylinder(RenderContext renderContext, int noOfSide, float radius, float height)
		super(cylinderVertexData(renderContext, noOfSide, radius, height));
	
	private VertexData cylinderVertexData(RenderContext renderContext, int noOfSide, float radius, float height)

		// calculate the position of each points
		// theta = 360 degree / noOfSide
		// projection point on X-axis is r*sin(theta)
		// projection point on Y-axis is r*cos(theta)
		// z is chosen as height in order to simplify the program
	
		float theta = (float)Math.PI*2/noOfSide;
		// construct the position of cylinder VertexDate
		// cylinder contains noOfSide rectangular faces + 2
		// and noOfSide*2 points
		float[] v = new float[noOfSide*2*3];
		int i;
		for (i=0; i<noOfSide; i++)
		{
			// clock-wise
			v[noOfSide*6]   = radius * (float)Math.sin(theta*noOfSide); // x
			v[noOfSide*6+1] = radius * (float)Math.cos(theta*noOfSide); // y
			v[noOfSide*6+2] = height; // z
			
			v[noOfSide*6]   = radius * (float)Math.sin(theta*noOfSide); // x
			v[noOfSide*6+1] = radius * (float)Math.cos(theta*noOfSide); // y
			v[noOfSide*6+2] = -height; // z
			
			v[noOfSide*6]   = radius * (float)Math.sin(theta*(noOfSide+1)); // x
			v[noOfSide*6+1] = radius * (float)Math.cos(theta*(noOfSide+1)); // y
			v[noOfSide*6+2] = -height; // z
			
			v[noOfSide*6]   = radius * (float)Math.sin(theta*(noOfSide+1)); // x
			v[noOfSide*6+1] = radius * (float)Math.cos(theta*(noOfSide+1)); // y
			v[noOfSide*6+2] = height; // z
		}

}
