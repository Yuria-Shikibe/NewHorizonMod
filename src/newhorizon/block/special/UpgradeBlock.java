package newhorizon.block.special;

import arc.Core;
import arc.audio.Sound;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.audio.SoundLoop;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import newhorizon.content.NHFx;
import newhorizon.feature.UpgradeData;
import newhorizon.feature.UpgradeData.DataEntity;
import newhorizon.interfaces.ScalableBlockc;
import newhorizon.interfaces.Scalablec;
import newhorizon.interfaces.Upgraderc;
import newhorizon.vars.NHVars;

import static mindustry.Vars.*;
import static newhorizon.ui.TableFunc.LEN;
import static newhorizon.ui.TableFunc.OFFSET;

public class UpgradeBlock extends Block{
	public static final int defaultID = -1;
	public static final int buttonPerLine = 8;
	
	protected Sound upgradeSound = Sounds.build;
	public float upgradeEffectChance = 0.04f;

	public final Seq<Block> linkTarget = new Seq<>();
	public Color baseColor = Pal.accent;
	public Effect upgradeEffect = NHFx.upgrading;
	public float range = 400f;
	
	public final Seq<UpgradeData> upgradeDatas = new Seq<>();
	protected void addUpgrades(UpgradeData... inputs) {
		for(UpgradeData data : inputs){
			upgradeDatas.add(data);
		}
	}

	public UpgradeBlock(String name) {
		super(name);
		update = true;
		buildCostMultiplier = 2;
		configurable = true;
		solid = true;
		copyConfig = true;
		sync = true;
		
		config(Point2.class, (Cons2<UpgradeBlockBuild, Point2>)UpgradeBlockBuild::linkPos);
		config(Integer.class, (Cons2<UpgradeBlockBuild, Integer>)UpgradeBlockBuild::upgradeData);
		config(IntSeq.class, (UpgradeBlockBuild entity, IntSeq seq) -> {
			entity.linkPos(Point2.pack(seq.get(0) + entity.tileX(), seq.get(1) + entity.tileY()));
			int index = seq.get(2);
			if(index >= 0 && index < entity.datas.size && entity.canUpgrade(entity.datas.get(index)))entity.upgradeData(index);
		});
	}
	
	@Override
	public void init(){
		super.init();
		for(Block block : linkTarget){
			if(!(block instanceof ScalableBlockc) || !(block.buildType.get() instanceof Scalablec))throw new IllegalArgumentException("null @linkTarget :[red]'" + name + "'[]");
			((ScalableBlockc)block).setLink(this);
		}
		if(upgradeDatas.isEmpty())throw new IllegalArgumentException("");
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
	}

	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.output, (t) -> {
			t.row().add("[gray]Link Targets:").left().pad(OFFSET).row();
			for(Block block : linkTarget){
				t.table(t2 -> {
					t2.left();
					t2.table(table -> table.image(block.fullIcon).size(LEN * 1.5f).left()).size(LEN * 1.5f + OFFSET / 2f).pad(OFFSET / 2f).left();
					t2.table(table -> table.add(block.localizedName).left()).pad(OFFSET / 2f).left();
				}).left().grow().row();
			}
		});
		
		stats.add(Stat.ammo, UpgradeData.ammo(upgradeDatas, 0));
	}

	@Override
	public void setBars() {
		super.setBars();

		bars.add("upgradeProgress",
			(UpgradeBlockBuild entity) -> new Bar(
				() -> Core.bundle.get("ui.remain-time"),
				() -> Color.valueOf("#FF732A"),
				() -> entity.remainTime / entity.costTime()
			)
		);
	}

	@Override
	public void load() {
		super.load();
	}
	
	public class UpgradeBlockBuild extends Building implements Upgraderc{
		public Seq<DataEntity> datas = new Seq<>();
		
		{
			setData();
		}
		
		public int link = -1;
		public int upgradingID = defaultID;
		public int latestSelectID = -1;
		public float remainTime;
		public float warmup;
		
		protected transient Color baseColorTst = getLinkColor();
		protected transient SoundLoop upgradeSoundLoop = new SoundLoop(upgradeSound, 1f);
		
		protected boolean coreValid(Building core) {
			return core != null && core.items != null && !core.items.empty();
		}
		
		protected boolean coreValid() {
			return core() != null && core().items != null && !core().items.empty();
		}
		
		@Override
		public IntSeq config(){
			IntSeq seq = new IntSeq(3);
			
			 Tmp.p1.set(Point2.unpack(link).sub(tile.x, tile.y));
			 
			 seq.addAll(Tmp.p1.x, Tmp.p1.y, latestSelectID);
			 return seq;
		}
		
		@Override
		public void consumeItems(DataEntity data){
			if(state.rules.infiniteResources || cheating())return;
			Building core = core();
			if(coreValid(core))core.items.remove(data.requirements());
		}
		
		@Override
		public boolean canUpgrade(DataEntity data) {
			if(state.rules.infiniteResources || state.rules.editor)return true;
			
			Building core = core();
			return coreValid() && !isUpgrading() && core.items.has(data.requirements());
		}

		public float costTime(){
			return (upgradingID >= datas.size || upgradingID < 0) ? 0 : datas.get(upgradingID).costTime();
		}
		
		@Override//Data Upgrade
		public void upgradeData(DataEntity data){
			if(!canUpgrade(data))return;
			consumeItems(data);
			remainTime = data.costTime();
		}
		
		//Data Upgrade
		public void upgradeData(int data){
			upgradingID = data;
			if(state.rules.editor){
				completeUpgrade();
				return;
			}
			
			upgradeData(all().get(data));
		}
		
		@Override//Updates
		public void updateUpgrading() {
			upgradeSoundLoop = new SoundLoop(upgradeSound, 1f);
			upgradeSoundLoop.update(x, y, true);
			remainTime -= delta() * efficiency() * Vars.state.rules.buildSpeedMultiplier;
		}
		
		@Override
		public void completeUpgrade() {
			if(upgradingID < 0 || upgradingID >= datas.size)return;
			upgradeSoundLoop.update(x, y, false);
			upgradeSoundLoop.stop();
			Sounds.unlock.at(this);
			Fx.healBlockFull.at(x, y, block.size, baseColorTst);
			
			latestSelectID = upgradingID;
			
			datas.get(upgradingID).upgrade();
			switchAmmo(datas.get(upgradingID));
			upgradingID = defaultID;
		}
		
		//UI
		protected void buildUpgradeDataTable(Table t) {
			t.pane(table -> datas.each(data -> !data.isUnlocked || (data.type().isLeveled && !data.isMaxLevel()), data -> data.buildTable(table, this))).fillX().growY().row();
		}

		public void switchAmmo(DataEntity data){
			Sounds.click.at(this);
			datas.each(ammo -> ammo.selected = false);
			data.selected = true;
			latestSelectID = datas.indexOf(data);
			updateTarget();
		}
		
		public void buildSwitchAmmoTable(Table t, boolean setting) {
			t.table(Tex.paneSolid, table -> {
				if(setting){
					table.pane(cont -> 
						cont.button("Upgrade", Icon.settings, Styles.cleart, this::upgraderTableBuild).size(LEN * buttonPerLine, LEN)
					).fillX().height(LEN).pad(OFFSET / 3f).row();
				}
				
				table.pane(cont -> {
					int index = 0;
					for (DataEntity data : datas) {
						if(index % buttonPerLine == 0)cont.row().left();
						cont.button(new TextureRegionDrawable(data.type().icon), Styles.clearPartiali, LEN, () ->
							switchAmmo(data)
						).size(LEN).disabled(b ->
							!data.isUnlocked || data.selected
						).left();
						index ++;
					}
				}).fillX().height(LEN).pad(OFFSET / 3f);
				if(!setting)table.left();
			}).growX().fillY().padTop(OFFSET).row();
		}
		
		@Override
		public int linkPos(){
			return link;
		}
		
		@Override
		public void linkPos(int value) {
			if (linkValid())target().resetUpgrade();
			link = value;
			updateTarget();
		}

		//Overrides

		@Override
		public boolean onConfigureTileTapped(Building other) {
			if (!linkTarget.contains(other.block))return false;
			if (this == other || link == other.pos()) {
				configure(Tmp.p1.set(-1, -1));
				return false;
			} else if (!(other instanceof Scalablec)) {
				ui.showErrorMessage("Failed to connect, target " + other + " doesn't implement @Scalablec");
				return true;
			} else { 
				Scalablec target = (Scalablec)other;
				if (!target.isConnected() && target.team() == team && target.within(this, range())) {
					configure(Point2.unpack(target.pos()));
					return false;
				}
			}
			return true;
		}

		@Override
		public void upgraderTableBuild(){
			BaseDialog dialog = new BaseDialog("Upgrade", Styles.fullDialog);
			dialog.cont.clear();
			dialog.addCloseListener();
			dialog.cont.pane(t -> {
				//
				t.table(Tex.buttonEdge3, table -> {
					table.row().left();
					table.button(
						Icon.infoCircle, Styles.clearPartiali, () -> datas.get(latestSelectID).showInfo(false, this, core().items)
					).size(LEN).disabled(b -> latestSelectID < 0 || datas.isEmpty()).left();
					table.button(
						Icon.menu, Styles.clearPartiali, () -> new BaseDialog(""){{
							addCloseButton();
							cont.pane(t -> UpgradeData.ammo(upgradeDatas, 0).display(t));
						}}.show()
					).size(LEN).padLeft(OFFSET / 3 * 2);
				}).left().growX().fillY().row();

				buildSwitchAmmoTable(t, false);

				t.image().pad(OFFSET).fillX().height(4f).color(Pal.accent).row();
				buildUpgradeDataTable(t);
				t.image().pad(OFFSET).fillX().height(4f).color(Pal.accent).row();
				
			}).grow().row();
			dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).growX().height(LEN);
			dialog.show();
		}
		
		@Override
		public void updateTile() {
			if(remainTime >= 0){
				updateUpgrading();
				if(Mathf.chanceDelta(upgradeEffectChance))for(int i : Mathf.signs)upgradeEffect.at(x + i * Mathf.random(block.size / 2f * tilesize), y - Mathf.random(block.size / 2f * tilesize), block.size / 2f, baseColorTst);
			}else if(isUpgrading())completeUpgrade();
			
			
			if(efficiency() > 0 && isUpgrading()){
				if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
		}

		@Override
		public void placed() {
			super.placed();
			baseColorTst = getLinkColor();
		}
		
		@Override
		public void drawConfigure() {
			Drawf.dashCircle(x, y, range(), baseColorTst);

			Draw.color(baseColorTst);
			Lines.square(x, y, block.size * tilesize / 2f + 1.0f);

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
			write.i(this.latestSelectID);
			write.i(this.upgradingID);
			
			datas.each(data -> data.write(write));
		}

		@Override
		public void read(Reads read, byte revision) {
			this.remainTime = read.f();
			this.link = read.i();
			this.upgradingID = read.i();
			this.latestSelectID = read.i();

			datas.each(data -> data.read(read, revision));
		}
		
		@Override
		public void updateTarget() {
			if (linkValid()){
				if(latestSelectID >= 0 && datas.get(latestSelectID).isUnlocked)target().setData(datas.get(latestSelectID));
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
			return target instanceof Scalablec && linkTarget.contains(target.block) && target.team == team && within(target, range());
		}
		
		@Override public Color getLinkColor(){return baseColor == null ? team.color : baseColor;}
		@Override public boolean isUpgrading(){return (upgradingID != defaultID || remainTime >= 0) && !state.rules.editor;}
		@Override public float range() { return range; }
		@Override public void buildConfiguration(Table table) {buildSwitchAmmoTable(table, true);}
		@Override public void draw() {
			Draw.rect(region, x, y);
			Draw.z(Layer.bullet);
			
			Lines.stroke(block.size * warmup / 2f, baseColorTst);
			Lines.square(x, y, block.size * tilesize / 2.5f, -remainTime);
			Lines.square(x, y, block.size * tilesize / 2f, remainTime);
			
			Buildingc target;
			if((target = target()) != null){
				
				Lines.square(target.getX(), target.getY(), target.block().size * tilesize / 2.5f, -remainTime);
				Lines.square(target.getX(), target.getY(), target.block().size * tilesize / 2f, remainTime);
			}
		}
		@Override public void onRemoved() {
			NHVars.world.upgraderGroup.remove(this);
			if(linkValid())target().resetUpgrade();
		}
		public Scalablec target(){return linkValid() ? (Scalablec)link() : null;}
		
		@Override
		public Seq<DataEntity> all(){
			return datas;
		}
	}
}









