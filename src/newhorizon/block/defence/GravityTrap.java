package newhorizon.block.defence;

import arc.Core;
import arc.func.Cons2;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.feature.PosLightning;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.vars.NHVars;
import newhorizon.vars.NHWorldVars;

import static mindustry.Vars.*;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class GravityTrap extends Block{
	protected Cons2<HyperSpaceWarper.Carrier, GravityTrapBuild> act = (c, b) -> {
		c.intercepted = true;
		PosLightning.createEffect(c, b , NHColor.darkEnrColor, 2, PosLightning.WIDTH * 1.5f);
	};
	
	private static Tile tmpTile;
	
	public int range = 24;
	
	public GravityTrap(String name){
		super(name);
		solid = true;
		configurable = true;
		update = true;
		hasPower = true;
		canOverdrive = false;
		sync = true;
		noUpdateDisabled = true;
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.range, range, StatUnit.blocks);
		stats.add(Stat.output, (t) -> {
			t.row().add("[gray]Legends:").left().pad(OFFSET).growX().height(LEN).row();
			t.image().size(LEN).color(Pal.lancerLaser).padTop(OFFSET);
			t.add("[lightgray]Friendly Force Field").fill().padLeft(OFFSET / 2).row();
			t.image().size(LEN).color(Pal.accent).padTop(OFFSET);
			t.add("[lightgray]Hostile & Friendly Mixed Force Field").fill().padLeft(OFFSET / 2).row();
			t.image().size(LEN).color(Pal.redderDust).padTop(OFFSET);
			t.add("[lightgray]Hostile Force Field").fill().padLeft(OFFSET / 2).row();
		});
		
	}
	
	@Override
	public void init(){
		super.init();
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		if(headless)return;
		
		NHVars.world.drawGully(player.team());
		
		Draw.color(Pal.place);
		
		Draw.alpha(0.1f);
		Fill.circle(x * tilesize + offset, y * tilesize + offset, range * tilesize);
	}
	
	public class GravityTrapBuild extends Building implements Ranged, BeforeLoadc{
		public float warmup;
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(warmup);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			warmup = read.f();
		}
		
		@Override
		public void updateTile(){
			if(efficiency() > 0){
				if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			NHVars.world.drawGully(team);
		}
		
		public boolean active(){
			return warmup > 0.5f && power.status > 0.75f;
		}
		
		@Override
		public void draw(){
			super.draw();
			
			Draw.reset();
			float sin = Mathf.absin(Time.time, 8f, size / 2f);
			
			Draw.z(Layer.bullet + 1f);
			Draw.color(team.color);
			float length = tilesize * size / 4f + sin;
			
			TextureRegion region = Core.atlas.find(NewHorizon.configName("linked-arrow"));
			for(int i = 0; i < 4; i++){
				Tmp.v1.trns(i * 90, -length);
				
				Draw.rect(region, x + Tmp.v1.x, y + Tmp.v1.y, region.width * warmup * Draw.scl, region.height * warmup * Draw.scl, i * 90);
			}
			Draw.reset();
		}
		
		@Override
		public float range(){
			return range * tilesize;
		}
		
		@Override
		public Building init(Tile tile, Team team, boolean shouldAdd, int rotation){
			NHWorldVars.advancedLoad.add(this);
			return super.init(tile, team, shouldAdd, rotation);
		}
		
		@Override
		public void remove(){
			super.remove();
			NHVars.world.gravityTraps.remove(this);
		}
		
		@Override
		public void placed(){
			super.placed();
			beforeLoad();
		}
		
		@Override
		public void beforeLoad(){
			NHVars.world.gravityTraps.add(this);
		}
	}
}
