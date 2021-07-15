package newhorizon.func;


import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.ui;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class SettingDialog extends BaseDialog{
	private static boolean changed = false;
	
	public SettingDialog(){
		super("@nh-setting");
		setFillParent(true);
		cont.pane(table -> {
			table.top();
			table.pane(t -> {
				t.left();
				t.marginLeft(OFFSET);
				t.add("@mod.ui.setting-dialog");
				t.add("[gray]You can get back here through: ").left().row();
				t.add("[accent]<ModDialog>[gray] -> [accent]NewHorizonMod[gray] -> [accent]<View Content>[gray] -> ").left().padLeft(LEN).row();
				t.add("[accent]<VanillaSettings>[gray] -> [accent]" + Core.bundle.get("settings.game") + "[gray] -> [accent]NEW HORIZON[gray] -> ").left().padLeft(LEN).row();
				t.add("@settings").color(Pal.lancerLaser).left().padLeft(LEN * 2f).row();
			}).growX().height(LEN * 2f).row();
			table.image().color(Pal.accent).growX().height(OFFSET / 4).pad(OFFSET / 2).row();
			for(NHSetting.SettingEntry key : NHSetting.entries){
				table.table(t -> {
					t.button(key.key, Styles.clearTogglet, () -> {
						if(key.warn() && !NHSetting.getBool(key.key))setting(key);
						else {
							setChanged(key);
							NHSetting.setBoolOnce(key.key, !NHSetting.getBool(key.key));
						}
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
	
	private static void setting(NHSetting.SettingEntry key){
		if(!NHSetting.getBool(key.key)){
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
					NHSetting.setBoolOnce(key.key, true);
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
					NHSetting.setBoolOnce(key.key, false);
					setChanged(key);
					dialog.hide();
				}).size(LEN * 3, LEN).padLeft(OFFSET / 2);
			}).padTop(OFFSET / 2).fillX();
			dialog.show();
		}
	}
	
	private static void setChanged(NHSetting.SettingEntry key){
		if(key.needReload)changed = true;
	}
}
