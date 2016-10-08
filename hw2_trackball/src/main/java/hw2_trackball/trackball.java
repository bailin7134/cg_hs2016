package hw2_trackball;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
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
			// read vertex of teapot from folder obj
			teapotReader = new ObjReader();
			try {
				VertexData vertexData = teapotReader.read("/home/lbai/Desktop/CG_UniBE/cg_hs2016/obj/teapot.obj", 1f, renderContext);
			} catch(Exception e) {
				System.out.print("Cannot read teapot");
		    	System.out.print(e.getMessage());
			}
			
			// write vertex into scene manger
								
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			teapot = new Shape(vertexData);
			sceneManager.addShape(teapot);

			renderContext = r;
			
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
	}

	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask
	{
		public void run()
		{
    		// Trigger redrawing of the render window
    		renderPanel.getCanvas().repaint(); 
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class SimpleMouseListener implements MouseListener
	{
    	public void mousePressed(MouseEvent e) {}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
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
