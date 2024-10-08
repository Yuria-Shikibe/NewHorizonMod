package newhorizon.expand;

import arc.Core;
import newhorizon.NHInputControl;
import newhorizon.NHInputListener;
import newhorizon.NHModCore;
import newhorizon.NHRenderer;
import newhorizon.expand.cutscene.NHCSS_Core;
import newhorizon.expand.game.NHWorldData;

public class NHVars{
	public static NHWorldData worldData;
	public static NHCSS_Core cutscene;
	public static NHRenderer renderer;
	public static NHModCore core;
	public static NHInputListener listener;
	public static NHInputControl control;


	public static void init(){
		if(!Core.app.isHeadless()){
		}
		worldData = new NHWorldData();
		renderer = new NHRenderer();
		listener = new NHInputListener();
		control = new NHInputControl();

		core = new NHModCore();
		Core.app.addListener(core);

	}
}
