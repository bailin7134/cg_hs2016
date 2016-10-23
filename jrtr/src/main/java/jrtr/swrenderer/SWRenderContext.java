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
	private Matrix4f camerInv;		// inverse camera matrix
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
