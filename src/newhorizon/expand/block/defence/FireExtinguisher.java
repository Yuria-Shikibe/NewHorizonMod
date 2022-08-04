package newhorizon.expand.block.defence;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.EnumSet;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.Fires;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.util.graphic.OptionalMultiEffect;

import static mindustry.Vars.tilesize;

public class FireExtinguisher extends Block{
	public float range = 280f;
	public float intensity = 500;
	public float reloadTime = 120f;
	public float coolantMultiplier = 1f;
	
	public Color baseColor = NHColor.lightSkyBack;
	
	public Effect extinguishEffect = new OptionalMultiEffect(NHFx.circle, new Effect(50f, 500f, e -> {
		Draw.color(e.color, Color.white, e.fout() * 0.65f);
		Fill.circle(e.x, e.y, Vars.tilesize / 2f * e.fout());
		Lines.stroke(3 * e.fout());
		Lines.spikes(e.x, e.y, e.rotation * e.finpow(), 5 * e.fout() + e.rotation / 10 * e.fslope(), 4, 45);
	}), new Effect(25f, e -> {
		Draw.z(Layer.bullet - 0.1f);
		Fill.light(e.x, e.y, Lines.circleVertices(e.rotation * e.finpow()), e.rotation * e.finpow(), Color.clear, Tmp.c1.set(e.color).lerp(Color.white, e.fout() * 0.65f).lerp(Color.clear, 0.3f + 0.7f * e.fin(Interp.exp5In)));
	}));
	
	public FireExtinguisher(String name){
		super(name);
		
		hasLiquids = hasPower = hasItems = true;
		update = true;
		solid = true;
		
		flags = EnumSet.of(BlockFlag.extinguisher);
	}
	
	@Override
	public void setStats(){
		super.setStats();
		
		stats.add(Stat.reload, 60f / (reloadTime), StatUnit.perSecond);
		stats.add(Stat.range, range / tilesize, StatUnit.blocks);
		stats.add(Stat.abilities, intensity, StatUnit.seconds);
	}
	
	@Override
	public void init(){
		super.init();
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		super.drawPlace(x, y, rotation, valid);
		
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Vars.player.team().color);
	}
	
	public class FireExtinguisherBuild extends Building implements Ranged{
		public float reload = 0;
		public boolean activated = true;
		
		public boolean isActive(){
			return activated;
		}
		
		@Override
		public void updateTile(){
			if(timer.get(20f)){
				activated = false;
				Vars.indexer.eachBlock(this, range(), b -> Fires.has(b.tileX(), b.tileY()), b -> {
					activated = efficiency > 0;
				});
			}
			
//			Log.info(activated);
			
			if(activated){
				if(reload < reloadTime){
					reload += edelta();
				}else{
					reload = 0;
					
					consume();
					extinguishEffect.at(x, y, range(), baseColor);
					
					Geometry.circle(tileX(), tileY(), Mathf.ceil(range() / tilesize), ((x1, y1) -> Fires.extinguish(Vars.world.tile(x1, y1), intensity)));
				}
			}
			
		}
		
		@Override
		public float range(){
			return range;
		}
		
		@Override
		public void drawSelect(){
			Drawf.dashCircle(x, y, range(), team.color);
		}
	}
}
