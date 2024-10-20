package newhorizon.util.graphic;

import arc.graphics.Blending;
import arc.graphics.Gl;

public class NHBlending{
	public static final Blending shadow = new Blending(Gl.constantAlpha, Gl.oneMinusConstantAlpha) {
		@Override
		public void apply() {
			Gl.enable(Gl.blend);
			Gl.blendColor(0, 0, 0, 0.22f);
			Gl.blendFuncSeparate(src, dst, srcAlpha, dstAlpha);
		}
	};

	
}
