package jrtr.swrenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Point4f;

import jrtr.Texture;

/**
 * Manages textures for the software renderer. Not implemented here.
 */
public class SWTexture implements Texture {

	private BufferedImage texture;
	
	private int canvasSize(int i)
	{
		int canvasWid = texture.getWidth();
		int canvasHei = texture.getHeight();
		
		if (i == 1)
			return canvasWid;
		else
			return canvasHei;
	}
	
	public void load(String fileName) throws IOException {
		
		File f = new File(fileName);
		texture = ImageIO.read(f);
	}

	public int getNearestNeighbourColor(float x, float y) {
		return texture.getRGB(Math.round(x*(canvasSize(1) - 1)), Math.round((1 - y)*(canvasSize(2) - 1)));
	}
	
	public int getBilinearInterpolatedColor(float x, float y) {
		Point4f scaled = new Point4f(x*(canvasSize(1) - 1), (1 - y)*(canvasSize(2) - 1), 1, 1);
		int[][][] imagePixels = new int[2][2][];
		//the first coordinate signifies the top/bottom, the second left/right
		imagePixels[0][0] = getRgb(texture.getRGB(Math.round(scaled.x),Math.round(scaled.y)));
		imagePixels[0][1] = getRgb(texture.getRGB(Math.round(scaled.x),Math.round(scaled.y)));
		imagePixels[1][0] = getRgb(texture.getRGB(Math.round(scaled.x),Math.round(scaled.y)));
		imagePixels[1][1] = getRgb(texture.getRGB(Math.round(scaled.x),Math.round(scaled.y)));
		float horzCoeff = Math.round(scaled.x) - scaled.x;
		int[][] weightedTopBot = new int[2][];
		for (int i = 0; i < 2; i++) {
			weightedTopBot[i] = interpolateBetween(imagePixels[i][0], imagePixels[i][1], horzCoeff);
		}
		float vertCoeff = Math.round(scaled.y) - scaled.y;
		int[] result = interpolateBetween(weightedTopBot[0], weightedTopBot[1], vertCoeff);
		
		return (result[0] << 16) | (result[1] << 8) | result[2];
	}
	
	private int[] interpolateBetween(int[] colorNear, int[] colorFar, float coeff) {
		int[] avg = new int[3];
		for (int i = 0; i < colorNear.length; i++) {
			avg[i] = (int)(colorNear[i]*coeff + colorFar[i]*(1-coeff));
		}
		return avg;
	}
	
	private int[] getRgb(int hexaColor) {
		int[] rgb = new int[3];
		int bitmask = 0x0000FF;
		for (int i = 0; i < 3; i++) {
			rgb[2 - i] = (hexaColor >> 8*i) & bitmask;
		}
		return rgb;
	}

	private int getBilinInterp(int u0v0, int u0v1, int u1v0, int u1v1, float wu, float wv){
		float cb = u0v0*(1-wu) + u1v0*wu;
		float ct = u0v1*(1-wu) + u1v1*wu;
		float c = cb*(1-wv) + ct*wv;
		return Math.round(c);
	}
}
