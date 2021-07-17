package newhorizon.block.defence;

import arc.Core;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.core.World;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Damagec;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.block.special.CommandableBlock;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.func.DrawFuncs;
import newhorizon.func.TableFs;
import newhorizon.vars.NHVars;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.func.TableFs.LEN;

public abstract class CommandableAttackerBlock extends CommandableBlock{
	public float range = 100f;
	public float spread = 120f;
	public float prepareDelay = 60f;
	public float reloadTime = 240f;
	public int storage = 1;
	
	
	@NotNull protected BulletType bulletHitter;
	
	public CommandableAttackerBlock(String name){
		super(name);
		
		replaceable = true;
		canOverdrive = false;
		
		config(Integer.class, CommandableAttackerBlockBuild::commandAll);
		config(Point2.class, CommandableAttackerBlockBuild::setTarget);
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		super.drawPlace(x, y, rotation, valid);
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.range, range / tilesize, StatUnit.blocks);
	}
	
	@Override
	public void setBars() {
		super.setBars();
		bars.add("progress",
			(CommandableAttackerBlockBuild entity) -> new Bar(
				() -> Core.bundle.get("bar.progress"),
				() -> Pal.power,
				() -> (entity.reload % reloadTime) / reloadTime
			)
		);
		bars.add("storage",
			(CommandableAttackerBlockBuild entity) -> new Bar(
				() -> Core.bundle.format("bar.capacity", UI.formatAmount(entity.storaged())),
				() -> Pal.ammo,
				() -> (float)entity.storaged() / storage
			)
		);
	}
	
	public abstract class CommandableAttackerBlockBuild extends CommandableBlockBuild{
		public int target = -1;
		public float reload;
		public float countBack = prepareDelay;
		public boolean preparing = false;
		
		protected boolean attackGround = true;
		
		@Override
		public boolean isCharging(){return consValid() && reload < reloadTime * storage;}
		
		public int storaged(){return (int)(reload / reloadTime);}
		
		@Override
		public int getTarget(){
			return target;
		}
		
		@Override
		@NotNull
		public CommandableBlockType getType(){
			return CommandableBlockType.attacker;
		}
		
		@Override
		public float spread(){
			return spread;
		}
		
		public void setTarget(Point2 p){
			NHVars.world.commandPos = target = p.pack();
			for(CommandableBlockBuild build : NHVars.world.commandables){
				if(build != null && build.team == team && groupBoolf.get(this, build)){
					build.overlap();
				}
			}
		}
		
		
		@Override
		public boolean isPreparing(){
			return preparing && countBack > 0;
		}
		
		@Override
		public void setPreparing(){
			preparing = true;
			countBack = prepareDelay;
		}
		
		@Override
		public BlockStatus status(){
			return canCommand() ? BlockStatus.active : isCharging() ? BlockStatus.noOutput : BlockStatus.noInput;
		}
		
		@Override
		public void updateTile(){
			if(reload < reloadTime * storage && consValid()){
				reload += efficiency() * delta();
			}
			
			if(isPreparing()){
				countBack -= efficiency() * delta();
			}else if(preparing){
				countBack = prepareDelay;
				preparing = false;
				shoot(lastTarget);
			}
		}
		
		@Override
		public boolean canCommand(){
			Tile tile = world.tile(NHVars.world.commandPos);
			return tile != null && consValid() && storaged() > 0 && within(tile, range);
		}
		
		@Override
		public boolean overlap(){
			target = NHVars.world.commandPos;
			return false;
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			target = read.i();
			reload = read.f();
			preparing = read.bool();
			countBack = read.f();
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.i(target);
			write.f(reload);
			write.bool(preparing);
			write.f(countBack);
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			Tmp.p1.set(Point2.unpack(NHVars.world.commandPos));
			
			float realSpread = spread();
			
			Drawf.dashCircle(x, y, range, team.color);
			
			if(target < 0 && NHVars.world.commandPos < 0)return;
			
			Seq<CommandableBlockBuild> builds = new Seq<>();
			for(CommandableBlockBuild build : NHVars.world.commandables){
				if(build != this && build != null && build.team == team && groupBoolf.get(this, build) && build.canCommand()){
					builds.add(build);
					DrawFuncs.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
					realSpread = Math.max(realSpread, build.spread());
				}
			}
			
			for(CommandableBlockBuild build : builds){
				DrawFuncs.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
			}
			
			Tmp.p1.set(Point2.unpack(NHVars.world.commandPos));
			
			if(NHVars.world.commandPos > 0){
				DrawFuncs.posSquareLink(Pal.accent, 1, 2, true, x, y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
				DrawFuncs.drawConnected(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), 10f, Pal.accent);
				Drawf.circles(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), realSpread, Pal.accent);
			}
			
			if(isValid())builds.add(this);
			for(CommandableBlockBuild build : builds){
				float time = build.delayTime();
				DrawFuncs.overlayText("Delay: " + TableFs.format(time) + " Sec.", build.x, build.y, build.block.size * tilesize / 2f, time > 4.5f ? Pal.accent : Pal.lancerLaser, true);
			}
			DrawFuncs.overlayText(Core.bundle.format("mod.ui.participants", builds.size), World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), tilesize * 2f, Pal.accent, true);
		}
		
		public void commandAll(Integer pos){
			Tmp.p1.set(Point2.unpack(pos));
			float realSpread = 0f;
			
			Seq<CommandableBlockBuild> participants = new Seq<>();
			for(CommandableBlockBuild build : NHVars.world.commandables){
				if(build.team == team && groupBoolf.get(this, build) && build.canCommand() && !build.isPreparing()){
					build.command(pos);
					participants.add(build);
					build.lastAccessed(Iconc.modeAttack + "");
					realSpread = Math.max(realSpread, build.spread());
				}
			}
			if(!Vars.headless && participants.size > 0){
				if(team != Vars.player.team())TableFs.showToast(Icon.warning, "[#ff7b69]Caution: []Attack " +  Tmp.p1.x + ", " + Tmp.p1.y, NHSounds.alarm);
				NHFx.attackWarning.at(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), realSpread, team.color, participants);
				NHFx.spawn.at(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), realSpread, team.color);
			}
		}
		
		@Override
		public void command(Integer pos){
			setPreparing();
			lastTarget = pos;
		}
		
		public abstract void shoot(Integer pos);
		
		@Override
		public void buildConfiguration(Table table){
			table.table(Tex.paneSolid, t -> {
				t.button(Icon.modeAttack, Styles.clearPartiali, () -> {
					configure(NHVars.world.commandPos);
				}).size(LEN).disabled(b -> NHVars.world.commandPos < 0);
				t.button("@mod.ui.select-target", Icon.move, Styles.cleart, LEN, () -> {
					TableFs.pointSelectTable(t, this::configure);
				}).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.isSelecting).row();
			}).fill();
			
		}
		
		@Override
		public float range(){
			return range;
		}
	}
	
	public abstract class AttackerEntity extends CommandEntity implements Damagec{}
}
