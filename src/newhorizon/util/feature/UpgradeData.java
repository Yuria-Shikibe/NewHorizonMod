package newhorizon.util.feature;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;
import mindustry.world.modules.ItemModule;
import newhorizon.NewHorizon;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.content.NHUpgradeDatas;
import newhorizon.expand.interfaces.Upgraderc;
import newhorizon.util.ui.TableFunc;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.*;

public class UpgradeData implements Comparable<UpgradeData>{
	public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
	
	public TextureRegion icon, turretRegion;
	public String name, localizedName, description;
	public float costTime;
	public int maxLevel;
	public boolean isLeveled;
	public int defaultLevel = 0;
	
	public float itemCostParma = 1;
	public float timeCostParma = 1;
	
	public Effect
			chargeEffect = Fx.none,
			chargeBeginEffect = Fx.none;
	public float continuousTime = 0f;
	public float inaccuracy;
	public float velocityInaccuracy;
	public float chargeTime = 0f;
	public float reloadTime;
	public float randX;
	public float range = -1;
	public int salvos = 1;
	public float burstSpacing = 5f;
	public BulletType selectAmmo;
	public Sound shootSound = Sounds.bigshot, chargeSound;
	
	public float reloadDamageUp;
	public float reloadSpeedUp;
	public float defenceUp;
	
	public float maxDamageScl = 2f;
	public float maxReloadReduce = 0.65f;
	public float maxDamageReduce = 0.65f;
	
	public UpgradeData(){
		this("level-up", NHBullets.none, 0, new ItemStack(Items.copper, 0));
	}
	
	public UpgradeData(
			String name,
			BulletType selectAmmo,
			float costTime,
			ItemStack... items
	) {
		this.costTime = costTime;
		requirements.addAll(items);
		this.selectAmmo = selectAmmo;
		this.name = name;
		NHUpgradeDatas.all.add(this);
	}
	
	public void init(){
		localizedName = Core.bundle.get("upgrade-data." + name, "null");
		description = Core.bundle.get("upgrade-data." + name + ".description", "null");
		
		if(maxDamageReduce >= 1)maxDamageReduce %= 1;
		if(!isLeveled)maxLevel = 1;
	}
	
	
	public void load(){
		this.icon = Core.atlas.find(NewHorizon.name(name));
	}
	
	@Override
	public String toString(){
		return "#UpgradeData: " + name;
	}
	
	public DataEntity newSubEntity(){
//		DataEntity data = Pools.obtain(DataEntity.class, DataEntity::new);
//		data.init(defaultLevel);
//		return data;
		return new DataEntity(){{init(defaultLevel);}};
	}
	
	@Override
	public int compareTo(@NotNull UpgradeData o){
		return Integer.compare(hashCode(), o.hashCode());
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(!(o instanceof UpgradeData)) return false;
		UpgradeData that = (UpgradeData)o;
		return Objects.equals(name, that.name) && Objects.equals(selectAmmo.id, that.selectAmmo.id);
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(name, selectAmmo.id);
	}
	
	protected static void sep(Table table, String text){
		table.row();
		table.add(text);
	}
	
	public static StatValue ammo(Seq<UpgradeData> datas, int indent){
		return table -> {
			
			table.row();
			
			Seq<UpgradeData> seq = datas.copy().sort();
			
			for(UpgradeData data : seq){
				BulletType type = data.selectAmmo;
				
				//no point in displaying unit icon twice
				
				table.table().padTop(OFFSET);
				table.image(data.icon).size(3 * 8).padRight(4).right().top();
				table.add(data.localizedName).padRight(10).left().top();
				
				table.table(bt -> {
					bt.left().defaults().padRight(3).left();
					
					if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
						if(type.continuousDamage() > 0){
							bt.add(Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized());
						}else{
							bt.add(Core.bundle.format("bullet.damage", type.damage));
						}
					}
					
					if(type.buildingDamageMultiplier != 1){
						sep(bt, Core.bundle.format("bullet.buildingdamage", (int)(type.buildingDamageMultiplier * 100)));
					}
					
					if(type.splashDamage > 0){
						sep(bt, Core.bundle.format("bullet.splashdamage", (int)type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
					}
					
					if(!Mathf.equal(type.reloadMultiplier, 1f)){
						sep(bt, Core.bundle.format("bullet.reload", Strings.autoFixed(type.reloadMultiplier, 2)));
					}
					
					if(type.knockback > 0){
						sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
					}
					
					if(type.healPercent > 0f){
						sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)));
					}
					
					if(type.pierce || type.pierceCap != -1){
						sep(bt, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
					}
					
					if(type.incendAmount > 0){
						sep(bt, "@bullet.incendiary");
					}
					
					if(type.homingPower > 0.01f){
						sep(bt, "@bullet.homing");
					}
					
					if(type.lightning > 0){
						sep(bt, Core.bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
					}
					
					if(type.status != StatusEffects.none){
						sep(bt, (type.minfo.mod == null ? type.status.emoji() : "") + "[stat]" + type.status.localizedName);
					}
					
					if(type.fragBullet != null){
						sep(bt, Core.bundle.format("bullet.frags", type.fragBullets));
						bt.row();
						
						StatValues.ammo(ObjectMap.of(UnitTypes.block, type.fragBullet), indent + 1).display(bt);
					}
				}).padTop(-9).padLeft(indent * 8).left().get().background(Tex.underline);
				
				table.row();
			}
		};
	}
	
	public class DataEntity{
		public int level;
		public boolean isUnlocked, selected;
		
		@Override
		public String toString(){
			return "DataEntity{" + type().toString() + " | level:" + level + '}';
		}
		
		public float costTime() {
			return costTime * (1 + (isLeveled ? level * timeCostParma : 0));
		}
		
		public boolean isMaxLevel(){ return !isLeveled || level >= UpgradeData.this.maxLevel; }
		
		public boolean available(){return isUnlocked;}
		
		public void upgrade(){
			isUnlocked = true;
			if(isLeveled)level ++;
			else level = 1;
		}
		
		public void init(int level){
			this.level = level;
			if(level > 0)isUnlocked = true;
		}
		
		public float speedUP(){
			return Mathf.clamp(reloadSpeedUp * this.level, Float.MIN_VALUE, maxReloadReduce);
		}
		
		public float defenceUP(){
			return Mathf.clamp(defenceUp * this.level, Float.MIN_VALUE, maxDamageReduce);
		}
		
		public UpgradeData type(){
			return UpgradeData.this;
		}
		
		public void write(Writes write) {
			write.bool(this.isUnlocked);
			write.bool(this.selected);
			write.i(this.level);
		}
		
		public void read(Reads read, byte revision) {
			this.isUnlocked = read.bool();
			this.selected = read.bool();
			this.level = read.i();
		}
		
		public void showInfo(boolean drawCons, Upgraderc from, ItemModule module){
			BaseDialog dialog = new BaseDialog("@consume");
			dialog.addCloseListener();
			dialog.cont.pane(table -> {
				table.margin(15f);
				table.table(Tex.clear, t -> {
					t.image(icon).fill();
					t.button(new TextureRegionDrawable(NHContent.ammoInfo), Styles.colori, () -> new BaseDialog("@info") {{
						addCloseListener();
						cont.pane(t -> cont.pane(table -> buildBulletTypeInfo(table, selectAmmo)).size(460).row()).row();
						cont.button("@back", Icon.left, Styles.cleart, this::hide).size(LEN * 3, LEN).pad(OFFSET / 2);
					}}.show()).fill().padLeft(OFFSET);
				}).growX().row();
				table.add("<< " + localizedName + " >>").color(Pal.accent).padTop(OFFSET).row();
				table.add("Description: ").color(Pal.accent).left().padTop(OFFSET).row();
				table.add(tabSpace + description).color(Color.lightGray).left().row();
				table.image().fillX().pad(OFFSET / 3).height(OFFSET / 3).color(Pal.accent).row();
				if(drawCons){
					table.pane(t -> {
						int index = 0;
						for(ItemStack stack : requirements()){
							if(module != null || index % 7 == 0)table.row();
							if(module != null){
								TableFunc.itemStack(table, stack, module);
							}else table.add(new ItemDisplay(stack.item, stack.amount, false)).padLeft(OFFSET / 2).left();
							index ++;
						}
					}).left().row();
					table.add("[lightgray]CanUpgrade?: " + judge(from.canUpgrade(this)) + "[]").left().row();
				}
				table.row();
			}).grow().row();
			dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).growX().height(LEN);
			dialog.show();
		}
		
		public void buildTable(Table cont, Upgraderc from) {
			Table info = new Table(Tex.pane, t -> {
				Label label = new Label("");
				t.update(() -> {
					label.setText(new StringBuilder().append("[lightgray]NeededTime: [accent]").append(format(costTime() / 60)).append("[lightgray] sec[]"));
					if(!isLeveled && available())t.remove();
					if(isLeveled && isMaxLevel())t.remove();
				});
				
				t.pane(table -> table.image(icon).size(LEN).left()).left().fill().padLeft(OFFSET);
				
				t.pane(table -> {
					table.add(localizedName).color(Pal.accent).growX().left().row();
					
					table.add(label).left().row();
					if(type().isLeveled){
						table.image().fillX().pad(OFFSET / 2).height(4f).color(Color.lightGray).left().row();
						Label labelL = new Label(""), labelR = new Label(""), lableD = new Label("");
						
						table.update(() -> {
							labelL.setText(new StringBuilder().append("[lightgray]Level: [accent]").append(level));
							labelR.setText(new StringBuilder().append("[lightgray]ReloadSpeedUp: [accent]").append(getPercent(speedUP())));
							lableD.setText(new StringBuilder().append("[lightgray]DefenceUP: [accent]").append(getPercent(defenceUP())));
						});
						
						table.add(labelL).left().row();
						table.add(labelR).left().row();
						table.add(lableD).left().row();
					}
				}).growX().height(LEN).padLeft(OFFSET / 2).padRight(OFFSET / 2);
				
				t.table(Tex.clear, table -> {
					table.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(true, from, from.core().items)).size(LEN);
					table.button(Icon.upOpen, Styles.clearPartiali, () -> from.configure(from.all().indexOf(this))).size(LEN).disabled(b -> !from.canUpgrade(this) || from.isUpgrading());
				}).fillX().height(LEN).right().padRight(OFFSET);
			});
			cont.add(info).padTop(OFFSET / 2).padBottom(OFFSET / 2).growX().fillY().row();
		}
		
		public ItemStack[] requirements() {
			return ItemStack.mult(requirements.toArray(), (itemCostParma * level + 1f) * Vars.state.rules.buildCostMultiplier);
		}
	}

}