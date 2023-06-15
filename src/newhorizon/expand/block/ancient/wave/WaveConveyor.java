package newhorizon.expand.block.ancient.wave;

import arc.Core;
import arc.func.FloatFloatf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.Autotiler;
import mindustry.world.meta.BlockGroup;
import newhorizon.content.NHColor;
import newhorizon.expand.game.wave.SineWaveEnergySource;
import newhorizon.expand.game.wave.WaveEnergyBuilding;
import newhorizon.expand.game.wave.WaveEnergyStack;
import newhorizon.expand.game.wave.WaveEnergyState;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.tilesize;

public class WaveConveyor extends Block implements Autotiler, WaveEnergyBlock{
	
	public TextureRegion[] topRegions;
	public TextureRegion[] botRegions;
	
	public Color transparentColor = new Color(0.4f, 0.4f, 0.4f, 0.1f);
	
	public float decayCoefficient = 0.965f;
	public int waveTransLength = 4;
	
	public float waveWidth = 4f;
	public float waveStroke = 1f;
	public float maxAcceptEnergy = 10f;
	
	public float waveFloor = 0.025f;
	public float waveCeil = 0.75f;
	
	public Color colorBase = NHColor.lightSkyMiddle;
	public Color colorTo = NHColor.lightSkyFront;
	public Color colorMax = NHColor.ancientHeat;
	
	public boolean armored = false;
	
	/** Should be limited to [0, 1], avoid using a {@link arc.math.Mathf#clamp(float)} method to save res.*/
	public FloatFloatf colorLerpCurve = input -> input * input * input;
	
	
	public WaveConveyor(String name){
		super(name);
		
		rotate = true;
		solid = true;
		
		group = BlockGroup.transportation;
		update = true;
		conveyorPlacement = true;
		unloadable = false;
		noUpdateDisabled = true;
		underBullets = true;
		noSideBlend = true;
		isDuct = true;
		
		configurable = true;
		priority = TargetPriority.transport;
	}
	
	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
		int[] bits = getTiling(plan, list);
		
		if(bits == null) return;
		
		Draw.scl(bits[1], bits[2]);
		Draw.alpha(0.5f);
		Draw.rect(botRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
		Draw.color();
		Draw.rect(topRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
		Draw.scl();
	}
	
	@Override
	public TextureRegion[] icons(){
		return new TextureRegion[]{Core.atlas.find("duct-bottom"), topRegions[0]};
	}
	
	@Override
	public void load(){
		super.load();
		
		botRegions = new TextureRegion[5];
		topRegions = new TextureRegion[5];
		
		int i;
		for(i = 0; i < 5; ++i) {
			botRegions[i] = Core.atlas.find(name + "-bot-" + i);
		}
		
		topRegions = new TextureRegion[5];
		
		for(i = 0; i < 5; ++i) {
			topRegions[i] = Core.atlas.find(name + "-top-" + i);
		}
	}
	
	@Override
	public boolean blendsArmored(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
		return Point2.equals(tile.x + Geometry.d4(rotation).x, tile.y + Geometry.d4(rotation).y, otherx, othery)
				|| ((!otherblock.rotatedOutput(otherx, othery) && Edges.getFacingEdge(otherblock, otherx, othery, tile) != null &&
				Edges.getFacingEdge(otherblock, otherx, othery, tile).relativeTo(tile) == rotation) ||
				
				((otherblock.rotatedOutput(otherx, othery)) && (otherblock.isDuct) && Point2.equals(otherx + Geometry.d4(otherrot).x, othery + Geometry.d4(otherrot).y, tile.x, tile.y)));
	}
	
	@Override
	public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
		if(!(otherblock instanceof WaveEnergyBlock))return false;
		WaveEnergyBlock wb = (WaveEnergyBlock)otherblock;
		
		if(!armored){
			return (wb.outputWave() || (lookingAt(tile, rotation, otherx, othery, otherblock)))
					&& lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
		}else{
			return (wb.outputWave() && blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock)) || (lookingAt(tile, rotation, otherx, othery, otherblock));
		}
	}
	
	@Override
	public boolean acceptWave(){
		return true;
	}
	
	@Override
	public boolean outputWave(){
		return true;
	}
	
//	protected static void rotate90(Vec2 vec2, int dir){
//		float x = vec2.x;
//
//		if(dir == 0)return;
//		else if(dir == 1){
//			vec2.x = -vec2.y;
//			vec2.y = x;
//		}else if(dir == 2){
//			vec2.x
//		}
//	}
	
	public class WaveConveyorBuild extends Building implements WaveEnergyBuilding{
		public int blendbits, xscl, yscl, blending;
		byte recDir;
		public @Nullable Building next;
		public @Nullable
		WaveEnergyBuilding nextc;
		
		public WaveEnergyState energy;
		public WaveEnergyStack waveStack;
		
		{
			energy = waveStack = new WaveEnergyStack(waveTransLength);
			energy.decayScl = decayCoefficient;
		}
		
		@Override
		public void handleItem(Building source, Item item){
			recDir = relativeToEdge(source.tile);
			
			noSleep();
		}
		
		@Override
		public void created(){
			energy.init(this);
		}
		
		
		@Override
		public void onProximityUpdate(){
			super.onProximityUpdate();
			
			int[] bits = buildBlending(tile, rotation, null, true);
			blendbits = bits[0];
			xscl = bits[1];
			yscl = bits[2];
			blending = bits[4];
			next = front();
			nextc = next instanceof WaveEnergyBuilding ? (WaveEnergyBuilding)next : null;
		}
		
		@Override
		public void buildConfiguration(Table table){
			table.button(Icon.upOpen, () -> {
				energy = new SineWaveEnergySource().init(10, 0.1f);
				energy.init(this);
			});
		}
		
		@Override
		public void drawSelect(){
			super.drawSelect();
			
			DrawFunc.overlayText(energy.getCurrentEnergy() + "", x, y - 12, 0, team.color, false);
		}
		
		@Override
		public boolean acceptItem(Building source, Item item){
			if(!(source instanceof WaveEnergyBuilding))return false;
			WaveEnergyState ee = nextc.getWave();
			
			return item == null &&
				(armored ?
						//armored acceptance
						((source.block.rotate && source.front() == this && ee.hasOutput() && source.block.isDuct) ||
								Edges.getFacingEdge(source.tile(), tile).relativeTo(tile) == rotation) :
						//standard acceptance - do not accept from front
						!(source.block.rotate && next == source) && Edges.getFacingEdge(source.tile, tile) != null && Math.abs(Edges.getFacingEdge(source.tile, tile).relativeTo(tile.x, tile.y) - rotation) != 2
				);
	}
		
		@Override
		public void updateTile(){
			energy.update();
			
			if(nextc != null){
				WaveEnergyState targetEnergy = nextc.getWave();
				if(targetEnergy.acceptInput(this)){
					targetEnergy.input(this, energy.output(targetEnergy.expectInput()) * decayCoefficient);
				}
			}
			
		}
		
		@Override
		public void payloadDraw(){
			Draw.rect(fullIcon, x, y);
		}
		
		@Override
		public void draw(){
			float rotation = rotdeg();
			int r = this.rotation;
			
			//draw extra ducts facing this one for tiling purposes
			for(int i = 0; i < 4; i++){
				if((blending & (1 << i)) != 0){
					int dir = r - i;
					float rot = i == 0 ? rotation : (dir)*90;
					drawAt(x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, 0, rot, i != 0 ? SliceMode.bottom : SliceMode.top);
				}
			}
			
			float i = 0;
			
			for(float e : waveStack.stacks){
				float lerp = waveStack.getAxis(e);
				
				Draw.z(Layer.blockUnder + 0.1f);
				Tmp.v1
					.set(Geometry.d4x(recDir) * tilesize / 2f, Geometry.d4y(recDir) * tilesize / 2f)
					.lerp(Geometry.d4x(r) * tilesize / 2f, Geometry.d4y(r) * tilesize / 2f, Mathf.clamp((i / waveTransLength + 1f) / 2f));
				
				i++;
				
				Fill.circle(x + Tmp.v1.x, y + Tmp.v1.y, 2);
			}
			
			//draw item
			
			
			Draw.scl(xscl, yscl);
			drawAt(x, y, blendbits, rotation, SliceMode.none);
			Draw.reset();
		}
		
		protected void drawAt(float x, float y, int bits, float rotation, SliceMode slice){
			Draw.z(Layer.blockUnder);
			Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);
			
			Draw.z(Layer.blockUnder + 0.2f);
			Draw.color(transparentColor);
			Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);
			Draw.color();
			Draw.rect(sliced(topRegions[bits], slice), x, y, rotation);
		}
		
		@Override
		public WaveEnergyState getWave(){
			return energy;
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.b(recDir);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			recDir = read.b();
		}
	}
}
