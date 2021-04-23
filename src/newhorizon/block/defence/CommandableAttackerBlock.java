package newhorizon.block.defence;

import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.meta.BlockStatus;
import newhorizon.block.special.CommandableBlock;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.func.DrawFuncs;
import newhorizon.func.TableFs;
import newhorizon.vars.NHCtrlVars;
import newhorizon.vars.NHWorldVars;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.func.TableFs.LEN;

public abstract class CommandableAttackerBlock extends CommandableBlock{
	public float range = 100f;
	public float spread = 120f;
	public float prepareDelay = 60f;
	public float reloadTime = 240f;
	
	@NotNull protected BulletType bulletHitter;
	
	public CommandableAttackerBlock(String name){
		super(name);
		
		replaceable = true;
		canOverdrive = false;
		
		config(Integer.class, CommandableAttackerBlockBuild::triggered);
		config(Point2.class, CommandableAttackerBlockBuild::setTarget);
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		super.drawPlace(x, y, rotation, valid);
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
	}
	
	public abstract class CommandableAttackerBlockBuild extends CommandableBlockBuild{
		public transient int lastTarget = -1;
		public int target = -1;
		public float reload;
		public float countBack = prepareDelay;
		public boolean preparing = false;
		
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
			for(CommandableBlockBuild build : NHWorldVars.commandables){
				if(build != null && build.getType() == CommandableBlockType.attacker){
					build.overlap();
				}
			}
			NHWorldVars.commandPos = target = p.pack();
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
		public boolean canCommand(){
			Tile tile = world.tile(NHWorldVars.commandPos);
			return tile != null && consValid() && NHWorldVars.commandPos > 0 && within(tile, range);
		}
		
		@Override
		public boolean overlap(){
			target = -1;
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
			Tmp.p1.set(Point2.unpack(NHWorldVars.commandPos));
			
			float realSpread = spread();
			
			Drawf.dashCircle(x, y, range, team.color);
			
			if(target < 0 && NHWorldVars.commandPos < 0)return;
			
			Seq<CommandableBlockBuild> builds = new Seq<>();
			for(CommandableBlockBuild build : NHWorldVars.commandables){
				if(build != this && build != null && build.team == team && build.getType() == CommandableBlockType.attacker && build.canCommand()){
					builds.add(build);
					DrawFuncs.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
					realSpread = Math.max(realSpread, build.spread());
				}
			}
			
			for(CommandableBlockBuild build : builds){
				DrawFuncs.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
			}
			
			if(target > 0){
				Tmp.p1.set(Point2.unpack(target));
				DrawFuncs.posSquareLink(Pal.accent, 1, 2, true, x, y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
				DrawFuncs.drawConnected(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), 10f, Pal.accent);
				Drawf.circles(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), realSpread, Pal.accent);
			}else if(NHWorldVars.commandPos > 0){
				Tmp.p1.set(Point2.unpack(NHWorldVars.commandPos));
				DrawFuncs.posSquareLink(Pal.place, 1, 2, true, x, y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
				DrawFuncs.drawConnected(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), 10f, Pal.place);
				Drawf.circles(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), realSpread, Pal.place);
			}
			
			if(isValid())builds.add(this);
			for(CommandableBlockBuild build : builds){
				float time = build.delayTime();
				DrawFuncs.overlayText("Delay: " + TableFs.format(time) + " Sec.", build.x, build.y, build.block.size * tilesize / 2f, time > 4.5f ? Pal.accent : Pal.lancerLaser, true);
			}
			
			DrawFuncs.overlayText("Participants: " + builds.size, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), tilesize * 2f, Pal.accent, true);
		}
		
		public void commandAll(Integer pos){
			Tmp.p1.set(Point2.unpack(pos));
			
			float realSpread = 0f;
			
			Seq<CommandableBlockBuild> participants = new Seq<>();
			for(CommandableBlockBuild build : NHWorldVars.commandables){
				if(build.team == team && build.getType() == CommandableBlockType.attacker && build.canCommand() && !build.isPreparing()){
					build.configure(pos);
					participants.add(build);
					realSpread = Math.max(realSpread, build.spread());
				}
			}
			
			if(participants.size < 1)return;
			Vars.ui.announce(Iconc.warning  + " Caution: Attack " +  Tmp.p1.x + ", " + Tmp.p1.y, 4f);
			NHFx.attackWarning.at(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), realSpread, team.color, participants);
			NHFx.spawn.at(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), realSpread, team.color);
			for(Player p : Groups.player){
				NHSounds.alarm.at(p);
			}
		}
		
		@Override
		public void triggered(Integer pos){
			setPreparing();
			Log.info("Trigged");
			lastTarget = pos;
		}
		
		public abstract void shoot(Integer pos);
		
		@Override
		public void buildConfiguration(Table table){
			table.table(Tex.paneSolid, t -> {
				t.button(Icon.upOpen, Styles.clearPartiali, () -> {
					commandAll(target < 0 ? NHWorldVars.commandPos : target);
				}).size(LEN).disabled(b -> isPreparing());
				t.button("@mod.ui.select-target", Icon.move, Styles.cleart, LEN, () -> {
					TableFs.pointSelectTable(t, this::configure);
				}).size(LEN * 4, LEN).disabled(b -> NHCtrlVars.isSelecting).row();
			}).fill();
		}
		
		@Override
		public float range(){
			return range;
		}
	}
	
	public abstract class AttackerEntity extends CommandEntity implements Damagec{}
}
