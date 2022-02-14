package newhorizon.util.func;

import arc.func.Intc2;
import arc.struct.IntSeq;

/**
 * Multi Thread Safe
 * */
public class NHGeom{
	public static void raycast(int x0f, int y0f, int x1, int y1, int width, int height, Intc2 cons){
		IntSeq calculated = new IntSeq();
		
		int x0 = x0f;
		int y0 = y0f;
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		
		int tx = dx + width;
		int ty = dy + height;
		
		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;
		
		int err = dx - dy;
		int e2;
		while(x0 != x1 && y0 != y1){
			
			for(int w = -width / 2; w < width / 2; w++){
				for(int h = -height / 2; h < height / 2; h++){
					int index = (h + y0) * tx + w + x0;
					if(calculated.contains(index))continue;
					cons.get(x0 + w, y0 + h);
					calculated.add(index);
				}
			}
			
			e2 = 2 * err;
			if(e2 > -dy){
				err = err - dy;
				x0 = x0 + sx;
			}
			
			if(e2 < dx){
				err = err + dx;
				y0 = y0 + sy;
			}
		}
	}
	
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
	//                Log.info(dy + dx * (endY - startY) + "/" + (endX - startX) * (endY - startY));
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
