package newhorizon.block.defence;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NewHorizon;
import newhorizon.func.Functions;
import newhorizon.func.NHSetting;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.vars.NHWorldVars;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;
import static newhorizon.vars.NHVars.allTeamSeq;

public class GravityGully extends Block{
	private static Tile tmpTile;
	
	public int range = 24;
	
	public GravityGully(String name){
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
		int teamIndex = allTeamSeq.indexOf(Vars.player.team());
		
		NHWorldVars.drawGully(teamIndex);
		
		Draw.color(Pal.place);
		
		Functions.square(x, y, range, (x1, y1) -> {
			tmpTile = world.tile(x1, y1);
			if(tmpTile != null){
				tmpTile.getBounds(Tmp.r1).getCenter(Tmp.v1);
				Draw.alpha(0.45f);
				Fill.square(Tmp.v1.x, Tmp.v1.y, tilesize / 2f);
			}
		});
		
		Drawf.square(x * tilesize + offset, y * tilesize + offset, range * tilesize * Mathf.sqrt2 + 4f, 0, Pal.place);
	}
	
	public class GravityGullyBuild extends Building implements Ranged, BeforeLoadc{
		public int teamIndex;
		public transient boolean loaded = false;
		public transient boolean active = false;
		public transient final Seq<IntSeq> effectedArea = new Seq<>();
		public float warmup;
		
		public void setIntercept(boolean add){
			if(add)NHWorldVars.gravGullyGroup.add(this);
			else NHWorldVars.gravGullyGroup.remove(this);
			if((active || add) && isValid())for(IntSeq t : effectedArea)t.incr(teamIndex, Mathf.sign(add));
			active = add;
		}
		
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
			if(power.status < 0.75f && active){
				setIntercept(false);
			}else if(power.status >= 0.75f && !active){
				setIntercept(true);
			}
			
			if(efficiency() > 0){
				if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
			
			if(!loaded)beforeLoad();
		}
		
		@Override
		public Building init(Tile tile, Team team, boolean shouldAdd, int rotation){
			NHWorldVars.advancedLoad.add(this);
			return super.init(tile, team, shouldAdd, rotation);
		}
		
		@Override
		public void placed(){
			super.placed();
			beforeLoad();
		}
		
		@Override
		public void onDestroyed(){
			super.onDestroyed();
		}
		
		@Override
		public void remove(){
			if(active){
				setIntercept(false);
				active = true;
			}
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			NHWorldVars.drawGully(teamIndex);
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
		public void beforeLoad(){
			teamIndex = allTeamSeq.indexOf(team);
			
			Functions.square(World.toTile(x), World.toTile(y), range, (x1, y1) -> {
				tmpTile = world.tile(x1, y1);
				if(tmpTile != null)effectedArea.add(NHWorldVars.intercepted.get(tmpTile));
				NHSetting.log("Added");
			});
			
			if(power.status >= 0.75f && !active)setIntercept(true);
			loaded = true;
		}
	
	}
}
