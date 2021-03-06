/*
 * Name: Computer Graphics Assignment #1
 * Author: Lin Bai
 * Student No.: 09935404
 * Unit: Department of Computer Science
 * E-mail: lin.bai@students.unibe.ch
 */

package homework1;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

public class Torus extends Shape
{
	public Torus(RenderContext renderContext, float outerRadius, float innerRadius, int outerResolution, int innerResolution)
	{
		super(torusVertexData(renderContext, outerRadius, innerRadius, outerResolution, innerResolution));
	}
	
	private static VertexData torusVertexData(RenderContext renderContext, float outerRadius, float innerRadius, int outerResolution, int innerResolution)
	{
		double p = 2*Math.PI/innerResolution;
		double t = 2*Math.PI/outerResolution;

		float[] v = new float[innerResolution*outerResolution*3];
		float[] c = new float[v.length];
		
		for(int i=0; i<outerResolution; i++)
			for(int j=0; j<innerResolution; j++)
				{
					v[(i*outerResolution+j)*3]   = (outerRadius+innerRadius*(float)Math.cos(j*p))*(float)Math.cos(i*t);
					v[(i*outerResolution+j)*3+1] = (outerRadius+innerRadius*(float)Math.cos(j*p))*(float)Math.sin(i*t);
					v[(i*outerResolution+j)*3+2] = innerRadius*(float)Math.sin(j*p);
				}
		
		for(int i=0; i<c.length/12; i++)
		{
			c[i*12] = 0;
			c[i*12*1] = 0;
			c[i*12+2] = 1;
			c[i*12+3] = 1;
			c[i*12+4] = 0;
			c[i*12+5] = 0;
			c[i*12+6] = 1;
			c[i*12+7] = 0;
			c[i*12+8] = 0;
			c[i*12+9] = 0;
			c[i*12+10] = 0;
			c[i*12+11] = 1;
		}
		// small circle first
		// t is fixed, p varies from 0 to 2*pi
		
		VertexData vertexData = renderContext.makeVertexData(v.length/3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);

		// build indices
		int[] indices = new int[v.length*2*3];
		for(int i=0; i<outerResolution-1; i++)
		{
			for(int j=0; j<innerResolution-1; j++)
			{
				// four angles of the rectangular are
				// i*innerResolution+j, i*innerResolution+j+1
				// (i+1)*innerResolution+j, (i+1)*innerResolution+j+1
				indices[(i*outerResolution+j)*6]=i*innerResolution+j;
				indices[(i*outerResolution+j)*6+1]=i*innerResolution+j+1;
				indices[(i*outerResolution+j)*6+2]=(i+1)*innerResolution+j+1;

				indices[(i*outerResolution+j)*6+3]=i*innerResolution+j;
				indices[(i*outerResolution+j)*6+4]=(i+1)*innerResolution+j;
				indices[(i*outerResolution+j)*6+5]=(i+1)*innerResolution+j+1;
			}
			// the corner of inner circle
			indices[(i*outerResolution+innerResolution-1)*6]=i*innerResolution;
			indices[(i*outerResolution+innerResolution-1)*6+1]=(i+1)*innerResolution-1;
			indices[(i*outerResolution+innerResolution-1)*6+2]=(i+2)*innerResolution-1;

			indices[(i*outerResolution+innerResolution-1)*6+3]=i*innerResolution;
			indices[(i*outerResolution+innerResolution-1)*6+4]=(i+1)*innerResolution;
			indices[(i*outerResolution+innerResolution-1)*6+5]=(i+2)*innerResolution-1;
		}

		// corner of outer circle
		// 0...innerResolution-1
		// (outerResolution-1)*innerResolution...outerResolution*innerResolution-1
		for(int j=0; j<innerResolution-1; j++)
		{
			indices[((outerResolution-1)*innerResolution+j)*6]=j;
			indices[((outerResolution-1)*innerResolution+j)*6+1]=(outerResolution-1)*innerResolution+j;
			indices[((outerResolution-1)*innerResolution+j)*6+2]=(outerResolution-1)*innerResolution+j+1;

			indices[((outerResolution-1)*innerResolution+j)*6+3]=j;
			indices[((outerResolution-1)*innerResolution+j)*6+4]=j+1;
			indices[((outerResolution-1)*innerResolution+j)*6+5]=(outerResolution-1)*innerResolution+j+1;
		}
		// the corner of inner circle
		indices[(outerResolution*innerResolution-1)*6]= 0;
		indices[(outerResolution*innerResolution-1)*6+1]= innerResolution-1;
		indices[(outerResolution*innerResolution-1)*6+2]= outerResolution*innerResolution-1;
		indices[(outerResolution*innerResolution-1)*6+3]= 0;
		indices[(outerResolution*innerResolution-1)*6+4]= (outerResolution-1)*innerResolution;
		indices[(outerResolution*innerResolution-1)*6+5]= outerResolution*innerResolution-1;

		vertexData.addIndices(indices);

		return vertexData;
	}
}
