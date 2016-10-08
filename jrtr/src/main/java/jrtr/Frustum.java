package jrtr;

import javax.vecmath.Matrix4f;

/**
 * Stores the specification of a viewing frustum, or a viewing
 * volume. The viewing frustum is represented by a 4x4 projection
 * matrix. You will extend this class to construct the projection 
 * matrix from intuitive parameters.
 * <p>
 * A scene manager (see {@link SceneManagerInterface}, {@link SimpleSceneManager}) 
 * stores a frustum.
 */
public class Frustum {

	private Matrix4f projectionMatrix;
	private float near, far, aspectRatio, fov;
	
	/**
	 * Construct a default viewing frustum. The frustum is given by a 
	 * default 4x4 projection matrix.
	 */
	public Frustum()
	{
		// set default values
		setNear(1f);
		setFar(100f);
		setAspect(1f);
		getFovGrad(60f);
	}
	
	/**
	 * Return the 4x4 projection matrix, which is used for example by 
	 * the renderer.
	 * 
	 * @return the 4x4 projection matrix
	 */
	public Matrix4f getProjectionMatrix()
	{
		return projectionMatrix;
	}

	public void setNear(float near)
	{
		this.near = near;
		update();
	}

	public float getNear()
	{
		return near;
	}

	public void setFar(float far)
	{
		this.far = far;
		update();
	}

	public float getFar()
	{
		return far;
	}

	public void setAspect(float aspectRatio)
	{
		this.aspectRatio = aspectRatio;
		update();
	}

	public float getAspect()
	{
		return aspectRatio;
	}

	public void getFovGrad(float fov)
	{
		this.fov = (float) (fov/180*Math.PI);
		update();
	}

	public float getFov()
	{
		return fov;
	}

	private void update()
	{
		projectionMatrix = new Matrix4f(
				(float) (1/(aspectRatio*Math.tan(fov/2))), 0f, 0f, 0f, 
				0f, (float) (1/Math.tan(fov/2)), 0f, 0f,
				0f, 0f, (float) (near+far)/(near-far), (float) (2*near*far/(near-far)),
				0f, 0f, -1f, 0f);
	}
}
