package newhorizon.func;

import arc.Core;
import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.mod.Mods;
import mindustry.ui.dialogs.SettingsMenuDialog;
import newhorizon.NewHorizon;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class NHSetting{
	//private static final Json json = new Json();
	private static final String initKey = "initialized";
	private static boolean debug = false;
	private static Fi setting;
	private static final Properties settingList = new Properties();
	private static String path = "";
	private static boolean loaded;
	
	public static boolean versionChange = false;
	public static final Seq<SettingEntry> entries = new Seq<>();
	public static final ObjectMap<String, String> defaultKeys = new ObjectMap<>();
	public static Mods.ModMeta modMeta = new Mods.ModMeta();
	
	static{
		defaultKeys.put("initialized", "null version");
		defaultKeys.put("@active.hid-start-log", String.valueOf(false));
		defaultKeys.put("@active.admin-panel", String.valueOf(false));
		defaultKeys.put("@active.debug", String.valueOf(false));
		
		for(String key : defaultKeys.keys()){
			if(key.startsWith("@")){
				SettingEntry entry = new SettingEntry(key);
				entries.add(entry);
			}
		}
		
		SettingEntry.add("@active.override", String.valueOf(true), true);
		SettingEntry.add("@active.advance-load*", String.valueOf(false), true);
		SettingEntry.add("@active.tool-panel*", String.valueOf(false), true);
	}
 
	public static class SettingEntry{
		public static void add(String key, String value, boolean needReload){
			entries.add(new SettingEntry(key).setNeedReload(needReload));
			defaultKeys.put(key, value);
		}
		
		public final String key;
		public String description;
		public String warning;
		public final boolean typeOfBool;
		public final boolean typeOfGraphics;
		
		public SettingEntry(String key){
			this.key = key;
			if(key.endsWith("*"))warning = Core.bundle.get(key.replaceFirst("@", "") + ".warning", "null");
			description = Core.bundle.get(key.replaceFirst("@", "") + ".description");
			typeOfBool = key.contains("active");
			typeOfGraphics = key.contains("graphics");
		}
		
		public boolean needReload = false;
		public boolean bool(){return typeOfBool;}
		public boolean warn(){return key.endsWith("*");}
		public SettingEntry setNeedReload(boolean b){
			needReload = b;
			return this;
		}
	}
	
	public static void settingFile() throws IOException{
		Fi fi = new Fi(Vars.modDirectory + "/new-horizon/NHSettings.properties");
		Fi fileParent = fi.parent();
		if (!fileParent.exists()) {
			fileParent.mkdirs();
		}
		if(!fi.exists())loaded = fi.file().createNewFile();
		path = fi.path();
		setting = fi;
		settingList.load(new FileInputStream(fi.file()));
	}
	
	public static void initSetting() throws IOException{
		Properties pro = new Properties();
		pro.load(new FileInputStream(setting.file()));
		if(pro.isEmpty()){
			pro.setProperty(initKey, "0");
			FileOutputStream fos = new FileOutputStream(setting.file());
			pro.store(fos, "Update properties");
		}
	}
	
	public static void initSettingList() throws IOException{
		debug = !Vars.headless && getBool("@active.debug");
		modMeta = Vars.mods.locateMod(NewHorizon.MOD_NAME).meta;
		
		if(!modMeta.version.equals(settingList.getProperty(initKey))){
			versionChange = true;
			updateProperty(modMeta.version);
		}
		
	}
	
	public static void updateSettingMenu(){
		SettingsMenuDialog settingTable = Vars.ui.settings;
		settingTable.game.row();
		settingTable.game.button("MOD: [sky]" + modMeta.displayName, Icon.settings, () -> {
			new SettingDialog().show();
		}).size(LEN * 6f, LEN - OFFSET);
//		for(SettingEntry entry : entries){
//			if(entry.typeOfBool){
//				if(entry.typeOfGraphics)settingTable.graphics.checkPref();
//				else settingTable.game.checkPref();
//			}
//		}
//
	}
	
	private static void updateProperty(String version) throws IOException{
		Properties pro = new Properties();
		
		defaultKeys.each((key, value) -> {
			if(!key.equals(initKey) && settingList.containsKey(key)){
				defaultKeys.put(key, settingList.getProperty(key));
			}
		});
		
		
		settingList.clear();
		loaded = !setting.file().delete();
		
		defaultKeys.each( (key, name) -> {
			if(key.equals(initKey))pro.setProperty(initKey, version);
			else pro.setProperty(key, name);
		});

		FileOutputStream fos;
		try{
			fos = new FileOutputStream(path);
		}catch(FileNotFoundException e){
			throw new RuntimeException(e);
		}
		pro.store(fos, version);
		settingList.load(new FileInputStream(setting.file()));
	}
	
	public static void updateSettingFi() throws IOException{
		FileOutputStream fos;
		try{
			fos = new FileOutputStream(setting.file());
		}catch(FileNotFoundException e){
			throw new RuntimeException(e);
		}
		settingList.store(fos, "Update properties");
	}
	
	public static boolean getBool(String key){
		return Boolean.parseBoolean(settingList.getProperty(key));
	}
	
	public static void debug(Runnable run){
		if((Vars.headless && Vars.state.rules.infiniteResources) || (debug || (Vars.net.client() && Vars.state.rules.infiniteResources)))run.run();
	}
	
	public static void log(Object message){
		debug(() -> Log.info(message));
	}
	
	public static void setBoolOnce(String key, boolean bool){
		if(key.startsWith("@"))settingList.setProperty(key, String.valueOf(bool));
		else debug(() -> Log.info("Target key is not a Boolean"));
		try{
			updateSettingFi();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public static void settingApply(){
		TableFs.disableTable();
		debug = getBool("@active.debug");
		if(NHSetting.getBool("@active.tool-panel*"))TableFs.showTable();
	}
}
