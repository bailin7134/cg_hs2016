package homework1;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

public class torus extends Shape
{
	public torus(RenderContext renderContext, float outerRadius, float innerRadius)
	{
		super(torusVertexData(renderContext, outerRadius, innerRadius));
	}
	
	private static VertexData torusVertexData(RenderContext renderContext, float outerRadius, float innerRadius)
	{

		float[] c = new float[1024];
		float[] v = new float[1024];
		VertexData vertexData = renderContext.makeVertexData(v.length/3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);

		int[] indices = new int[1024];
		vertexData.addIndices(indices);

		return vertexData;
	}
}
