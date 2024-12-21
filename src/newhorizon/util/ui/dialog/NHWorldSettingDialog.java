package newhorizon.util.ui.dialog;

import arc.Core;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NHVars;
import newhorizon.expand.game.NHWorldData;

import java.lang.reflect.Field;


public class NHWorldSettingDialog extends BaseDialog{
	public static NHWorldData data(){
		return NHVars.worldData;
	}
	
	public Table settings;
	
	public static Seq<SettingEntry> allSettings = new Seq<>();
	
	public static Jval settingsJson;
	
	public static final String SETTINGS_KEY = "nh-world-settings"; 
	
	static{
		Class<NHWorldData> dataClass = NHWorldData.class;
		
		try{
			allSettings.addAll(
				new BoolSetting(dataClass.getField("jumpGateUsesCoreItems"), true),
				new BoolSetting(dataClass.getField("applyEventTriggers"), false)
			);
		}catch(NoSuchFieldException e){
			e.printStackTrace();
		}
	}
	
	public NHWorldSettingDialog(){
		super("New Horizon World Settings");
		
//		settings = new Table();
//		settings.left();
//		settings.defaults().growX().fillY();
//
//		Log.info(allSettings.size);
//
//		settings.add("213").row();
//
//		settings.add("213").row();
		
		cont.pane(new Table(t -> {
			allSettings.each(e -> e.buildTable(t));
		})).margin(60f).growY().fillX();
		
		addCloseButton();
	}
	
	@Override
	public Dialog show(){
		if(Vars.editor.tags.containsKey(SETTINGS_KEY)){
			settingsJson = Jval.read(Vars.editor.tags.get(SETTINGS_KEY));
			allSettings.each(entry -> {
				if(!settingsJson.has(entry.key))entry.initRules();
			});
		}else{
			settingsJson = Jval.newObject();
			allSettings.each(SettingEntry::initRules);
		}
		
		return super.show();
	}
	
	@Override
	public void hide(){
		Vars.editor.tags.put(SETTINGS_KEY, settingsJson.toString(Jval.Jformat.plain));
		super.hide();
	}
	
	public static void writeToWorldData(){
		Jval initContext = Jval.read(Vars.state.map.tags.get(SETTINGS_KEY));
		allSettings.each(e -> e.initWorldData(initContext));
	}
	
	public static class BoolSetting extends SettingEntry{
		public boolean def = false;
		
		public BoolSetting(Field dataField){
			super(dataField);
		}
		
		public BoolSetting(Field dataField, boolean def){
			super(dataField);
			this.def = def;
		}
		
		@Override
		public Boolean defData(){
			return def;
		}
		
		@Override
		public void initWorldData(Jval jval){
			try{
				dataField.set(data(), jval.getBool(dataField.getName(), def));
			}catch(IllegalAccessException e){
				e.printStackTrace();
			}
		}
		
		@Override
		public void initRules(){
			settingsJson.put(dataField.getName(), def);
		}
		
		@Override
		public void buildTable(Table table){
			String name = Core.bundle.get("nh.world-settings." + key + ".name");
			String desc = Core.bundle.getOrNull("nh.world-settings." + key + ".description");
			
			table.table(set -> {
				set.marginLeft(12f);
				
				CheckBox box = new CheckBox(name);
				
				box.update(() -> {
					box.setChecked(settingsJson.getBool(key, def));
				});
				
				box.changed(() -> {
					settingsJson.put(key, !settingsJson.getBool(key, def));
				});
				
				box.left();
				
				set.add(box).left();
				
				if(desc != null)Vars.ui.addDescTooltip(set, desc);
			}).left().padBottom(6f).expandX().height(50f).row();
		}
	}
	
	public static abstract class SettingEntry{
		public final Field dataField;
		public final String key;
		
		public SettingEntry(Field dataField){
			this.dataField = dataField;
			key = dataField.getName();
		}
		
		public abstract void initWorldData(Jval jval);
		public abstract void initRules();
		public abstract void buildTable(Table table);
		
		public abstract Object defData();
	}
}
