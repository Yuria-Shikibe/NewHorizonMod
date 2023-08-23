package newhorizon.expand.eventsys.types;

import arc.func.Prov;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.util.func.NHFunc;

public abstract class TargetableEventType extends WorldEventType{
	public Prov<Team> defaultTeam = () -> Vars.state.rules.waveTeam;
	
	public TargetableEventType(String name){
		super(name);
		hasCoord = true;
	}
	
	@Override
	public Position source(WorldEvent event){
		Team team = event.team;
		CoreBlock.CoreBuild coreBuild = team.core();
		if(coreBuild == null){
			if(team == Vars.state.rules.waveTeam){
				return new Vec2().set(Vars.state.hasSpawns() && Vars.spawner.getFirstSpawn() != null ? Vars.spawner.getFirstSpawn() : Vec2.ZERO);
			}else return new Vec2(-120, -120);
		}
		
		CoreBlock.CoreBuild b =  Geometry.findFurthest(coreBuild.x, coreBuild.y, Vars.state.rules.waveTeam.cores());
		return b == null ? Vec2.ZERO : b;
	}
	
	//TODO use multi threads
	@Override
	public Position target(WorldEvent e){
		Rand rand = NHFunc.rand;
		rand.setSeed(e.id);
		
		Building b = null;
		
		int times = 0;
		
		Team team = e.team;
		Position source = source(e);
		
		Seq<Building> all = new Seq<>(Groups.build.size());
		Groups.build.copy(all);
		all.remove(bi -> bi.team == team);
		
		Vec2 t = null;
		
		loop: while(b == null && all.any()){
			int index = rand.random(all.size - 1);
			b = all.get(index);
			
			int i = 0;
			
			if(GravityTrapField.IntersectedAllyRect.get(b, Tmp.r1.setSize(4).setCenter(b.x, b.y))){
				while(i < 30){
					Tmp.v1.rnd(240).add(b).clamp(-Vars.finalWorldBounds, -Vars.finalWorldBounds, Vars.finalWorldBounds + Vars.world.unitHeight(), Vars.finalWorldBounds + Vars.world.unitWidth());
					if(GravityTrapField.IntersectedAllyRect.get(b, Tmp.r1.setSize(4).setCenter(Tmp.v1.x, Tmp.v1.y))){
						i++;
						continue;
					}
					
					t = Tmp.v1.cpy();
					break loop;
				}
				
				all.remove(index);
				b = null;
			}
		}
		
		if(t == null){
			t = Vec2.ZERO.cpy();
			if(b != null)t = new Vec2().set(b);
			if(all.isEmpty())t = new Vec2().set(source);
		}
		
		return t;
	}
	
	@Override
	public void init(WorldEvent event){
		super.init(event);
		event.team(defaultTeam.get());
		event.set(target(event));
	}
}
