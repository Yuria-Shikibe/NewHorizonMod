package newhorizon.contents.blocks.special;

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
	public float upgradeEffectChance = 0.04f;
	//public TextureRegion[] levelRegions;

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
		//for (UpgradeAmmoData data : initUpgradeAmmoDatas)data.load();
		//initUpgradeBaseData.load();
		/*for(int i = 1; i < maxLevel; i ++){
			levelRegions[i] = Core.atlas.find(name + "-" + i);
		}*/
	}

	public class UpgraderBlockBuild extends Building implements Ranged {
		
		public UpgradeBaseData baseData = (UpgradeBaseData)initUpgradeBaseData.clone();
		public Seq<UpgradeAmmoData> ammoDatas = new Seq<>();

		public int link = -1;
		public int upgradingID = DFTID;
		public int lastestSelectID = 0;
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

		public CoreBlock.CoreBuild core() {return this.team.core();}

		protected boolean isUpgrading() {return remainTime > 0;}
		
		public boolean canUpgrade(UpgradeData data) {
			CoreBlock.CoreBuild core = core();
			return 
				coreValid(core) && 
				baseData.level >= data.unlockLevel &&
					(
					state.rules.infiniteResources || (
						!isUpgrading() && core.items.has(data.requirements()) 
					)
				);
		}

		protected float costTime() {
			return 
				upgradingID == DFTID ? 0 : 
				upgradingID == -1	? baseData.costTime() :
				upgradingID >= 0 	? ammoDatas.get(upgradingID).costTime() :
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
			remainTime = costTime();
		}
		
		//Updates
		protected void updateUpgrading() {
			if (isUpgrading()) {
				remainTime -= (state.rules.infiniteResources ? 100000 : 1) * Time.delta * efficiency();
			} else completeUpgrade();
		}

		protected void completeUpgrade() {
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
			t.pane(table -> {
				table.button(Icon.infoCircle, () -> {
					ammoDatas.get(lastestSelectID).showInfo(ammoDatas.get(lastestSelectID), false);
				}).size(60).disabled(lastestSelectID < 0 || ammoDatas.isEmpty());
				
				table.button(Icon.hostSmall, () -> {
					new Dialog("All Info") {{
						setFillParent(true);
						cont.pane(infos -> {
							baseData.buildUpgradeInfoAll(infos);
							for (UpgradeAmmoData ammoData : ammoDatas)ammoData.buildUpgradeInfoAll(infos);
						}).size(LEN * 12 + OFFSET * 3, LEN * 5f + OFFSET);
						cont.button("Leave", this::hide).left().size(120, 50).pad(4);
					}}.show();
				}).size(60f).left();
				
				table.button("Back", dialog::hide).size(120f, 60f).left();
			}).left().size(240f, 60f).pad(OFFSET);
			t.row();
			buildSwitchAmmoTable(t);
			t.row();
		}
		
		
		//UI
		protected void buildUpgradeDataTable(Table t) {
			t.pane(table -> {
				if(baseData.level < maxLevel)baseData.buildTable(table);
				else baseData.buildTableComplete(table);
				for (UpgradeAmmoData ammoData : ammoDatas) if (ammoData != null && !ammoData.isUnlocked)ammoData.buildTable(table);
			}).size(LEN * 12 + OFFSET * 3, LEN * 4f + OFFSET);
		}

		public void switchAmmo(UpgradeAmmoData data){
			for(UpgradeAmmoData ammo : ammoDatas)ammo.selected = false;
			data.selected = true;
			lastestSelectID = data.id;
			updateTarget();
		}
		
		protected void buildSwitchAmmoTable(Table t) {
			final int buttonPerLine = 8;
			t.pane(table -> {
				int index = 0;
				for (UpgradeAmmoData ammoData : ammoDatas) {
					if(index % buttonPerLine == 0)table.row().left();
					table.button(new TextureRegionDrawable(ammoData.icon), () -> {
						switchAmmo(ammoData);
					}).size(60).disabled( 
						target() == null || !ammoData.isUnlocked || ammoData.selected
					).left();
					index++;
				}
			}).size(60 * buttonPerLine, 60).pad(OFFSET).left();
		}

		//Target confirm
		protected boolean linkValid() {
			if (link == -1) return false;
			Building linkTarget = world.build(link);
			return linkTarget instanceof Scalablec/* && linkTarget.block == toUpgradeClass*/ && linkTarget.team == team && within(linkTarget, range());
		}

		//Targeter
		protected Scalablec target() {
			return linkValid() ? (Scalablec)world.build(link) : null;
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

			if (link == other.pos()) {
				setLink(-1);
				return false;
			} else if (!(other instanceof Scalablec)) {
				ui.showErrorMessage("Failed to connect, target doesn't implement @Interface Scalablec");
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
		public float range() {
			return range;
		}

		@Override
		public void buildConfiguration(Table table) {
			table.button(Icon.logic, () -> {
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
			}).size(60f).left();
		}

		@Override
		public void updateTile() {
			if (upgradingID != DFTID){
				updateUpgrading();
				if(Mathf.chanceDelta(upgradeEffectChance)){
					for(int i : Mathf.signs)upgradeEffect.at(x + i * Mathf.random(block.size / 2 * tilesize), y - Mathf.random(block.size / 2 * tilesize), block.size / 2, baseColor);
				}
			}
			
			Events.on(EventType.WorldLoadEvent.class, e -> {
				setData(initUpgradeAmmoDatas);
				updateTarget();
			});
		}

		@Override
		public void onDestroyed() {
			super.onDestroyed();
			if(linkValid())target().resetUpgrade();
		}

		@Override
		public void placed() {
			super.placed();
			setData(initUpgradeAmmoDatas);
		}
		
		@Override
		public void onRemoved() {
			if(linkValid())target().resetUpgrade();
		}

		//Draw Methods

		@Override
		public void draw() {
			Draw.rect(region, x, y);
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
				sin = Mathf.absin(Time.time(), 6f, 1f),
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

		protected void updateTarget() {
			if (linkValid()){
				target().setBaseData(baseData);
				if(lastestSelectID >= 0 && ammoDatas.get(lastestSelectID).isUnlocked)target().setAmmoData(ammoDatas.get(lastestSelectID));
			}
		}
		
		
	}
}









