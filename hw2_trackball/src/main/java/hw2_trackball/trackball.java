package hw2_trackball;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;

import java.awt.Point;
import java.awt.event.*;
import java.io.IOException;

import javax.vecmath.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class trackball
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static ObjReader teapotReader;
	static Shape teapot;
	static float currentstep, basicstep;
	static boolean mouseIsPressed;
	static float v1PosX, v1PosY, v1PosZ, v2PosX, v2PosY, v2PosZ;
	static Vector3f pressPos;
	static Point v1PrePoint, v2PrePoint;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
	public final static class SimpleRenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;

			VertexData vertexData;
			teapotReader = new ObjReader();
			try {
				vertexData = teapotReader.read("..\\..\\..\\..\\..\\obj\\teapot.obj", 1.0f, renderContext);
			} catch (IOException e) {
				vertexData = createCube();
				System.out.print("Failed to read teapot!");
				System.out.print(e.getMessage());
			}


			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			teapot = new Shape(vertexData);
			sceneManager.addShape(teapot);

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);

			// Load some more shaders
			normalShader = renderContext.makeShader();
			try {
				normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
			} catch(Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			diffuseShader = renderContext.makeShader();
			try {
				diffuseShader.load("../jrtr/shaders/diffuse.vert", "../jrtr/shaders/diffuse.frag");
			} catch(Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			// Make a material that can be used for shading
			material = new Material();
			material.shader = diffuseShader;
			material.diffuseMap = renderContext.makeTexture();
			try {
				material.diffuseMap.load("../textures/plant.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}

			// Register a timer task
			Timer timer = new Timer();
			basicstep = 0.01f;
			currentstep = basicstep;
			timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		}

		private VertexData createCube(){
			// Make a simple geometric object: a cube

			// The vertex positions of the cube
			float v[] = {-1,-1,1, 1,-1,1, 1,1,1, -1,1,1,		// front face
					-1,-1,-1, -1,-1,1, -1,1,1, -1,1,-1,	// left face
					1,-1,-1,-1,-1,-1, -1,1,-1, 1,1,-1,		// back face
					1,-1,1, 1,-1,-1, 1,1,-1, 1,1,1,		// right face
					1,1,1, 1,1,-1, -1,1,-1, -1,1,1,		// top face
					-1,-1,1, -1,-1,-1, 1,-1,-1, 1,-1,1};	// bottom face

			// The vertex normals 
			float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
					-1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
					0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
					1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
					0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
					0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face

			// The vertex colors
			float c[] = {1,0,0, 1,0,0, 1,0,0, 1,0,0,
					0,1,0, 0,1,0, 0,1,0, 0,1,0,
					1,0,0, 1,0,0, 1,0,0, 1,0,0,
					0,1,0, 0,1,0, 0,1,0, 0,1,0,
					0,0,1, 0,0,1, 0,0,1, 0,0,1,
					0,0,1, 0,0,1, 0,0,1, 0,0,1};

			// Texture coordinates 
			float uv[] = {0,0, 1,0, 1,1, 0,1,
					0,0, 1,0, 1,1, 0,1,
					0,0, 1,0, 1,1, 0,1,
					0,0, 1,0, 1,1, 0,1,
					0,0, 1,0, 1,1, 0,1,
					0,0, 1,0, 1,1, 0,1};

			// Construct a data structure that stores the vertices, their
			// attributes, and the triangle mesh connectivity
			VertexData vertexData = renderContext.makeVertexData(24);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
			vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
			vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);

			// The triangles (three vertex indices for each triangle)
			int indices[] = {0,2,3, 0,1,2,			// front face
					4,6,7, 4,5,6,			// left face
					8,10,11, 8,9,10,		// back face
					12,14,15, 12,13,14,	// right face
					16,18,19, 16,17,18,	// top face
					20,22,23, 20,21,22};	// bottom face

			vertexData.addIndices(indices);
			return vertexData;
		}
	}


	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask
	{
		public void run()
		{
			Vector3f v1 = new Vector3f(), v2 = new Vector3f();
			Point v1Point, v2Point;
			
			// store the vector v1, when mouse is pressed
			if(mouseIsPressed)
			{
				v1Point = renderPanel.getCanvas().getMousePosition();
				if(v1Point == null)
				{
					v1Point = v1PrePoint;
					System.out.println("v1Point"+v1Point.getX()+","+v1Point.getY());
					System.out.println("v1PrePoint"+v1PrePoint.getX()+","+v1PrePoint.getY());
					System.out.println("null point get");
				}
				else
					v1PrePoint = v1Point;
				
				v1 = prtOnSphere(v1Point);
				/*
	    		v1PosZ = 1-v1PosX*v1PosX-v1PosY*v1PosY;
	    		if(v1PosZ <= 0)
	    			v1PosZ = 0;
	    		v1PosZ = (float)Math.sqrt((double)v1PosZ);
	    		v1 = new Vector3f(v1PosX, v1PosY, v1PosZ);
	    		v1.normalize();
				*/
				try {
				    // wait until the mouse moves
				    Thread.sleep(100);
				} catch ( java.lang.InterruptedException ie) {
				    System.out.println(ie);
				}
			}
			
			// check the vector v2
			if(mouseIsPressed)
			{
				v2Point = renderPanel.getCanvas().getMousePosition();
				if(v2Point == null)
				{
					v2Point = v2PrePoint;
					System.out.println("v2Point"+v2Point.getX()+","+v2Point.getY());
					System.out.println("v2PrePoint"+v2PrePoint.getX()+","+v2PrePoint.getY());
					System.out.println("null point get");
				}
				else
					v2PrePoint = v2Point;
				
				v2 = prtOnSphere(v2Point);
				/*
				Point point = renderPanel.getCanvas().getMousePosition();
				float v2PosX = (float)point.getX();
				float v2PosY = (float)point.getY();
				float v2PosZ = 1-v2PosX*v2PosX-v2PosY*v2PosY;
	    		if(v2PosZ <= 0)
	    			v2PosZ = 0;
	    		v2PosZ = (float)Math.sqrt((double)v2PosZ);
	    		v2 = new Vector3f(v2PosX, v2PosY, v2PosZ);
	    		v2.normalize();
	    		System.out.println("mouseDragged"+v2PosX+","+v2PosY);
	    		*/
			}
			
			// rotation between the points
			if(!v1.equals(v2))
			{
				Vector3f axis = new Vector3f(), rt = new Vector3f();;
				double theta;
				Quat4f delta = new Quat4f(), q = new Quat4f();
				Matrix3f r = new Matrix3f();

				System.out.println("axis v1PrePoint"+v1PrePoint.getX()+","+v1PrePoint.getY());
				System.out.println("axis v2PrePoint"+v2PrePoint.getX()+","+v2PrePoint.getY());
				axis.cross(v1, v2);
				System.out.println("axis"+axis.x+","+axis.y+","+axis.z);
				theta = v1.angle(v2);
				delta.set(new AxisAngle4f(axis.x, axis.y, axis.z, (float)theta));
				
				// get shape transformation
				Matrix4f t = new Matrix4f(teapot.getTransformation());
				t.get(rt);
				t.get(r);
				q.set(r);
				delta.mul(q);
				t.set(delta,rt,1);
				teapot.setTransformation(t);
			}
			renderPanel.getCanvas().repaint();
		}
		
		////////////
		//need adaptation !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		private Vector3f prtOnSphere(Point point)
		{
			int height = renderPanel.getCanvas().getHeight();
			int width = renderPanel.getCanvas().getWidth();
			double r = (double) Math.min(height, width);

			double x = point.getX()- width/2;
			double y = height/2 - point.getY();

			x = x*2/r;
			y = y*2/r;

			if(Math.abs(x) > 1 || Math.abs(y)>1)
				return null;

			double det = 1 - x*x - y*y;

			if(det <= 0)
				return null;

			double z = Math.sqrt(det);

			Vector3f v = new Vector3f((float) x, (float) y, (float) z);
			v.normalize();
			return v;
			
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class SimpleMouseListener implements MouseListener
	{
    	public void mousePressed(MouseEvent e) {
    		mouseIsPressed = true;
    		v1PosX = e.getX();
    		v1PosY = e.getY();
    		System.out.println("mousePressed"+v1PosX+","+v1PosY);
    	}
    	public void mouseReleased(MouseEvent e) {
    		mouseIsPressed = false;
    	}
    	public void mouseEntered(MouseEvent e) {
    		/*
    		v1PosX = e.getX();
    		v1PosY = e.getY();
    		System.out.println("mouseEntered"+v1PosX+","+v1PosY);
    		*/
    	}
    	public void mouseExited(MouseEvent e) {
    		/*
    		v1PosX = e.getX();
    		v1PosY = e.getY();
    		System.out.println("mouseExited"+v1PosX+","+v1PosY);
    		*/
    	}
    	public void mouseClicked(MouseEvent e) {
    		/*
    		v1PosX = e.getX();
    		v1PosY = e.getY();
    		System.out.println(v1PosY);
    		*/
    	}
	}
	
	/**
	 * A key listener for the main window. Use this to process key events.
	 * Currently this provides the following controls:
	 * 's': stop animation
	 * 'p': play animation
	 * '+': accelerate rotation
	 * '-': slow down rotation
	 * 'd': default shader
	 * 'n': shader using surface normals
	 * 'm': use a material for shading
	 */
	public static class SimpleKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyChar())
			{
				case 's': {
					// Stop animation
					currentstep = 0;
					break;
				}
				case 'p': {
					// Resume animation
					currentstep = basicstep;
					break;
				}
				case '+': {
					// Accelerate roation
					currentstep += basicstep;
					break;
				}
				case '-': {
					// Slow down rotation
					currentstep -= basicstep;
					break;
				}
				case 'n': {
					// Remove material from shape, and set "normal" shader
					teapot.setMaterial(null);
					renderContext.useShader(normalShader);
					break;
				}
				case 'd': {
					// Remove material from shape, and set "default" shader
					teapot.setMaterial(null);
					renderContext.useDefaultShader();
					break;
				}
				case 'm': {
					// Set a material for more complex shading of the shape
					if(teapot.getMaterial() == null) {
						teapot.setMaterial(material);
					} else
					{
						teapot.setMaterial(null);
						renderContext.useDefaultShader();
					}
					break;
				}
				
				/*
				 *  add parameters for Frustum and Camera
				 *  image 1
				 *  F: aspect ration 1, 
				 *     vertical field of view 60, 
				 *     near far clip plans 1,100
				 *  C: center of projection 0,0,40
				 *     look at point 0,0,0
				 *     up vector 0,1,0
				 */
				case 'h': {
					sceneManager.getFrustum().setNear(1);
					sceneManager.getFrustum().setFar(100);
					sceneManager.getFrustum().setAspect(1);
					sceneManager.getFrustum().getFovGrad(60);
					sceneManager.getCamera().setCenterOfProjection(new Vector3f(0f,0f,40f));
					sceneManager.getCamera().setLookAtPoint(new Vector3f(0f,0f,0f));
					sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
					break;
				}
				
				/*
				 *  add parameters for Frustum and Camera
				 *  image 2
				 *  F: aspect ration 1, 
				 *     vertical field of view 60, 
				 *     near far clip plans 1,100
				 *  C: center of projection --10,40,40
				 *     look at point -5,0,0
				 *     up vector 0,1,0
				 */
				case 'v': {
					sceneManager.getFrustum().setNear(1);
					sceneManager.getFrustum().setFar(100);
					sceneManager.getFrustum().setAspect(1);
					sceneManager.getFrustum().getFovGrad(60);
					sceneManager.getCamera().setCenterOfProjection(new Vector3f(-10f,40f,40f));
					sceneManager.getCamera().setLookAtPoint(new Vector3f(-5f,0f,0f));
					sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
					break;
				}
			}
			
			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		
		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
        {
        }

	}
	
	/**
	 * The main function opens a 3D rendering window, implemented by the class
	 * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called backed 
	 * for initialization automatically. It then constructs a simple 3D scene, 
	 * and starts a timer task to generate an animation.
	 */
	public static void main(String[] args)
	{		
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new SimpleRenderPanel();
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new SimpleMouseListener());
	    renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}

}
