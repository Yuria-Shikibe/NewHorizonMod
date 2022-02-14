package newhorizon.util.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SettingsMenuDialog;
import newhorizon.content.NHContent;

import static newhorizon.util.Tool_JsonHandler.*;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class NHWorldSettingDialog extends BaseDialog{
	public static NHWorldSettingDialog dialog;
	
	public Jval settings; //Json Map
	
	public void addSliderSetting(Table table, String key, String requirements, int def, int min, int max, int step, SettingsMenuDialog.StringProcessor sp){
		Slider slider = new Slider(min, max, step, false);
		
		initKey_Context(key, def);
		
		slider.setValue(settings.getInt(key, def));
		
		Label value = new Label("", Styles.outlineLabel);
		Table content = new Table();
		content.add(Core.bundle.get("nh.setting." + key + ".name"), Styles.outlineLabel).left().growX().wrap();
		content.add(value).padLeft(10f).right();
		content.margin(3f, 33f, 3f, 33f);
		content.touchable = Touchable.disabled;
		
		slider.changed(() -> {
			settings.put(key, (int)slider.getValue());
			value.setText(sp.get((int)slider.getValue()));
		});
		
		slider.update(() -> {
			boolean d = requirements != null && !settings.getBool(requirements, false);
			slider.setDisabled(d);
			slider.touchable = d ? Touchable.disabled : Touchable.enabled;
			slider.color.set(d ? Color.gray : Color.white);
		});
		
		slider.change();
		table.stack(slider, content).width(Math.min(Core.graphics.getWidth() / 1.2f, 980f)).left().padTop(4f);
		table.row();
	}
	
	public void addBoolSetting(Table table, String key, String requirements, boolean def){
		CheckBox box = new CheckBox(Core.bundle.get("nh.setting." + key + ".name"));
		
		initKey_Context(key, def);
		
		box.update(() -> box.setChecked(settings.getBool(key, def)));
		
		box.changed(() -> {
			settings.put(key, box.isChecked());
		});
		
		box.setDisabled(() -> requirements != null && !settings.getBool(requirements, false));
		
		box.left();
		table.add(box).growX().left().padTop(3f);
		table.row();
	}
	
	public void addBoolSetting(Table table, String key, boolean def){
		addBoolSetting(table, key, null, def);
	}
	
	public NHWorldSettingDialog(){
		super("@mod.ui.nh-extra-menu");
		
		if(Vars.editor.tags.containsKey(ALL_SETTINGS))settings = Jval.read(Vars.editor.tags.get(ALL_SETTINGS));
		else settings = Jval.newObject();
		cont.pane(table -> {
			table.button("@mod.ui.cutscene-menu", new TextureRegionDrawable(NHContent.icon), () -> {
				new CutsceneMenu().show();
			}).height(120f).growX().margin(OFFSET).marginLeft(LEN).pad(OFFSET).padBottom(OFFSET).row();
			
			table.image().growX().height(OFFSET / 4).pad(OFFSET / 2).padLeft(LEN).padRight(LEN).color(Color.lightGray).row();
			
			setContext(settings);
			
			
			addBoolSetting(table, JUMP_GATE_USE_CORE_ITEMS, false);
			addBoolSetting(table, JUMP_GATE_CHEAT_ENABLED, true);
			addBoolSetting(table, BEACON_ENABLE, false);
//			addBoolSetting(table, BEACON_FIELD_POLYGON, BEACON_ENABLE, false);
			addBoolSetting(table, BEACON_UNIT_FIELD, BEACON_ENABLE, true);
			addSliderSetting(table, BEACON_CAPTURE_SCORE, BEACON_ENABLE, 1_000_000, 500_000, 100_000_000, 50000, s -> s + " Score");
			
			endContext();
		}).margin(OFFSET * 2).grow().self(t -> {
			if(!Vars.mobile)t.padLeft(LEN * 2f).padRight(LEN * 2f);
		});
		
		
		
		addCloseButton();
	}
	
	@Override
	public void hide(){
		super.hide();
		
		Vars.editor.tags.put(ALL_SETTINGS, settings.toString());
	}
}
