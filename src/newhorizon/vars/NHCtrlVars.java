package newhorizon.vars;

import arc.math.geom.Rect;
import arc.math.geom.Vec2;

public class NHCtrlVars{
	public boolean isSelecting = false;
	public boolean pressDown = false;
	public final Vec2 ctrlVec2 = new Vec2();
	
	public final Rect rect = new Rect();
	
	public final Vec2 from = new Vec2(), to = new Vec2();
}
