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
		float[] v = new float[(noOfSide+1)*2*3];
		int i;
		// center points of top and bottom
		v[0] = 0;
		v[1] = 0;
		v[2] = height;

		v[(noOfSide+1)*3] = 0;
		v[(noOfSide+1)*3+1] = 0;
		v[(noOfSide+1)*3+2] = 0;
		// side points
		for (i=1; i<noOfSide+1; i++)
		{
			v[i*3]   = radius * (float)Math.sin(theta*i); // x
			v[i*3+1] = radius * (float)Math.cos(theta*i); // y
			v[i*3+2] = height; // z

			v[(noOfSide+2)*3]   = radius * (float)Math.sin(theta*i); // x
			v[(noOfSide+2)*3+1] = radius * (float)Math.cos(theta*i); // y
			v[(noOfSide+2)*3+2] = 0; // z
		}

		float[] c = new float[(noOfSide+1)*2*3];
        int a = -1;
        for (i = 0; i < noOfSide+1; i++) {
            c[++a] = 1;
            c[++a] = 1;
            c[++a] = 1;

            c[++a] = 0;
            c[++a] = 0;
            c[++a] = 0;
        }

		int s = noOfSide;
		int indices[] = new int[4*s*3];	// 4 triangles per segment
		int indicesTop[] = new int[noOfSide*3];
		int indicesBottom[] = new int[noOfSide*3];
		int indicesSide[] = new int[2*noOfSide*3];
		for(i=0; i<noOfSide-1; i++)
		{
			indices[i*3] = 0;
			indices[i*3+1] = i+1;
			indices[i*3+2] = i+2;
			
			indices[(noOfSide+i)*3] = noOfSide+1;
			indices[(noOfSide+i)*3+1] = noOfSide+i+2;
			indices[(noOfSide+i)*3+2] = noOfSide+i+3;
		}
		indices[(noOfSide-1)*3] = 0;
		indices[(noOfSide-1)*3+1] = noOfSide;
		indices[(noOfSide-1)*3+2] = 1;

		indices[(2*noOfSide-1)*3] = noOfSide+1;
		indices[(2*noOfSide-1)*3+1] = 2*noOfSide+1;
		indices[(2*noOfSide-1)*3+2] = noOfSide+2;

		for(i=0; i<noOfSide-1; i++)
		{
			indicesTop[i*3] = 0;
			indicesTop[i*3+1] = i+1;
			indicesTop[i*3+2] = i+2;

			indicesBottom[i*3] = noOfSide+1;
			indicesBottom[i*3+1] = i+1+noOfSide+1;
			indicesBottom[i*3+2] = i+1+noOfSide+2;
		}
		indicesTop[(noOfSide-1)*3] = 0;
		indicesTop[(noOfSide-1)*3+1] = noOfSide;
		indicesTop[(noOfSide-1)*3+2] = 1;

		indicesBottom[(noOfSide-1)*3] = noOfSide+1;
		indicesBottom[(noOfSide-1)*3+1] = 2*noOfSide+2;
		indicesBottom[(noOfSide-1)*3+2] = noOfSide+2;
		// side
		for(i=1; i<noOfSide-1; i++)
		{
			indicesSide[(i-1)*6] = i;
			indicesSide[(i-1)*6+1] = i+1;
			indicesSide[(i-1)*6+2] = i+noOfSide+1;
			
			indicesSide[(i-1)*6+3] = i;
			indicesSide[(i-1)*6+4] = i+noOfSide+1;
			indicesSide[(i-1)*6+5] = i+noOfSide+1+1;
			
			indices[2*noOfSide+(i-1)*6] = i;
			indices[2*noOfSide+(i-1)*6+1] = i+1;
			indices[2*noOfSide+(i-1)*6+2] = i+noOfSide+1;
			
			indices[2*noOfSide+(i-1)*6+3] = i;
			indices[2*noOfSide+(i-1)*6+4] = i+noOfSide+1;
			indices[2*noOfSide+(i-1)*6+5] = i+noOfSide+1+1;
		}
		indicesSide[(2*noOfSide-2)] = 1;
		indicesSide[(2*noOfSide-2)+1] = noOfSide+2;
		indicesSide[(2*noOfSide-2)+2] = 2*noOfSide+1;

		indicesSide[(2*noOfSide-1)] = 1;
		indicesSide[(2*noOfSide-1)+1] = noOfSide;
		indicesSide[(2*noOfSide-1)+2] = 2*noOfSide+1;

		VertexData vertexData = renderContext.makeVertexData(v.length/3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);

		vertexData.addIndices(indices);

		return vertexData;
	}
}
