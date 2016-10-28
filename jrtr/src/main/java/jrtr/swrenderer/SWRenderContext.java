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
		if(xNew>=0 && xNew<colorBuffer.getWidth() && yNew>=0 && yNew<colorBuffer.getWidth())
		{
			// set the color into white
			int rgb = 0xFFFFFFFF;
			colorBuffer.setRGB(xNew, yNew, rgb);
		}
		// System.out.println(p);
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
		System.out.println("edgeFunction:"+edgeFunction);
		
		Matrix3f edgF = new Matrix3f(	positions[0][0], positions[0][1], positions[0][3],
				positions[1][0], positions[1][1], positions[1][3],
				positions[2][0], positions[2][1], positions[2][3]
				);
		edgF.invert();
		Vector3f ax = new Vector3f(texcoords[0][0],texcoords[1][0],texcoords[2][0]);
		edgF.transform(ax);
		Vector3f ay = new Vector3f(texcoords[0][1],texcoords[1][1],texcoords[2][1]);
		edgF.transform(ay);
		Vector3f a1 = new Vector3f(1,1,1);
		edgF.transform(a1);
		edgF.transpose();
		System.out.println("edgF:"+edgF);
//		System.out.println("ax:"+ax);
//		System.out.println("ay:"+ay);
//		System.out.println("a1:"+a1);
		
		
//		int startX = 0, int  = 0, startY = 0, endY = 0;
//		boundRect(positions, startX, endX, startY, endY);
		int endX=(int)Math.max((int)Math.max(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]);
		int endY=(int)Math.max((int)Math.max(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]);
		
		int startX=(int)Math.min((int)Math.min(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]);
		int startY=(int)Math.min((int)Math.min(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]);

		
		Vector3f arrayW = new Vector3f(1/positions[0][3],1/positions[1][3],1/positions[2][3]);
		

		Matrix3f alphabetagamma = new Matrix3f();
		 //start with smallest "possible" bounding rectangle
		Point4f topLeft = new Point4f(500 - 1, 500 - 1,0,0);
		Point4f botRight = new Point4f(0, 0,0,0);
		float[] oneOverWarray = new float[3];
		for (int i = 0; i < 3; i++) {
			int x = (int) (positions[i][0]/positions[i][3]);
			int y = (int) (positions[i][1]/positions[i][3]);
			topLeft.x = (int) Math.min(topLeft.x, x);
			topLeft.y = (int) Math.min(topLeft.y, y);
			botRight.x = (int) Math.max(botRight.x, x);
			botRight.y = (int) Math.max(botRight.y, y);
			
			float[] row = { positions[i][0], positions[i][1], positions[i][3] };
			alphabetagamma.setRow(i, row);
			oneOverWarray[i] = 1/positions[i][3];
		}
//		System.out.println("topLeft:"+topLeft+",botRight:"+botRight);
		alphabetagamma.invert();
//		System.out.println("alphabetagamma:"+alphabetagamma);
		

		topLeft.x = Math.max(0, topLeft.x);
		topLeft.y = Math.max(0, topLeft.y);
		botRight.x = Math.min(500 - 1, botRight.x);
		botRight.y = Math.min(500 - 1, botRight.y);
		//if the area is 0, make it not go trough the draw step at all
		if (topLeft.x == botRight.x && topLeft.y == botRight.y) {
			botRight.x = topLeft.x - 1;
			botRight.y = topLeft.y - 1;
		}
		
		// points
//		System.out.println("point 1:"+positions[0][0]/positions[0][3]+","+positions[0][1]/positions[0][3]);
//		System.out.println("point 2:"+positions[1][0]/positions[1][3]+","+positions[1][1]/positions[1][3]);
//		System.out.println("point 3:"+positions[2][0]/positions[2][3]+","+positions[2][1]/positions[2][3]);
//
//
//		Point3f pnt1 = new Point3f(280.1051f,193.78441f, 1);
//		Point3f pnt2 = new Point3f(203.89488f,266.2156f, 1);
//		Point3f pnt3 = new Point3f(280.1051f,266.2156f, 1);
//		edgeFunction.transform(pnt1);
//		edgeFunction.transform(pnt2);
//		edgeFunction.transform(pnt3);
//		System.out.println("edgeFunction 1:"+pnt1);
//		System.out.println("edgeFunction 2:"+pnt2);
//		System.out.println("edgeFunction 3:"+pnt3);

		
		int[] boundry;
		if(positions[0][3]<0 && positions[1][3]<0 && positions[2][3]<0)
			boundry = rect(positions);
		else
			boundry = new int[] {0, colorBuffer.getWidth(), 0, colorBuffer.getHeight()};
		
		for(int x=startX; x<=endX; x++)
		{
			for(int y=startY; y<=endY; y++)
//		for(int x=0; x<colorBuffer.getWidth(); x++)
//			for(int y=0; y<colorBuffer.getHeight(); y++)				
			{
				Vector3f vertexWeights = getVertexWeights(x, y, alphabetagamma);
				if(x==279 && y==240)
					System.out.println("("+x+","+y+")"+"vertexWeights:"+vertexWeights);
				
				Vector3f pnt = new Vector3f(x,y,1);
				// calculate alpha beta gamma
				edgeFunction.transform(pnt);
				System.out.println("edgeFunction.transform"+pnt);
				if(x==279 && y==240)
					System.out.println("("+x+","+y+")"+"edgeFunction:"+pnt);
				// calculate z-buffer
				float w_recp = arrayW.dot(pnt);
				// check whether point is in
				if(pnt.x>=0 && pnt.y>=0 && pnt.z>=0)
				{
					// inside
					if(w_recp>zBuffer[x][y])
					{
						zBuffer[x][y] = w_recp;

//						int c;
//						if (renderItem.getShape().getMaterial() != null && renderItem.getShape().getMaterial().getTexture() != null)
//							c = interpolateColorFromTexture(vertexWeights,texCoords, material.getTexture());
//						else 
//							c = interpolateColor(vertexWeights, colors).getRGB();
						
						int rgb = 0xFFFFFFFF;
//						colorBuffer.setRGB(x,y,rgb);
						colorBuffer.setRGB(x, colorBuffer.getHeight() - y - 1, rgb);
					}
				}
			}
		}

		for(int x = boundry[0]; x<boundry[1]; x++){
			for(int y = boundry[2]; y<boundry[3]; y++){
				Vector3f p = new Vector3f(x,y,1);
				float w = a1.dot(p);	// actually 1/w
				edgF.transform(p);
				System.out.println("edgF.transform"+p);
				if(p.x<0 || p.y<0 || p.z<0)
					continue;
				if(w>zBuffer[x][y]) {
					zBuffer[x][y] = w;
					if(renderItem.getShape().getMaterial() == null)
						;
//						useColors(x,y,w,p,colors);
//					else
//						useTexture(x,y,w,ax,ay, (SWTexture) renderItem.getShape().getMaterial().diffuseMap);
				}
			}
		}
	}

	private int[] rect(float[][] positions){
		float[][] pos = {{positions[0][0]/positions[0][3], positions[1][0]/positions[1][3], positions[2][0]/positions[2][3]},
				{positions[0][1]/positions[0][3], positions[1][1]/positions[1][3], positions[2][1]/positions[2][3]},
		};
		int xmin = Math.round(Math.min(Math.min(pos[0][0], pos[0][1]), pos[0][2]));
		int xmax = Math.round(Math.max(Math.max(pos[0][0], pos[0][1]), pos[0][2]));
		int ymin = Math.round(Math.min(Math.min(pos[1][0], pos[1][1]), pos[1][2]));
		int ymax = Math.round(Math.max(Math.max(pos[1][0], pos[1][1]), pos[1][2]));

		if(xmin < 0)
			xmin = 0;
		if(ymin < 0)
			ymin = 0;
		if(xmin > colorBuffer.getWidth())
			xmin = colorBuffer.getWidth();
		if(ymin > colorBuffer.getHeight())
			ymin = colorBuffer.getHeight();

		return new int[] {xmin, xmax, ymin, ymax};
	}
	
	private Vector3f getVertexWeights(int x, int y, Matrix3f alphabetagamma) {
		Vector3f abgVector = new Vector3f();
		float[] coeffs = new float[3];
		for (int i = 0; i < 3; i++) {
			alphabetagamma.getColumn(i, abgVector);
			float coeff = abgVector.dot(new Vector3f(x, y, 1));
			if (coeff < 0)
				return null;
			else coeffs[i] = coeff;
		}
		return new Vector3f(coeffs);
	}
	
	private void boundRect(float[][] positions, int startX, int endX, int startY, int endY)
	{
		// calculate the boundary rectangle
		endX=(int)Math.max((int)Math.max(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]);
		endY=(int)Math.max((int)Math.max(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]);
		
		startX=(int)Math.min((int)Math.min(positions[0][0]/positions[0][3], positions[1][0]/positions[1][3]), positions[2][0]/positions[2][3]);
		startY=(int)Math.min((int)Math.min(positions[0][1]/positions[0][3], positions[1][1]/positions[1][3]), positions[2][1]/positions[2][3]);
//		System.out.println("startX:"+startX+", endX:"+endX+", startY:"+startY+", endY:"+endY);
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
