package hw2_landscape;

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
public class fracLand
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
	static ObjReader objReader;
	static Shape airplane, landscape;
	static boolean mouseActive;
	static boolean forwards;
	static boolean backwards;
	static boolean yawP;
	static boolean yawN;
	static boolean rollP;
	static boolean rollN;
	static boolean pitchP;
	static boolean pitchN;

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


			VertexData vertexData = createLandscape(1, 1, renderContext);
			//VertexData vertexData = createCube();
			VertexData vertexDataAirplane;
			objReader = new ObjReader();
			
			teapotReader = new ObjReader();
			try {
				vertexDataAirplane = objReader.read("../../../../../obj/airplane.obj", 1.0f, renderContext);
			} catch (IOException e) {
				vertexDataAirplane = defaultShape();
				System.out.print("Failed to read teapot!");
				System.out.print(e.getMessage());
			}


			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			airplane = new Shape(vertexDataAirplane);
			landscape = new Shape(vertexData);
//			sceneManager.addShape(airplane);
			sceneManager.addShape(landscape);

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
		
		private float rand()
		{
			//this function generate value range from -50 o +50
			float min = 0;
			float max = 10;
			float range = max-min;
			return (float)Math.random()*range+min;
		}

		private VertexData createLandscape(int iter, int imgSize, RenderContext renderContext){
			// define the 2D map size is 2
//			int sideLenIter = 2;
			int sideLen = (int) Math.pow(2, imgSize)+1;
			int prtNum = (int) Math.pow(2, iter)+1;
			int midLen = (sideLen-1)/2;
			float[][] mapHeight = new float[sideLen][sideLen];
			float midAvg;
			float smoothValue = (float)Math.pow(2, -1);
			
			// step 1 : initialize four cornor's value (height)
			mapHeight[0][0] = 8;
			mapHeight[0][sideLen-1] = 7;
			mapHeight[sideLen-1][0] = 1;
			mapHeight[sideLen-1][sideLen-1] = 4;
			// diamond
			mapHeight[midLen][midLen] = (mapHeight[0][0]+mapHeight[0][sideLen-1]+mapHeight[sideLen-1][0]+mapHeight[sideLen-1][sideLen-1])/4;
			
			// step 2: assign height to map mesh according to diamond and square
			for(int iterVar=0; iterVar<iter; iterVar++){
				int subMapLen = (sideLen-1)>>iterVar;
				int subMapMid = subMapLen>>1;
				int subMapNum = 1<<iterVar;
				int x,y;

				/*
				 * square
				 * find the middle point of each square
				*/
				for(int i=0; i<subMapNum; i++){
					for(int j=0; j<subMapNum; j++){
						// average height of fours corner points of this square
						x=subMapMid+i*subMapLen;
						y=subMapMid+j*subMapLen;
						midAvg = (mapHeight[x-subMapMid][y-subMapMid] + mapHeight[x-subMapMid][y+subMapMid] + mapHeight[x+subMapMid][y+subMapMid] + mapHeight[x+subMapMid][y-subMapMid])/4;
						System.out.println("square: "+mapHeight[x-subMapMid][y-subMapMid]+","+ mapHeight[x-subMapMid][y+subMapMid]+","+ mapHeight[x+subMapMid][y+subMapMid]+","+ mapHeight[x+subMapMid][y-subMapMid]);
						mapHeight[x][y] = midAvg;// + rand()*smoothValue;
					}
				}
				System.out.println("square: "+mapHeight[midLen][midLen]);

				/*
				 * diamond
				 * there are four points for each square, top, bottom, right left
				 * if the same point is calculated twice, the former value will be
				 * over-written,
				 * the points on the board of map have only three points to
				 * calculate the average height;
				*/

				for(int i=0; i<subMapNum; i++){
					for(int j=0; j<subMapNum; j++){
						if(i==0){
							midAvg = (mapHeight[0][j*subMapLen]+mapHeight[subMapMid][j*subMapLen+subMapMid]+mapHeight[0][(j+1)*subMapLen])/3;
							mapHeight[0][j*subMapLen + subMapMid]=midAvg;// +rand()*smoothValue;
						}
						else{
							midAvg = (mapHeight[i*subMapLen][j*subMapLen]+mapHeight[i*subMapLen+subMapMid][j*subMapLen+subMapMid]+mapHeight[i*subMapLen][(j+1)*subMapLen] + mapHeight[i*subMapLen-subMapMid][j*subMapLen+subMapMid])/4;
							mapHeight[i*subMapLen][j*subMapLen + subMapMid] = midAvg;// +rand()*smoothValue;
						}
						if(i==subMapNum-1){
							midAvg = (mapHeight[(i+1)*subMapLen][j*subMapLen]+mapHeight[i*subMapLen+subMapMid][j*subMapLen+subMapMid] + mapHeight[(i+1)*subMapLen][(j+1)*subMapLen])/3;
							mapHeight[(i+1)*subMapLen][j*subMapLen + subMapMid] = midAvg;// +rand()*smoothValue;
						}
						else{
							midAvg = (mapHeight[(i+1)*subMapLen][j*subMapLen]+mapHeight[i*subMapLen+subMapMid][j*subMapLen+subMapMid]+mapHeight[(i+1)*subMapLen][(j+1)*subMapLen]+mapHeight[(i+1)*subMapLen+subMapMid][j*subMapLen+subMapMid])/4;
							mapHeight[(i+1)*subMapLen][j*subMapLen + subMapMid] = midAvg;// +rand()*smoothValue;
						}
						
						if(j==0){
							midAvg = (mapHeight[(i+1)*subMapLen][0]+mapHeight[i*subMapLen+subMapMid][subMapMid]+mapHeight[i*subMapLen][0])/3;
							mapHeight[i*subMapLen + subMapMid][0] = midAvg;// +rand()*smoothValue;
						}
						else{
							midAvg = (mapHeight[i*subMapLen][j*subMapLen] + mapHeight[(i+1)*subMapLen][j*subMapLen]+mapHeight[i*subMapLen+subMapMid][j*subMapLen+subMapMid]+mapHeight[i*subMapLen+subMapMid][j*subMapLen-subMapMid])/4;
							mapHeight[i*subMapLen + subMapMid][j*subMapLen] = midAvg;// +rand()*smoothValue;
						}
						if(j==subMapNum-1){
							midAvg = (mapHeight[i*subMapLen+subMapMid][j*subMapLen+subMapMid]+mapHeight[(i+1)*subMapLen][(j+1)*subMapLen]+mapHeight[i*subMapLen][(j+1)*subMapLen])/3;
							mapHeight[i*subMapLen+subMapMid][(j+1)*subMapLen] = midAvg;// +rand()*smoothValue;
						}
						else{
							midAvg = (mapHeight[i*subMapLen+subMapMid][j*subMapLen+subMapMid]+mapHeight[(i+1)*subMapLen][(j+1)*subMapLen]+mapHeight[i*subMapLen+subMapMid][(j+1)*subMapLen+subMapMid]+mapHeight[i*subMapLen][(j+1)*subMapLen])/4;
							mapHeight[i*subMapLen+subMapMid][(j+1)*subMapLen] =  midAvg;// +rand()*smoothValue;
						}
					}
				}
			}
			
			for(int prtH = 0; prtH<sideLen; prtH++){
				for(int prtW = 0; prtW<sideLen; prtW++){
					System.out.print(mapHeight[prtH][prtW]+",\t\t");
				}
				System.out.println();
			}
			

			int i1, j1;
			float maxValue=0, minValue=8;
			for(i1 = 0; i1 < sideLen; i1++)
				for(j1 = 0; j1 < sideLen; j1++){
					if(maxValue<mapHeight[i1][j1])
						maxValue = mapHeight[i1][j1];
					if(minValue>mapHeight[i1][j1])
						minValue = mapHeight[i1][j1];
				}
			System.out.println("max value is: "+maxValue+"min value is: "+minValue);

			// step 3 build the essential element of vertex
			float[] v = new float[prtNum*prtNum*3];
			float[] c = new float[v.length];

			/*
			 * assign value from (0,0) to (sideLen-1,sideLen-1)
			 * row by row
			 */
			for(int i=0; i<prtNum; i++)
				for(int j=0; j<prtNum; j++)
				{
					v[(i*prtNum+j)*3] = i-midLen;
					v[(i*prtNum+j)*3+1] = j-midLen;
					v[(i*prtNum+j)*3+2] = mapHeight[i][j];
					System.out.println("zuobiao: "+v[(i*prtNum+j)*3]+","+v[(i*prtNum+j)*3+1]+","+v[(i*prtNum+j)*3+2]);
				}


			for(int prtH = 0; prtH<v.length; prtH++)
				System.out.print(v[prtH]);
			System.out.println(" ");

			for(int i=0; i<prtNum; i++)
				for(int j=0; j<prtNum; j++)
				{
					if(mapHeight[i][j]>(maxValue+minValue)/2){
						c[(i*prtNum+j)*3] = 1f;
						c[(i*prtNum+j)*3+1] = 1f;
						c[(i*prtNum+j)*3+2] = 1f;
					}
					if(mapHeight[i][j]<=(maxValue+minValue)/2){
						c[(i*prtNum+j)*3] = 1;
						c[(i*prtNum+j)*3+1] = 0;
						c[(i*prtNum+j)*3+2] = 0;
					}
				}
			// Construct a data structure that stores the vertices, their
			// attributes, and the triangle mesh connectivity
			VertexData vertexData = renderContext.makeVertexData(prtNum*prtNum);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);

			int indices[] = new int[(prtNum-1)*(prtNum-1)*2*3];
			int a = 0;
			int t1,t2,t3,t4,t5,t6;
			for(int i=0; i<prtNum-1; i++){
				for(int j=0; j<prtNum-1; j++){
					indices[a++] = prtNum*i+j;			// first triangle
					indices[a++] = prtNum*i+j+1;
					indices[a++] = prtNum*(i+1)+j+1;
					indices[a++] = prtNum*i+j;			// second  triangle
					indices[a++] = prtNum*(i+1)+j;
					indices[a++] = prtNum*(i+1)+j+1;
					System.out.println("round: "+i+","+j);
					t1 = prtNum*i+j;			// first triangle
					t2 = prtNum*i+j+1;
					t3 = prtNum*(i+1)+j+1;
					t4 = prtNum*i+j;			// second  triangle
					t5 = prtNum*(i+1)+j;
					t6 = prtNum*(i+1)+j+1;
					System.out.println(t1+","+t2+","+t3);
					System.out.println(t4+","+t5+","+t6);
				}
			}
			System.out.println("length="+indices.length);
			for(int prtH = 0; prtH<indices.length; prtH++)
				System.out.print(indices[prtH]+",");
			System.out.println(" ");
			
			vertexData.addIndices(indices);
			return vertexData;
		}

		private VertexData defaultShape(){
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
			float d_angle = 0.01f;
			float d_distance = 0.1f;

			float trans = 0, alpha = 0, beta = 0, gamma = 0;

			if(forwards)
				trans = trans + d_distance;
			if(backwards)
				trans = trans - d_distance;
			if(yawP)
				beta = beta + d_angle;
			if(yawN)
				beta = beta - d_angle;
			if(pitchP)
				alpha = alpha + d_angle;
			if(pitchN)
				alpha = alpha - d_angle;
			if(rollP)
				gamma = gamma + d_angle;
			if(rollN)
				gamma = gamma - d_angle;

			if(!(trans == 0 && alpha == 0 && beta == 0 && gamma == 0)){
				Matrix4f rot = new Matrix4f();
				Vector4f x = new Vector4f();
				Vector4f y = new Vector4f();
				Vector4f z = new Vector4f();
				Vector4f t = new Vector4f();
				Vector4f dt = new Vector4f(0,0,-trans,0);
				Matrix4f c = sceneManager.getCamera().getCameraMatrix();
				c.invert();
				c.getColumn(0, x);
				c.getColumn(1, y);
				c.getColumn(2, z);
				
				rot.setIdentity();
//				rot.setRotation(new AxisAngle4f(1, 0, 0, alpha));
				rot.rotX(alpha);
				rot.transform(y);
				rot.transform(z);

				rot.setIdentity();
//				rot.setRotation(new AxisAngle4f(0, 1, 0, beta));
				rot.rotY(beta);
				rot.transform(x);
				rot.transform(z);

				rot.setIdentity();
//				rot.setRotation(new AxisAngle4f(0, 0, 1, gamma));
				rot.rotZ(gamma);
				rot.transform(x);
				rot.transform(y);

				c.setColumn(0,x);
				c.setColumn(1,y);
				c.setColumn(2,z);
				c.getColumn(3, t);
				c.transform(dt);
				t.add(dt);
				c.setColumn(3,t);

				Matrix4f t_airplane = new Matrix4f(c);
				c.invert();
				sceneManager.getCamera().setCameraMatrix(c);
				
				dt = new Vector4f(0, 0, -4, 0);
				t_airplane.transform(dt);
				t.add(dt);
				t_airplane.setColumn(3,t);
				t_airplane.setColumn(0, z);
				x.negate();
				t_airplane.setColumn(2, x);
				airplane.setTransformation(t_airplane);
				
			}
			renderPanel.getCanvas().repaint();
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
			switch(e.getKeyCode()) {
				case KeyEvent.VK_W: {
					forwards = true;
					break;
				}
				case KeyEvent.VK_S: {
					backwards = true;
					break;
				}
				case (char) KeyEvent.VK_UP: {
					pitchP = true;
					break;
				}
				case (char) KeyEvent.VK_DOWN: {
					pitchN = true;
					break;
				}
				case KeyEvent.VK_A: {
					yawP = true;
					break;
				}
				case KeyEvent.VK_D: {
					yawN = true;
					break;
				}
				case (char) KeyEvent.VK_LEFT: {
					rollP = true;
					break;
				}
				case (char) KeyEvent.VK_RIGHT: {
					rollN = true;
					break;
				}
				case (char) KeyEvent.VK_N: {
					// Remove material from shape, and set "normal" shader
					landscape.setMaterial(null);
					renderContext.useShader(normalShader);
					break;
				}
				case (char) KeyEvent.VK_M: {
					// Remove material from shape, and set "default" shader
					landscape.setMaterial(null);
					renderContext.useDefaultShader();
					break;
				}
			}
			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}

		public void keyReleased(KeyEvent e){
			switch(e.getKeyCode()){
				case KeyEvent.VK_W: {
					forwards = false;
					break;
				}
				case KeyEvent.VK_S: {
					backwards = false;
					break;
				}
				case KeyEvent.VK_A: {
					yawP = false;
					break;
				}
				case KeyEvent.VK_D: {
					yawN = false;
					break;
				}
				case (char) KeyEvent.VK_UP: {
					pitchP = false;
					break;
				}
				case (char) KeyEvent.VK_DOWN: {
					pitchN = false;
					break;
				}
				case (char) KeyEvent.VK_LEFT: {
					rollP = false;
					break;
				}
				case (char) KeyEvent.VK_RIGHT: {
					rollN = false;
					break;
				}
			}

			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		public void keyTyped(KeyEvent e){
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
