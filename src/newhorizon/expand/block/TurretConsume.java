package newhorizon.expand.block;

import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;

import static mindustry.Vars.iconMed;

public class TurretConsume extends ConsumeLiquid{
	public TurretConsume(Liquid liquid, float amount){
		super(liquid, amount);
		booster = true;
	}
	
	@Override
	public void build(Building build, Table table){
		table.add(new ReqImage(liquid.uiIcon, () -> build.liquids.get(liquid) > 0)).size(iconMed).top().left();
	}
	
	@Override
	public void display(Stats stats){
		stats.add(booster ? Stat.booster : Stat.input, liquid, amount * 60f, true);
	}
}
