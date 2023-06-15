package newhorizon.util.graphic;

import arc.graphics.Blending;
import arc.graphics.Gl;

public class NHBlending{
	public static final Blending sustainAlpha = new Blending(Gl.srcAlpha, Gl.oneMinusSrcAlpha){{
	}},
		test2 = new Blending(Gl.blendSrcRgb, Gl.blendEquationRgb),
		test3 = new Blending(Gl.oneMinusSrcColor, Gl.blendDstRgb);
	
}
