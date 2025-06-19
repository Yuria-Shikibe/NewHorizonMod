package newhorizon.util.ui.dialog;

import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.NHLogic;


public class NHWorldSettingDialog extends BaseDialog{
    public NHWorldSettingDialog(){
        super("New Horizon World Settings");

        cont.button("Create Default Raid", Icon.logic, () -> {
            NHLogic.updateWprocList();
            NHLogic.registerDefaultRaid();

        }).size(180f * 2f + 10, 60f);

        addCloseButton();
    }
}