package newhorizon.contents.blocks.special;

import arc.input.*;
import arc.util.pooling.*;
import arc.util.io.*;
import arc.*;
import arc.scene.style.*;
import arc.func.Cons;
import arc.math.geom.*;
import arc.struct.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.math.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import mindustry.game.*;
import mindustry.ctype.*;
import mindustry.audio.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.logic.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.experimental.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import newhorizon.NewHorizon;

import newhorizon.contents.data.*;
import newhorizon.contents.interfaces.*;
import newhorizon.contents.items.*;
import newhorizon.contents.effects.NHFx;
import newhorizon.contents.colors.*;
import newhorizon.contents.bullets.special.NHLightningBolt;
import newhorizon.contents.data.*;

import static newhorizon.contents.data.UpgradeData.*;

import static mindustry.Vars.*;

public class UpgraderBlock extends Block {
	public static final int DFTID = -2; 
	public static final Seq<UpgraderBlockBuild> upgradecGroup = new Seq<>(UpgraderBlockBuild.class);
	public static final int buttonPerLine = 8;
	public static final float buttonSize = LEN;
	
	protected SoundLoop upgradeSound = new SoundLoop(Sounds.build, 1.1f);
	//Level from 1 - maxLevel
	public int   maxLevel = 9;
	public float upgradeEffectChance = 0.04f;
	//public TextureRegion[] levelRegions;
	
	//
	public Block linkTarget;
	public Color baseColor = Pal.accent;
	public Block toUpgradeClass;
	public Effect upgradeEffect = NHFx.upgrading;
	public float range = 400f;

	public UpgradeBaseData initUpgradeBaseData = new UpgradeBaseData();
	public Seq<UpgradeAmmoData> initUpgradeAmmoDatas = new Seq<>();

	protected void addUpgrades(UpgradeAmmoData... datas) {
		int index = 0;

		for (UpgradeAmmoData data : datas) {
			data.id = index;
			initUpgradeAmmoDatas.add(data);
			index++;
		}
	}

	public UpgraderBlock(String name) {
		super(name);
		update = true;
		buildCostMultiplier = 2;
		configurable = true;
		solid = true;
		//levelRegions = new TextureRegion[maxLevel];
	}
	
	@Override
	public void init(){
		super.init();
		if(linkTarget == null) throw new IllegalArgumentException("null @linkTarget :[red]'" + name + "'[]");
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
	}

	@Override
	public void setBars() {
		super.setBars();
		bars.add("level",
			(UpgraderBlockBuild entity) -> new Bar(
				() -> "Level: " + entity.baseData.level,
				() -> NHColor.lightSky,
				() -> 1
			)
		);

		bars.add("upgradeProgress",
			(UpgraderBlockBuild entity) -> new Bar(
				() -> "RestTime",
				() -> Color.valueOf("#FF732A"),
				() -> entity.remainTime / entity.costTime()
			)
		);
	}

	@Override
	public void load() {
		super.load();
		for (UpgradeAmmoData data : initUpgradeAmmoDatas)data.load();
		initUpgradeBaseData.load();
		/*for(int i = 1; i < maxLevel; i ++){
			levelRegions[i] = Core.atlas.find(name + "-" + i);
		}*/
	}
	
	/*public void allBuilds(Team team){
		new Dialog("All Upgrader") {{
			keyDown(KeyCode.escape, this::hide);
			keyDown(KeyCode.back, this::hide);
			setFillParent(true);
			cont.pane(infos -> {
				for(UpgraderBlockBuild building : upgradecGroup){
					if(building.team.id != team.id)continue;
					building.upgraderTableBuild();
				}
			}).fillX().height(LEN * 5).row();
			cont.button("Back", Icon.exit, this::hide).left().fillX().height(LEN).pad(4);
		}}.show();
	}*/
	
	public class UpgraderBlockBuild extends Building implements Ranged, Upgraderc{
		public UpgradeBaseData baseData = (UpgradeBaseData)initUpgradeBaseData.clone();
		public Seq<UpgradeAmmoData> ammoDatas = new Seq<>();

		public int link = -1;
		public int upgradingID = DFTID;
		public int lastestSelectID = -1;
		public float remainTime;
		
		
		protected BaseDialog dialog = new BaseDialog("Upgrade");
		
		protected boolean coreValid(CoreBlock.CoreBuild core) {
			if(core == null || core.items == null || core.items.empty())return false;
			return true;
		}
		
		protected void consumeItems(UpgradeData data){
			if(state.rules.infiniteResources)return;
			CoreBlock.CoreBuild core = core();
			if(coreValid(core))core.items.remove(data.requirements());
		}
		
		@Override
		public boolean canUpgrade(UpgradeData data) {
			if(data instanceof UpgradeBaseData){
				UpgradeBaseData upgradeData = (UpgradeBaseData)data;
				if(upgradeData.level == maxLevel)return false;
			}
			
			if(data instanceof UpgradeMultData){
				UpgradeMultData upgradeData = (UpgradeMultData)data;
				if(upgradeData.isUnlocked)return false;
			}
			
			CoreBlock.CoreBuild core = core();
			return 
				coreValid(core) && (
					(baseData.level >= data.unlockLevel && !isUpgrading() && core.items.has(data.requirements()) )
					||
					state.rules.infiniteResources
				);
		}

		public float costTime(){
			return 
				upgradingID == DFTID ? 0 : 
				upgradingID == -1    ? baseData.costTime() :
				upgradingID >= 0     ? ammoDatas.get(upgradingID).costTime() :
				0;
		}
		
		@Override//Data Upgrade
		public void upgradeData(UpgradeData data){
			if(!canUpgrade(data))return;
			consumeItems(data);
			upgradingID = data.id;
			remainTime = costTime();
		}
		
		@Override//Updates
		public void updateUpgrading() {
			if (isUpgrading()) {
				upgradeSound.update(x, y, true);
				remainTime -= (state.rules.infiniteResources ? 100000 : 1) * Time.delta * efficiency();
			} else completeUpgrade();
		}
		
		@Override
		public void completeUpgrade() {
			upgradeSound.update(x, y, false);
			upgradeSound.stop();
			Sounds.unlock.at(this);
			Fx.healBlockFull.at(x, y, block.size, baseColor);
			
			if (upgradingID == -1) {
				baseData.plusLevel();
			} else if (!ammoDatas.isEmpty()) {
				lastestSelectID = upgradingID;
				ammoDatas.get(upgradingID).isUnlocked = true;
			}
			
			updateTarget();
			upgradingID = DFTID;
		}
		
		//UI
		protected void buildTable(Table t) {
			t.table(Tex.button, table -> {
				table.button(Icon.infoCircle, Styles.clearPartiali, () -> {
					ammoDatas.get(lastestSelectID).showInfo(ammoDatas.get(lastestSelectID), false);
				}).size(LEN).disabled(b -> lastestSelectID < 0 || ammoDatas.isEmpty());
				
				table.button(Icon.hostSmall, Styles.clearTransi, () -> {
					new Dialog("All Info") {{
						keyDown(KeyCode.escape, this::hide);
						keyDown(KeyCode.back, this::hide);
						setFillParent(true);
						cont.pane(infos -> {
							baseData.buildUpgradeInfoAll(infos);
							for (UpgradeAmmoData ammoData : ammoDatas)ammoData.buildUpgradeInfoAll(infos);
						}).fillX().height(LEN * 5).row();
						cont.button("Back", Icon.exit, this::hide).left().fillX().height(LEN).pad(4);
					}}.show();
				}).size(LEN).left();
				table.button("Back", Icon.exit, Styles.cleart, dialog::hide).size(160f, LEN).left();
			}).left().pad(OFFSET);
			t.row();
			buildSwitchAmmoTable(t, false);
			t.row();
		}
		
		
		//UI
		protected void buildUpgradeDataTable(Table t) {
			t.pane(table -> {
				if(baseData.level < maxLevel)baseData.buildTable(table);
				else baseData.buildTableComplete(table);
				for (UpgradeAmmoData ammoData : ammoDatas) if (ammoData != null && !ammoData.isUnlocked)ammoData.buildTable(table);
			}).fillX().height(LEN * 3.4f);
		}

		public void switchAmmo(UpgradeAmmoData data){
			Sounds.click.at(this);
			ammoDatas.each(ammo -> {ammo.selected = false;});
			data.selected = true;
			lastestSelectID = data.id;
			updateTarget();
		}
		
		protected void buildSwitchAmmoTable(Table t, boolean setting) {
			t.table(Tex.button, table -> {
				if(setting){
					table.pane(cont -> 
						{cont.button("Upgrade", Icon.settings, Styles.cleart, () -> {upgraderTableBuild();}).size(60f * buttonPerLine, 60f);}
					).size(buttonSize * buttonPerLine, buttonSize).pad(OFFSET / 3f).row();
				}
				
				table.pane(cont -> {
					int index = 0;
					for (UpgradeAmmoData ammoData : ammoDatas) {
						if(index % buttonPerLine == 0)cont.row().left();
						cont.button(new TextureRegionDrawable(ammoData.icon), Styles.clearPartiali, buttonSize, () -> {
							switchAmmo(ammoData);
						}).size(buttonSize).disabled( b ->
							!ammoData.isUnlocked || ammoData.selected
						).left();
						index ++;
					}
				}).width(buttonSize * buttonPerLine);
				if(!setting)table.left();
			}).width(buttonSize * buttonPerLine + 2 * OFFSET).pad(OFFSET).left();
		}

		protected void setLink(int value) {
			if (value == -1) {
				if (linkValid())target().resetUpgrade();
			}
			this.link = value;
			updateTarget();
		}

		//Overrides

		@Override
		public boolean onConfigureTileTapped(Building other) {
			if (this == other) {
				setLink(-1);
				return false;
			}
			if (other.block.name != linkTarget.name)return false;
			if (link == other.pos()) {
				setLink(-1);
				return false;
			} else if (!(other instanceof Scalablec)) {
				ui.showErrorMessage("Failed to connect, target '" + other.toString() + "' doesn't implement @Scalablec");
				return true;
			} else { 
				Scalablec target = (Scalablec)other;
				if (!target.isConnected() && target.team() == team && target.within(this, range())) {
					setLink(target.pos());
					return false;
				}
			}
			return true;
		}

		
		
		@Override
		public void upgraderTableBuild(){
			dialog.cont.clear();
			dialog.cont.pane(t -> {
				buildTable(t);
				t.row();
				t.image().pad(OFFSET).fillX().height(4f).color(Pal.accent).row();
				buildUpgradeDataTable(t);
				t.row();
				t.image().pad(OFFSET).fillX().height(4f).color(Pal.accent).row();
			});
			dialog.show();
			dialog.keyDown(KeyCode.escape, dialog::hide);
			dialog.keyDown(KeyCode.back, dialog::hide);
		}
		
		@Override
		public void updateTile() {
			if (upgradingID != DFTID){
				updateUpgrading();
				if(Mathf.chanceDelta(upgradeEffectChance))for(int i : Mathf.signs)upgradeEffect.at(x + i * Mathf.random(block.size / 2 * tilesize), y - Mathf.random(block.size / 2 * tilesize), block.size / 2, baseColor);
			}
			
			Events.on(EventType.WorldLoadEvent.class, e -> {
				setData(initUpgradeAmmoDatas);
				updateTarget();
				upgradecGroup.add(this);
			});
		}

		@Override
		public void onDestroyed() {
			super.onDestroyed();
			if(linkValid())target().resetUpgrade();
			upgradecGroup.remove(this);
		}

		@Override
		public void placed() {
			super.placed();
			upgradecGroup.add(this);
			setData(initUpgradeAmmoDatas);
		}
		
		@Override
		public void drawConfigure() {
			Drawf.dashCircle(x, y, range(), baseColor);
			Draw.color(baseColor);
			Lines.square(x, y, block.size * tilesize / 2);
			if (linkValid()) {
				Scalablec target = target();
				Lines.square(target.getX(), target.getY(), target.block().size * tilesize / 2);
				float
				sin = Mathf.absin(Time.time, 6f, 1f),
				r1 = (block.size / 2 + 1) * tilesize + sin,
				r2 = (target.block().size / 3 + 2) * tilesize + sin;

				Tmp.v1.trns(angleTo(target), r1);
				Tmp.v2.trns(target.angleTo(this), r2);
				int sigs = (int)(dst(target) / tilesize);

				Lines.stroke(4, Pal.gray);
				Lines.dashLine(x + Tmp.v1.x, y + Tmp.v1.y, target.getX() + Tmp.v2.x, target.getY() + Tmp.v2.y, sigs);
				Lines.stroke(2, baseColor);
				Lines.dashLine(x + Tmp.v1.x, y + Tmp.v1.y, target.getX() + Tmp.v2.x, target.getY() + Tmp.v2.y, sigs);
				Drawf.circles(x, y, r1, baseColor);
				Drawf.arrow(x, y, target.getX(), target.getY(), 2 * tilesize + sin, 4 + sin, baseColor);

				Drawf.circles(target.getX(), target.getY(), r2, baseColor);
				Draw.reset();
			}
			Draw.reset();
		}

		@Override
		public void write(Writes write) {
			write.f(this.remainTime);
			write.i(this.link);
			write.i(this.lastestSelectID);
			write.i(this.upgradingID);

			baseData.write(write);
			for (UpgradeAmmoData ammoData : ammoDatas)ammoData.write(write);
		}

		@Override
		public void read(Reads read, byte revision) {
			setData(initUpgradeAmmoDatas);
			
			this.remainTime = read.f();
			this.link = read.i();
			this.upgradingID = read.i();
			this.lastestSelectID = read.i();

			baseData.read(read, revision);
			if(!ammoDatas.isEmpty())for(UpgradeAmmoData ammoData : ammoDatas)ammoData.read(read, revision);
		}

		protected void setData(Seq<UpgradeAmmoData> datas){
			baseData.from = this;
			for (UpgradeAmmoData data : datas){
				ammoDatas.add( (UpgradeAmmoData)(data.clone()) );
			}
			for (UpgradeAmmoData data : ammoDatas)data.from = this;
		}

		@Override
		public void updateTarget() {
			if (linkValid()){
				target().setBaseData(baseData);
				if(lastestSelectID >= 0 && ammoDatas.get(lastestSelectID).isUnlocked)target().setAmmoData(ammoDatas.get(lastestSelectID));
			}
		}
		
		@Override//Target confirm
		public boolean linkValid() {
			if (link == -1) return false;
			Building target = world.build(link);
			return target instanceof Scalablec && linkTarget.name == target.block.name && target.team == team && within(target, range());
		}
		
		public CoreBlock.CoreBuild core(){return this.team.core();}

		@Override public boolean isUpgrading(){return remainTime > 0;}
		@Override public float range() { return range; }
		@Override public void buildConfiguration(Table table) {buildSwitchAmmoTable(table, true);}
		@Override public void draw() {Draw.rect(region, x, y);}
		@Override public void onRemoved() {
			upgradecGroup.remove(this);
			if(linkValid())target().resetUpgrade();
		}
		@Override public Scalablec target() {return linkValid() ? (Scalablec)world.build(link) : null;}
	}
}









