package newhorizon.block.special;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
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
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.storage.StorageBlock;
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
	public float range = 1200f;
	public Sound shootSound = Sounds.shootBig;
	public float shake;
	public float recoilAmount = 5f;
	public TextureRegion baseRegion;
	
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
		Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, range, Pal.accent);
	}
	
	@Override
	public void init(){
		super.init();
	}
	
	public class DeliveryBuild extends Building implements Ranged{
		public int link = -1;
		public float rotation = 90;
		public float reload;
		public float heat;
		public float recoil;
		
		public transient DeliveryBuild acceptDelivery;
		
		@Override public boolean acceptItem(Building source, Item item) {
			if(items.get(item) >= getMaximumAccepted(item) || !linkValid())return false;
			if(link().block() instanceof StorageBlock || link() instanceof DeliveryBuild || link() instanceof MassDriver.MassDriverBuild) return link().acceptItem(source, item);
			return link().block().consumes.itemFilters.get(item.id) && this.items.get(item) < Math.min(this.getMaximumAccepted(item), link().getMaximumAccepted(item) / 2);
		}
		
		@Override
		public float range(){
			return range;
		}
		
		public Building link(){
			return Vars.world.build(link);
		}
		
		@Override
		public boolean onConfigureTileTapped(Building other){
			if (this == other || this.link == other.pos()){
				configure(-1);
				return true;
			}
			if (other.team == team && other.block.hasItems && within(other, range())) {
			    if(acceptDelivery != null && acceptDelivery.pos() == other.pos()) return false;
				configure(other.pos());
				Log.info("Link" + other.pos());
				return false;
			}
			return true;
		}
		
		@Override
		public void configure(Object value){
			super.configure(value);
			if(value instanceof Integer){
			    if(link() != null && link() instanceof DeliveryBuild) ((DeliveryBuild)link()).acceptDelivery = null;
				link = (int)value;
				items.clear();
				for(int i = 0; i < 4; i++){
					Time.run(4 * i, () -> {
						if(!isValid())return;
						Tmp.v1.trns(rotation, -(recoil + size * Vars.tilesize / 2f));
						NHFx.trail.at(x + Tmp.v1.x, y + Tmp.v2.y, 3f, team.color);
					});
				}
			}
		}
		
		public boolean linkValid() {
			return link() != null && link().items != null;
		}
		
		@Override
		public void updateTile(){
			heat = Mathf.lerp(heat, 0, coolDown);
			recoil = Mathf.lerp(recoil, 0, coolDown);
			
			if(linkValid()){
				this.rotation = Mathf.slerpDelta(this.rotation, this.angleTo(link()), rotateSpeed * this.efficiency());
				if(link() instanceof DeliveryBuild) ((DeliveryBuild)link()).acceptDelivery = this;
			}
			
			if(linkValid() && Angles.angleDist(rotation, angleTo(link())) < 10){
				reload += efficiency() * Time.delta;
				if(reload >= reloadTime){
					if(shouldDeliver())deliver();
					reload = 0;
				}
			}
		}
		
		protected boolean shouldDeliver(){
			boolean shouldDeliver = true;
			if(linkValid()){
				if(link() instanceof ItemTurret.ItemTurretBuild){
					ItemTurret.ItemTurretBuild turret = (ItemTurret.ItemTurretBuild)link();
					ItemTurret out = (ItemTurret)link().block;
					if(turret.totalAmmo > out.maxAmmo / 2)return false;
				}
				for(Item item : Vars.content.items()){
					if(items.get(item) <= 0)continue;
					if(link().items.get(item) + items.get(item) > link().getMaximumAccepted(item)){
						shouldDeliver = false;
						break;
					}
				}
			}
			else shouldDeliver = false;
			return shouldDeliver;
		}
		
		protected void deliver(){
			if(!linkValid() || items.total() <= 0)return;
			
			int totalUsed = 0;
			this.heat = 1;
			this.recoil = recoilAmount;
			
			DeliveryData data = Pools.obtain(DeliveryData.class, DeliveryData::new);
			data.to = link();
			
			for(int i = 0; i < Vars.content.items().size; ++i) {
				int maxTransfer = this.items.get(Vars.content.item(i));
				data.items[i] = maxTransfer;
				this.items.remove(Vars.content.item(i), maxTransfer);
			}
			
			float lifeScl = Mathf.clamp(dst(link()) / NHContent.deliveryBullet.range(), 0, range / NHContent.deliveryBullet.range());
			NHContent.deliveryBullet.create(this, team, x, y, rotation, 1, 1, lifeScl, data);
			Effect.shake(shake, shake, this);
			shootSound.at(this, Mathf.random(0.9F, 1.1F));
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
		public void drawConfigure(){
			super.drawConfigure();
			
			Drawf.dashCircle(x, y, range(), team.color);
			boolean check = false;
			if(link() instanceof DeliveryBuild) {
			    DeliveryBuild build = (DeliveryBuild)link();
			    while(true) {
			        if(build.link() != null && build.link() instanceof DeliveryBuild) {
			            if(build.link().id == id) {
			                check = true;
			                break;
			            }
			            
			            build = (DeliveryBuild)build.link();
			        }
			        else break;
			    }
			}
			
			drawLinkConfigure(true, id);
			if(!check) 
			    drawLinkConfigure(false, id, true);
		}
		
		// false 向前绘制， true 向后绘制
		//闭合时， 不向后绘制主方块的link()
		public void drawLinkConfigure(boolean accept, int configId) {
		    drawLinkConfigure(accept, configId, false);
		}
		
		public void drawLinkConfigure(boolean accept, int configId, boolean drawed) {
		    if(acceptDelivery != null && accept && acceptDelivery.id != configId) acceptDelivery.drawLinkConfigure(accept, configId);
		    if(linkValid()) {
		        if(!drawed) drawLinkArrow();
		        if(link() instanceof DeliveryBuild && !accept) ((DeliveryBuild)link()).drawLinkConfigure(false, configId);
		    }
		}
		
		protected void drawLinkArrow() {
		    Draw.color(Pal.accent);
			Lines.stroke(1.0F);
			Lines.square(link().x, link().y, link().block.size * Vars.tilesize / 2.0F + 1.0F);
			Draw.reset();
			DrawFuncs.posSquareLink(team.color, 2f, 4f, true, this, link());
			Drawf.arrow(x, y, link().x, link().y, 15f, 6f, team.color);
		}
		
		@Override public void write(Writes write) {
			write.f(this.rotation);
			write.i(this.link);
		}
		@Override public void read(Reads read, byte revision) {
			this.rotation = read.f();
			this.link = read.i();
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