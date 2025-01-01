package newhorizon.util.func;

import arc.func.Intc2;

/**
 * Multi Thread Safe
 * */
public class NHGeom{
	public static void square(int x, int y, int radius, Intc2 cons) {
	    for(int dx = -radius; dx <= radius; ++dx) {
	        for(int dy = -radius; dy <= radius; ++dy) {
	            cons.get(dx + x, dy + y);
	        }
	    }
	}
	
	public static void squareAbs(int startX, int startY, int endX, int endY, Intc2 cons) {
	    if(startX > endX || startY > endY)throw new IllegalArgumentException("MIN > MAX");
	    for(int dx = startX; dx <= endX; ++dx) {
	        for(int dy = startY; dy <= endY; ++dy) {
	            cons.get(dx, dy);
	        }
	    }
	}
	
	/**
	 * @param height Half of the total height
	 * @param width  Half of the total width
	 * */
	public static void square(int x, int y, int width, int height, Intc2 cons) {
	    for(int dx = -width; dx <= width; ++dx) {
	        for(int dy = -height; dy <= height; ++dy) {
	            cons.get(dx + x, dy + y);
	        }
	    }
	}
}
