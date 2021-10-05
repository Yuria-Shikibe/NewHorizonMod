package newhorizon.feature;

import arc.files.Fi;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Align;
import arc.util.io.PropertiesUtils;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mods;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NewHorizon;

import java.util.Comparator;

import static newhorizon.func.TableFunc.LEN;
import static newhorizon.func.TableFunc.OFFSET;

public class InternalTools{
	private static Fi toProcess;
	
	
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
									toStorage.add(k + " = <TODO>" + v);
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
								kvs.keys().toSeq().sort(Comparator.comparing(s -> s.split(" = ")[0])).each(k -> {
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
							
							toStorage.sort(Comparator.comparing(s -> s.split(" = ")[0]));
							
							func.get(original, ori);
							func.get(preview, pre);
						});
					}, "properties");
				}).growX().height(LEN).pad(OFFSET).row();
				t.button("Storage", Icon.download, Styles.cleart, () -> {
					int before = toProcess.readString().hashCode();
					
					StringBuilder stringBuilder = new StringBuilder();
					toStorage.each(s -> {
						stringBuilder.append(s.replaceAll("\n", "\\n")).append("\n");
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
}
