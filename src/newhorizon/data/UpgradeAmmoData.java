package newhorizon.data;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.type.ItemStack;
import mindustry.ui.Styles;
import newhorizon.NewHorizon;
import newhorizon.content.NHBullets;
import newhorizon.content.NHItems;

import static newhorizon.func.TableFuncs.*;

import java.lang.reflect.Field;

import static mindustry.Vars.tilesize;

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
		this("level-up", "default-ammo", NHBullets.none, 0, 0, new ItemStack(NHItems.emergencyReplace, 0));
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
			t.pane(table -> table.image(icon).size(LEN).left()).size(LEN).left().pad(OFFSET / 3);

			t.pane(table -> {
				table.add("[lightgray]AmmoType: [accent]" + Core.bundle.get(name) + "[]").left().row();
				table.add("[lightgray]IsSelected: " + getJudge(selected) + "[]").left().row();
				table.add("[lightgray]IsUnlocked: " + getJudge(isUnlocked) + "[]").left().row();
			}).width(LEN * 6f).growY().pad(OFFSET);
			
			t.table(Tex.button, table -> {
				table.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(false)).size(LEN);
				table.button(Icon.upOpen, Styles.clearPartiali, () -> from.switchAmmo(this)).size(LEN).disabled(b -> !isUnlocked || selected);
			}).height(LEN + OFFSET).pad(OFFSET);
		}).pad(OFFSET / 2).fillX().growY().row();
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
				Class<?> typeClass = selectAmmo.getClass();
				Field[] fields = typeClass.getFields();
				for(Field field : fields){
					try{
						//table.add(field.getGenericType().toString()).row();
						if(field.getGenericType().toString().equals("boolean"))table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": ").append(getJudge(field.getBoolean(selectAmmo))).append("[]")).left().row();
						if(field.getGenericType().toString().equals("float") && field.getFloat(selectAmmo) > 0)table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": [accent]").append(field.getFloat(selectAmmo)).append("[]")).left().row();
						if(field.getGenericType().toString().equals("int") && field.getInt(selectAmmo) > 0)table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": [accent]").append(field.getInt(selectAmmo)).append("[]")).left().row();
					}catch(IllegalAccessException err){
						throw new IllegalArgumentException(err);
					}
				}
			}).size(460).row();
			cont.button("@back", Icon.left, this::hide).size(120, 50).pad(4);
		}}.show();
	}
	
	@Override
	public void infoText(Table table){
		table.button(new TextureRegionDrawable(ammoInfo), Styles.colori, () -> new Dialog("") {{
			keyDown(KeyCode.escape, this::hide);
			keyDown(KeyCode.back, this::hide);
			cont.pane(t -> {
				t.add("[lightgray]Damage: [accent]" + format(selectAmmo.damage) + "[]").left().row();
				if(selectAmmo.splashDamageRadius > 0){
					t.add(tabSpace + "[lightgray]SplashDamage: [accent]" + format(selectAmmo.splashDamage) + "[]").left().row();
					t.add(tabSpace + "[lightgray]SplashDamageRadius: [accent]" + format(selectAmmo.splashDamageRadius / tilesize) + "[]").left().row();
				}
				if(selectAmmo.knockback > 0)t.add("[lightgray]SplashDamageRadius: [accent]" + format(selectAmmo.knockback) + "[]").left().row();

				t.add("[lightgray]CanFrag?: " + getJudge(selectAmmo.fragBullet != null) + "[]").left().row();
				if(selectAmmo.fragBullet != null)t.add(tabSpace + "[lightgray]Frags: [accent]" + selectAmmo.fragBullets + "[]").left().row();

				t.add("[lightgray]CanFragLightnings?: " + getJudge(selectAmmo.lightning > 0) + "[]").left().row();
				if(selectAmmo.lightning > 0){
					t.add(tabSpace + "[lightgray]MaxLightningLength: [accent]" + format(selectAmmo.lightningLength + selectAmmo.lightningLengthRand) + "[]").left().row();
					t.add(tabSpace + "[lightgray]LightningDamage: [accent]" + format(selectAmmo.lightningDamage) + "[]").left().row();
				}

				t.add("[lightgray]CanHoming?: " + getJudge(selectAmmo.homingPower > 0) + "[]").left().row();
				if(selectAmmo.homingPower > 0){
					t.add(tabSpace + "[lightgray]HomingRange: [accent]" + format(selectAmmo.homingRange / tilesize) + "[]").left().row();
					t.add(tabSpace + "[lightgray]HomingPower: [accent]" + format(selectAmmo.homingPower) + "[]").left().row();
				}

				t.add("[lightgray]CanPierceUnits?: " + getJudge(selectAmmo.pierce || (selectAmmo.collidesAir && selectAmmo.collides)) + "[]").left().row();
				t.add("[lightgray]CanPierceTiles?: " + getJudge(selectAmmo.pierceBuilding || selectAmmo.collidesTiles) + "[]").left().row();
			}).row();
			cont.button("More Info", () -> ammoInfoText()).size(180, 50);
			cont.button("@back", Icon.left, this::hide).size(120, 50).pad(OFFSET / 2);
		}}.show()).size(ammoInfo.height + OFFSET / 2);
	}
	
	@Override
	public void addText(Table table){
		table.add("[lightgray]AmmoType: [accent]" + Core.bundle.get(name) + "[]").left().row();
	}

}





