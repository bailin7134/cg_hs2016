package jrtr.swrenderer;

import jrtr.RenderContext;
import jrtr.RenderItem;
import jrtr.SceneManagerInterface;
import jrtr.SceneManagerIterator;
import jrtr.Shader;
import jrtr.Texture;
import jrtr.VertexData;
import jrtr.VertexData.VertexElement;
import jrtr.glrenderer.GLRenderPanel;

import java.awt.image.*;
import java.util.LinkedList;

import javax.vecmath.*;
import java.util.ListIterator;

/**
 * A skeleton for a software renderer. It works in combination with
 * {@link SWRenderPanel}, which displays the output image. In project 3 
 * you will implement your own rasterizer in this class.
 * <p>
 * To use the software renderer, you will simply replace {@link GLRenderPanel} 
 * with {@link SWRenderPanel} in the user application.
 */
public class SWRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private BufferedImage colorBuffer;
	private Matrix4f obj2World;		// object to world matrix
	private Matrix4f camerInv;		// inverse camera matrix, invert later
	private Matrix4f viewPnt;		// viewpoint matrix
	private Matrix4f proj;			// projection matrix
		
	public void setSceneManager(SceneManagerInterface sceneManager)
	{
		this.sceneManager = sceneManager;
	}
	
	/**
	 * This is called by the SWRenderPanel to render the scene to the 
	 * software frame buffer.
	 */
	public void display()
	{
		if(sceneManager == null) return;
		
		beginFrame();
	
		SceneManagerIterator iterator = sceneManager.iterator();	
		while(iterator.hasNext())
		{
			draw(iterator.next());
		}		
		
		endFrame();
	}

	/**
	 * This is called by the {@link SWJPanel} to obtain the color buffer that
	 * will be displayed.
	 */
	public BufferedImage getColorBuffer()
	{
		return colorBuffer;
	}
	
	/**
	 * Set a new viewport size. The render context will also need to store
	 * a viewport matrix, which you need to reset here. 
	 */
	public void setViewportSize(int width, int height)
	{
		colorBuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		// viewport matrix from project
		// x1-x0 = width, y1-y0 = height
		viewPnt = new Matrix4f(width/2,0,0,width/2,  0,-height/2,0,height/2,  0,0,0.5f,0.5f,  0,0,0,1);
	}
		
	/**
	 * Clear the framebuffer here.
	 */
	private void beginFrame()
	{
		camerInv = new Matrix4f(sceneManager.getCamera().getCameraMatrix());
		proj = new Matrix4f(sceneManager.getFrustum().getProjectionMatrix());
		colorBuffer = new BufferedImage(colorBuffer.getWidth(), colorBuffer.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
	}
	
	private void endFrame()
	{		
	}
	
	/**
	 * The main rendering method. You will need to implement this to draw
	 * 3D objects.
	 */
	private void draw(RenderItem renderItem)
	{
		// get vertex data
		VertexData vertexData = renderItem.getShape().getVertexData();
		// get indices from vertex
		int indices[] = vertexData.getIndices();
		LinkedList<VertexElement> vertexElements = vertexData.getElements();
		
		// p' transformation
		// 1. get object to world matrix
		obj2World = renderItem.getShape().getTransformation();
		// p'=DPC^{-1}Mp
		// trans = DPC^{-1}M
		// M.mul(N) = M*N;
		Matrix4f trans = new Matrix4f(viewPnt);
		trans.mul(proj);
		trans.mul(camerInv);
		trans.mul(obj2World);
	
		// get the information of POSITION, COLOR, NORMAL and TEXTCOORD
		for(int num=0; num<vertexElements.size(); num++)
		{
			switch (vertexElements.get(num).getSemantic()){
			case POSITION:
				float[] pntArray = vertexElements.get(num).getData();
				for(int i=0; i<pntArray.length/3; i++){
					Vector4f pnt = new Vector4f(pntArray[i*3], pntArray[i*3+1], pntArray[i*3+2], 1);
					// transform to p'
					trans.transform(pnt);
					// projection
					int xNew = (int)(pnt.x/pnt.w);
					int yNew = (int)(pnt.y/pnt.w);
					// check the point in canvas or not
					if(xNew<0 || yNew<0 || xNew>=colorBuffer.getWidth() || yNew>=colorBuffer.getWidth())
						continue;
					// set the color into white
					int rgb = 0xFFFFFFFF;
					colorBuffer.setRGB(xNew, yNew, rgb);
				}
				break;
			case COLOR:
				break;
			case NORMAL:
				break;
			case TEXCOORD:
				break;
			default:
				break;
			}
		}
	}
	
	private void draw2(RenderItem renderItem)
	{
		// Variable declarations
		VertexData vertexData = renderItem.getShape().getVertexData();
		LinkedList<VertexData.VertexElement> vertexElements = vertexData.getElements();
		int indices[] = vertexData.getIndices();
		float[][] positions = new float[3][4];
		float[][] colors = new float[3][3];
		float[][] normals = new float[3][3];
		float[][] texcoords = new float[3][2];
		Matrix4f t = new Matrix4f(viewPnt);
		
		// p' transformation
		// 1. get object to world matrix
		obj2World = renderItem.getShape().getTransformation();
		// p'=DPC^{-1}Mp
		// trans = DPC^{-1}M
		// M.mul(N) = M*N;
		t.mul(proj);
		camerInv.invert();
		t.mul(camerInv);
		t.mul(obj2World);
		
		// Skeleton code to assemble triangle data
		int k = 0; // index of triangle vertex, k is 0,1, or 2
		// Loop over all vertex indices
		for(int j=0; j<indices.length; j++)
		{
			int i = indices[j];
			// Loop over all attributes of current vertex
			ListIterator<VertexData.VertexElement> itr =
			vertexElements.listIterator(0);
			while(itr.hasNext())
			{
				VertexData.VertexElement e = itr.next();
				if(e.getSemantic() == VertexData.Semantic.POSITION)
				{
					Vector4f p = new Vector4f
					(e.getData()[i*3],e.getData()[i*3+1],e.getData()[i*3+2],1);
					t.transform(p);  // transform into 2D homogeneous coordinate
					positions[k][0] = p.x;
					positions[k][1] = p.y;
					positions[k][2] = p.z;
					positions[k][3] = p.w;
					k++;
				}
				else if(e.getSemantic() == VertexData.Semantic.COLOR)
				{
					// you need to collect other vertex attributes (colors, normals) too
					colors[k][0] = e.getData()[i*3];
					colors[k][1] = e.getData()[i*3+1];
					colors[k][2] = e.getData()[i*3+2];
				}
				else if(e.getSemantic() == VertexData.Semantic.NORMAL)
				{
					normals[k][0] = e.getData()[i*3];
					normals[k][1] = e.getData()[i*3+1];
					normals[k][2] = e.getData()[i*3+2];
				}
				else if(e.getSemantic() == VertexData.Semantic.TEXCOORD)
				{
					texcoords[k][0] = e.getData()[i*2];
					texcoords[k][1] = e.getData()[i*2+1];
				}
				
				// Draw triangle as soon as we collected the data for 3 vertices
				if(k == 3)
				{
					// Draw the triangle with the collected three vertex positions, etc.
					rasterizeTriangle(positions, colors, normals, texcoords, renderItem);
					k = 0;
				}
			}
		}
	}
	
	private void rasterizeTriangle(float[][]positions, float[][]colors, float[][]normals, float[][]texcoords, RenderItem renderItem)
	{
		if (!(positions[0][3]<0 && positions[1][3]<0 && positions[2][3]<0))
			System.out.println("Point is out of scope!");
		
		// calculate edge function
		Matrix3f edgeFunction = new Matrix3f(
				//x				 y				  w
				positions[0][0], positions[0][1], positions[0][3],
				positions[1][0], positions[1][1], positions[1][3],
				positions[2][0], positions[2][1], positions[2][3]);
		edgeFunction.invert();
		
		// times given matrix coordinate
		Vector3f vecX = new Vector3f(texcoords[0][0],texcoords[1][0],texcoords[2][0]);
		Vector3f vecY = new Vector3f(texcoords[0][1],texcoords[1][1],texcoords[2][1]);
		Vector3f vec1 = new Vector3f(1,1,1);
		edgeFunction.transform(vecX);
		edgeFunction.transform(vecY);
		edgeFunction.transform(vec1);
		
		System.out.println(edgeFunction);
		/*
		// check the point is visible and behind eye or not
		int[] boundry;
		if(positions[0][3]<0 && positions[1][3]<0 && positions[2][3]<0)
			boundry = rect(positions);
		else
			boundry = new int[] {0, colorBuffer.getWidth(), 0, colorBuffer.getHeight()};

		for(int x = boundry[0]; x<boundry[1]; x++){
			for(int y = boundry[2]; y<boundry[3]; y++){
				Vector3f p = new Vector3f(x,y,1);
				float w = vec1.dot(p);	// actually 1/w
				edgeFunction.transform(p);
				if(p.x<0 || p.y<0 || p.z<0)
					continue;
				if(w>zBuffer[x][y]) {
					zBuffer[x][y] = w;
					if(renderItem.getShape().getMaterial() == null)
						useColors(x,y,w,p,colors);
					else
						useTexture(x,y,w,vecX,vecY, (SWTexture) renderItem.getShape().getMaterial().diffuseMap);
				}
			}
		}
		*/
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public Shader makeShader()	
	{
		return new SWShader();
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useShader(Shader s)
	{
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useDefaultShader()
	{
	}

	/**
	 * Does nothing. We will not implement textures for the software renderer.
	 */
	public Texture makeTexture()
	{
		return new SWTexture();
	}
	
	public VertexData makeVertexData(int n)
	{
		return new SWVertexData(n);		
	}
}
