package newhorizon.func;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.util.Log;
import mindustry.Vars;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class NHSetting{
	private static Fi setting;
	private static Properties settingList = new Properties();
	private static String path = "";
	private static boolean loaded;
	public static final ObjectMap<String, String> defaultKeys = new ObjectMap<>();
	private static final String initKey = "initialized";
	
	static{
		defaultKeys.put("initialized", "null version");
		defaultKeys.put("@active.tool-panel*", String.valueOf(false));
		defaultKeys.put("@active.admin-panel", String.valueOf(false));
		defaultKeys.put("@active.advance-load*", String.valueOf(false));
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
		Mods.ModMeta meta = new Mods.ModMeta();
		for(Mods.LoadedMod mod : Vars.mods.list()){
			if(mod == null || mod.main == null)continue;
			if(mod.main.getClass() == NewHorizon.class){
				meta = mod.meta;
				break;
			}
		}
		if(!meta.version.equals(settingList.getProperty(initKey)))updateProperty(meta.version);
	}
	
	private static void updateProperty(String version) throws IOException{
		Log.info(settingList);
		Properties pro = new Properties();
		
		defaultKeys.each((key, value) -> {
			if(!key.equals(initKey) && settingList.containsKey(key)){
				defaultKeys.put(key, settingList.getProperty(key));
			}
		});
		Log.info("[MAP]" + defaultKeys);
		
		settingList.clear();
		loaded = !setting.file().delete();
		
		defaultKeys.each( (key, name) -> {
			if(key.equals(initKey))pro.setProperty(initKey, version);
			else pro.setProperty(key, name);
		});
		
		
		Log.info(pro);

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
	
	public static void setBoolOnce(String key, boolean bool){
		if(key.startsWith("@"))settingList.setProperty(key, String.valueOf(bool));
		else Log.info("Pro target key is not a Boolean");
		try{
			updateSettingFi();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public static void settingApply(){
		TableFuncs.disableTable();
		if(NHSetting.getBool("@active.tool-panel*"))TableFuncs.showTable();
	}
	
}
