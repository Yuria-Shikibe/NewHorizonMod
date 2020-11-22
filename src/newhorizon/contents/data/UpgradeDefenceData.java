package newhorizon.contents.data;

import java.lang.reflect.Field;

import arc.audio.*;
import arc.util.pooling.*;
import arc.util.io.*;
import arc.*;
import arc.func.*;
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
import newhorizon.contents.items.*;
import newhorizon.contents.data.UpgradeData.*;

import arc.scene.ui.ImageButton.*;
import static mindustry.Vars.*;
import static mindustry.gen.Tex.*;

public class UpgradeDefenceData extends UpgradeMultData{
	public static final int ID = 1;
	
	public Effect
		activeEffect = Fx.none;
			
	public Cons<Building> action = entity -> {};
	public float reloadTime;
	public float activeHealthPercent = 0.2f;
	public BulletType selectAmmo;
	public Sound shootSound = Sounds.bigshot;
	public TextureRegion skillInfo;
	
	protected float reload;
	
	public UpgradeDefenceData(
		String name,
		String description,
		float costTime,
		int unlockLevel,
		ItemStack... items
	) {
		super(name, description, costTime, unlockLevel, items);
	}
	
	@Override
	public void load() {
		super.load();
	}
	
	public void update(){
		if(this.reload <= this.reloadTime)this.reload += Time.delta;
	}
	
	public void act(Building target){
		if(this.reload < this.reloadTime)return;
		action.get(target);
		this.reload = 0;
	}
	
	@Override
	public void infoText(Table table){
		table.button(new TextureRegionDrawable(ammoInfo), Styles.colori, () -> {
			new Dialog("") {{
				cont.pane(t -> {
					t.add("[lightgray]N/A[]").left().row();
				}).row();
				cont.button("Back", this::hide).size(120, 50).pad(OFFSET / 2);
			}}.show();
		}).size(ammoInfo.height + OFFSET / 2);
	}
	
	@Override
	public void buildUpgradeInfoAll(Table t) {
		t.image().fillX().pad(OFFSET).height(4f).color(Color.lightGray).row();
		t.pane(t2 -> {
			t2.pane(table -> {
				table.image(icon).size(LEN).left();
			}).size(LEN).left();

			t2.pane(table -> {
				table.add("[lightgray]DefenceType: [accent]" + Core.bundle.get(name) + "[]").left().row();
				table.add("[lightgray]IsSelected: " + getJudge(selected) + "[]").left().row();
				table.add("[lightgray]IsUnlocked: " + getJudge(isUnlocked) + "[]").left().row();
			}).size(LEN * 6f, LEN).pad(OFFSET);
			
			t2.pane(table -> {
				table.button(Icon.infoCircle, Styles.clearTransi, () -> {showInfo(this, false);}).size(LEN);
				table.button(Icon.upOpen, Styles.clearTransi, () -> {from.switchDefence(this);}).size(LEN).disabled(!isUnlocked || selected);
			}).size(LEN * 2, LEN).pad(OFFSET);
		}).size(LEN * 11, LEN * 1.5f).row();
		t.image().fillX().pad(OFFSET).height(4f).color(Color.lightGray).row();
	}
		
	
	@Override
	public void addText(Table table){
		table.add("[lightgray]Skill: [accent]" + Core.bundle.get(name) + "[]").left().row();
	}
	
	@Override
	public void write(Writes write) {
		write.f(this.reload);
		super.write(write);
	}
	
	@Override
	public void read(Reads read, byte revision) {
		this.reload = read.f();
		super.read(read, revision);
	}

}





