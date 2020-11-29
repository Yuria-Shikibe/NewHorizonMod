package newhorizon.contents.data;

import java.lang.reflect.Field;

import arc.input.*;
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

public class UpgradeAmmoData extends UpgradeMultData{
	//Ammo Datas//
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
	//Data endl//
	
	public TextureRegion ammoInfo;
	
	public UpgradeAmmoData(){
		this("level-up", "default-ammo", UpgradeData.none, 0, 0, new ItemStack(NHItems.emergencyReplace, 0));
	}
	
	public UpgradeAmmoData(
		String name,
		String description,
		BulletType selectAmmo,
		float costTime,
		int unlockLevel,
		ItemStack... items
	) {
		super(name, description, costTime, unlockLevel, items);
		this.selectAmmo = selectAmmo;
	}
	
	@Override
	public void buildUpgradeInfoAll(Table t2) {
		t2.table(Tex.button, t -> {
			t.pane(table -> {
				table.image(icon).size(LEN).left();
			}).size(LEN).left();

			t.pane(table -> {
				table.add("[lightgray]AmmoType: [accent]" + Core.bundle.get(name) + "[]").left().row();
				table.add("[lightgray]IsSelected: " + getJudge(selected) + "[]").left().row();
				table.add("[lightgray]IsUnlocked: " + getJudge(isUnlocked) + "[]").left().row();
			}).size(LEN * 6f, LEN).pad(OFFSET);
			
			t.table(Tex.button, table -> {
				table.button(Icon.infoCircle, Styles.clearTransi, () -> {showInfo(this, false);}).size(LEN);
				table.button(Icon.upOpen, Styles.clearPartiali, () -> {from.switchAmmo(this);}).size(LEN).disabled(b -> !isUnlocked || selected);
			}).height(LEN + OFFSET).pad(OFFSET);
		}).pad(OFFSET / 2).fillX().height(LEN * 1.5f).row();
	}
	
	@Override
	public void load() {
		super.load();
		this.ammoInfo = Core.atlas.find(NewHorizon.NHNAME + "upgrade-info");
	}
	
	public void ammoInfoText(){
		new Dialog("") {{
			keyDown(KeyCode.escape, this::hide);
			keyDown(KeyCode.back, this::hide);
			cont.pane(table -> {
				Class typeClass = selectAmmo.getClass();
				Field[] fields = typeClass.getFields();
				for(Field field : fields){
					try{
						table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": [lightgray]").append(field.get(selectAmmo)).append("[]")).left().row();
					}catch(IllegalAccessException err){
						
					}
				}
			}).size(460).row();
			cont.button("Back", this::hide).size(120, 50).pad(4);
		}}.show();
	}
	
	@Override
	public void infoText(Table table){
		table.button(new TextureRegionDrawable(ammoInfo), Styles.colori, () -> {
			new Dialog("") {{
				keyDown(KeyCode.escape, this::hide);
				keyDown(KeyCode.back, this::hide);
				cont.pane(t -> {
					t.add("[lightgray]Damage: [accent]" + df.format(selectAmmo.damage) + "[]").left().row();
					if(selectAmmo.splashDamageRadius > 0){
						t.add(tabSpace + "[lightgray]SplashDamage: [accent]" + df.format(selectAmmo.splashDamage) + "[]").left().row();
						t.add(tabSpace + "[lightgray]SplashDamageRadius: [accent]" + df.format(selectAmmo.splashDamageRadius / tilesize) + "[]").left().row();
					}
					if(selectAmmo.knockback > 0)t.add("[lightgray]SplashDamageRadius: [accent]" + df.format(selectAmmo.knockback) + "[]").left().row();
					
					t.add("[lightgray]CanFrag?: " + getJudge(selectAmmo.fragBullet != null) + "[]").left().row();
					if(selectAmmo.fragBullet != null)t.add(tabSpace + "[lightgray]Frags: [accent]" + selectAmmo.fragBullets + "[]").left().row();
					
					t.add("[lightgray]CanFragLightnings?: " + getJudge(selectAmmo.lightning > 0) + "[]").left().row();
					if(selectAmmo.lightning > 0){
						t.add(tabSpace + "[lightgray]MaxLightningLength: [accent]" + df.format(selectAmmo.lightningLength + selectAmmo.lightningLengthRand) + "[]").left().row();
						t.add(tabSpace + "[lightgray]LightningDamage: [accent]" + df.format(selectAmmo.lightningDamage) + "[]").left().row();
					}
					
					t.add("[lightgray]CanHoming?: " + getJudge(selectAmmo.homingPower > 0) + "[]").left().row();
					if(selectAmmo.homingPower > 0){
						t.add(tabSpace + "[lightgray]HomingRange: [accent]" + df.format(selectAmmo.homingRange / tilesize) + "[]").left().row();
						t.add(tabSpace + "[lightgray]HomingPower: [accent]" + df.format(selectAmmo.homingPower) + "[]").left().row();
					}
					
					t.add("[lightgray]CanPierceUnits?: " + getJudge(selectAmmo.pierce || (selectAmmo.collidesAir && selectAmmo.collides)) + "[]").left().row();
					t.add("[lightgray]CanPierceTiles?: " + getJudge(selectAmmo.pierceBuilding || selectAmmo.collidesTiles) + "[]").left().row();
				}).row();
				cont.button("More Info", () -> {ammoInfoText();}).size(180, 50);
				cont.button("Back", this::hide).size(120, 50).pad(OFFSET / 2);
			}}.show();
		}).size(ammoInfo.height + OFFSET / 2);
	}
	
	@Override
	public void addText(Table table){
		table.add("[lightgray]AmmoType: [accent]" + Core.bundle.get(name) + "[]").left().row();
	}

}





