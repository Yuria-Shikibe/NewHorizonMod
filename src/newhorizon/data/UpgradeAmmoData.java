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
import newhorizon.func.TableFuncs;

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
			t.pane(table -> table.image(icon).size(TableFuncs.LEN).left()).size(TableFuncs.LEN).left();

			t.pane(table -> {
				table.add("[lightgray]AmmoType: [accent]" + Core.bundle.get(name) + "[]").left().row();
				table.add("[lightgray]IsSelected: " + TableFuncs.getJudge(selected) + "[]").left().row();
				table.add("[lightgray]IsUnlocked: " + TableFuncs.getJudge(isUnlocked) + "[]").left().row();
			}).size(TableFuncs.LEN * 6f, TableFuncs.LEN).pad(TableFuncs.OFFSET);
			
			t.table(Tex.button, table -> {
				table.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(false)).size(TableFuncs.LEN);
				table.button(Icon.upOpen, Styles.clearPartiali, () -> from.switchAmmo(this)).size(TableFuncs.LEN).disabled(b -> !isUnlocked || selected);
			}).height(TableFuncs.LEN + TableFuncs.OFFSET).pad(TableFuncs.OFFSET);
		}).pad(TableFuncs.OFFSET / 2).fillX().height(TableFuncs.LEN * 1.5f).row();
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
						if(field.getGenericType().toString().equals("boolean"))table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": ").append(TableFuncs.getJudge(field.getBoolean(selectAmmo))).append("[]")).left().row();
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
				t.add("[lightgray]Damage: [accent]" + TableFuncs.format(selectAmmo.damage) + "[]").left().row();
				if(selectAmmo.splashDamageRadius > 0){
					t.add(TableFuncs.tabSpace + "[lightgray]SplashDamage: [accent]" + TableFuncs.format(selectAmmo.splashDamage) + "[]").left().row();
					t.add(TableFuncs.tabSpace + "[lightgray]SplashDamageRadius: [accent]" + TableFuncs.format(selectAmmo.splashDamageRadius / tilesize) + "[]").left().row();
				}
				if(selectAmmo.knockback > 0)t.add("[lightgray]SplashDamageRadius: [accent]" + TableFuncs.format(selectAmmo.knockback) + "[]").left().row();

				t.add("[lightgray]CanFrag?: " + TableFuncs.getJudge(selectAmmo.fragBullet != null) + "[]").left().row();
				if(selectAmmo.fragBullet != null)t.add(TableFuncs.tabSpace + "[lightgray]Frags: [accent]" + selectAmmo.fragBullets + "[]").left().row();

				t.add("[lightgray]CanFragLightnings?: " + TableFuncs.getJudge(selectAmmo.lightning > 0) + "[]").left().row();
				if(selectAmmo.lightning > 0){
					t.add(TableFuncs.tabSpace + "[lightgray]MaxLightningLength: [accent]" + TableFuncs.format(selectAmmo.lightningLength + selectAmmo.lightningLengthRand) + "[]").left().row();
					t.add(TableFuncs.tabSpace + "[lightgray]LightningDamage: [accent]" + TableFuncs.format(selectAmmo.lightningDamage) + "[]").left().row();
				}

				t.add("[lightgray]CanHoming?: " + TableFuncs.getJudge(selectAmmo.homingPower > 0) + "[]").left().row();
				if(selectAmmo.homingPower > 0){
					t.add(TableFuncs.tabSpace + "[lightgray]HomingRange: [accent]" + TableFuncs.format(selectAmmo.homingRange / tilesize) + "[]").left().row();
					t.add(TableFuncs.tabSpace + "[lightgray]HomingPower: [accent]" + TableFuncs.format(selectAmmo.homingPower) + "[]").left().row();
				}

				t.add("[lightgray]CanPierceUnits?: " + TableFuncs.getJudge(selectAmmo.pierce || (selectAmmo.collidesAir && selectAmmo.collides)) + "[]").left().row();
				t.add("[lightgray]CanPierceTiles?: " + TableFuncs.getJudge(selectAmmo.pierceBuilding || selectAmmo.collidesTiles) + "[]").left().row();
			}).row();
			cont.button("More Info", () -> ammoInfoText()).size(180, 50);
			cont.button("@back", Icon.left, this::hide).size(120, 50).pad(TableFuncs.OFFSET / 2);
		}}.show()).size(ammoInfo.height + TableFuncs.OFFSET / 2);
	}
	
	@Override
	public void addText(Table table){
		table.add("[lightgray]AmmoType: [accent]" + Core.bundle.get(name) + "[]").left().row();
	}

}





