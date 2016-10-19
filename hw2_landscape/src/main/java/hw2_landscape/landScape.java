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
public class landScape
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
	static boolean move_for;
	static boolean move_bak;
	static boolean y_left;
	static boolean y_right;
	static boolean z_left;
	static boolean z_right;
	static boolean x_up;
	static boolean x_dow;

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


			VertexData vertexData = createLandscape(3, renderContext);
			//VertexData vertexData = createCube();
			VertexData vertexDataAirplane;
			objReader = new ObjReader();
			
			teapotReader = new ObjReader();
			try {
				vertexDataAirplane = objReader.read("../obj/airplane.obj", 1.0f, renderContext);
			} catch (IOException e) {
				vertexDataAirplane = defaultShape();
				System.out.print("Failed to read teapot!");
				System.out.print(e.getMessage());
			}


			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			airplane = new Shape(vertexDataAirplane);
			landscape = new Shape(vertexData);
			sceneManager.addShape(airplane);
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
			float max = 0;
			float range = max-min;
			return (float)Math.random()*range;
		}

		private VertexData createLandscape(int imgSize, RenderContext renderContext){
			// define the 2D map size is 2
			int sideLen = (int) Math.pow(2, imgSize)+1;
			int midLen = (sideLen-1)/2;
			float[][] mapH = new float[sideLen][sideLen];
			float midAvg;
			float smoothValue = (float)Math.pow(2, -1);

			int fS=sideLen;
			int hS=midLen;
			int iter=0;
			
			// step 1 : initialize four cornor's value (height)
			mapH[0][0] = -32;
			mapH[0][sideLen-1] = 0;
			mapH[sideLen-1][0] = 16;
			mapH[sideLen-1][sideLen-1] = 64;
//			mapH[0][0] = 64;
//			mapH[0][sideLen-1] = 64;
//			mapH[sideLen-1][0] = 64;
//			mapH[sideLen-1][sideLen-1] = 64;
			
			// step 2: assign height to map mesh according to diamond and square
			for(int iterVar=0; iterVar<imgSize; iterVar++){
				fS = (sideLen-1)>>iterVar;
				hS = fS>>1;
				iter = 1<<iterVar;
				int x,y;

				/*
				 * square
				 * find the middle point of each square
				*/
				for(int i=0; i<iter; i++){
					for(int j=0; j<iter; j++){
						// average height of fours corner points of this square
						x=hS+i*fS;
						y=hS+j*fS;
						midAvg = (mapH[x-hS][y-hS] + mapH[x-hS][y+hS] + mapH[x+hS][y+hS] + mapH[x+hS][y-hS])/4;
//						System.out.println("square: "+mapH[x-hS][y-hS]+","+ mapH[x-hS][y+hS]+","+ mapH[x+hS][y+hS]+","+ mapH[x+hS][y-hS]);
						mapH[x][y] = midAvg+rand()*smoothValue;
					}
				}
//				System.out.println("square :");
//				for(int prtH = 0; prtH<sideLen; prtH++){
//					for(int prtW = 0; prtW<sideLen; prtW++){
//						System.out.print(mapH[prtH][prtW]+",\t\t");
//					}
//					System.out.println();
//				}

				/*
				 * diamond
				 * there are four points for each square, top, bottom, right left
				 * if the same point is calculated twice, the former value will be
				 * over-written,
				 * the points on the board of map have only three points to
				 * calculate the average height;
				*/
				for(int i=0; i<iter; i++){
					for(int j=0; j<iter; j++){
//						x=hS+i*fS-1;
//						y=hS+j*fS-1;
//						mapH[x+fS][y]=0;
//						mapH[x-fS][y]=0;
//						mapH[x][y+fS]=0;
//						mapH[x][y-fS]=0;
						

//						mapH[x+fS][y]=rand();
//						mapH[x-fS][y]=rand();
//						mapH[x][y+fS]=rand();
//						mapH[x][y-fS]=rand();
//
//						fS/=2;
//						hS/=2;
						if(i==0){
							midAvg = (mapH[0][j*fS]+mapH[hS][j*fS+hS]+mapH[0][(j+1)*fS])/3;
							mapH[0][j*fS + hS]=midAvg+rand()*smoothValue;
						}
						else{
							midAvg = (mapH[i*fS][j*fS]+mapH[i*fS+hS][j*fS+hS]+mapH[i*fS][(j+1)*fS] + mapH[i*fS-hS][j*fS+hS])/4;
							mapH[i*fS][j*fS + hS] = midAvg+rand()*smoothValue;
						}
						if(i==iter-1){
							midAvg = (mapH[(i+1)*fS][j*fS]+mapH[i*fS+hS][j*fS+hS] + mapH[(i+1)*fS][(j+1)*fS])/3;
							mapH[(i+1)*fS][j*fS + hS] = midAvg+rand()*smoothValue;
						}
						else{
							midAvg = (mapH[(i+1)*fS][j*fS]+mapH[i*fS+hS][j*fS+hS]+mapH[(i+1)*fS][(j+1)*fS]+mapH[(i+1)*fS+hS][j*fS+hS])/4;
							mapH[(i+1)*fS][j*fS + hS] = midAvg+rand()*smoothValue;
						}
						
						if(j==0){
							midAvg = (mapH[(i+1)*fS][0]+mapH[i*fS+hS][hS]+mapH[i*fS][0])/3;
							mapH[i*fS + hS][0] = midAvg+rand()*smoothValue;
						}
						else{
							midAvg = (mapH[i*fS][j*fS] + mapH[(i+1)*fS][j*fS]+mapH[i*fS+hS][j*fS+hS]+mapH[i*fS+hS][j*fS-hS])/4;
							mapH[i*fS + hS][j*fS] = midAvg+rand()*smoothValue;
						}
						if(j==iter-1){
							midAvg = (mapH[i*fS+hS][j*fS+hS]+mapH[(i+1)*fS][(j+1)*fS]+mapH[i*fS][(j+1)*fS])/3;
							mapH[i*fS+hS][(j+1)*fS] = midAvg+rand()*smoothValue;
						}
						else{
							midAvg = (mapH[i*fS+hS][j*fS+hS]+mapH[(i+1)*fS][(j+1)*fS]+mapH[i*fS+hS][(j+1)*fS+hS]+mapH[i*fS][(j+1)*fS])/4;
							mapH[i*fS+hS][(j+1)*fS] =  midAvg+rand()*smoothValue;
						}
					}
				}
				System.out.println("diamondssssssssssssss :");
				for(int prtH = 0; prtH<sideLen; prtH++){
					for(int prtW = 0; prtW<sideLen; prtW++){
						System.out.print(mapH[prtH][prtW]+",\t\t");
					}
					System.out.println();
				}
			}

			System.out.println("step is :"+fS+", "+hS+", "+iter);
			
			for(int prtH = 0; prtH<sideLen; prtH++){
				for(int prtW = 0; prtW<sideLen; prtW++){
					System.out.print(mapH[prtH][prtW]+",\t\t");
				}
				System.out.println();
			}
			

			int i1, j1;
			float maxValue=0, minValue=8;
			for(i1 = 0; i1 < sideLen; i1++)
				for(j1 = 0; j1 < sideLen; j1++){
					if(maxValue<mapH[i1][j1])
						maxValue = mapH[i1][j1];
					if(minValue>mapH[i1][j1])
						minValue = mapH[i1][j1];
				}
			System.out.println("max value is: "+maxValue+"min value is: "+minValue);

			// step 3 build the essential element of vertex
			float[] v = new float[sideLen*sideLen*3];
			float[] c = new float[v.length];

			/*
			 * assign value from (0,0) to (sideLen-1,sideLen-1)
			 * row by row
			 */

			int x = -midLen;
			int y = -midLen;
			for(int i=0; i<sideLen; i++)
				for(int j=0; j<sideLen; j++)
				{
					v[(i*sideLen+j)*3] = x+hS*i;// y+hS*j-(x+hS*i)*(float)Math.cos(45/2/Math.PI);
					v[(i*sideLen+j)*3+1] = y+hS*j;//(x+hS*i)*(float)Math.sin(45/2/Math.PI)-mapH[i][j]; //
					v[(i*sideLen+j)*3+2] = mapH[i][j];
					System.out.println("zuobiao: "+v[(i*sideLen+j)*3]+","+v[(i*sideLen+j)*3+1]+","+v[(i*sideLen+j)*3+2]);
				}

			/*test
			float v[]={-1,-1,0, -1,0,0, -1,1,0,
			           0,-1,0,   0,0,0,  0,1,0,
			           1,-1,0,   1,0,0,  1,1,0};
			*/
			for(int prtH = 0; prtH<v.length; prtH++)
				System.out.print(v[prtH]);
			System.out.println(" ");

			for(int i=0; i<sideLen; i++)
				for(int j=0; j<sideLen; j++)
				{
					if(mapH[i][j]>(maxValue+minValue)*6/7){
						c[(i*sideLen+j)*3]   = 1f;
						c[(i*sideLen+j)*3+1] = 1f;
						c[(i*sideLen+j)*3+2] = 1f;
					}
					if(mapH[i][j]<=(maxValue+minValue)*6/7){
						c[(i*sideLen+j)*3]   = 0f;
						c[(i*sideLen+j)*3+1] = 1f;
						c[(i*sideLen+j)*3+2] = 0f;
					}
					if(mapH[i][j]<=(maxValue+minValue)*1/7){
						c[(i*sideLen+j)*3]   = 0f;
						c[(i*sideLen+j)*3+1] = 0f;
						c[(i*sideLen+j)*3+2] = 1f;
					}
				}
			// Construct a data structure that stores the vertices, their
			// attributes, and the triangle mesh connectivity
			VertexData vertexData = renderContext.makeVertexData(sideLen*sideLen);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);

			int indices[] = new int[(sideLen-1)*(sideLen-1)*2*3];
			int a = 0;
			int t1,t2,t3,t4,t5,t6;
//			int indices[] = {0,1,4, 0,3,4, 1,2,5, 1,4,5,
//					         3,4,7, 3,6,7, 4,5,8, 4,7,8};

			for(int i=0; i<sideLen-1; i++){
				for(int j=0; j<sideLen-1; j++){
					indices[a++] = sideLen*i+j;			// first triangle
					indices[a++] = sideLen*i+j+1;
					indices[a++] = sideLen*(i+1)+j+1;
					indices[a++] = sideLen*i+j;			// second  triangle
					indices[a++] = sideLen*(i+1)+j;
					indices[a++] = sideLen*(i+1)+j+1;
					/*
					System.out.println("round: "+i+","+j);
					t1 = sideLen*i+j;			// first triangle
					t2 = sideLen*i+j+1;
					t3 = sideLen*(i+1)+j+1;
					t4 = sideLen*i+j;			// second  triangle
					t5 = sideLen*(i+1)+j;
					t6 = sideLen*(i+1)+j+1;
					System.out.println(t1+","+t2+","+t3);
					System.out.println(t4+","+t5+","+t6);
					*/
				}
			}
			/*
			System.out.println("length="+indices.length);
			for(int prtH = 0; prtH<indices.length; prtH++)
				System.out.print(indices[prtH]+",");
			System.out.println(" ");
			*/
			
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
	// when solving this question,
	// the code from Internet is taken as reference
	// Lin Bai 2016-10-20
	public static class AnimationTask extends TimerTask
	{
		public void run()
		{
			float move = 0;
			float x_dgr = 0;
			float y_dgr = 0;
			float z_dgr = 0;

			if(move_for)
				move = move + 0.05f;
			if(move_bak)
				move = move - 0.05f;
			if(x_up)
				x_dgr = x_dgr + 0.01f;
			if(x_dow)
				x_dgr = x_dgr - 0.01f;
			if(y_left)
				y_dgr = y_dgr + 0.01f;
			if(y_right)
				y_dgr = y_dgr - 0.01f;
			if(z_left)
				z_dgr = z_dgr + 0.01f;
			if(z_right)
				z_dgr = z_dgr - 0.01f;

			if(move != 0 || x_dgr != 0 || y_dgr != 0 || z_dgr != 0){
				Matrix4f rot = new Matrix4f();
				Vector4f x = new Vector4f();
				Vector4f y = new Vector4f();
				Vector4f z = new Vector4f();
				Vector4f t = new Vector4f();
				Vector4f dt = new Vector4f(0,0,-move,0);
				Matrix4f camerM = sceneManager.getCamera().getCameraMatrix();
				camerM.invert();  // convert from world-2-camera to camera-2-world matrix
				camerM.getColumn(0, x); // get x column
				camerM.getColumn(1, y); // get y column
				camerM.getColumn(2, z); // get z column
				camerM.getColumn(3,t); // get camera point
				
				rot.setIdentity();
				rot.setRotation(new AxisAngle4f(x.x, x.y, x.z, x_dgr));
				rot.transform(y);
				rot.transform(z);

				rot.setIdentity();
				rot.setRotation(new AxisAngle4f(y.x, y.y, y.z, y_dgr));
				rot.transform(x);
				rot.transform(z);

				rot.setIdentity();
				rot.setRotation(new AxisAngle4f(z.x, z.y, z.z, z_dgr));
				rot.transform(x);
				rot.transform(y);

				camerM.transform(dt);
				t.add(dt);
				
				camerM.setColumn(0,x);
				camerM.setColumn(1,y);
				camerM.setColumn(2,z);
				camerM.setColumn(3,t);

				Matrix4f t_airplane = new Matrix4f(camerM);
				camerM.invert();
				sceneManager.getCamera().setCameraMatrix(camerM);
				
				dt = new Vector4f(0, 0, -4, 0);
				t_airplane.transform(dt);
				t.add(dt);
				t_airplane.setColumn(3,t);
				t_airplane.setColumn(0,z);
				x.negate();
				t_airplane.setColumn(2,x);
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
					move_for = true;
					break;
				}
				case KeyEvent.VK_S: {
					move_bak = true;
					break;
				}
				case KeyEvent.VK_A: {
					y_left = true;
					break;
				}
				case KeyEvent.VK_D: {
					y_right = true;
					break;
				}
				case KeyEvent.VK_UP: {
					x_up = true;
					break;
				}
				case KeyEvent.VK_DOWN: {
					x_dow = true;
					break;
				}
				case KeyEvent.VK_LEFT: {
					z_left = true;
					break;
				}
				case KeyEvent.VK_RIGHT: {
					z_right = true;
					break;
				}
				case KeyEvent.VK_N: {
					// Remove material from shape, and set "normal" shader
					airplane.setMaterial(null);
					renderContext.useShader(normalShader);
					break;
				}
				case KeyEvent.VK_M: {
					// Set a material for more complex shading of the shape
					if(airplane.getMaterial() == null) {
						airplane.setMaterial(material);
					} else
					{
						airplane.setMaterial(null);
						renderContext.useDefaultShader();
					}
					break;
				}
				case KeyEvent.VK_V: {
					sceneManager.getFrustum().setNear(1);
					sceneManager.getFrustum().setFar(100);
					sceneManager.getFrustum().setAspect(1);
					sceneManager.getFrustum().getFovGrad(60);
					sceneManager.getCamera().setCenterOfProjection(new Vector3f(10f,10f,10f));//-60f,60f));
					sceneManager.getCamera().setLookAtPoint(new Vector3f(0f,0f,0f));
					sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
					break;
				}
			}

			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}

		public void keyReleased(KeyEvent e){
			switch(e.getKeyCode()){
				case KeyEvent.VK_W: {
					move_for = false;
					break;
				}
				case KeyEvent.VK_S: {
					move_bak = false;
					break;
				}
				case KeyEvent.VK_A: {
					y_left = false;
					break;
				}
				case KeyEvent.VK_D: {
					y_right = false;
					break;
				}
				case KeyEvent.VK_UP: {
					x_up = false;
					break;
				}
				case KeyEvent.VK_DOWN: {
					x_dow = false;
					break;
				}
				case KeyEvent.VK_LEFT: {
					z_left = false;
					break;
				}
				case KeyEvent.VK_RIGHT: {
					z_right = false;
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
