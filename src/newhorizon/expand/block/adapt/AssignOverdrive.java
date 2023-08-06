package newhorizon.expand.block.adapt;

import arc.Core;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
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
import newhorizon.expand.interfaces.LinkGroupc;

import static mindustry.Vars.tilesize;

public class AssignOverdrive extends OverdriveProjector{
	public int maxLink = 5;
	public float strokeOffset = 0.1f;
	public float strokeClamp = 0;
	
	public AssignOverdrive(String name){
		super(name);
		configurable = true;
		saveConfig = update = true;
		solid = true;
		hasItems = true;
		hasPower = true;
		config(Integer.class, (Cons2<AssignOverdriveBuild, Integer>)AssignOverdriveBuild::linkPos);
		config(Point2.class, (Cons2<AssignOverdriveBuild, Point2>)AssignOverdriveBuild::linkPos);
		config(Point2[].class, (AssignOverdriveBuild entity, Point2[] point2s) -> {
			for(Point2 p : point2s){
				entity.linkPos(Point2.pack(p.x + entity.tileX(), p.y + entity.tileY()));
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
		addBar("boost", (AssignOverdriveBuild entity) -> new Bar(() -> Core.bundle.format("bar.boost", (int)(entity.realBoost() * 100)), () -> Pal.accent, () -> entity.realBoost() / (hasBoost ? speedBoost + speedBoostPhase : speedBoost)));
	}
	
	public class AssignOverdriveBuild extends OverdriveBuild implements LinkGroupc{
		protected IntSeq targets = new IntSeq(maxLink);
		
		@Override
		public Point2[] config(){
			Point2[] out = new Point2[targets.size];
			for(int i = 0; i < out.length; i++){
				out[i] = Point2.unpack(targets.get(i)).sub(tile.x, tile.y);
			}
			return out;
		}
		
		@Override
		public void draw(){
			if (this.block.variants != 0 && this.block.variantRegions != null) {
				Draw.rect(this.block.variantRegions[Mathf.randomSeed((long)this.tile.pos(), 0, Math.max(0, this.block.variantRegions.length - 1))], this.x, this.y, this.drawrot());
			} else {
				Draw.rect(this.block.region, this.x, this.y, this.drawrot());
			}
			
			this.drawTeamTop();
			
			float f = 1f - (Time.time / 100f) % 1f;
			
			Draw.color(baseColor, phaseColor, phaseHeat);
			Draw.alpha(heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
			Draw.rect(topRegion, x, y);
			Draw.alpha(1f);
			Lines.stroke(Mathf.clamp(2f * Mathf.curve(f, strokeClamp, 1) + strokeOffset) * heat);
			
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
		public boolean onConfigureBuildTapped(Building other){
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
			Drawf.light(x, y, 50f * smoothEfficiency, baseColor, 0.7f * smoothEfficiency);
		}
		
		@Override
		public void updateTile(){
			smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
			heat = Mathf.lerpDelta(heat, efficiency > 0 ? 1f : 0f, 0.08f);
			charge += heat * Time.delta;
			
			if(hasBoost){
				phaseHeat = Mathf.lerpDelta(phaseHeat, optionalEfficiency, 0.1f);
			}
			
			if(charge >= reload){
				float realRange = range + phaseHeat * phaseRangeBoost;
				
				charge = 0f;
				linkBuilds().each(other -> other.applyBoost(realBoost(), reload + 1f));
			}
			
			if(timer(timerUse, useTime) && efficiency > 0){
				consume();
			}
		}
		
		@Override
		public void drawConfigure(){
			float realRange = range + phaseHeat * phaseRangeBoost;
			
			float offset = size * tilesize / 2f + 1f;
			
			Lines.stroke(3f, Pal.gray);
			Lines.square(x, y, offset + 1f);
			
			Seq<Building> buildings = linkBuilds();
			
			//I just cant use Draw.z() in this shitty method<<<<<
			for(Building b : buildings){
				float targetOffset = b.block.size * tilesize / 2f + 1f;
				float angle = angleTo(b);
				
				boolean right = Mathf.equal(angle, 0, 90);
				boolean up = Mathf.equal(angle, 90, 90);
				boolean horizontal = Mathf.equal(angle, 0, 45) || Mathf.equal(angle, 180, 45);
				
				float
					fromX = x + Mathf.num(horizontal) * Mathf.sign(right) * offset, toX = b.x + Mathf.num(!horizontal) * Mathf.sign(!right) * targetOffset,
					fromY = y + Mathf.num(!horizontal) * Mathf.sign(up) * offset, toY = b.y + Mathf.num(horizontal) * Mathf.sign(!up) * targetOffset;
				
				Tmp.v1.set(horizontal ? toX : fromX, !horizontal ? toY : fromY);
				
				Draw.color(Pal.gray);
				Lines.stroke(3);
				Lines.line(fromX, fromY, Tmp.v1.x, Tmp.v1.y, false);
				Lines.line(Tmp.v1.x, Tmp.v1.y, toX, toY, false);
				Fill.square(Tmp.v1.x, Tmp.v1.y, 1.5f);
				Lines.square(b.x, b.y, b.block().size * tilesize / 2f + 2.0f);
			}
			
			for(Building b : buildings){
				float targetOffset = b.block.size * tilesize / 2f + 1f;
				float angle = angleTo(b);
				
				boolean right = Mathf.equal(angle, 0, 90);
				boolean up = Mathf.equal(angle, 90, 90);
				
				boolean horizontal = Mathf.equal(angle, 0, 45) || Mathf.equal(angle, 180, 45);
				float
						fromX = x + Mathf.num(horizontal) * Mathf.sign(right) * offset, toX = b.x + Mathf.num(!horizontal) * Mathf.sign(!right) * targetOffset,
						fromY = y + Mathf.num(!horizontal) * Mathf.sign(up) * offset, toY = b.y + Mathf.num(horizontal) * Mathf.sign(!up) * targetOffset;
				
				Tmp.v1.set(horizontal ? toX : fromX, !horizontal ? toY : fromY);

				Draw.color(baseColor);
				Lines.stroke(1);
				Lines.line(fromX, fromY, Tmp.v1.x, Tmp.v1.y, false);
				Lines.line(Tmp.v1.x, Tmp.v1.y,toX, toY, false);
				Fill.square(Tmp.v1.x, Tmp.v1.y, 0.5f);
				Draw.alpha(0.35f);
				Draw.mixcol(Color.white, Mathf.absin(4f, 0.45f));
				Fill.square(b.x, b.y, targetOffset);
				Draw.color(baseColor);
				Draw.alpha(1f);
				Lines.square(b.x, b.y, b.block().size * tilesize / 2f + 1.0f);
			}
			
			Lines.stroke(1f, baseColor);
			Lines.square(x, y, offset);
			
			Drawf.dashCircle(x, y, range(), baseColor);
		}
		
		@Override
		public void drawLink(){
			LinkGroupc.super.drawLink();
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
			
			if(other != null && !targets.removeValue(value) && targets.size < maxLink - 1 && within(other, range()))targets.add(value);
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
