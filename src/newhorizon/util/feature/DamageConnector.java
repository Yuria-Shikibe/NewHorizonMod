package newhorizon.util.feature;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.geom.Rect;
import arc.math.geom.Vec3;
import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.game.Team;
import mindustry.gen.Nulls;
import mindustry.gen.Teamc;
import mindustry.gen.Timedc;
import mindustry.gen.Unit;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.expand.entities.NHBaseEntity;

public class DamageConnector {
	public Color color;
	public Cons<DamageConnectorEntity> draw = e -> {};
	public float maxDamageInvolveScl = 0.75f;
	public int maxInvolve = 12;
	
	private static final Rect rect = new Rect();
	
	public class DamageConnectorEntity extends NHBaseEntity implements Teamc, Timedc{
		public Unit unit = Nulls.unit;
		public Team team = Team.derelict;
		public float time, lifetime;
		
		public ObjectMap<Integer, Vec3> lastPoint = new ObjectMap<>(maxInvolve);
		
		public DamageConnector type(){return DamageConnector.this;}
		
		@Override
		public void draw(){
		
		}
		
		public void init(Unit unit, float lifetime){
			this.unit = unit;
			team = unit.team;
			this.lifetime = lifetime;
		}
		
		@Override
		public void update(){
			time = Math.min(time + Time.delta, lifetime);
			if (time >= lifetime || !unit.isValid()) {
				remove();
			}
		}
		
		@Override
		public int classId(){
			return 0;
		}
		
		@Override
		public boolean cheating(){
			return unit.cheating();
		}
		
		@Override
		public CoreBlock.CoreBuild core(){
			return unit.core();
		}
		
		@Override
		public CoreBlock.CoreBuild closestCore(){
			return unit.closestCore();
		}
		
		@Override
		public CoreBlock.CoreBuild closestEnemyCore(){
			return unit.closestEnemyCore();
		}
		
		@Override
		public Team team(){
			return team;
		}
		
		@Override
		public void team(Team team){
			this.team = team;
		}
		
		@Override public float fin(){return time / lifetime;}
		@Override public float time(){return time;}
		@Override public void time(float time){this.time = time;}
		@Override public float lifetime(){return lifetime;}
		@Override public void lifetime(float lifetime){this.lifetime = lifetime;}
	}
}

