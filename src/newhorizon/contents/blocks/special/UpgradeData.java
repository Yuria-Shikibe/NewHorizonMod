package newhorizon.contents.blocks.special;

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
import newhorizon.contents.effects.NHFx;
import newhorizon.contents.colors.*;
import newhorizon.contents.bullets.special.NHLightningBolt;
import newhorizon.contents.blocks.special.UpgraderBlock.UpgraderBlockBuild;

import static mindustry.Vars.*;

public class UpgradeData{
	public static final float LEN = 60f, OFFSET = 12f;
	private static final String offsetSpace = "    ";
	
	public static abstract class UpgradeBasicData{
		public float costTime;
		public float timeCostcoefficien = 0.125f;
		
		public abstract void buildTable(Table t);
		
		public UpgraderBlockBuild from;
		public final Seq<ItemStack> requirements = new Seq<>();
		public Boolf<ImageButton> disable = b -> false;
	}
	
	public static class UpgradeAmmoData extends UpgradeBasicData{
		public Effect 
			chargeEffect = Fx.none, 
			chargeBeginEffect = Fx.none;
		
		public UpgradeAmmoData(
			String name, 
			String description, 
			int unlockLevel, 
			BulletType selectAmmo,
			float costTime,
			ItemStack... items
		){
			this.name = name;
			this.description = description;
			this.unlockLevel = unlockLevel;
			this.selectAmmo = selectAmmo;
			this.costTime = costTime;
			this.icon = icon;
			requirements.addAll(items);
		}
		
		public void load (){
			this.icon = Core.atlas.find(NewHorizon.NHNAME + name);
		}
		
		public int id;
		
		public UpgraderBlockBuild from;
		public TextureRegion icon;
		public final String name, description;
		public final int unlockLevel;
		public final BulletType selectAmmo;
		public float costTime;
		
		public boolean isUnlocked = false;
		public boolean selected = false;
		
		public void require(ItemStack... items){
			requirements.addAll(items);
		}
		
		public void buildTable(Table t){
			t.row();
			
			t.image().width(LEN * 10 + OFFSET * 3.5f).height(4f).color(Color.lightGray);
			t.row();
			t.pane(table -> {
				buildDescriptions(table);
			}).size(LEN * 10 + OFFSET * 3, LEN * 1.8f + OFFSET);
			t.row();
			t.image().width(LEN * 10 + OFFSET * 3.5f).height(4f).color(Color.lightGray);
			t.row();
			t.add("").row();
		}
		
		public void buildDescriptions(Table t){
			
			/* length = 40 * 12 + 20(offset) * 2 = 560
			   width = 40 * 4 = 160
			 [Icon]			 [Bs]
			■■■■ ■■■■■■ ■■
			■■■■ [TEXTS]	 ■■
			■■■■			 ■■
			■■■■			 ■■
			*/	
			t.pane(table -> {
				table.image(icon).size(LEN);
			}).size(LEN);
			
			t.pane(table -> {
				table.add("[gray]BulletType: [accent]" + Core.bundle.get(name) + "[]").left().row();
				table.add("[gray]NeededTime: [accent]" + costTime + "[]").left().row();
			}).size(LEN * 6f, LEN).pad(OFFSET);
			
			t.pane(table -> {
				table.button(Icon.infoCircle, () -> showInfo(this)).size(LEN);
				table.button(Icon.hammer, () -> {
					from.upgradeAmmo(this);
				}).size(LEN).disabled(disable);
			}).size(LEN * 2f, LEN).pad(OFFSET);
		}
		
		public void showInfo(UpgradeAmmoData data){
			new Dialog(""){{
				setFillParent(true);
				cont.margin(15f);
				cont.image(icon).row();
				cont.add("Description: ").color(Pal.accent).left().row();
				cont.add(offsetSpace + Core.bundle.get(description)).color(Color.lightGray).left().row();
				cont.image().width(300f).pad(2).height(4f).color(Pal.accent);
				cont.row();
				cont.button("Leave", this::hide).size(120, 50).pad(4);
			}}.show();
		}
		
		public void write(Writes write) {
			write.bool(this.isUnlocked);
			write.bool(this.selected);
	    }
		
		public void read(Reads read, byte revision){
			this.isUnlocked = read.bool();
			this.selected = read.bool();
	    }
	}
	
	public static class UpgradeBaseData extends UpgradeBasicData{
		public int level = 0;
		public BulletType selectAmmo;
		
		public UpgradeBaseData(){
			this.speedMPL = 0.1f;
			this.damageMPL = 0.1f;
			this.defenceMPL = 0.1f;
			this.costTime = 600f;
		}
		
		public UpgradeBaseData(float speedMPL, float damageMPL, float defenceMPL, float costTime, ItemStack... items){
			this.speedMPL = speedMPL;
			this.damageMPL = damageMPL;
			this.defenceMPL = defenceMPL;
			this.costTime = costTime;
			requirements.addAll(items);
		}
		
		public final float speedMPL;
		public final float damageMPL;
		public final float defenceMPL;
		
		public TextureRegion icon;
		
		public void load(){this.icon = Core.atlas.find(NewHorizon.NHNAME + "upgrade2");}
		
		public boolean equals(Object obj){
			if(obj == this)return true;
			if(obj instanceof UpgradeBaseData){
				UpgradeBaseData data = (UpgradeBaseData)obj;
				return 
					data.from.equals(from) && 
					data.level == level && 
					data.speedMPL == speedMPL &&
					data.damageMPL == damageMPL &&
					data.defenceMPL == defenceMPL;
			}
			return false;
		}
		
		public UpgradeBaseData reset(){
			this.level = 0;
			return this;
		}
		
		public String toString(){
			return
			  "    SpeedMultPerLever: " + speedMPL +
			"\n    DamageMultPerLever: " + damageMPL +
			"\n    DefenceMultPerLever: " + defenceMPL;
			
		}
	
		public void write(Writes write) {
			write.i(this.level);
	    }
		
		public void read(Reads read, byte revision){
			this.level = read.i();
	    }
		
		public void plusLevel(){this.level ++;}
		
		public int level(){return level + 1;}
		
		public void buildTable(Table t){
			t.row();
			t.image().width(LEN * 10 + OFFSET * 3.5f).height(4f).color(Color.lightGray);
			t.row();
			
			t.pane(table -> {
				table.image(icon).size(LEN);
			}).size(LEN);
			
			t.pane(table -> {
				table.add("IsOnline?: " + (from!=null)).color(Color.green).row();
				table.add("[gray]Upgrade: [accent]Level " + level() + "[]").left().row();
			}).size(LEN * 6f, LEN).pad(OFFSET);
			
			t.pane(table -> {
				table.button(Icon.infoCircle, () -> showInfo(this)).size(LEN);
				table.button(Icon.hammer, () -> {
					from.upgradeBase(this);
				}).size(LEN).disabled(disable);
			}).size(LEN * 2f, LEN).pad(OFFSET);
			t.row();
			t.image().width(LEN * 10 + OFFSET * 3.5f).height(4f).color(Color.lightGray);
			t.row();
			t.add("").row();
		}
		
		public void showInfo(UpgradeBaseData data){
			new Dialog(""){{
				setFillParent(true);
				cont.margin(15f);
				cont.image(icon).row();
				cont.add("Description: ").color(Pal.accent).left().row();
				cont.add(offsetSpace + Core.bundle.get("upgrade-discription")).color(Color.lightGray).left().row();
				cont.image().width(300f).pad(2).height(4f).color(Pal.accent);
				cont.row();
				cont.button("Leave", this::hide).size(120, 50).pad(4);
			}}.show();
		}
		
	}
	
}








