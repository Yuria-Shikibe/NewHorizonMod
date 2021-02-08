package newhorizon.func;


import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static newhorizon.func.TableFuncs.LEN;
import static newhorizon.func.TableFuncs.OFFSET;

public class SettingDialog extends Dialog{
	public SettingDialog(){
		super("@nh-setting");
		cont.table(table -> {
			for(String key : NHSetting.defaultKeys.keys()){
				if(!key.startsWith("@"))continue;
				table.table(t -> {
					t.button(key, Styles.clearTogglet, () -> {
						if(key.endsWith("*"))setting(key, Core.bundle.get(key.replaceAll("@", "")), Core.bundle.get(key + ".extra", "@null"));
						NHSetting.setBoolOnce(key, !NHSetting.getBool(key));
					}).size(LEN * 4, LEN).update(b -> b.setChecked(NHSetting.getBool(key)));
				}).row();
			}
		});
		
		cont.row().button("@back", Icon.left, Styles.cleart, () -> {
			this.hide();
			NHSetting.settingApply();
		}).fillX().height(TableFuncs.LEN).pad(TableFuncs.OFFSET / 3);
		this.keyDown((key) -> {
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
				}).fillX().height(LEN).row();
				dialog.cont.image().fillX().height(OFFSET / 3).color(Pal.ammo).row();
			}else{
				dialog.cont.pane(t -> {
					t.add("Are you sure you want to active " + description + "?").color(Color.gray).padTop(OFFSET / 4);
				}).fillX().height(LEN).row();
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
			}).fillX().height(LEN).row();
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
