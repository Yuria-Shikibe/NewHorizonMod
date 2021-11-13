package newhorizon.util.func;

import arc.math.Interp;

public class NHInterp{
	public static final Interp.BounceOut bounce5Out = new Interp.BounceOut(5);
	public static final Interp.Pow pow10 = new Interp.Pow(10);
	public static final Interp zero = a -> 0;
}
