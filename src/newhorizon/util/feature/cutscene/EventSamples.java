package newhorizon.util.feature.cutscene;

import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockFlag;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHBullets;
import newhorizon.content.NHUnitTypes;
import newhorizon.util.feature.cutscene.events.*;

public class EventSamples{
	public static CutsceneEvent jumpgateUnlock, fleetInbound, allyBuildersInbound, allyStrikersInbound, allySavoursInbound,
			jumpgateUnlockObjective, waveTeamRaid, fleetApproaching, destroyGravityTraps, destroyReactors;
	
	public static void load(){
		allyBuildersInbound = new FleetEvent("ally-builders-inbound"){{
			unitTypeMap = ObjectMap.of(UnitTypes.poly, 2);
			reloadTime = 90 * 60;
			
			removeAfterTriggered = false;
			cannotBeRemove = true;
			updatability = e -> teamFunc.get(e).data().countType(UnitTypes.poly) + 2 <= Units.getCap(teamFunc.get(e));
			teamFunc = e -> Vars.state.rules.defaultTeam;
			targetFunc = e -> {
				CoreBlock.CoreBuild build = teamFunc.get(e).cores().firstOpt();
				if(build == null)return new Vec2();
				else return build;
			};
			sourceFunc = targetFunc;
			angle = e -> {
				CoreBlock.CoreBuild build = teamFunc.get(e).cores().firstOpt();
				if(build == null)return 45f;
				Tmp.v1.set(build).sub(Vars.world.unitWidth() / 2f, Vars.world.unitHeight() / 2f);
				float ang = Tmp.v1.angle();
				if(ang > 270)return 135f;
				else if(ang > 180)return 45f;
				else if(ang > 90)return 315f;
				else return 215f;
			};
			exist = e -> teamFunc.get(e).data().hasCore();
		}};
		
		allyStrikersInbound = allyBuildersInbound.copyAnd(FleetEvent.class, "ally-strikers-inbound", e -> {
			e.unitTypeMap = ObjectMap.of(NHUnitTypes.striker, 3);
			e.removeAfterTriggered = true;
			e.reloadTime = 600f;
		});
		
		allySavoursInbound = allyBuildersInbound.copyAnd(FleetEvent.class, "ally-savour-inbound", e -> {
			e.unitTypeMap = ObjectMap.of(NHUnitTypes.saviour, 1, NHUnitTypes.rhino, 4, UnitTypes.mega, 4);
			e.removeAfterTriggered = true;
			e.reloadTime = 600f;
		});
		
		
		fleetInbound = new FleetEvent("inbuilt-hostile-fleet-inbound"){{
			unitTypeMap = ObjectMap.of(NHUnitTypes.guardian, 3);
			reloadTime = 30 * 60;
			
			removeAfterTriggered = cannotBeRemove = true;
		}};
		
		destroyReactors = new DestroyObjectiveEvent("destroyReactors"){{
			targets = e -> {
				Seq<Building> buildings = new Seq<>();
				
				Groups.build.each(b -> b.isValid() && b.team != Vars.state.rules.defaultTeam && b.block.flags.contains(BlockFlag.reactor), buildings::add);
				return buildings;
			};
			
			cannotBeRemove = true;
		}};
		
//		destroyGravityTraps = new DestroyObjectiveEvent("destroyGravityTraps"){{
//			targets = e -> {
//				Seq<Building> buildings = new Seq<>();
//
//				NHFunc.getObjects(NHVars.world.gravityTraps).each(b -> {
//					if(b.team != Vars.state.rules.defaultTeam)buildings.add(b.build);
//				});
//
//				return buildings;
//			};
//
//			targetBlock = NHBlocks.gravityTrap;
//			cannotBeRemove = true;
//
//		}};
		
		fleetApproaching = new FleetEvent("fleetApproaching"){{
			reloadTime = 60 * 60 * 8;
			targetFunc = e -> {
				if(Vars.state.hasSpawns())return Vars.spawner.getFirstSpawn();
				if(Vars.state.rules.waveTeam.cores().isEmpty())return new Vec2(300, 300);
				CoreBlock.CoreBuild coreBuild = Vars.state.rules.defaultTeam.core();
				
				if(coreBuild == null)return Vars.state.rules.waveTeam.core();
				
				return Geometry.findFurthest(coreBuild.x, coreBuild.y, Vars.state.rules.waveTeam.cores());
			};
			
			teamFunc = e -> Vars.state.rules.waveTeam;
			
			unitTypeMap = ObjectMap.of(NHUnitTypes.striker, 6, NHUnitTypes.hurricane, 2, NHUnitTypes.guardian, 2);
			
			cannotBeRemove = true;
			removeAfterTriggered = false;
		}};
		
		waveTeamRaid = new RaidEvent("waveTeamRaid"){{
			reloadTime = 60 * 60 * 6;
			
			
			number = 30;
			shootDelay = 6f;
			bulletType = NHBullets.airRaidMissile;
			
			removeAfterTriggered = false;
			cannotBeRemove = true;
		}};
		
		jumpgateUnlock = new SignalEvent("jumpgateUnlock"){{
			action = () -> NHBlocks.jumpGatePrimary.unlock();
			position = new Vec2(888, 1392);
			
			cannotBeRemove = true;
			removeAfterVictory = false;
		}};
		
		jumpgateUnlockObjective = new ObjectiveEvent("jumpgateUnlockObjective"){{
			info = e -> "Find The Signal Source";
			trigger = e -> NHBlocks.jumpGatePrimary.unlocked();
			
			cannotBeRemove = true;
			removeAfterVictory = false;
		}};
	}
}
