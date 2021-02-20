package newhorizon.feature;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
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
import newhorizon.NewHorizon;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.content.NHItems;
import newhorizon.content.NHUpgradeDatas;
import newhorizon.interfaces.Upgraderc;

import static newhorizon.func.TableFuncs.*;

public class UpgradeData{
	public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
	public int unlockLevel;
	public TextureRegion icon;
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
	public int salvos = 1;
	public float burstSpacing = 5f;
	public BulletType selectAmmo;
	public Sound shootSound = Sounds.bigshot;
	
	public float reloadSpeedUp;
	public float defenceUp;
	public float maxReloadReduce = 0.65f;
	public float maxDamageReduce = 0.65f;
	
	public UpgradeData(){
		this("level-up", NHBullets.none, 0, 0, new ItemStack(NHItems.emergencyReplace, 0));
	}
	
	public UpgradeData(
			String name,
			BulletType selectAmmo,
			float costTime,
			int unlockLevel,
			ItemStack... items
	) {
		this.costTime = costTime;
		this.unlockLevel = unlockLevel;
		requirements.addAll(items);
		this.selectAmmo = selectAmmo;
		
		NHUpgradeDatas.all.add(this);
	}
	
	public void init(){
		name = "upgrade-data." + name;
		localizedName = Core.bundle.get(name, "null");
		description = Core.bundle.get(name + ".description", "null");
		
		if(maxDamageReduce >= 1)maxDamageReduce %= 1;
	}
	
	
	public void load(){
		this.icon = Core.atlas.find(NewHorizon.MOD_NAME + name);
	}
	
	@Override
	public String toString(){
		return "[UpgradeData]: " + name;
	}
	
	public DataEntity newSubEntity(){
		return new DataEntity(){{init(defaultLevel);}};
	}
	
	public class DataEntity{
		public int level;
		public boolean isUnlocked, selected;
		
		public float costTime() {
			return costTime * (1 + (isLeveled ? level * timeCostParma : 0)) * Vars.state.rules.buildSpeedMultiplier;
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
		
		public void showInfo(boolean drawCons, Upgraderc from){
			BaseDialog dialog = new BaseDialog("@consume");
			dialog.addCloseListener();
			dialog.cont.margin(15f);
			dialog.cont.table(Tex.button, table -> {
				table.pane( t -> t.image(icon)).size(icon.height + OFFSET / 2).left();
				table.pane(this::infoText).size(icon.height + OFFSET / 2).pad(OFFSET / 2);
			}).row();
			dialog.cont.add("<< " + localizedName + " >>").color(Pal.accent).row();
			dialog.cont.add("Description: ").color(Pal.accent).left().row();
			dialog.cont.add(tabSpace + description).color(Color.lightGray).left().row();
			if(drawCons){
				dialog.cont.pane(table -> {
					int index = 0;
					for(ItemStack stack : requirements()){
						if(index % 5 == 0)table.row();
						table.add(new ItemDisplay(stack.item, stack.amount, false)).padRight(5).left();
						index ++;
					}
				}).left().row();
				if(UpgradeData.this.unlockLevel > 0)dialog.cont.add("[lightgray]Requires Level: [accent]" + unlockLevel + "[]").left().row();
				dialog.cont.add("[lightgray]CanUpgrade?: " + getJudge(from.canUpgrade(this)) + "[]").left().row();
			}
			dialog.cont.image().width(300f).pad(2).height(4f).color(Pal.accent);
			dialog.cont.row();
			dialog.cont.button("@back", Icon.left, dialog::hide).size(LEN * 2.5f, LEN).pad(OFFSET / 3);
			dialog.show();
		}
		
		public void buildTable(Table cont, Upgraderc from) {
			Table info = new Table(Tex.button, t -> {
				Label label = new Label("");
				t.update(() -> {
					label.setText(new StringBuilder().append("[lightgray]NeededTime: [accent]").append(format(costTime() / 60)).append("[lightgray] sec[]"));
					if(!isLeveled && available())t.remove();
					if(isLeveled && isMaxLevel())t.remove();
				});
				
				t.pane(table -> table.image(icon).size(LEN).left()).left().padLeft(OFFSET / 2f).size(LEN);
				
				t.pane(table -> {
					table.add(localizedName).color(Pal.accent).left().row();
					
					table.add(label).left().row();
					if(UpgradeData.this.isLeveled){
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
				}).size(LEN * 6f, LEN * 1.5f).left().pad(OFFSET);
				
				t.table(Tex.button, table -> {
					table.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(true, from)).size(LEN);
					table.button(Icon.upOpen, Styles.clearPartiali, this::upgrade).size(LEN).disabled(b -> !from.canUpgrade(this));
				}).height(LEN + OFFSET).left().pad(OFFSET);
			});
			cont.add(info).pad(OFFSET / 2).fillX().height(LEN * 2f).row();
		}
		
		public void infoText(Table table){
			table.button(new TextureRegionDrawable(NHContent.ammoInfo), Styles.colori, () -> new BaseDialog("@Info") {{
				addCloseListener();
				cont.pane(t -> cont.pane(table -> buildBulletTypeInfo(table, selectAmmo)).size(460).row()).row();
				cont.button("@back", Icon.left, Styles.cleart, this::hide).size(LEN * 3, LEN).pad(OFFSET / 2);
			}}.show()).size(NHContent.ammoInfo.height + OFFSET / 2);
		}
		
		public ItemStack[] requirements() {
			return ItemStack.mult(requirements.toArray(), (itemCostParma * level + 1f) * Vars.state.rules.buildCostMultiplier);
		}
	}

}