package newhorizon.block.defence;

import arc.Core;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.interfaces.LinkGroupc;

import static mindustry.Vars.tilesize;

public class AssignOverdrive extends OverdriveProjector{
	public int maxLink = 5;
	
	public AssignOverdrive(String name){
		super(name);
		configurable = true;
		saveConfig = saveData = update = true;
		sync = true;
		config(Integer.class, (Cons2<AssignOverdriveBuild, Integer>)AssignOverdriveBuild::linkPos);
		config(Point2.class, (Cons2<AssignOverdriveBuild, Point2>)AssignOverdriveBuild::linkPos);
		config(Point2[].class, (entity, point2s) -> {
			for(Point2 p : point2s){
				entity.configure(Point2.pack(p.x + entity.tileX(), p.y + entity.tileY()));
			}
		});
		
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.powerConnections, maxLink, StatUnit.none);
	}
	
	@Override
	public void setBars(){
		super.setBars();
		bars.add("boost", (AssignOverdriveBuild entity) -> new Bar(() -> Core.bundle.format("bar.boost", (int)(entity.realBoost() * 100)), () -> Pal.accent, () -> entity.realBoost() / (hasBoost ? speedBoost + speedBoostPhase : speedBoost)));
	}
	
	//Fuck that OverdriveProject's params even don't have a protected modifier. FUCK
	public class AssignOverdriveBuild extends Building implements LinkGroupc{
		protected float heat;
		protected float charge = Mathf.random(reload);
		protected float phaseHeat;
		protected float smoothEfficiency;
		protected IntSeq targets = new IntSeq(maxLink);
		
		@Override
		public Point2[] config(){
			if(Vars.net.active())return new Point2[]{};
			Point2[] out = new Point2[targets.size];
			for(int i = 0; i < out.length; i++){
				out[i] = Point2.unpack(targets.get(i)).sub(tile.x, tile.y);
			}
			return out;
		}
		
		@Override
		public void draw(){
			super.draw();
			
			float f = 1f - (Time.time / 100f) % 1f;
			
			Draw.color(baseColor, phaseColor, phaseHeat);
			Draw.alpha(heat * Mathf.absin(Time.time, 10f, 1f) * 0.5f);
			Draw.rect(topRegion, x, y);
			Draw.alpha(1f);
			Lines.stroke((2f * f + 0.1f) * heat);
			
			float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
			Lines.beginLine();
			for(int i = 0; i < 4; i++){
				Lines.linePoint(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
				if(f < 0.5f) Lines.linePoint(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
			}
			Lines.endLine(true);
			
			Draw.reset();
		}
		
		@Override
		public boolean onConfigureTileTapped(Building other){
			if(other != null && within(other, range())){
				configure(other.pos());
				return false;
			}
			return true;
		}
		
		@Override
		public boolean linkValid(Building b){
			return b != null && b.team == team && b.block.canOverdrive;
		}
		
		@Override
		public void drawLight(){
			Drawf.light(team, x, y, 50f * smoothEfficiency, baseColor, 0.7f * smoothEfficiency);
		}
		
		@Override
		public void updateTile(){
			smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency(), 0.08f);
			heat = Mathf.lerpDelta(heat, consValid() ? 1f : 0f, 0.08f);
			charge += heat * Time.delta;
			
			if(hasBoost){
				phaseHeat = Mathf.lerpDelta(phaseHeat, Mathf.num(cons.optionalValid()), 0.1f);
			}
			
			if(charge >= reload){
				float realRange = range + phaseHeat * phaseRangeBoost;
				
				charge = 0f;
				linkBuilds().each(other -> other.applyBoost(realBoost(), reload + 1f));
			}
			
			if(timer(timerUse, useTime) && efficiency() > 0 && consValid()){
				consume();
			}
		}
		
		public float realBoost(){
			return consValid() ? (speedBoost + phaseHeat * speedBoostPhase) * efficiency() : 0f;
		}
		
		@Override
		public void drawConfigure(){
			float realRange = range + phaseHeat * phaseRangeBoost;
			
			Draw.color(baseColor);
			Lines.square(x, y, block.size * tilesize / 2f + 1.0f);
			
			Drawf.dashCircle(x, y, realRange, baseColor);
			
			drawLink();
		}
		
		@Override
		public IntSeq linkGroup(){
			return targets;
		}
		
		@Override
		public void linkGroup(IntSeq seq){
			targets = seq;
		}
		
		@Override
		public boolean linkValid(){
			for(Building b : linkBuilds()){
				if(!linkValid(b)){
					targets.removeValue(b.pos());
					return false;
				}
			}
			return true;
		}
		
		@Override
		public int linkPos(){
			return pos();
		}
		
		@Override
		public Building link(){
			return Vars.world.build(targets.first());
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(heat);
			write.f(phaseHeat);
			mindustry.io.TypeIO.writeObject(write, targets);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			heat = read.f();
			phaseHeat = read.f();
			targets = (IntSeq)mindustry.io.TypeIO.readObject(read);
		}
		
		@Override
		public void linkPos(int value){
			Building other = Vars.world.build(value);
			
			boolean contains = targets.removeValue(value);
			if(!contains && targets.size < maxLink - 1)targets.add(value);
			
		}
		
		public void updatePos(){
			for(int pos : targets.items){
				if(!linkValid(Vars.world.build(pos))){
					targets.removeValue(pos);
				}
			}
		}
		
		@Override
		public Color getLinkColor(){
			return baseColor;
		}
		
		@Override
		public float range(){
			return range;
		}
	}
}
