package newhorizon.block.special;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
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
import newhorizon.content.NHFx;
import newhorizon.feature.UpgradeData;
import newhorizon.feature.UpgradeData.DataEntity;
import newhorizon.func.TableFuncs;
import newhorizon.func.TextureFilterValue;
import newhorizon.interfaces.Scalablec;
import newhorizon.interfaces.Upgraderc;

import static mindustry.Vars.*;

public class UpgradeBlock extends Block {
	public static final int defaultID = -1;
	public static final Seq<UpgraderBlockBuild> upgradecGroup = new Seq<>(UpgraderBlockBuild.class);
	public static final int buttonPerLine = 8;
	
	protected SoundLoop upgradeSound = new SoundLoop(Sounds.build, 1.1f);
	public int   maxLevel = 9;
	public float upgradeEffectChance = 0.04f;

	public Block linkTarget;
	public Color baseColor = Pal.accent;
	public Effect upgradeEffect = NHFx.upgrading;
	public float range = 400f;
	
	public final Seq<UpgradeData> upgradeDatas = new Seq<>();
	protected void addUpgrades(UpgradeData... inputs) {
		//upgradeDatas.addAll(inputs);
		for(UpgradeData data : inputs){
			upgradeDatas.add(data);
			Log.info(data.toString());
		}
		Log.info("All added");
	}

	public UpgradeBlock(String name) {
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
		if(linkTarget == null)throw new IllegalArgumentException("null @linkTarget :[red]'" + name + "'[]");
		if(upgradeDatas.isEmpty())throw new IllegalArgumentException("");
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
	}
	
	public class UpgraderBlockBuild extends Building implements Ranged, Upgraderc{
		public Seq<DataEntity> datas = new Seq<>();

		public int link = -1;
		public int upgradingID = defaultID;
		public int lastestSelectID = -1;
		public float remainTime;

		protected BaseDialog dialog = new BaseDialog("Upgrade", Styles.fullDialog);
		
		protected boolean coreValid(CoreBlock.CoreBuild core) {
			return core != null && core.items != null && !core.items.empty();
		}
		
		@Override
		public void consumeItems(DataEntity data){
			if(state.rules.infiniteResources)return;
			CoreBlock.CoreBuild core = core();
			if(coreValid(core))core.items.remove(data.requirements());
		}
		
		@Override
		public boolean canUpgrade(DataEntity data) {
			if(data.level == maxLevel || (data.available() && !data.type().isLeveled))return false;
			
			if(state.rules.infiniteResources)return true;
			
			CoreBlock.CoreBuild core = core();
			return 
				coreValid(core) && (
					data.isMaxLevel() && !isUpgrading() && core.items.has(data.requirements())
				) && data.type().unlockLevel <= datas.first().level;
		}

		public float costTime(){
			return (upgradingID >= datas.size || upgradingID < 0) ? 0 : datas.get(upgradingID).costTime();
		}
		
		@Override//Data Upgrade
		public void upgradeData(DataEntity data){
			if(!canUpgrade(data))return;
			consumeItems(data);
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
			if(upgradingID < 0 || upgradingID >= datas.size)return;
			upgradeSound.update(x, y, false);
			upgradeSound.stop();
			Sounds.unlock.at(this);
			Fx.healBlockFull.at(x, y, block.size, baseColor);
			
			datas.get(upgradingID).upgrade();
			switchAmmo(datas.get(upgradingID));
			
			updateTarget();
			upgradingID = defaultID;
			dialog.cont.update(() -> {});
		}
		
		//UI
		protected void buildUpgradeDataTable(Table t) {
			t.pane(table -> datas.each(data -> !data.isUnlocked || (data.type().isLeveled && !data.isMaxLevel()), data -> data.buildTable(table, this))).fillX().growY().row();
		}

		public void switchAmmo(DataEntity data){
			Sounds.click.at(this);
			datas.each(ammo -> ammo.selected = false);
			data.selected = true;
			lastestSelectID = datas.indexOf(data);
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
					for (DataEntity data : datas) {
						if(index % buttonPerLine == 0)cont.row().left();
						cont.button(new TextureRegionDrawable(data.type().icon), Styles.clearPartiali, TableFuncs.LEN, () ->
							switchAmmo(data)
						).size(TableFuncs.LEN).disabled(b ->
							!data.isUnlocked || data.selected
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
							Icon.infoCircle, Styles.clearPartiali, () -> datas.get(lastestSelectID).showInfo(false, this)
					).size(TableFuncs.LEN).disabled(b -> lastestSelectID < 0 || datas.isEmpty()).left();

					table.button(Icon.hostSmall, Styles.clearTransi, () ->
							new BaseDialog("All Info") {{
								this.addCloseListener();
								setFillParent(true);
								cont.pane(infos -> datas.each(data -> data.buildTable(infos, UpgraderBlockBuild.this))).fillX().height(TableFuncs.LEN * 5).row();
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
			if (upgradingID != defaultID){
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
			
			//for (UpgradeAmmoData ammoData : ammoDatas)ammoData.write(write);
			datas.each(data -> data.write(write));
		}

		@Override
		public void read(Reads read, byte revision) {
			setData();
			
			this.remainTime = read.f();
			this.link = read.i();
			this.upgradingID = read.i();
			this.lastestSelectID = read.i();

			datas.each(data -> data.read(read, revision));
			//if(!ammoDatas.isEmpty())for(UpgradeAmmoData ammoData : ammoDatas)ammoData.read(read, revision);
		}

		@Override
		public void updateTarget() {
			if (linkValid()){
				if(lastestSelectID >= 0 && datas.get(lastestSelectID).isUnlocked)target().setData(datas.get(lastestSelectID));
				target().setLinkPos(pos());
			}
		}
		
		public void setData(){
			for(UpgradeData d : upgradeDatas)datas.add(d.newSubEntity());
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









