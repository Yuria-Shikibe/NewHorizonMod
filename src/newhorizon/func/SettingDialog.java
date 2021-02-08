package newhorizon.func;


import arc.Core;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

import static newhorizon.func.TableFuncs.LEN;

public class SettingDialog extends Dialog{
	public SettingDialog(){
		super("@nh-setting");
		
		cont.table(table -> {
			for(String key : NHSetting.defaultKeys.keys()){
				if(!key.startsWith("@"))continue;
				table.table(t -> {
					t.button(key, Styles.clearTogglet, () -> {
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
}
