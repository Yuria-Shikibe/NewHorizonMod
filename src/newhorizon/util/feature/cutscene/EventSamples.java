package newhorizon.util.feature.cutscene;

import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockFlag;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHBullets;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.feature.cutscene.events.*;
import newhorizon.util.func.NHFunc;

public class EventSamples{
	public static CutsceneEvent jumpgateUnlock,
			jumpgateUnlockObjective, waveTeamRaid, fleetApproaching, destroyGravityTraps, destroyReactors;
	
	public static void load(){
		
		destroyReactors = new DestroyObjectiveEvent("destroyReactors"){{
			targets = e -> {
				Seq<Building> buildings = new Seq<>();
				
				Groups.build.each(b -> b.isValid() && b.team != Vars.state.rules.defaultTeam && b.block.flags.contains(BlockFlag.reactor), buildings::add);
				return buildings;
			};
			
			cannotBeRemove = true;
		}};
		
		destroyGravityTraps = new DestroyObjectiveEvent("destroyGravityTraps"){{
			targets = e -> {
				Seq<Building> buildings = new Seq<>();
				
				NHVars.world.gravityTraps.each(b -> {
					if(b.team != Vars.state.rules.defaultTeam)buildings.add(b);
				});
				
				return buildings;
			};
			
			targetBlock = NHBlocks.gravityTrap;
			cannotBeRemove = true;
			
		}};
		
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
			
		}};
		
		waveTeamRaid = new RaidEvent("waveTeamRaid"){{
			reloadTime = 60 * 60 * 6;
			targetFunc = e -> {
				Rand rand = NHFunc.rand;
				rand.setSeed(e.id);
				
				Building b = null;
				int times = 0;
				
				Seq<Building> all = new Seq<>();
				Groups.build.copy(all);
				
				while(b == null && times < 1024 && all.any()){
					int index = rand.random(all.size - 1);
					b = all.get(index);
					if(b.team == Vars.state.rules.waveTeam || (b.proximity().size < 3 && b.block.health < 1600)){
						all.remove(index);
						b = null;
					}
					times++;
				}
				
				return new Vec2().set(b == null ? Vec2.ZERO : b);
			};
			
			teamFunc = e -> Vars.state.rules.waveTeam;
			
			number = 30;
			shootDelay = 6f;
			bulletType = NHBullets.airRaidMissile;
			
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
