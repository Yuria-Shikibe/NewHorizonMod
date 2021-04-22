package newhorizon.func;


import arc.graphics.Color;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class SettingDialog extends BaseDialog{
	public SettingDialog(){
		super("@nh-setting");
		setFillParent(true);
		cont.pane(table -> {
			table.pane(t -> {
				t.add("[gray]You can get back to here by [accent]<ModDialog>[gray] -> [accent]NewHorizonMod[gray] -> [accent]<View Content>[gray] -> ");
				t.add("@settings").color(Pal.lancerLaser).row();
			}).growX().height(LEN).row();
			table.image().color(Pal.accent).growX().height(OFFSET / 4).pad(OFFSET / 2).row();
			for(NHSetting.SettingEntry key : NHSetting.entries){
				table.table(t -> {
					t.button(key.key, Styles.clearTogglet, () -> {
						if(key.warn())setting(key);
						else NHSetting.setBoolOnce(key.key, !NHSetting.getBool(key.key));
					}).height(LEN).growX().update(b -> b.setChecked(key.bool() && NHSetting.getBool(key.key)));
					t.button(Icon.info, Styles.cleari, LEN, () -> {
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
					}).size(LEN);
				}).growX().fillY().padLeft(LEN).padRight(LEN).row();
			}
		}).grow();
		
		addCloseButton();
	}
	
	
	private static void setting(NHSetting.SettingEntry key){
		if(!NHSetting.getBool(key.key)){
			BaseDialog dialog = new BaseDialog("Caution");
			dialog.addCloseListener();
			if(Vars.mobile){
				dialog.cont.pane(t -> {
					t.add("[gray]The " + key.description + " [lightgray]IS NOT SUITABLE[gray] for [lightgray]PHONES[gray].").row();
					t.add("ARE YOU SURE YOU WANT TO ACTIVE IT?").color(Pal.ammo).padTop(OFFSET / 4).row();
					t.add(key.warning).color(Pal.ammo).padTop(OFFSET / 4);
				}).fill().row();
				dialog.cont.image().fillX().height(OFFSET / 3).color(Pal.ammo).row();
			}else{
				dialog.cont.pane(t -> {
					t.add("Are you sure you want to active it ?").color(Color.gray).padTop(OFFSET / 4);
				}).fill().row();
				dialog.cont.image().fillX().height(OFFSET / 3).color(Color.gray).row();
			}
			dialog.cont.pane(t -> {
				t.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN);
				t.button("@yes", Icon.play, Styles.cleart, () -> {
					NHSetting.setBoolOnce(key.key, true);
					dialog.hide();
				}).size(LEN * 3, LEN).padLeft(OFFSET / 2);
			}).padTop(OFFSET / 2).fillX();
			dialog.show();
		}else{
			BaseDialog dialog = new BaseDialog("Caution");
			dialog.addCloseListener();
			dialog.cont.pane(t -> {
				t.add("Are you sure you want to disable it ?").color(Color.gray).padTop(OFFSET / 4);
			}).fill().row();
			dialog.cont.image().fillX().height(OFFSET / 3).color(Color.gray).row();
			dialog.cont.pane(t -> {
				t.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN);
				t.button("@yes", Icon.play, Styles.cleart, () -> {
					NHSetting.setBoolOnce(key.key, false);
					dialog.hide();
				}).size(LEN * 3, LEN).padLeft(OFFSET / 2);
			}).padTop(OFFSET / 2).fillX();
			dialog.show();
		}
	}
}
