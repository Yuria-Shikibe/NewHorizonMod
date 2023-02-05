package newhorizon.util.func;

import arc.math.Interp;
import arc.math.Mathf;

public class NHInterp{
//	public static final Interp upThenFastDown = x -> -0.926837f + 1.85241f * x + 0.202482f / x;
	public static final Interp upThenFastDown = x -> 1.0115f * (1.833f * (0.9991f * x - 1.1f) + 0.2f / (0.9991f * x - 1.1f) + 2.2f);
	public static final Interp artillery = x -> 1 - 2 * (x-0.5f) * (x-0.5f);
	public static final Interp artilleryPlus = x -> 3 * x - 3 * x * x + 0.25f;
	public static final Interp artilleryPlusReversed = x -> -3 * x + 3 * x * x + 1;
	public static final Interp.BounceOut bounce5Out = new Interp.BounceOut(5);
	public static final Interp.BounceIn bounce5In = new Interp.BounceIn(5);
	public static final Interp.Pow pow10 = new Interp.Pow(10);
	public static final Interp zero = a -> 0;
	public static final Interp inOut = a -> 2 * (0.9f * a + 0.31f) + 1f / (5f * (a + 0.1f)) - 1.6f;
	public static final Interp inOut2 = x -> 1.6243f * (0.9f * x + 0.46f) + 1 / (10 * (x + 0.1f)) -1.3f;
	public static final Interp parabola4 = x -> 4 * (x - 0.5f) * (x - 0.5f);
	public static final Interp parabola4Reversed = x -> -4 * (x - 0.5f) * (x - 0.5f) + 1;
	public static final Interp parabola4Reversed_X4 = x -> (-4 * (x - 0.5f) * (x - 0.5f) + 1) * 2.75f;
	public static final Interp laser = x -> Interp.pow10Out.apply(x * 1.5f) * Mathf.curve(1 - x, 0, 0.085f);
}
