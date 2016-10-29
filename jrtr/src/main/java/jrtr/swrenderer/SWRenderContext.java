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
//		camerInv.invert();
		t.mul(camerInv);
		t.mul(obj2World);
//		System.out.println(viewPnt);
//		System.out.println(proj);
//		System.out.println(camerInv);
//		System.out.println(obj2World);
		
		// Skeleton code to assemble triangle data
		int k = 0; // index of triangle vertex, k is 0,1, or 2
		// Loop over all vertex indices
		for(int j=0; j<indices.length; j++)
		{
			int i = indices[j];
			// Loop over all attributes of current vertex
			ListIterator<VertexData.VertexElement> itr = vertexElements.listIterator(0);
			Vector4f p = new Vector4f();
			while(itr.hasNext())
			{
				VertexData.VertexElement e = itr.next();
				if(e.getSemantic() == VertexData.Semantic.POSITION)
				{
					p = new Vector4f(e.getData()[i*3], e.getData()[i*3+1], e.getData()[i*3+2], 1);
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

				pointPlot(p);
				
				// Draw triangle as soon as we collected the data for 3 vertices
				if(k == 3)
				{
					// Draw the triangle with the collected three vertex positions, etc.
					// check whether point is behind eye
					rasterizeTriangle(positions, colors, normals, texcoords, renderItem);
					k = 0;
				}
			}
		}
	}

	private void pointPlot(Vector4f point)
	{
		// projection
		int xNew = (int)(point.x/point.w);
		int yNew = (int)(point.y/point.w);
//		System.out.println(xNew+","+yNew);
		// check the point in canvas or not
		if(xNew>=0 && xNew<colorBuffer.getWidth() && yNew>=0 && yNew<colorBuffer.getHeight())
		{
			// set the color into white
			int rgb = 0xFFFFFFFF;
			colorBuffer.setRGB(xNew, yNew, rgb);
		}
		// System.out.println(p);
	}

	private void rasterizeTriangle(float[][] positions,float[][] colors,float[][] normals,float[][] texcoords,RenderItem renderItem){	
		Matrix3f edgeFunction = new Matrix3f(
				positions[0][0], positions[0][1], positions[0][3],
				positions[1][0], positions[1][1], positions[1][3],
				positions[2][0], positions[2][1], positions[2][3]);

		edgeFunction.invert();
		Vector3f intepW = new Vector3f(1,1,1);
		edgeFunction.transform(intepW);
		edgeFunction.transpose();

		int startX, startY, endX, endY;
		endX = Math.round(Math.max((int)Math.max(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]));
		endY = Math.round(Math.max((int)Math.max(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]));
		startX = Math.round(Math.min((int)Math.min(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]));
		startY = Math.round(Math.min((int)Math.min(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]));
		
		// boundary correction
		if(startX<0)
			startX = 0;
		if(startY<0)
			startY = 0;
		if(endX>=colorBuffer.getWidth())
			endX=colorBuffer.getWidth()-1;
		if(endY>=colorBuffer.getHeight())
			endY=colorBuffer.getHeight()-1;

//		System.out.println(startX+","+endX+","+startY+","+endY);
//		System.out.println(zBuffer.length);
//		System.out.println(zBuffer[0].length);

		for(int x=startX; x<=endX; x++)
		{
			for(int y=startY; y<=endY; y++)
			{
				Vector3f p = new Vector3f(x,y,1);
				float w_recp = intepW.dot(p);
				edgeFunction.transform(p);
				if(p.x>=0 && p.y>=0 && p.z>=0){
					if(w_recp>zBuffer[x][y])
					{
						zBuffer[x][y] = w_recp;
						applyColor(x,y,w_recp,p,colors);
					}
				}
			}
		}
	}

	private void applyColor(int x, int y, float w, Vector3f p, float[][] colors){
		float r, g, b;
		
		r = (colors[0][0]*p.x+colors[1][0]*p.y+colors[2][0]*p.z)/w;
		g = (colors[0][1]*p.x+colors[1][1]*p.y+colors[2][1]*p.z)/w;
		b = (colors[0][2]*p.x+colors[1][2]*p.y+colors[2][2]*p.z)/w;
		
		if(r>=1)
			r = 1;
		if(b>1)
			b = 1;
		if(g>1)
			g = 1;
		Color c = new Color(r,g,b);
		colorBuffer.setRGB(x, y, c.getRGB());
	}

	private void rasterizeTriangle2(float[][]positions, float[][]colors, float[][]normals, float[][]texcoords, RenderItem renderItem)
	{
		// calculate edge function
		Matrix3f edgeFunction = new Matrix3f(
				//x				 y				  w
				positions[0][0], positions[0][1], positions[0][3],
				positions[1][0], positions[1][1], positions[1][3],
				positions[2][0], positions[2][1], positions[2][3]);
		edgeFunction.invert();
		Vector3f intepW = new Vector3f(1,1,1);
		edgeFunction.transform(intepW);
		edgeFunction.transpose();
		System.out.println("edgeFunction:"+edgeFunction);
		
		int endX = Math.round(Math.max((int)Math.max(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]));
		int endY = Math.round(Math.max((int)Math.max(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]));
		int startX = Math.round(Math.min((int)Math.min(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]));
		int startY = Math.round(Math.min((int)Math.min(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]));

		for(int x=startX; x<=endX; x++)
		{
			for(int y=startY; y<=endY; y++)
			{
				Vector3f pnt = new Vector3f(x,y,1);
				// calculate alpha beta gamma
				edgeFunction.transform(pnt);
				if(x==startX || y==startY)
					System.out.println("("+x+","+y+")"+"edgeFunction:"+pnt);
				// calculate z-buffer
				float w_recp = 0;//???????????
				// check whether point is in
				if(pnt.x>=0 && pnt.y>=0 && pnt.z>=0)
				{
					// inside
					if(w_recp>zBuffer[x][y])
					{
						zBuffer[x][y] = w_recp;
						int rgb = 0xFFFFFFFF;
						colorBuffer.setRGB(x, colorBuffer.getHeight() - y - 1, rgb);
					}
				}
			}
		}
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
