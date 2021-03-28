package newhorizon.content;

import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.graphics.MultiPacker;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValue;
import newhorizon.func.NHSetting;
import newhorizon.func.SettingDialog;
import org.jetbrains.annotations.NotNull;

import static newhorizon.func.TableFs.LEN;

public class NHIconGenerator extends UnlockableContent{
	public NHIconGenerator(){
		super("specific-icon-generator");
		localizedName = Core.bundle.get("settings");
	}
	
	@Override
	public ContentType getContentType(){
		return ContentType.error;
	}
	
	@Override
	public TextureRegion icon(Cicon icon){
		return NHContent.ammoInfo;
	}
	
	@Override
	public void setStats(){
		stats.add(Stat.abilities, new StatValue(){
			@Override
			public void display(Table table){
				table.button("@settings", Icon.info, Styles.cleart, () -> new SettingDialog().show()).size(LEN * 3, LEN);
			}
		});
	}
	
	@Override
	public boolean isHidden(){
		return true;
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		super.createIcons(packer);
		if(!NHSetting.getBool("@active.advance-load*") || Vars.headless)return;
		NHLoader.fullIconNeeds.each( (name, iconSet) -> {
			TextureAtlas.AtlasRegion t = Core.atlas.find(iconSet.type.name);
			if(t.found()){
				PixmapRegion r = Core.atlas.getPixmap(t);
				Pixmap region = r.crop();
				
				Pixmap base = new Pixmap(r.width, r.height);
				base.drawPixmap(region);
				TextureAtlas.AtlasRegion cell = Core.atlas.find(iconSet.type.name + "-cell");
				if(cell != null && cell.found())base.drawPixmap(NHLoader.fillColor(Core.atlas.getPixmap(cell), Team.sharded.color), - 1, 0);
				
				for(Weapon w : iconSet.weapons){
					if(w.top)continue;
					NHLoader.drawWeaponPixmap(base, w, false);
				}
				
				base = NHLoader.getOutline(base, NHLoader.outlineColor);
				for(Weapon w : iconSet.weapons){
					if(!w.top)continue;
					NHLoader.drawWeaponPixmap(base, w, true);
				}
				packer.add(MultiPacker.PageType.main, name + "-icon", base);
			}else Log.info("[Create Fail]" + name);
		});
	}
	
	@Override
	public void load(){
		super.load();
		
		NHLoader.needBeLoad.each( (arg, tex) -> tex = Core.atlas.find(arg));
		//Vars.content.blocks().remove(NHLoader.iconGenerator);
	}
	
	public static class IconSet{
		public final Seq<Weapon> weapons = new Seq<>();
		public final UnitType type;
		
		public IconSet(@NotNull UnitType type, @Nullable Weapon[] weapons){
			this.type = type;
			if(weapons != null)this.weapons.addAll(weapons);
		}
	}
}
