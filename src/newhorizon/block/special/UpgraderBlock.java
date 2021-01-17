package newhorizon.block.special;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.audio.SoundLoop;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Stat;
import newhorizon.content.NHColor;
import newhorizon.data.*;
import newhorizon.content.NHFx;
import newhorizon.func.TableFuncs;
import newhorizon.interfaces.Scalablec;
import newhorizon.interfaces.Upgraderc;
import newhorizon.func.TextureFilterValue;

import static mindustry.Vars.*;

public class UpgraderBlock extends Block {
	public static final int DFTID = -2; 
	public static final Seq<UpgraderBlockBuild> upgradecGroup = new Seq<>(UpgraderBlockBuild.class);
	public static final int buttonPerLine = 8;
	
	protected SoundLoop upgradeSound = new SoundLoop(Sounds.build, 1.1f);
	public int   maxLevel = 9;
	public float upgradeEffectChance = 0.04f;

	public Block linkTarget;
	public Color baseColor = Pal.accent;
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
	public void setStats(){
		super.setStats();
		stats.add(Stat.output, new TextureFilterValue(linkTarget.icon(Cicon.medium), "Link Target: [accent]" + linkTarget.localizedName + "[]."));
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
	}
	
	public class UpgraderBlockBuild extends Building implements Ranged, Upgraderc{
		public UpgradeBaseData baseData = (UpgradeBaseData)initUpgradeBaseData.clone();
		public Seq<UpgradeAmmoData> ammoDatas = new Seq<>();

		public int link = -1;
		public int upgradingID = DFTID;
		public int lastestSelectID = -1;
		public float remainTime;

		protected BaseDialog dialog = new BaseDialog("Upgrade", Styles.fullDialog);
		
		protected boolean coreValid(CoreBlock.CoreBuild core) {
			return core != null && core.items != null && !core.items.empty();
		}
		
		protected void consumeItems(UpgradeData data){
			if(state.rules.infiniteResources)return;
			CoreBlock.CoreBuild core = core();
			if(coreValid(core))core.items.remove(data.requirements());
		}
		
		@Override
		public boolean canUpgrade(UpgradeData data) {
			if(data instanceof UpgradeLevelData){
				UpgradeLevelData upgradeData = (UpgradeLevelData)data;
				if(upgradeData.level == maxLevel)return false;
			}
			
			if(data instanceof UpgradeMultData){
				UpgradeMultData upgradeData = (UpgradeMultData)data;
				if(upgradeData.isUnlocked)return false;
			}
			
			if(state.rules.infiniteResources)return true;
			
			CoreBlock.CoreBuild core = core();
			return 
				coreValid(core) && (
					baseData.level >= data.unlockLevel && !isUpgrading() && core.items.has(data.requirements())
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
				remainTime -= (state.rules.infiniteResources ? Float.MAX_VALUE : 1) * Time.delta * efficiency();
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
				switchAmmo(ammoDatas.get(upgradingID));
			}
			
			updateTarget();
			upgradingID = DFTID;
		}
		
		//UI
		protected void buildUpgradeDataTable(Table t) {
			t.pane(table -> {
				if(baseData.level < maxLevel)baseData.buildTable(table);
				else baseData.buildTableComplete(table);

				ammoDatas.each(ammo -> !ammo.isUnlocked, ammo -> ammo.buildTable(table));
			}).fillX().growY().row();
		}

		public void switchAmmo(UpgradeAmmoData data){
			Sounds.click.at(this);
			ammoDatas.each(ammo -> ammo.selected = false);
			data.selected = true;
			lastestSelectID = data.id;
			updateTarget();
		}
		
		public void buildSwitchAmmoTable(Table t, boolean setting) {
			t.table(Tex.button, table -> {
				if(setting){
					table.pane(cont -> 
						cont.button("Upgrade", Icon.settings, Styles.cleart, this::upgraderTableBuild).size(TableFuncs.LEN * buttonPerLine, TableFuncs.LEN)
					).fillX().height(TableFuncs.LEN).pad(TableFuncs.OFFSET / 3f).row();
				}
				
				table.pane(cont -> {
					int index = 0;
					for (UpgradeAmmoData ammoData : ammoDatas) {
						if(index % buttonPerLine == 0)cont.row().left();
						cont.button(new TextureRegionDrawable(ammoData.icon), Styles.clearPartiali, TableFuncs.LEN, () ->
							switchAmmo(ammoData)
						).size(TableFuncs.LEN).disabled(b ->
							!ammoData.isUnlocked || ammoData.selected
						).left();
						index ++;
					}
				}).fillX().height(TableFuncs.LEN).pad(TableFuncs.OFFSET / 3f);
				if(!setting)table.left();
			}).grow().pad(TableFuncs.OFFSET).row();
		}

		protected void setLink(int value) {
			if (linkValid())target().resetUpgrade();
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
			if (!other.block.name.equals(linkTarget.name))return false;
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
			dialog.addCloseListener();
			dialog.cont.pane(t -> {
				//
				t.table(Tex.button, table -> {
					table.row().left();
					table.button(
							Icon.infoCircle, Styles.clearPartiali, () -> ammoDatas.get(lastestSelectID).showInfo(false)
					).size(TableFuncs.LEN).disabled(b -> lastestSelectID < 0 || ammoDatas.isEmpty()).left();

					table.button(Icon.hostSmall, Styles.clearTransi, () ->
							new BaseDialog("All Info") {{
								this.addCloseListener();
								setFillParent(true);
								cont.pane(infos -> {
									baseData.buildUpgradeInfoAll(infos);
									for (UpgradeAmmoData ammoData : ammoDatas)ammoData.buildUpgradeInfoAll(infos);
								}).fillX().height(TableFuncs.LEN * 5).row();
								cont.button("@back", Icon.left, this::hide).fillX().height(TableFuncs.LEN).pad(TableFuncs.OFFSET / 3);
							}}.show()
					).size(TableFuncs.LEN).left();
					table.button("@back", Icon.left, Styles.cleart, dialog::hide).size(TableFuncs.LEN * 3.5f, TableFuncs.LEN).left().pad(TableFuncs.OFFSET / 3);
				}).left().pad(TableFuncs.OFFSET).row();

				buildSwitchAmmoTable(t, false);

				t.image().pad(TableFuncs.OFFSET).fillX().height(4f).color(Pal.accent).row();
				buildUpgradeDataTable(t);
				t.image().pad(TableFuncs.OFFSET).fillX().height(4f).color(Pal.accent).row();

				t.fill();
			});
			dialog.show();
		}
		
		@Override
		public void updateTile() {
			if (upgradingID != DFTID){
				updateUpgrading();
				if(Mathf.chanceDelta(upgradeEffectChance))for(int i : Mathf.signs)upgradeEffect.at(x + i * Mathf.random(block.size / 2f * tilesize), y - Mathf.random(block.size / 2f * tilesize), block.size / 2f, baseColor);
			}
			
			Events.on(EventType.WorldLoadEvent.class, e -> {
				setData();
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
			setData();
		}
		
		@Override
		public void drawConfigure() {
			Drawf.dashCircle(x, y, range(), baseColor);

			Draw.color(getColor());
			Lines.square(x, y, block().size * tilesize / 2f + 1.0f);

			drawLink();
			if (linkValid()){
				target().drawConnected();
				target().drawMode();
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
			//for (UpgradeAmmoData ammoData : ammoDatas)ammoData.write(write);
			ammoDatas.each(ammo -> ammo.write(write));
		}

		@Override
		public void read(Reads read, byte revision) {
			setData();
			
			this.remainTime = read.f();
			this.link = read.i();
			this.upgradingID = read.i();
			this.lastestSelectID = read.i();

			baseData.read(read, revision);

			ammoDatas.each(ammo -> ammo.read(read, revision));
			//if(!ammoDatas.isEmpty())for(UpgradeAmmoData ammoData : ammoDatas)ammoData.read(read, revision);
		}

		protected void setData(){
			baseData.from = this;
			for (UpgradeAmmoData data : initUpgradeAmmoDatas){
				data.from = this;
				ammoDatas.add( (UpgradeAmmoData)(data.clone()) );
			}
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
			return target instanceof Scalablec && linkTarget.name.equals(target.block.name) && target.team == team && within(target, range());
		}
		
		public CoreBlock.CoreBuild core(){return this.team.core();}

		@Override public Color getColor(){return baseColor;}
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









