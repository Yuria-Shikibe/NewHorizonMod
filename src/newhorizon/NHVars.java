package newhorizon;

import arc.Core;
import mindustry.Vars;
import newhorizon.expand.cutscene.components.CutsceneControl;
import newhorizon.expand.cutscene.components.CutsceneUI;
import newhorizon.expand.game.NHWorldData;
import newhorizon.util.ui.TableFunc;

public class NHVars{
	public static NHModCore core;

	public static NHWorldData worldData;
	public static NHRenderer renderer;
	public static NHInputListener listener;
	public static NHInputControl control;

	public static CutsceneControl cutscene;
	public static CutsceneUI cutsceneUI;

	public static void init(){
		worldData = new NHWorldData();
		listener = new NHInputListener();
		control = new NHInputControl();

		cutscene = new CutsceneControl();
		cutsceneUI = new CutsceneUI();

		core = new NHModCore();
		Core.app.addListener(core);

		if(Vars.headless)return;
		renderer = new NHRenderer();
		NHSetting.loadUI();
		if(NHSetting.getBool(NHSetting.DEBUG_PANEL)) TableFunc.tableMain();
	}
}
