package homework1;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

public class cylinder extends Shape
{
	public cylinder(RenderContext renderContext, int noOfSide, float radius, float height)
	{
		super(cylinderVertexData(renderContext, noOfSide, radius, height));
	}
	
	private static VertexData cylinderVertexData(RenderContext renderContext, int noOfSide, float radius, float height)
	{
		// calculate the position of each points
		// theta = 360 degree / noOfSide
		// projection point on X-axis is r*sin(theta)
		// projection point on Y-axis is r*cos(theta)
		// z is chosen as height in order to simplify the program
	
		double theta = Math.PI*2/noOfSide;
		// construct the position of cylinder VertexDate
		// cylinder contains noOfSide rectangular faces + 2
		// and noOfSide*2 points
		float[] v = new float[noOfSide*6*3];
		for (int i=0; i<noOfSide; i++)
		{
			// upper center point
			v[i*6*3] = 0;
			v[i*6*3+1] = 0;
			v[i*6*3+2] = height;
			// side points
			v[i*6*3+3] = radius * (float)Math.sin(theta*i); // x
			v[i*6*3+4] = radius * (float)Math.cos(theta*i); // y
			v[i*6*3+5] = height; // z
			v[i*6*3+6] = radius * (float)Math.sin(theta*(i+1)); // x
			v[i*6*3+7] = radius * (float)Math.cos(theta*(i+1)); // y
			v[i*6*3+8] = height; // z
			// lower center point
			v[i*6*3+9] = 0;
			v[i*6*3+10] = 0;
			v[i*6*3+11] = 0;
			// side points
			v[i*6*3+12] = radius * (float)Math.sin(theta*i); // x
			v[i*6*3+13] = radius * (float)Math.cos(theta*i); // y
			v[i*6*3+14] = 0; // z
			v[i*6*3+15] = radius * (float)Math.sin(theta*(i+1)); // x
			v[i*6*3+16] = radius * (float)Math.cos(theta*(i+1)); // y
			v[i*6*3+17] = 0; // z
		}

		float[] c = new float[v.length];
        for (int i=0; i<noOfSide; i=i+2) {
        	for (int j=0; j<6; j++)
        	{
        		c[i*18+j*3] = 1;
        		c[i*18+j*3+1] = 0;
        		c[i*18+j*3+2] = 0;
        	}
        }
        for (int i=1; i<noOfSide; i=i+2) {
        	for (int j=0; j<6; j++)
        	{
        		c[i*18+j*3] = 0;
        		c[i*18+j*3+1] = 0;
        		c[i*18+j*3+2] = 1;
        	}
        }

		int indices[] = new int[4*noOfSide*3];
		for(int i=0; i<noOfSide; i++)
		{
			indices[i*4*3] = i*6;
			indices[i*4*3+1] = i*6+1;
			indices[i*4*3+2] = i*6+2;

			indices[i*4*3+3] = i*6+3;
			indices[i*4*3+4] = i*6+4;
			indices[i*4*3+5] = i*6+5;
			
			indices[i*4*3+6] = i*6+1;
			indices[i*4*3+7] = i*6+2;
			indices[i*4*3+8] = i*6+5;
			
			indices[i*4*3+9] = i*6+1;
			indices[i*4*3+10] = i*6+4;
			indices[i*4*3+11] = i*6+5;
		}

		VertexData vertexData = renderContext.makeVertexData(v.length/3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);

		vertexData.addIndices(indices);

		return vertexData;
	}
}
