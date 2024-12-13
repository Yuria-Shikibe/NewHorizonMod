package newhorizon.util;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons2;
import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Align;
import arc.util.Scaling;
import arc.util.Tmp;
import arc.util.io.PropertiesUtils;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mods;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.util.func.NHPixmap;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class Tool_Internal{
	private static Fi toProcess;
	private static Color tmpColor;
	
	public static void showTexture(Texture texture){
		new BaseDialog("Debug"){{
			cont.image(Draw.wrap(texture)).scaling(Scaling.fit).center();
			addCloseButton();
		}}.show();
	}
	
	public static void patchBundle(){
		new BaseDialog(""){{
			cont.fill(t -> {
				Mods.LoadedMod mod = Vars.mods.getMod(NewHorizon.class);
				StringMap bundleEN = new StringMap();
				StringMap bundleTG = new StringMap();
				
				Seq<String> toStorage = new Seq<>();
				
				ObjectMap<String, Color> original = new ObjectMap<>();
				ObjectMap<String, Color> preview = new ObjectMap<>();
				
				PropertiesUtils.load(bundleEN, mod.root.child("bundles").child("bundle.properties").reader());
				
				Table ori = new Table(Tex.pane), pre = new Table(Tex.pane);
				
				t.button("Select Target File", Icon.list, Styles.cleart, () -> {
					bundleTG.clear();
					toStorage.clear();
					original.clear();
					preview.clear();
					
					Vars.platform.showMultiFileChooser(fi -> {
						Vars.ui.loadAnd("[accent]Loading Bundle", () -> {
							toProcess = fi;
							PropertiesUtils.load(bundleTG, toProcess.reader());
							
							bundleEN.each((k, v) -> {
								if(bundleTG.containsKey(k)){
									toStorage.add(k + " = " + bundleTG.get(k));
									preview.put(k + " = " + bundleTG.get(k), Color.white);
								}else{
									preview.put(k + " = " + v, Pal.heal);
									original.put(k + " = " + v, new Color().set(Color.lightGray).a(0));
									toStorage.add(k + " =TODO " + v);
								}
							});
							
							bundleTG.each((k, v) -> {
								if(bundleEN.containsKey(k)){
									original.put(k + " = " + v, Color.white);
								}else{
									original.put(k + " = " + v, Pal.redderDust);
									preview.put(k + " = " + v, new Color().set(Color.lightGray).a(0));
								}
							});
							
							Cons2<ObjectMap<String, Color>, Table> func = (kvs, table) -> {
								table.clearChildren();
								int[] missing = new int[1];
								kvs.keys().toSeq().sortComparing(c -> c.split(" = ")[0]).each(k -> {
									Color v = kvs.get(k);
									float[] h = new float[1];
									table.table(v.equals(Color.white) ? Tex.clear : ((TextureRegionDrawable)Tex.whiteui).tint(v.r, v.g, v.b, 0.35f), c -> {
										if(v.a == 0)missing[0]++;
										c.align(Align.topLeft);
										Label ln = c.add(v.a == 0 ? "" : (table.getRows() - missing[0] + 1 + "")).top().width(LEN).get();
										ln.setFontScale(0.825f);
										ln.setColor(v.r, v.g, v.b, 1);
										c.image().growY().get().setColor(v.r, v.g, v.b, 1);
										Label l = c.add(k).pad(OFFSET * 1.5f).get();
										l.setFontScale(0.825f);
										l.setColor(1, 1, 1, v.a);
										h[0] = l.getPrefHeight();
									}).fillX().height(h[0]).row();
								});
							};
							
							toStorage.sortComparing(c -> c.split(" = ")[0]);
							
							func.get(original, ori);
							func.get(preview, pre);
						});
					}, "properties");
				}).growX().height(LEN).pad(OFFSET).row();
				t.button("Storage", Icon.download, Styles.cleart, () -> {
					int before = toProcess.readString().hashCode();
					
					StringBuilder stringBuilder = new StringBuilder();
					toStorage.each(s -> {
						stringBuilder.append(s.replaceAll("\n", "\\\\n")).append("\n");
					});
					
					String s = stringBuilder.toString();
					toProcess.writeString(s, false);
					if(before != s.hashCode())Vars.ui.showText("Success Successfully", before + " -> " + s.hashCode());
				}).disabled(c -> toStorage.isEmpty() || toProcess == null || !toProcess.exists()).growX().height(LEN).pad(OFFSET).row();
				
				t.pane(t1 -> {
					t1.add(ori).fill();
					t1.image().color(Pal.accent).width(OFFSET / 4).pad(OFFSET / 2).growY();
					t1.add(pre).fill();
				}).grow();
			});
			
			addCloseButton();
		}}.show();
	}
	
	public static void fireAnime(){
		Vars.ui.loadfrag.show();
		Vars.ui.loadAnd("[accent]Generating", () -> {
			Color from = NHColor.lightSkyFront, to = Color.darkGray;
			
			Fi root = NHPixmap.processedDir();
			Fi fireRoot = root.child("ult-fire");
			if(!fireRoot.exists())fireRoot.mkdirs();
			
			Rand rand = new Rand();
			
			long id = rand.nextLong();
			int num = 13;
			int total = 40;
			String name = NewHorizon.name("square"), outPut = "ult-fire-";
			int size = 160;
			float step = 1.9f;
			
			rand.setSeed(id);
			
			Pixmap square = Core.atlas.getPixmap(name).crop();
			
			for(int fIndex = 0; fIndex < total; fIndex++){
				Pixmap base = new Pixmap(size, size);
				
				for(int i = 0; i < num; i++){
					float fin = ((fIndex + rand.random(total + i)) % total) / (float)total;
					float fout = 1 - fin * 0.95f;
					float fslope = Math.max(0.515f - Math.abs(fin - 0.5f), 0.005f) * 2f;
					
					Vec2 vec2 = new Vec2().setToRandomDirection(rand).scl(rand.random(3, 6) + rand.random(4, 32) * Interp.circleOut.apply(fin) * step);
					Tmp.c1.set(from).lerp(to, rand.range(0.2f) + Interp.swingIn.apply(fin) * 0.8f * rand.random(0.75f, 1.25f));
					Pixmap pixmap = Pixmaps.scale(square, fslope * rand.random(0.15f, 0.8f) + 0.075f);
					NHPixmap.mulColor(pixmap, Tmp.c1);
					
					base.draw(pixmap, (base.width - pixmap.width) / 2 + (int)vec2.x, (base.height - pixmap.height) / 2 + (int)vec2.y, true);
				}
				
				rand.setSeed(id);
				
				PixmapIO.writePng(fireRoot.child(outPut + fIndex + ".png"), base);
				
				Vars.ui.loadfrag.setProgress((float)fIndex / total);
			}
			
			Vars.ui.loadfrag.hide();
		});
		
	}
	
	public static void textureLerp(){
		toProcess = new Fi("");
		if(toProcess.exists()){
			Pixmap pixmap = PixmapIO.readPNG(toProcess);
			pixmap.each((x, y) -> {
				tmpColor = new Color().rgba8888(pixmap.get(x, y));
				tmpColor.a(tmpColor.r);
				tmpColor.r = tmpColor.g = tmpColor.b = 1;
				tmpColor.clamp();
				pixmap.set(x, y, tmpColor);
			});
			PixmapIO.writePng(toProcess, pixmap);
		}
	}
	
	public static void texturePick(){
		toProcess = new Fi("");
		if(toProcess.exists()){
			Pixmap pixmap = PixmapIO.readPNG(toProcess);
			pixmap.each((x, y) -> {
				tmpColor = new Color().rgba8888(pixmap.get(x, y));
				if(tmpColor.r > 2 / 255f || tmpColor.g > 2 / 255f || tmpColor.b > 2 / 255f){
					tmpColor.set(Color.clear);
					pixmap.set(x, y, tmpColor);
				}
			});
			PixmapIO.writePng(toProcess, pixmap);
		}
	}
	
	public static void fixSaves(){
	
	}
}
