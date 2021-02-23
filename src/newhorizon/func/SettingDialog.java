package newhorizon.func;


import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static newhorizon.func.TableFuncs.LEN;
import static newhorizon.func.TableFuncs.OFFSET;

public class SettingDialog extends BaseDialog{
	public SettingDialog(){
		super("@nh-setting");
		setFillParent(true);
		cont.pane(table -> {
			table.pane(t -> {
				t.add("[gray]You can get back to here by [accent]<ModDialog>[gray] -> [accent]NewHorizonMod[gray] -> [accent]<View Content>[gray] -> ");
				t.add("@settings").color(Pal.lancerLaser).row();
			}).width(Core.graphics.getWidth() / 2f).height(LEN).row();
			table.image().color(Pal.accent).growX().height(OFFSET / 4).pad(OFFSET / 2).row();
			for(String key : NHSetting.defaultKeys.keys()){
				if(!key.startsWith("@"))continue;
				table.table(t -> {
					t.button(key, Styles.clearTogglet, () -> {
						if(key.endsWith("*"))setting(key, Core.bundle.get(key.replaceAll("@", "")), Core.bundle.get((key.replaceFirst("@", "") + ".extra"), "@null"));
						else NHSetting.setBoolOnce(key, !NHSetting.getBool(key));
					}).height(LEN).width(Core.graphics.getWidth() / 2f).update(b -> b.setChecked(NHSetting.getBool(key)));
				}).row();
			}
		}).width(Core.graphics.getWidth() / 2f).fillY();
		
		cont.row().button("@back", Icon.left, Styles.cleart, () -> {
			hide();
			NHSetting.settingApply();
		}).fillX().height(TableFuncs.LEN).bottom();
		keyDown((key) -> {
			if (key == KeyCode.escape || key == KeyCode.back) {
				Core.app.post(this::hide);
				NHSetting.settingApply();
			}
		});
	}
	
	private static void setting(String key, String description){
		setting(key, description, "");
	}
	
	private static void setting(String key, String description, String caution){
		if(!NHSetting.getBool(key)){
			BaseDialog dialog = new BaseDialog("Caution");
			dialog.addCloseListener();
			if(Vars.mobile){
				dialog.cont.pane(t -> {
					t.add("[gray]The " + description + " [lightgray]IS NOT SUITABLE[gray] for [lightgray]PHONES[gray].").row();
					t.add("ARE YOU SURE YOU WANT TO ACTIVE IT?").color(Pal.ammo).padTop(OFFSET / 4).row();
					t.add(caution).color(Pal.ammo).padTop(OFFSET / 4);
				}).fill().row();
				dialog.cont.image().fillX().height(OFFSET / 3).color(Pal.ammo).row();
			}else{
				dialog.cont.pane(t -> {
					t.add("Are you sure you want to active " + description + "?").color(Color.gray).padTop(OFFSET / 4);
				}).fill().row();
				dialog.cont.image().fillX().height(OFFSET / 3).color(Color.gray).row();
			}
			dialog.cont.pane(t -> {
				t.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN);
				t.button("@yes", Icon.play, Styles.cleart, () -> {
					NHSetting.setBoolOnce(key, true);
					dialog.hide();
				}).size(LEN * 3, LEN).padLeft(OFFSET / 2);
			}).padTop(OFFSET / 2).fillX();
			dialog.show();
		}else{
			BaseDialog dialog = new BaseDialog("Caution");
			dialog.addCloseListener();
			dialog.cont.pane(t -> {
				t.add("Are you sure you want to disable " + description + "?").color(Color.gray).padTop(OFFSET / 4);
			}).fill().row();
			dialog.cont.image().fillX().height(OFFSET / 3).color(Color.gray).row();
			dialog.cont.pane(t -> {
				t.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN);
				t.button("@yes", Icon.play, Styles.cleart, () -> {
					NHSetting.setBoolOnce(key, false);
					dialog.hide();
				}).size(LEN * 3, LEN).padLeft(OFFSET / 2);
			}).padTop(OFFSET / 2).fillX();
			dialog.show();
		}
	}
}
