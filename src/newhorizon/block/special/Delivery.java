package newhorizon.block.special;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;
import mindustry.world.meta.BlockGroup;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.effects.EffectTrail;
import newhorizon.func.DrawFuncs;

public class Delivery extends Block{
	public float rotateSpeed = 0.04f;
	public float reloadTime = 100.0F;
	public float coolDown = 0.1f;
	public float lerpSpeedScl = 2f;
	public float strokeScl = 2f;
	public float range = 1600f;
	public Sound shootSound = Sounds.shootBig;
	public float shake;
	public float recoilAmount = 5f;
	public TextureRegion baseRegion;
	public int minDistribute = 10;
	
	public Effect shootEffect = NHFx.boolSelector, smokeEffect = NHFx.boolSelector;
	
	public Delivery(String name) {
		super(name);
		this.acceptsItems = true;
		configurable = true;
		this.hasItems = true;
		this.hasPower = true;
		this.solid = true;
		this.update = true;
		this.group = BlockGroup.storage;
		outlineIcon = true;
		expanded = true;
	}
	
	@Override
	protected TextureRegion[] icons() {
		return this.teamRegion.found() && this.minfo.mod == null ? new TextureRegion[]{baseRegion, teamRegions[Team.sharded.id], region} : new TextureRegion[]{baseRegion, region};
	}
	
	@Override
	public void load(){
		super.load();
		baseRegion = Core.atlas.find(name + "-base");
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		super.drawPlace(x, y, rotation, valid);
	}
	
	@Override
	public void init(){
		super.init();
	}
	
	public class DeliveryBuild extends Building{
		public float rotation = 90;
		public float reload;
		public float heat;
		public float recoil;
		public CoreBuild core;
		
		public boolean coreValid() {
			return core != null && core.items != null && !core.items.empty();
		}
		
		public void setCore(){
			core = this.team.core();
		}
		
		@Override
		public void updateTile(){
			heat = Mathf.lerp(heat, 0, coolDown);
			recoil = Mathf.lerp(recoil, 0, coolDown);
			
			if(!coreValid() && timer.get(20))setCore();
			if(coreValid()){
				this.rotation = Mathf.slerpDelta(this.rotation, this.angleTo(core), rotateSpeed * this.efficiency());
			}
			
			if(coreValid() && Angles.angleDist(rotation, angleTo(core)) < 10){
				if(this.items.total() >= minDistribute){
					reload += efficiency() * Time.delta;
					if(reload >= reloadTime){
						deliver();
						reload = 0;
					}
				}
			}
		}
		
		protected void deliver(){
			int totalUsed = 0;
			this.heat = 1;
			this.recoil = recoilAmount;
			
			DeliveryData data = Pools.obtain(DeliveryData.class, DeliveryData::new);
			data.to = core;
			
			for(int i = 0; i < Vars.content.items().size; ++i) {
				int maxTransfer = Math.min(this.items.get(Vars.content.item(i)), itemCapacity - totalUsed);
				data.items[i] = maxTransfer;
				totalUsed += maxTransfer;
				this.items.remove(Vars.content.item(i), maxTransfer);
			}
			
			float lifeScl = Mathf.clamp(dst(core) / NHContent.deliveryBullet.range(), 0, range / NHContent.deliveryBullet.range());
			NHContent.deliveryBullet.create(this, team, x, y, rotation, 1, 1, lifeScl, data);
			Effect.shake(shake, shake, this);
			shootSound.at(this.tile, Mathf.random(0.9F, 1.1F));
		}
		
		@Override
		public void placed(){
			super.placed();
			setCore();
		}
		
		@Override
		public void draw(){
			Draw.rect(baseRegion, x, y);
			Tmp.v1.trns(rotation, -recoil);
			Drawf.shadow(region, x + Tmp.v1.x - (float)size / 2.0F, y + Tmp.v1.y - (float)size / 2.0F, this.rotation - 90.0F);
			Draw.z(Layer.turret);
			Draw.rect(region, x + Tmp.v1.x, y + Tmp.v1.y, rotation - 90f);
			Draw.reset();
		}
		
		@Override
		public boolean acceptItem(Building source, Item item){
			return this.items.total() < itemCapacity ;
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			if(coreValid()){
				Draw.color(team.color);
				Lines.stroke(heat * strokeScl);
				Fill.circle(x, y, Lines.getStroke() * Vars.tilesize);
				Fill.circle(core.x, core.y, Lines.getStroke() * Vars.tilesize);
				DrawFuncs.posSquareLink(team.color, 2f, 4f, true, this, core);
			}
		}
		
		@Override public void write(Writes write) {
			write.f(this.rotation);
		}
		@Override public void read(Reads read, byte revision) {
			this.rotation = read.f();
		}
	}
	
	public static class DeliveryData implements Pool.Poolable{
		public EffectTrail t;
		public Building to;
		public int[] items;
		
		public DeliveryData() {
			this.items = new int[Vars.content.items().size];
		}
		
		public void reset() {
			t = null;
			this.to = null;
		}
	}
}
