package newhorizon.vars;

import arc.math.geom.Vec2;

public class NHCtrlVars{
	public static boolean isSelecting = false;
	public static boolean pressDown = false;
	public static final Vec2 ctrlVec2 = new Vec2();
	
	public static void reset(){
		pressDown = false;
		isSelecting = false;
		ctrlVec2.set(-1, -1);
	}
}
