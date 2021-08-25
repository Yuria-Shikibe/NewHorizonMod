package newhorizon.func;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.mod.Mods;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SettingsMenuDialog;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static mindustry.Vars.ui;
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
	
	private static float originalZoomMin = 0.5f, originalZoomMax = 5f;
	
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
		
		SettingEntry.add("@active.override", String.valueOf(false), true);
		SettingEntry.add("@active.advance-load*", String.valueOf(false), true);
		SettingEntry.add("@active.tool-panel*", String.valueOf(false), true);
		SettingEntry.add("@active.double-zoom*", String.valueOf(false), true);
	}
 
	public static void loadSettings(){
		Vars.ui.settings.graphics.checkPref("enableeffectdetails", true);
		
		originalZoomMin = Vars.renderer.minZoom;
		originalZoomMax = Vars.renderer.maxZoom;
		
		if(NHSetting.getBool("@active.double-zoom")){
			Vars.renderer.maxZoom = originalZoomMax * 4;
			Vars.renderer.minZoom = 0.6f;
		}else{
			Vars.renderer.maxZoom = originalZoomMax;
			Vars.renderer.minZoom = originalZoomMin;
		}
		
		applySettings();
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
		modMeta = Vars.mods.getMod(NewHorizon.class).meta;
		
		if(!modMeta.version.equals(settingList.getProperty(initKey))){
			versionChange = true;
			updateProperty(modMeta.version);
		}
	}
	
	public static void updateSettingMenu(){
		SettingsMenuDialog settingTable = Vars.ui.settings;
		settingTable.game.row();
		settingTable.game.button("MOD: [sky]" + modMeta.displayName, new TextureRegionDrawable(NHContent.icon2), LEN, () -> {
			new SettingDialog().show();
		}).size(LEN * 6f, LEN + OFFSET);
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
	
	public static void applySettings(){
		TableFs.disableTable();
		debug = getBool("@active.debug");
		if(NHSetting.getBool("@active.tool-panel*"))TableFs.showTable();
	}
	
	
	public static boolean enableDetails(){return Core.settings.getBool("enableeffectdetails");}
	
	public static class SettingDialog extends BaseDialog{
		private static boolean changed = false;
		
		public SettingDialog(){
			super("@nh-setting");
			setFillParent(true);
			cont.pane(table -> {
				table.center();
//				table.pane(t -> {
//					t.left();
//					t.marginLeft(OFFSET);
//					t.add("@mod.ui.setting-dialog");
//					t.add("[gray]You can get back here through: ").left().row();
//					t.add("[accent]<ModDialog>[gray] -> [accent]NewHorizonMod[gray] -> [accent]<View Content>[gray] -> ").left().padLeft(LEN).row();
//					t.add("[accent]<VanillaSettings>[gray] -> [accent]" + Core.bundle.get("settings.game") + "[gray] -> [accent]NEW HORIZON[gray] -> ").left().padLeft(LEN).row();
//					t.add("@settings").color(Pal.lancerLaser).left().padLeft(LEN * 2f).row();
//				}).growX().height(LEN * 2f).row();
				table.image().color(Pal.accent).growX().height(OFFSET / 3).pad(OFFSET / 2).row();
				for(SettingEntry key : entries){
					table.table(t -> {
						t.button(key.key, Styles.clearToggleMenut, () -> {
							if(key.warn() && !getBool(key.key))setting(key);
							else {
								setChanged(key);
								setBoolOnce(key.key, !getBool(key.key));
							}
						}).height(LEN).growX().update(b -> b.setChecked(key.bool() && getBool(key.key)));
						t.button(Icon.info, Styles.clearPartiali, LEN, () -> {
							new BaseDialog("@info"){{
								addCloseButton();
								cont.table(t -> {
									t.image().growX().height(OFFSET / 3).color(Pal.accent).pad(OFFSET / 3);
									t.add(key.key).color(Pal.accent);
									t.image().growX().height(OFFSET / 3).color(Pal.accent).pad(OFFSET / 3).row();
								}).growX().fillY().row();
								cont.pane(t->{
									t.add(key.description).color(Color.lightGray);
								}).growX().fillY();
							}}.show();
						}).size(LEN * 3, LEN);
					}).growX().fillY().padLeft(LEN).padRight(LEN).row();
				}
			}).grow();
			
			buttons.defaults().size(210f, 64f);
			buttons.button("@back", Icon.left, this::close).size(210f, 64f);
			
			this.keyDown((key) -> {
				if (key == KeyCode.escape || key == KeyCode.back) {
					close();
				}
			});
		}
		
		public void close(){
			if(changed)ui.showInfoOnHidden(Core.bundle.get("mod.ui.require.reload"), () -> Core.app.exit());
			else Core.app.post(this::hide);
		}
		
		private static void setting(SettingEntry key){
			if(!getBool(key.key)){
				BaseDialog dialog = new BaseDialog("Caution");
				dialog.addCloseListener();
				if(Vars.mobile){
					dialog.cont.pane(t -> {
						t.add("[gray]The " + key.description + " [lightgray]NOT SUITABLE[gray] for [lightgray]PHONES[gray].").row();
						t.add("ARE YOU SURE YOU WANT TO ACTIVATE IT?").color(Pal.ammo).padTop(OFFSET / 4).row();
						t.add(key.warning).color(Pal.ammo).padTop(OFFSET / 4);
					}).fill().row();
					dialog.cont.image().fillX().height(OFFSET / 3).color(Pal.ammo).row();
				}else{
					dialog.cont.pane(t -> {
						t.add("Are you sure you want to activate it?").color(Color.gray).padTop(OFFSET / 4);
					}).fill().row();
					dialog.cont.image().fillX().height(OFFSET / 3).color(Color.gray).row();
				}
				dialog.cont.pane(t -> {
					t.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN);
					t.button("@yes", Icon.play, Styles.cleart, () -> {
						setBoolOnce(key.key, true);
						setChanged(key);
						dialog.hide();
					}).size(LEN * 3, LEN).padLeft(OFFSET / 2);
				}).padTop(OFFSET / 2).fillX();
				dialog.show();
			}else{
				BaseDialog dialog = new BaseDialog("Caution");
				dialog.addCloseListener();
				dialog.cont.pane(t -> {
					t.add("Are you sure you want to disable it?").color(Color.gray).padTop(OFFSET / 4);
				}).fill().row();
				dialog.cont.image().fillX().height(OFFSET / 3).color(Color.gray).row();
				dialog.cont.pane(t -> {
					t.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN);
					t.button("@yes", Icon.play, Styles.cleart, () -> {
						setBoolOnce(key.key, false);
						setChanged(key);
						dialog.hide();
					}).size(LEN * 3, LEN).padLeft(OFFSET / 2);
				}).padTop(OFFSET / 2).fillX();
				dialog.show();
			}
		}
		
		private static void setChanged(SettingEntry key){
			if(key.needReload)changed = true;
		}
	}
}
