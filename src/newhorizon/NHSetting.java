package newhorizon;

import arc.Core;
import arc.scene.actions.RunnableAction;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.NHContent;

import static arc.Core.settings;
import static newhorizon.util.ui.TableFunc.LEN;

public class NHSetting{
	public static final String
			EFFECT_DETAIL = "nh_effectdetail",
			VANILLA_COST_OVERRIDE = "nh_overridecost",
			DEBUGGING = "nh_debugging",
			START_LOG = "nh_hide_starting_log",
			EXPERIMENTAL = "nh_experimental",
			TERRAIN_MODE = "nh_terrain_mode",
			DEBUG_PANEL = "nh_debug_panel";
	
	public static boolean enableEffectDetail = true;
	
	public static boolean changed = false;
	
	public static Seq<SettingKey<?>> all = new Seq<>();
	
	public static void load(){
		all.addAll(
			new BoolSetting(EFFECT_DETAIL, true, true),
			new BoolSetting(VANILLA_COST_OVERRIDE, false, true),
			//todo debugging need remake
			new BoolSetting(DEBUGGING, false, true),
			new BoolSetting(START_LOG, false, false),
			new BoolSetting(EXPERIMENTAL, false, true),
			new BoolSetting(TERRAIN_MODE, false, true),
			new BoolSetting(DEBUG_PANEL, false, true)
		);
		
		all.each(SettingKey::setDefault);
		
		enableEffectDetail = getBool(EFFECT_DETAIL);
	}
	
	public static void loadUI(){
		Vars.ui.settings.addCategory("@mod.ui.nh-extra-menu", new TextureRegionDrawable(NHContent.icon), NHSetting::buildTable);
	}
	
	static void update(){
		enableEffectDetail = getBool(EFFECT_DETAIL);
	}
	
	public static void buildTable(Table table){
		table.pane(t -> {
			all.each(s -> s.buildTable(t));
		}).margin(LEN).get().setForceScroll(false, true);
	}
	
	public static void showDialog(){
		new BaseDialog("@nh.setting"){{
			buildTable(cont);
			addCloseButton();
		}
			
			@Override
			public void hide(){
				super.hide();
				
				if(changed){
					Vars.ui.showConfirm("@mod.reloadrequired", () -> {
						Core.app.exit();
					});
				}
			}
		}.show();
	}
	
	public static abstract class SettingKey<T>{
		public SettingKey(String key){
			this.key = key;
		}
		
		
		
		public final String key;
		public boolean requireReload = false;
		
		public String name(){
			return Core.bundle.get("nh.setting." + key + ".name");
		}
		
		public String desc(){
			return Core.bundle.get("nh.setting." + key + ".desc");
		}
		
		public String warn(){
			return Core.bundle.getOrNull("nh.setting." + key + ".warn");
		}
		
		public boolean hasWarn(){
			return warn() != null;
		}
		
		public abstract T getValue();
		
		public abstract void setDefault();
		
		public abstract void buildTable(Table table);
	}
	
	public static class BoolSetting extends SettingKey<Boolean>{
		public boolean def = false;
		
		public BoolSetting(String key){
			super(key);
		}
		
		public BoolSetting(String key, boolean def){
			super(key);
			this.def = def;
		}
		
		public BoolSetting(String key, boolean def, boolean requireReload){
			super(key);
			this.def = def;
			this.requireReload = requireReload;
		}
		
		@Override
		public Boolean getValue(){
			return Core.settings.getBool(key);
		}
		
		@Override
		public void setDefault(){
			if(!Core.settings.has(key))Core.settings.put(key, def);
		}
		
		@Override
		public void buildTable(Table table){
			table.table(Tex.pane, t -> {
				CheckBox box;
				t.add(box = new CheckBox(name())).padRight(6f).left();
				Button b = t.button(Icon.info, Styles.cleari, () -> {
				
				}).right().get();
				
				t.row().collapser(i -> {
					i.left();
					i.defaults().left();
					i.add("@info.title").row();
					i.add(desc()).row();
					
					if(hasWarn()){
						i.add("@warning").color(Pal.redderDust).row();
						i.add(warn());
					}
				}, true, b::isChecked).growX();
				
				box.changed(() -> {
					settings.put(key, box.isChecked());
					if(requireReload){
						if(!changed){
							Dialog.setHideAction(() -> new RunnableAction(){{
								setRunnable(() -> {
									Vars.ui.showConfirm("@mod.reloadrequired", () -> {
										Core.app.exit();
									});
								});
							}});
						}
						changed = true;
					}
				});
				
				box.left();
				
				box.update(() -> box.setChecked(settings.getBool(key)));
			}).tooltip(desc()).growX().fillY().margin(8f).left().row();
		}
	}
	
	public static boolean enableDetails(){
		return enableEffectDetail;
	}
	
	public static boolean getBool(String key){
		return Core.settings.getBool(key);
	}
}
