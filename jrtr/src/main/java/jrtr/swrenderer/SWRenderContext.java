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

import java.awt.Color;
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
	private float[][] zBuffer;
		
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
			draw2(iterator.next());
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
		zBuffer = new float[colorBuffer.getWidth()][colorBuffer.getHeight()];
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
				for(int i=0; i<pntArray.length/3; i++)
				{
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
			ListIterator<VertexData.VertexElement> itr = vertexElements.listIterator(0);
			while(itr.hasNext())
			{
				VertexData.VertexElement e = itr.next();
				if(e.getSemantic() == VertexData.Semantic.POSITION)
				{
					Vector4f p = new Vector4f(e.getData()[i*3],e.getData()[i*3+1],e.getData()[i*3+2],1);
					t.transform(p);  // transform into 2D homogeneous coordinate
					positions[k][0] = p.x;
					positions[k][1] = p.y;
					positions[k][2] = p.z;
					positions[k][3] = p.w;
					plotPoint(p);
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
	
	private void plotPoint(Vector4f p)
	{
		// projection
		int xNew = (int)(p.x/p.w);
		int yNew = (int)(p.y/p.w);
		// set the color into white
		int rgb = 0xFFFFFFFF;
		if(xNew>=0 && xNew<colorBuffer.getWidth() && yNew>=0 && yNew<colorBuffer.getWidth())
			colorBuffer.setRGB(xNew, yNew, rgb);
	}
	
	private void rasterizeTriangle(float[][]positions, float[][]colors, float[][]normals, float[][]texcoords, RenderItem renderItem)
	{
		// calculate edge function
		Matrix3f edgeFunction = new Matrix3f(
				//x				 y				  w
				positions[0][0], positions[0][1], positions[0][3],
				positions[1][0], positions[1][1], positions[1][3],
				positions[2][0], positions[2][1], positions[2][3]);
		edgeFunction.invert();
		edgeFunction.transpose();
		
		Vector3f arrayW = new Vector3f(1,1,1);

		// calculate the boundary rectangle
		int endX=(int)Math.max((int)Math.max(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]);
		int endY=(int)Math.max((int)Math.max(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]);
		
		int startX=(int)Math.min((int)Math.min(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]);
		int startY=(int)Math.min((int)Math.min(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]);

		for(int x=startX; x<=endX; x++)
			for(int y=startY; y<=endY; y++)
			{
				Vector3f pnt = new Vector3f(x,y,1);
				// calculate alpha beta gamma
				edgeFunction.transform(pnt);
				// calculate z-buffer
				float w_recp = arrayW.dot(pnt);
				// check whether point is in
				if(pnt.x>=0 && pnt.y>=0 && pnt.z>=0)
				{
					// inside
					if(w_recp>zBuffer[x][y])
					{
						zBuffer[x][y] = w_recp;
//						colorCalc();
						int rgb = 0xFFFFFFFF;
						colorBuffer.setRGB(x,y,rgb);
					}
				}
				else
					continue;
			}
	}
	
	private void colorCalc(Vector3f pnt, float w_recp, float [][] colors)
	{
		float r = (colors[0][0]*pnt.x+colors[1][0]*pnt.y+colors[2][0]*pnt.z)/w_recp;
		if(r>1)
			r = 1;
		float g = (colors[0][1]*pnt.x+colors[1][1]*pnt.y+colors[2][1]*pnt.z)/w_recp;
		if(g>1)
			g = 1;
		float b = (colors[0][2]*pnt.x+colors[1][2]*pnt.y+colors[2][2]*pnt.z)/w_recp;
		if(b>1)
			b = 1;
		Color c = new Color(r,g,b);
//		colorBuffer.setRGB(pnt.x, pnt.y, c.getRGB());
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
