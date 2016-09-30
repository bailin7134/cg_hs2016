/*
 * Name: Computer Graphics Assignment #1
 * Author: Lin Bai
 * Student No.: 09935404
 * Unit: Department of Computer Science
 * E-mail: lin.bai@students.unibe.ch
 */

package homework1;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;
import java.util.Timer;
import java.util.TimerTask;

public class Animation
{	
	static Shape shape_cycle1, shape_cycle2, shape_cycle3, shape_body1, shape_body2;
	static float currentstep, basicstep;
	
	Matrix4f transformation;

	static float anglestep = (float)(2*Math.PI/360);
	static float angle = 0;
	
	public Animation(SimpleSceneManager sceneManager, RenderContext renderContext)
	{
		//shape_cycle1 = new torus(renderContext, 0.6f, 0.4f, 50, 50);
		//shape_cycle2 = new torus(renderContext, 0.6f, 0.4f, 50, 50);
		shape_cycle1 = new Cylinder(renderContext, 8, 1, 1);
		shape_cycle2 = new Cylinder(renderContext, 8, 1, 1);
		shape_cycle3 = new Cylinder(renderContext, 8, 0.2f, 5);
		shape_body1 = new Cube(renderContext);
		shape_body2 = new Cube(renderContext);
		
		sceneManager.addShape(shape_cycle1);
		sceneManager.addShape(shape_cycle2);
		sceneManager.addShape(shape_cycle3);
		sceneManager.addShape(shape_body1);
		sceneManager.addShape(shape_body2);
	}

	public void setTransformation(Matrix4f t)
	{
		transformation = t;
		
		Matrix4f t_body1 = new Matrix4f(t);
		Matrix4f trans_body1 = new Matrix4f();
		trans_body1.setIdentity();
		trans_body1.setTranslation(new Vector3f(0, 1, 0));
		t_body1.mul(trans_body1);
		shape_body1.setTransformation(t_body1);
   		
		Matrix4f t_body2 = new Matrix4f(t);
		Matrix4f trans_body2 = new Matrix4f();
		trans_body2.setIdentity();
		trans_body2.setTranslation(new Vector3f(0, -1, 0));
		t_body2.mul(trans_body2);;
		shape_body2.setTransformation(t_body2);

		Matrix4f t_cycle1 = new Matrix4f(t);
		Matrix4f trans_cycle1 = new Matrix4f();
		trans_cycle1.rotZ(angle);
		trans_cycle1.setTranslation(new Vector3f(-1, 2, 0));
		t_cycle1.mul(trans_cycle1);
		shape_cycle1.setTransformation(t_cycle1);
   		
		Matrix4f t_cycle2 = new Matrix4f(t);
		Matrix4f trans_cycle2 = new Matrix4f();
		trans_cycle2.rotZ(angle);
		trans_cycle2.setTranslation(new Vector3f(-1, -2, 0));
		t_cycle2.mul(trans_cycle2);
		shape_cycle2.setTransformation(t_cycle2);
   		
		Matrix4f t_cycle3 = new Matrix4f(t);
		Matrix4f trans_cycle3 = new Matrix4f();
		trans_cycle3.rotX(angle*2);
		trans_cycle3.setTranslation(new Vector3f(1, 0, 0));
		t_cycle3.mul(trans_cycle3);
		shape_cycle3.setTransformation(t_cycle3);
	}
	
	public Matrix4f getTransformation()
	{
		return transformation;
	}
	
	public void angle_calc(float currentAngle)
	{
		angle = currentAngle;
	}
	

}
