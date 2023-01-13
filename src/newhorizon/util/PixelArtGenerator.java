package newhorizon.util;

import arc.files.Fi;
import arc.graphics.*;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.logic.CanvasBlock;
import newhorizon.content.NHOverride;

public class PixelArtGenerator{
	static ObjectIntMap<Color> cache = new ObjectIntMap<>();
	static Seq<CanvasBlock.CanvasBuild> builds = new Seq<>();
	static int height, width;
	
	public static Fi toRead;
	public static Point2 leftDown = new Point2(), rightTop = new Point2();
	
	public static boolean process(){
		builds.clear();
		
		if(toRead == null)return false;
		Pixmap pixmap = PixmapIO.readPNG(toRead);
		
		Pixmap finalPixmap = pixmap;
		pixmap.each(((x, y) -> {
			finalPixmap.set(x, y, color8888(findMostSimilar(Tmp.c1.rgba8888(finalPixmap.get(x, y)))));
		}));
		
		Log.info("lerp complete");
		
		CanvasBlock type = null;
		
		for(int i = leftDown.x; i <= rightTop.x; i++){
			for(int j = rightTop.y; j >= leftDown.y; j--){
				Building building = Vars.world.build(i, j);
				if(!(building instanceof CanvasBlock.CanvasBuild)){
					return false;
				}else{
					CanvasBlock.CanvasBuild b = (CanvasBlock.CanvasBuild)building;
					type = (CanvasBlock)b.block;
					builds.addUnique(b);
				}
			}
		}
		
		Log.info("convert complete");
		
		if(type == null)return false;
		height = (rightTop.y - leftDown.y) / type.size * type.canvasSize;
		width = (rightTop.x - leftDown.x) / type.size * type.canvasSize;
		
		Pixmap processed;
		
		if(pixmap.width > height || pixmap.height > width)return false;
		
		int xScl = 1, yScl = 1;
		xScl = width / pixmap.width;
		yScl = height / pixmap.height;
		
		int scl = Math.min(xScl, yScl);
//		pixmap = Pixmaps.scale(pixmap, scl, scl, false);
		
		pixmap = Pixmaps.resize(pixmap, width, height);
		
		Pixmap finalPixmap1 = pixmap;
		new BaseDialog("Preview"){{
			addCloseButton();
			cont.table(t -> {
				t.image(new TextureRegion(new Texture(finalPixmap1))).scaling(Scaling.stretch);
			}).grow();
		}}.show();
		
		Log.info("fetch complete");
		
		Seq<Pixmap> split = new Seq<>(builds.size);
		
		for(int i = 0; i <= width / type.canvasSize; i++){
			for(int j = 0; j <= height / type.canvasSize; j++){
				split.add(Pixmaps.crop(pixmap, i * type.canvasSize, j * type.canvasSize, type.canvasSize, type.canvasSize));
			}
		}
		
		for(int i = 0; i < builds.size; i++){
			CanvasBlock.CanvasBuild b = builds.get(i);
			b.configure(b.packPixmap(split.get(Math.min(i, split.size - 1))));
		}
		
		Log.info("draw complete");
		
		split.each(Pixmap::dispose);
		pixmap.dispose();
		
		return true;
	}
	
	public static int findMostSimilar(Color color){
		if(cache.containsKey(color))return cache.get(color);
		
		float diff = Float.POSITIVE_INFINITY;
		int tgtColor = 0;
		
		for(int i = 0; i < NHOverride.validColor.size; i++){
			Color c = NHOverride.validColor.get(i);
			
			float d = colorDst(color, c);
			if(d < diff){
				diff = d;
				tgtColor = i;
			}
		}
		
		cache.put(color, tgtColor);
		
		return tgtColor;
	}
	
	public static int color8888(int index){
		return NHOverride.validColor.get(index).rgba8888();
	}
	
	public static float colorDst(Color color1, Color color2){
		return color1.diff(color2);
//		return Tmp.v31.set(color1.r, color1.g, color1.b).dst(color2.r, color2.g, color2.b);
	}
}
