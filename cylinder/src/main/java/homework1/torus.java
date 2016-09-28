package homework1;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

public class torus extends Shape
{
	public torus(RenderContext renderContext, float outerRadius, float innerRadius, int outerResolution, int innerResolution)
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
					v[(i*outerResolution+j)*3]   = (outerRadius+innerRadius*(float)Math.cos(j*p))*(float)Math.cos(t*outerResolution);
					v[(i*outerResolution+j)*3+1] = (outerRadius+innerRadius*(float)Math.cos(j*p))*(float)Math.sin(t*outerResolution);
					v[(i*outerResolution+j)*3+2] = innerRadius*(float)Math.sin(j*p);

					c[(i*outerResolution+j)*3]   = 1;
					c[(i*outerResolution+j)*3+1] = 0;
					c[(i*outerResolution+j)*3+2] = 0;
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
				// four angles of the rectangualr are
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
			indices[(i*outerResolution+innerResolution-1)*6]=i*innerResolution+innerResolution-1;
			indices[(i*outerResolution+innerResolution-1)*6+1]=i*innerResolution+innerResolution;
			indices[(i*outerResolution+innerResolution-1)*6+2]=(i+1)*innerResolution+innerResolution;

			indices[(i*outerResolution+innerResolution-1)*6+3]=i*innerResolution+innerResolution-1;
			indices[(i*outerResolution+innerResolution-1)*6+4]=(i+1)*innerResolution+innerResolution-1;
			indices[(i*outerResolution+innerResolution-1)*6+5]=(i+1)*innerResolution+innerResolution;
		}
		// corner of outer circle
		// 0...innerResolution-1
		// (outerResolution-1)*innerResolution...outerResolution*innerResolution-1
		for(int j=0; j<innerResolution-1; j++)
		{
			indices[((outerResolution-1)*innerResolution+j)*6]=j;
			indices[((outerResolution-1)*innerResolution+j)*6+1]=(outerResolution-1)*innerResolution+j+1;
			indices[((outerResolution-1)*innerResolution+j)*6+2]=(outerResolution-1)*innerResolution+j+1;

			indices[((outerResolution-1)*innerResolution+j)*6+3]=j;
			indices[((outerResolution-1)*innerResolution+j)*6+4]=(outerResolution-1)*innerResolution+j;
			indices[((outerResolution-1)*innerResolution+j)*6+5]=(outerResolution-1)*innerResolution+j+1;
		}
		// the corner of inner circle
		
		vertexData.addIndices(indices);

		return vertexData;
	}
}
