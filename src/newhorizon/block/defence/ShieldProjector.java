package newhorizon.block.defence;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.block.special.CommandableBlock;
import newhorizon.content.NHFx;
import newhorizon.func.DrawFuncs;
import newhorizon.func.TableFs;
import newhorizon.vars.NHVars;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.func.TableFs.LEN;

public class ShieldProjector extends CommandableBlock{
	public TextureRegion baseRegion, heatRegion;
	
	public Color heatColor = Pal.ammo;
	public float elevation = -1f;
	
	public float recoilAmount = 4f;
	public float restitution = 0.02f;
	public float cooldown = 0.02f;
	
	public float assistRange = 600f;
	public float reloadTime = 1800f;
	public float rotateSpeed = 0.015f;
	
	public float provideHealth = 2000f;
	public float provideRange = 240f;
	public float provideLifetime = 3600f;
	
	public float shootCone = 12f;
	
	protected static final Vec2 tr2 = new Vec2();
	
	public ShieldProjector(String name){
		super(name);
		
		update = configurable = hasItems = hasPower = hasShadow = outlineIcon = true;
		
		config(Integer.class, ShieldProjectorBuild::commandAll);
		config(Point2.class, ShieldProjectorBuild::setTarget);
		
		details = Core.bundle.get("mod.ui.cautions.shield-multable");
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.shootRange, assistRange / tilesize, StatUnit.blocks);
		stats.add(Stat.launchTime, provideLifetime / Time.toSeconds, StatUnit.seconds);
		stats.add(Stat.shieldHealth, provideHealth, StatUnit.shieldHealth);
	}
	
	@Override
	public void load(){
		super.load();
		baseRegion = Core.atlas.find(name + "-base", "block-" + size);
		heatRegion = Core.atlas.find(name + "-heat");
	}
	
	@Override
	public void init(){
		super.init();
		if(elevation < 0) elevation = size / 2f;
	}
	
	@Override
	public TextureRegion[] icons(){
		return new TextureRegion[]{baseRegion, region};
	}
	
	@Override
	public void setBars() {
		super.setBars();
		bars.add("progress",
			(ShieldProjectorBuild entity) -> new Bar(
				() -> Core.bundle.get("bar.progress"),
				() -> Pal.power,
				() -> entity.reload / reloadTime
			)
		);
	}
	
	public class ShieldProjectorBuild extends CommandableBlockBuild{
		public float rotation = 90;
		public float reload;
		public int target = -1;
		
		public transient float heat, recoil;
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(rotation);
			write.f(reload);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			rotation = read.f();
			reload = read.f();
		}

		@Override
		public @NotNull CommandableBlockType getType(){
			return CommandableBlockType.defender;
		}
		
		@Override
		public void setTarget(Point2 point2){
			NHVars.world.commandPos = target = point2.pack();
			for(CommandableBlockBuild build : NHVars.world.commandables){
				if(build != null && build.team == team && groupBoolf.get(build)){
					build.overlap();
				}
			}
			
		}
		
		@Override
		public int getTarget(){
			return target;
		}
		
		@Override
		public void updateTile(){
			if(reload < reloadTime && consValid()){
				reload = Mathf.clamp(reload + efficiency() * delta(), 0, reloadTime);
			}
			
			recoil = Mathf.lerpDelta(recoil, 0f, restitution);
			heat = Mathf.lerpDelta(heat, 0f, cooldown);
			
			Vec2 v;
			if((v = target()) != null)rotation = Mathf.slerpDelta(rotation, angleTo(v), rotateSpeed * efficiency());
		}
		
		@Override
		public void draw(){
			Draw.rect(baseRegion, x, y);
			Draw.color();
			
			Draw.z(Layer.turret);
			
			tr2.trns(rotation, -recoil);
			
			Drawf.shadow(region, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90);
			Draw.rect(region, x + tr2.x, y + tr2.y, rotation - 90);
			
			if(heatRegion.found()){
				if(heat <= 0.00001f)return;
				
				Draw.color(heatColor, heat);
				Draw.blend(Blending.additive);
				Draw.rect(heatRegion, x + tr2.x, y + tr2.y, rotation - 90);
				Draw.blend();
				Draw.color();
			}
		}
		
		@Override
		public void command(Integer point2){
			cons();
			heat = 1;
			recoil = recoilAmount;
			reload = 0;
			
			tr2.trns(rotation, -recoil);
			
			Sounds.lasercharge2.at(this);
			NHFx.square45_4_45.at(x + tr2.x, y + tr2.y, 0, team.color, world.tile(point2));
			NHFx.project.at(x + tr2.x, y + tr2.y, size * 0.8f, team.color, world.tile(point2));
		}
		
		@Override
		public void commandAll(Integer pos){
			float range = 0;
			float health = 0;
			
			Seq<CommandableBlockBuild> participants = new Seq<>();
			for(CommandableBlockBuild build : NHVars.world.commandables){
				if(build.team == team && groupBoolf.get(build) && build.canCommand() && !build.isPreparing()){
					build.command(pos);
					participants.add(build);
					build.lastAccessed(Iconc.modeAttack + "");
					range = Math.max(range, build.spread());
					health += build.delayTime();
				}
			}
			
			Projector p = Pools.obtain(Projector.class, Projector::new);
			p.init(health, range, provideLifetime, team, world.tile(pos));
			p.add();
		}
		
		@Override
		public void buildConfiguration(Table table){
			table.table(Tex.paneSolid, t -> {
				t.button(Icon.effect, Styles.clearPartiali, () -> {
					configure(NHVars.world.commandPos);
				}).size(LEN).disabled(b -> NHVars.world.commandPos < 0);
				t.button("@mod.ui.select-target", Icon.move, Styles.cleart, LEN, () -> {
					TableFs.pointSelectTable(t, this::configure);
				}).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.isSelecting).row();
			}).fill();
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			Vec2 t = target();
			
			Drawf.dashCircle(x, y, range(), team.color);
			
			if(t == null)return;
			
			float range = spread();
			float health = 0;
			
			Seq<CommandableBlockBuild> builds = new Seq<>();
			for(CommandableBlockBuild build : NHVars.world.commandables){
				if(build != this && build != null && build.team == team && groupBoolf.get(build) && build.canCommand()){
					builds.add(build);
					DrawFuncs.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, t.x, t.y);
					range = Math.max(range, build.spread());
					health += build.delayTime();
				}
			}
			
			for(CommandableBlockBuild build : builds){
				DrawFuncs.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, t.x, t.y);
			}
			
			DrawFuncs.posSquareLink(Pal.accent, 1, 2, true, x, y, t.x, t.y);
			DrawFuncs.drawConnected(t.x, t.y, 10f, Pal.accent);
			Draw.color(Pal.gray);
			Lines.stroke(3f);
			Lines.poly(t.x ,t.y, 6, range);
			Draw.color(Pal.accent);
			Lines.stroke(1f);
			Lines.poly(t.x ,t.y, 6, range);
			
			DrawFuncs.overlayText(Core.bundle.format("mod.ui.participants", builds.size) + "\n" + Core.bundle.format("mod.ui.shield-health", health), t.x ,t.y, tilesize * 2f, Pal.accent, true);
		}
		
		@Override
		public boolean canCommand(){
			Vec2 v;
			return consValid() && power.status > 0 && reload >= reloadTime && (v = target()) != null && within(v, range()) && Angles.angleDist(angleTo(v), rotation) < shootCone;
		}
		
		@Override
		public boolean overlap(){
			target = NHVars.world.commandPos;
			return false;
		}
		
		@Override
		public boolean isCharging(){
			return reload < reloadTime;
		}
		
		@Override
		public boolean isPreparing(){
			Vec2 v;
			if((v = target()) != null)return Angles.angleDist(angleTo(v), rotation) > shootCone;
			else return true;
		}
		
		@Override
		public void setPreparing(){}
		
		@Override
		public float delayTime(){return provideHealth; /*Here used this method for shield health*/}
		
		@Override
		public float spread(){return provideRange;}
		
		@Override
		public float range(){
			return assistRange * Mathf.curve(power.status, 0f, 0.75f);
		}
	}
	
	public class Projector extends CommandEntity{
		public transient float realRadius = 1, range;
		public transient float health = 0;
		public transient float hit = 0f;
		
		public transient boolean broken = false;
		
		@Override
		public float clipSize(){
			return realRadius * 2f;
		}
		
		public void init(float health, float range, float lifetime, Team team, Position target){
			time = -NHFx.project.lifetime * 0.7f;
			this.range = range;
			set(target);
			lifetime(lifetime);
			this.health = health;
			team(team);
		}
		
		@Override
		public void update(){
			time += Time.delta;
			
			if(time > 0 && time < lifetime){
				realRadius = Mathf.lerpDelta(realRadius, range, 0.0175f);
			}else if(time > lifetime || broken){
				realRadius = Mathf.lerpDelta(realRadius, 0, 0.03f);
			}
			
			if(hit > 0f)hit -= 1f / 5f * Time.delta;
			
			if(realRadius < 1)remove();
			
			if(health < 0 && !broken){
				broken = true;
				Fx.shieldBreak.at(x, y, realRadius, team.color);
			}
			
			
			if(!broken){
				Groups.bullet.intersect(x - realRadius, y - realRadius, realRadius * 2f, realRadius * 2f, trait -> {
					if(trait.team != team && trait.type.absorbable && Intersector.isInsideHexagon(x, y, realRadius * 2f, trait.x(), trait.y())){
						trait.absorb();
						NHFx.absorb.at(trait.x ,trait.y, Mathf.clamp(trait.damage() / 3f, 6f, 30f), team.color);
						hit = 1f;
						health -= trait.damage();
					}
				});
			}
		}
		
		@Override
		public void draw(){
			if(!broken){
				Draw.z(Layer.shields);
				
				Draw.color(team.color, Color.white, Mathf.clamp(hit));
				
				if(Core.settings.getBool("animatedshields")){
					Fill.poly(x, y, 6, realRadius);
				}else{
					Lines.stroke(1.5f);
					Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
					Fill.poly(x, y, 6, realRadius);
					Draw.alpha(1f);
					Lines.poly(x, y, 6, realRadius);
					Draw.reset();
				}
			}
			
			Draw.reset();
		}
	}
}
