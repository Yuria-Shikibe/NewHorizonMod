package newhorizon.expand.block.commandable;

import arc.Core;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.core.UI;
import mindustry.core.World;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import newhorizon.NHGroups;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.ui.TableFunc;

import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.LEN;

public abstract class CommandableAttackerBlock extends CommandableBlock{
	public float spread = 120f;
	public float prepareDelay = 60f;
	public int storage = 1;
	public ShootPattern shoot = new ShootPattern();
	
	
	
	protected BulletType bullet;
	protected UnitTypes spawnUnit;
	
	public CommandableAttackerBlock(String name){
		super(name);
		
		replaceable = true;
		canOverdrive = false;
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		super.drawPlace(x, y, rotation, valid);
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
	}
	
	@Override
	public void setStats(){
		super.setStats();
//		stats.add(Stat.instructions, t -> t.add(Core.bundle.format("mod.ui.support-logic-control", "shootp", "\n 1 -> Control All\n 2 -> Control Single")));
		stats.add(Stat.range, range / tilesize, StatUnit.blocks);
		stats.add(Stat.damage, StatValues.ammo(ObjectMap.of(this, bullet)));
	}
	
	@Override
	public void setBars() {
		super.setBars();
		addBar("progress",
			(CommandableAttackerBlockBuild entity) -> new Bar(
				() -> Core.bundle.get("bar.progress"),
				() -> Pal.power,
				() -> (entity.reload % reloadTime) / reloadTime
			)
		);
		addBar("storage",
			(CommandableAttackerBlockBuild entity) -> new Bar(
				() -> Core.bundle.format("bar.capacity", UI.formatAmount(entity.ammo())),
				() -> Pal.ammo,
				() -> (float)entity.ammo() / storage
			)
		);
	}
	
	public abstract class CommandableAttackerBlockBuild extends CommandableBlockBuild{
		@Override
		public boolean isCharging(){
			return efficiency > 0 && reload < reloadTime * storage && !initiateConfigure;
		}
		
		@Override
		public boolean shouldCharge(){
			return reload < reloadTime * storage;
		}
		
		public int ammo(){
			return (int)(reload / reloadTime);
		}

		@Override
		public void control(LAccess type, Object p1, double p2, double p3, double p4){
//			if(type == LAccess.shootp && timer.get(2, 10f) && (unit == null || !unit.isPlayer())){
//				if(p1 instanceof Posc){
//					Posc target = (Posc)p1;
//					Vec2 velocity;
//					if(target instanceof Velc)velocity = ((Velc)target).vel().cpy();
//					else velocity = Vec2.ZERO;
//					velocity.scl((delayTime(tmpPoint.set(World.toTile(velocity.x), World.toTile(velocity.y)).pack()) * Time.toSeconds + prepareDelay) / 1.5f).add(target);
//					int pos = tmpPoint.set(World.toTile(velocity.x), World.toTile(velocity.y)).pack();
//					if(Mathf.equal((float)p2,1))commandAll(pos);
//					if(Mathf.equal((float)p2,2) && canCommand(pos) && !isPreparing())command(pos);
//				}
//			}
			
			super.control(type, p1, p2, p3, p4);
		}
		
		@Override
		public BlockStatus status(){
			return canCommand(targetVec) ? BlockStatus.active : isCharging() ? BlockStatus.noOutput : BlockStatus.noInput;
		}
		
		@Override
		public void updateTile(){
			super.updateTile();
			
			if(shouldChargeConfigure()){
				configureChargeProgress += edelta() * warmup;
				if(configureChargeComplete()){
					shoot(lastConfirmedTarget);
				}
			}
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			target = read.i();
			reload = read.f();
			initiateConfigure = read.bool();
			configureChargeProgress = read.f();
			
			TypeIO.readVec2(read, lastConfirmedTarget);
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.i(target);
			write.f(reload);
			write.bool(initiateConfigure);
			write.f(configureChargeProgress);
			
			TypeIO.writeVec2(write, lastConfirmedTarget);
		}
		
		@Override
		public void command(Vec2 pos){
			lastConfirmedTarget.set(pos);
			targetVec.set(pos);
			target = Point2.pack(World.toTile(pos.x), World.toTile(pos.y));
			initiateConfigure = true;
			
			NHFx.attackWarningPos.at(lastConfirmedTarget.x, lastConfirmedTarget.y, configureChargeTime, team.color, tile);
		}
		
		/**
		 * Should Be Overridden.
		 *
		 * */
		public void shoot(Vec2 target){
			configureChargeProgress = 0;
			initiateConfigure = false;
			reload = Math.max(0, reload - reloadTime);
			
			consume();
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			
			Drawf.dashCircle(x, y, range, team.color);
			
			Seq<CommandableBlockBuild> builds = new Seq<>();
			for(CommandableBlockBuild build : NHGroups.commandableBuilds){
				if(build != this && build != null && build.team == team && sameGroup(build.block()) && build.canCommand(targetVec)){
					builds.add(build);
					DrawFunc.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, targetVec.x, targetVec.y);
				}
			}

			for(CommandableBlockBuild build : builds){
				DrawFunc.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, targetVec.x, targetVec.y);
			}

			if(builds.any()){
				DrawFunc.posSquareLink(Pal.accent, 1, 2, true, x, y, targetVec.x, targetVec.y);
				DrawFunc.drawConnected(targetVec.x, targetVec.y, 10f, Pal.accent);
			}

			if(canCommand(targetVec))builds.add(this);
			if(builds.any())DrawFunc.overlayText(Core.bundle.format("mod.ui.participants", builds.size), targetVec.x, targetVec.y, tilesize * 2f, Pal.accent, true);
		}

		public void commandAll(Vec2 pos){
			participantsTmp.clear();
			
			for(CommandableBlockBuild build : NHGroups.commandableBuilds){
				if(build.team == team && sameGroup(build.block()) && build.canCommand(pos)){
					build.command(pos);
					participantsTmp.add(build);
					build.lastAccessed(Iconc.modeAttack + "");
				}
			}

			if(!Vars.headless && participantsTmp.any()){
				if(team != Vars.player.team())TableFunc.showToast(Icon.warning, "[#ff7b69]Caution: []Attack " +  (int)(pos.x / 8) + ", " + (int)(pos.y / 8), NHSounds.alert2);
				NHFx.attackWarningRange.at(pos.x, pos.y, 80, team.color);
			}
		}
		
		
		@Override
		public boolean canCommand(Vec2 target){
			return ammo() > 0 && warmup > 0.25f && within(target, range()) && !isChargingConfigure();
		}
		
		@Override
		public void buildConfiguration(Table table){
			Vars.control.input.selectedBlock();
			
			table.table(Tex.paneSolid, t -> {
				t.button(Icon.modeAttack, Styles.cleari, () -> {
					configure(targetVec);
				}).size(LEN).disabled(b -> targetVec.epsilonEquals(x, y, 0.1f));
				t.button("@mod.ui.select-target", Icon.move, Styles.cleart, LEN, () -> {
					TableFunc.selectPos(t, this::configure);
				}).size(LEN * 4, LEN).row();
			}).fill();

		}
	}
}
