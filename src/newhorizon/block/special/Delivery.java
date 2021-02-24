package newhorizon.block.special;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
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
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.meta.BlockGroup;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.effects.EffectTrail;
import newhorizon.func.DrawFuncs;
import newhorizon.func.NHSetting;
import newhorizon.func.Tables;
import newhorizon.interfaces.Linkablec;

import java.util.Arrays;

import static newhorizon.func.TableFuncs.LEN;

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
		
		config(Integer.class, (DeliveryBuild tile, Integer point) -> tile.link = point);
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
		Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, range, Pal.place);
	}
	
	@Override
	public void init(){
		super.init();
	}
	
	public class DeliveryBuild extends Building implements Ranged, Linkablec{
		public int link = -1;
		public float rotation = 90;
		public float reload;
		public float heat;
		public float recoil;
		public boolean closure = false;
		public boolean transportBack = false;
		public Tables.ItemSelectTable itemTable = new Tables.ItemSelectTable();
		public transient DeliveryBuild acceptDelivery;
		
		@Override public boolean acceptItem(Building source, Item item) {
			if(transportBack){
				if(source != link())return false;
				else return items.get(item) < getMaximumAccepted(item);
			}
			if(items.get(item) >= getMaximumAccepted(item) || !linkValid())return false;
		    if(closure) return items.get(item) < getMaximumAccepted(item);
			if(link().block() instanceof StorageBlock || link() instanceof DeliveryBuild || link() instanceof MassDriver.MassDriverBuild) return link().acceptItem(source, item);
			return link().block().consumes.itemFilters.get(item.id) && this.items.get(item) < Math.min(this.getMaximumAccepted(item), link().getMaximumAccepted(item) / 2);
		}
		@Override public float range(){
			return range;
		}
		@Override public int linkPos(){
			return link;
		}
		@Override public void linkPos(int value){
			link = value;
		}
		
		@Override
		public boolean onConfigureTileTapped(Building other){
			if (this == other || this.link == other.pos()){
				configure(-1);
				return true;
			}
			if (other.team == team && other.block.hasItems && within(other, range())) {
			    if(acceptDelivery != null && acceptDelivery.pos() == other.pos()) return false;
			    if(other instanceof DeliveryBuild && ((DeliveryBuild)other).acceptDelivery != null) return false;
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
			    setNull();
			    linkPos((int)value);
				items.clear();
				for(int i = 0; i < 4; i++){
					Time.run(4 * i, () -> {
						if(!isValid())return;
						Tmp.v1.trns(rotation, -(recoil + size * Vars.tilesize / 2f));
						NHFx.trail.at(x + Tmp.v1.x, y + Tmp.v2.y, 3f, team.color);
					});
				}
				if(link() instanceof DeliveryBuild) ((DeliveryBuild)link()).acceptDelivery = this;
				flushLink();
			}
		}
		
		public void flushLink(){
		    ObjectSet<DeliveryBuild> set = new ObjectSet<>();
		    if(acceptDelivery != null) {
			    DeliveryBuild build = acceptDelivery;
			    while(true) {
			        if(build.acceptDelivery != null) {
			            set.add(build);
			            if(build.acceptDelivery.id == id) {
			                closure = true;
			                break;
			            }
			            
			            build = build.acceptDelivery;
			        } else {
			            closure = false;
			            break;
			        }
				    NHSetting.debug(() -> Log.info("Number of attempts: @", set.size));
			    }
		    }
		    else closure = false;
			NHSetting.debug(() -> Log.info("closure: @", closure));
		    set.each(ent -> ent.closure = closure);
        }
		
		public boolean linkValid() { return link() != null && link().team == team && link().items != null && link().isValid(); }
		
		@Override
		public Color getLinkColor(){
			return team.color;
		}
		
		@Override
		public void updateTile(){
			heat = Mathf.lerp(heat, 0, coolDown);
			recoil = Mathf.lerp(recoil, 0, coolDown);
			
			if(linkValid()){
				this.rotation = Mathf.slerpDelta(this.rotation, this.angleTo(link()), rotateSpeed * this.efficiency());
			}
			
			
			Log.info(reload + " | " + shouldDeliver());
			if(linkValid() && Angles.angleDist(rotation, angleTo(link())) < 10){
				if(reload >= reloadTime){
					if(shouldDeliver())deliver();
					reload = 0;
				}else reload += efficiency() * Time.delta;
			}
			
			if(transportBack){
				for(Item item : Vars.content.items()){
					dump(item);
				}
			}
		}
		
		public void setNull(){
			if(linkValid() && link() instanceof DeliveryBuild){
			    DeliveryBuild build = (DeliveryBuild)link();
			    build.acceptDelivery = null;
			    build.closure = false;
		    }
		}
		
		@Override
		public void remove(){
			super.remove();
			setNull();
			flushLink();
		}
		
		protected boolean shouldDeliver(){
			boolean shouldDeliver = true;
			if(linkValid()){
				if(!transportBack){
					if(link() instanceof ItemTurret.ItemTurretBuild){
						ItemTurret.ItemTurretBuild turret = (ItemTurret.ItemTurretBuild)link();
						ItemTurret out = (ItemTurret)link().block;
						if(turret.totalAmmo > out.maxAmmo / 2) return false;
					}
					for(Item item : Vars.content.items()){
						if(items.get(item) <= 0) continue;
						if(link().items.get(item) + items.get(item) > link().getMaximumAccepted(item)){
							shouldDeliver = false;
							break;
						}
					}
				}else{
					shouldDeliver = false;
					for(Item item : itemTable.getItems()){
						if(link().items().get(item) > 0 && acceptItem(link(), item)){
							shouldDeliver = true;
							break;
						}
					}
				}
			}else shouldDeliver = false;
			return shouldDeliver;
		}
		
		protected void deliver(){
			if(!linkValid() || (items.total() <= 0 && !transportBack))return;
			
			int totalUsed = 0;
			this.heat = 1;
			this.recoil = recoilAmount;
			
			DeliveryData data = Pools.obtain(DeliveryData.class, DeliveryData::new);
			data.to = link();
			data.from = this;
			data.transportBack = transportBack;
			
			if(!transportBack){
				for(int i = 0; i < Vars.content.items().size; ++i) {
					int maxTransfer = this.items.get(Vars.content.item(i));
					data.items[i] = maxTransfer;
					this.items.remove(Vars.content.item(i), maxTransfer);
				}
			}else{
				for(Item item : itemTable.getItems()) {
					data.items[Vars.content.items().indexOf(item)] = getMaximumAccepted(item);
				}
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
		public void buildConfiguration(Table table){
			Label l = new Label("@default");
			table.table(Tex.button, t -> {
				t.update(() -> {
					if(transportBack){
						l.setText("@stat.input");
						itemTable.color.a = 1;
					}else{
						l.setText("@stat.output");
						itemTable.color.a = 0;
					}
				});
				t.add(l).center().growX().height(LEN).row();
				t.button(Icon.upOpen, Styles.cleari, LEN, () -> {
					transportBack = !transportBack;
				}).update(b -> b.getStyle().imageUp = transportBack ? Icon.downOpen : Icon.upOpen).growX().fillY().row();
			}).growX().fillY().row();
			table.add(itemTable);
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			drawLinkConfigure(true, id);
			if(!closure)
				drawLinkConfigure(false, id, true);
			
			if(linkValid()){
				Drawf.square(link().x, link().y, link().block().size * Vars.tilesize / 2f + Vars.tilesize / 2f, Pal.place);
				DrawFuncs.drawConnected(link().x, link().y, link().block().size * Vars.tilesize / 2f + Vars.tilesize * 2f, Pal.place);
			}
			
			Drawf.dashCircle(x, y, range(), team.color);
		}
		
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
		    drawLink();
		}
		
		@Override public void write(Writes write) {
			write.f(this.rotation);
			write.i(this.link);
			write.bool(closure);
			write.bool(transportBack);
			itemTable.write(write);
		}
		@Override public void read(Reads read, byte revision) {
			this.rotation = read.f();
			this.link = read.i();
			closure = read.bool();
			transportBack = read.bool();
			itemTable.read(read, revision);
		}
	}
	
	public static class DeliveryData implements Pool.Poolable{
		public EffectTrail t;
		public Building to;
		public Building from;
		public boolean transportBack = false;
		public boolean needRotate = false;
		public int[] items;
		
		public DeliveryData() {
			this.items = new int[Vars.content.items().size];
		}
		
		public DeliveryData(DeliveryData data, boolean rotate){
			this.needRotate = rotate;
			items = data.items.clone();
			to = data.from;
			from = data.to;
		}
		
		public void reset() {
			t = null;
			this.to = null;
		}
		
		@Override
		public String toString(){
			return "DeliveryData{" + "t=" + t + ", to=" + to + ", from=" + from + ", transportBack=" + transportBack + ", needRotate=" + needRotate + ", items=" + Arrays.toString(items) + '}';
		}
	}
}