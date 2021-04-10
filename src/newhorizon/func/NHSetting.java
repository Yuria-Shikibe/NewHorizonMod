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
	//private static final Json json = new Json();
	private static final String initKey = "initialized";
	private static boolean debug = false;
	private static Fi setting;
	private static final Properties settingList = new Properties();
	private static String path = "";
	private static boolean loaded;
	
	public static final ObjectMap<String, String> defaultKeys = new ObjectMap<>();
	public static Mods.ModMeta modMeta = new Mods.ModMeta();
	
	static{
		defaultKeys.put("initialized", "null version");
		defaultKeys.put("@active.tool-panel*", String.valueOf(false));
		defaultKeys.put("@active.admin-panel", String.valueOf(false));
		defaultKeys.put("@active.advance-load*", String.valueOf(false));
		defaultKeys.put("@active.debug", String.valueOf(false));
	}
	
//	public static void initJson(){
//		json = Jval.read()
//	}
	
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
		for(Mods.LoadedMod mod : Vars.mods.list()){
			if(mod == null || mod.main == null)continue;
			if(mod.main.getClass() == NewHorizon.class){
				modMeta = mod.meta;
				break;
			}
		}
		
		debug = !Vars.headless && getBool("@active.debug");
		//modMeta = Vars.mods.locateMod(NewHorizon.NHNAME.substring(0, NewHorizon.NHNAME.length() - 1)).meta;
		
		if(!modMeta.version.equals(settingList.getProperty(initKey)))updateProperty(modMeta.version);
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
	
	public static void log(String message){
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
