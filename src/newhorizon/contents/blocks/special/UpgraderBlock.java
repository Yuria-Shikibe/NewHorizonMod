package newhorizon.contents.blocks.special;

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
import newhorizon.contents.interfaces.Scalablec;
import newhorizon.contents.items.*;
import newhorizon.contents.effects.NHFx;
import newhorizon.contents.colors.*;
import newhorizon.contents.bullets.special.NHLightningBolt;
import newhorizon.contents.data.*;
import static newhorizon.contents.data.UpgradeData.*;

import static mindustry.Vars.*;

public class UpgraderBlock extends Block {
	private static final int DFTID = -2; 
	//Level from 1 - maxLevel
	public int   maxLevel = 9;

	//public TextureRegion[] levelRegions;

	public Color baseColor = Pal.accent;
	public Block toUpgradeClass;

	public float range = 320f;

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
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
	}

	@Override
	public void setBars() {
		super.setBars();
		bars.add("level",
			(UpgraderBlockBuild entity) -> new Bar(
				() -> "Level",
				() -> NHColor.lightSky,
				() -> entity.baseData.level / maxLevel
			)
		);

		bars.add("upgradeProgress",
			(UpgraderBlockBuild entity) -> new Bar(
				() -> "RestTime",
				() -> Color.valueOf("#FF732A"),
				() -> entity.remainTime / entity.needsTime()
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

	public class UpgraderBlockBuild extends Building implements Ranged, Cloneable {
		
		public UpgradeBaseData baseData = (UpgradeBaseData)initUpgradeBaseData.clone().init();
		
		public Seq<UpgradeAmmoData> ammoDatas;
		
		protected void setAmmoDatas(Seq<UpgradeAmmoData> datas){
			for (UpgradeAmmoData : data){
				ammoDatas.add( (UpgradeAmmoData)data.clone().init() );
			}
		}

		public int link = -1;

		public int upgradingID = DFTID;
		public int lastestSelectID = 0;
		public float remainTime;

		protected BaseDialog dialog = new BaseDialog("Upgrade");
		
		protected void consumeItems(UpgradeData data){
			
		}

		public CoreBlock.CoreBuild core() {return this.team.core();}

		protected boolean isUpgrading() {return remainTime > 0;}
		
		public boolean canUpgrade(UpgradeData data) {
			return !isUpgrading()/*&& Needs */;
		}

		protected float needsTime() {
			return 
				upgradingID == DFTID ? 0 : 
				upgradingID == -1	? baseData.costTime * (1 + baseData.timeCostcoefficien * baseData.level) :
				upgradingID >= 0 	? ammoDatas.get(upgradingID).costTime :
				0;
		}
		
		//Data Upgrade
		public void upgradeData(UpgradeData data){
			if(!canUpgrade(data))return;
			consumeItems(data);
			if(data instanceof UpgradeBaseData){
				UpgradeBaseData baseDataOther = (UpgradeBaseData)data;
				if(baseDataOther.level == maxLevel)return;
				upgradingID = -1;
			}else if(data instanceof UpgradeAmmoData){
				UpgradeAmmoData ammoDataOther = (UpgradeAmmoData)data;
				upgradingID = ammoDataOther.id;
			}
			remainTime = needsTime();
		}
		
		//Updates
		protected void updateUpgrading() {
			if (isUpgrading()) {
				remainTime -= Time.delta * efficiency();
			} else {
				completeUpgrade();
			}
		}

		protected void completeUpgrade() {
			Fx.healBlockFull.at(x, y, block.size, baseColor);
			
			if (upgradingID == -1) {
				baseData.plusLevel();
				updateTarget();
			} else if (ammoDatas.isEmpty()) {
				
			} else {
				ammoDatas.get(upgradingID).isUnlocked = true;
				baseData.selectAmmo = ammoDatas.get(upgradingID).selectAmmo;
				lastestSelectID = upgradingID;
				updateTarget();
			}
			
			upgradingID = DFTID;
		}

		//UI
		protected void buildUpgradeBaseDataTable(Table t) {
			t.pane(table -> {
				table.add("UpgradeTargetInfo->>").row();

				table.add(baseData.toString()).row();
				
				baseData.buildTable(table);
			}).size(LEN * 10 + OFFSET * 3, LEN * 2.6f + OFFSET);
		}

		//UI
		protected void buildUpgradeAmmoDataTable(Table t) {
			t.pane(table -> {
				for (UpgradeAmmoData ammoData : ammoDatas)if (ammoData != null && !ammoData.isUnlocked)ammoData.buildTable(table);
			}).size(LEN * 10 + OFFSET * 3, LEN * 4f + OFFSET);
		}

		protected void buildSwitchAmmoTable(Table t) {
			t.pane(table -> {
				int index = 0;
				
				for (UpgradeAmmoData ammoData : ammoDatas) {
					if (ammoDatas.get(lastestSelectID).selected || ammoDatas.get(lastestSelectID).isUnlocked)continue;
					if ((index % 5) == 0)table.row();
					table.button(new TextureRegionDrawable(ammoData.icon), () -> {
						ammoDatas.get(lastestSelectID).selected = false;
						baseData.selectAmmo = ammoData.selectAmmo;
						ammoData.selected = true;
						lastestSelectID = ammoData.id;
						updateTarget();
					}).size(60).disabled(b -> scalaTarget() == null);
					index++;
				}
				table.button(Icon.infoCircle, () -> {
					new Dialog("") {{
						setFillParent(true);
						cont.margin(15f);
						cont.image().width(LEN * 1.5f).height(4f).color(Color.lightGray).row();
						cont.image(ammoDatas.get(lastestSelectID).icon).size(LEN * 1.5f).row();
						cont.image().width(LEN * 1.5f).height(4f).color(Color.lightGray).row();
						cont.add("<< Ammo >>").color(Pal.accent).row();
						cont.add("Description: ").color(Pal.accent).left().row();
						cont.add(offsetSpace + Core.bundle.get(ammoDatas.get(lastestSelectID).description)).color(Color.lightGray).left().row();
						cont.image().width(300f).pad(2).height(4f).color(Pal.accent);
						cont.row();
						cont.button("Leave", this::hide).size(120, 50).pad(4);
					}}.show();
				}).size(60).disabled(b -> baseData.selectAmmo == none);
			}).size(60 * 8f, 70);
		}

		//Target confirm
		protected boolean linkValid() {
			if (link == -1) return false;
			Building linkTarget = world.build(link);
			return linkTarget instanceof Scalablec/* && linkTarget.block == toUpgradeClass*/ && linkTarget.team == team && within(linkTarget, range());
		}

		//Targeter
		protected Building target() {
			return linkValid() ? world.build(link) : null;
		}

		protected Scalablec scalaTarget() {
			return linkValid() ? (Scalablec)world.build(link) : null;
		}

		protected void setLink(int value) {
			if (value == -1) {
				if (linkValid())scalaTarget().resetUpgrade();
			} else updateTarget();
			this.link = value;
		}

		//Overrides

		@Override
		public boolean onConfigureTileTapped(Building other) {
			if (this == other) {
				setLink(-1);
				return false;
			}

			if (link == other.pos()) {
				setLink(-1);
				return false;
			} else if (!(other instanceof Scalablec)) {
				ui.showErrorMessage("Failed to connect, target doesn't implement @Interface Scalablec");
				return true;
			} else { 
				Scalablec target = (Scalablec)other;
			
				if (!target.isConnected() && other.team == team && other.within(tile, range())) {
					setLink(other.pos());
					return false;
				}
			}
			
			return true;
		}

		@Override
		public float range() {
			return range;
		}

		@Override
		public void buildConfiguration(Table table) {
			table.button(Icon.add, () -> {
				dialog.cont.clear();
				dialog.cont.pane(t -> {
					t.add("UpgradingID>> " + upgradingID).row();
					t.add("SelectedID>> " + upgradingID).row();
					
					buildUpgradeBaseDataTable(t);
					t.row();
					buildUpgradeAmmoDataTable(t);
					t.row();
					buildSwitchAmmoTable(t);
					t.row();
					
				}).size(550f);
				dialog.cont.row();
				dialog.cont.button("Back", dialog::hide).size(120f, 50f);
				dialog.show();
				
			}).size(60f);
		}

		@Override
		public void updateTile() {
			if (upgradingID != DFTID)updateUpgrading();

			Events.on(EventType.WorldLoadEvent.class, e -> {
				setFrom();
				setAmmoData();
				updateTarget();
				setAmmoDatas(initUpgradeAmmoDatas);
			});
		}

		@Override
		public void onDestroyed() {
			super.onDestroyed();
			if (linkValid())scalaTarget().resetUpgrade();
		}

		@Override
		public void placed() {
			super.placed();
			setFrom();
			setAmmoDatas(initUpgradeAmmoDatas);
		}
		//Draw Methods

		@Override
		public void draw() {
			Draw.rect(region, x, y);
		}

		@Override
		public void drawConfigure() {
			Draw.color(baseColor);
			Lines.square(x, y, block.size * tilesize / 2);
			if (linkValid()) {
				Building target = target();
				Lines.square(target.x, target.y, target.block.size * tilesize / 2);
				float
				sin = Mathf.absin(Time.time(), 6f, 1f),
				r1 = (block.size / 2 + 1) * tilesize + sin,
				r2 = (target.block.size / 3 + 2) * tilesize + sin;

				Tmp.v1.trns(angleTo(target), r1);
				Tmp.v2.trns(target.angleTo(this), r2);
				int sigs = (int)dst(target) / tilesize;

				Lines.stroke(4, Pal.gray);
				Lines.dashLine(x + Tmp.v1.x, y + Tmp.v1.y, target.x + Tmp.v2.x, target.y + Tmp.v2.y, sigs);

				Lines.stroke(2, baseColor);
				Lines.dashLine(x + Tmp.v1.x, y + Tmp.v1.y, target.x + Tmp.v2.x, target.y + Tmp.v2.y, sigs);
				Drawf.circles(x, y, r1, baseColor);
				Drawf.arrow(x, y, target.x, target.y, 2 * tilesize + sin, 4 + sin, baseColor);

				Drawf.circles(target.x, target.y, r2, baseColor);
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
			this.remainTime = read.f();
			this.link = read.i();
			this.upgradingID = read.i();
			this.lastestSelectID = read.i();

			baseData.read(read, revision);
			for (UpgradeAmmoData ammoData : ammoDatas)ammoData.read(read, revision);
		}

		protected void setFrom() {
			baseData.from = this;
			for (UpgradeAmmoData ammoData : ammoDatas)ammoData.from = this;
		}

		protected void setAmmoData() {
			
			if(lastestSelectID > 0 && ammoDatas.get(lastestSelectID).isUnlocked){
				baseData.selectAmmo = ammoDatas.get(lastestSelectID).selectAmmo;
			}else baseData.selectAmmo = UpgradeData.none;
			
		}
		
		protected int drawLevel() {
			return baseData.level += 1;
		}

		protected void updateTarget() {
			if (linkValid())scalaTarget().updateUpgradeBase(baseData);
		}

		
	}
}









