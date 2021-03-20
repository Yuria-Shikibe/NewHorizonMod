package newhorizon.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.graphics.MultiPacker;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValue;
import newhorizon.NewHorizon;
import newhorizon.bullets.DeliveryBulletType;
import newhorizon.feature.UpgradeData;
import newhorizon.func.DrawFuncs;
import newhorizon.func.NHSetting;

import static newhorizon.func.TableFs.LEN;

public class NHContent extends UnlockableContent{
	public static TextureRegion
			iconLevel, ammoInfo;
	
	public static Color outlineColor = DrawFuncs.outlineColor;
	
	public static DeliveryBulletType deliveryBullet;
	
	static{
		NHLoader.put("mass-deliver-pack@@404049");
	}
	
	public NHContent(){
		super("specific-content-loader");
		localizedName = "NewHorizon-StartLog";
	}
	
	@Override
	public void setStats(){
		stats.add(Stat.abilities, new StatValue(){
			@Override
			public void display(Table table){
				table.button("StartLog", Icon.info, Styles.cleart, NewHorizon::startLog).size(LEN * 3, LEN);
			}
		});
	}
	
	@Override
	public TextureRegion icon(Cicon icon){
		return iconLevel;
	}
	
	@Override
	public ContentType getContentType(){
		return ContentType.error;
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		super.createIcons(packer);
		if(!NHSetting.getBool("@active.advance-load*") || Vars.headless)return;
		packer.add(MultiPacker.PageType.editor, this.name + "-icon-editor", Core.atlas.getPixmap((TextureAtlas.AtlasRegion)this.icon(Cicon.full)));
		
		NHLoader.outlineTex.each( (arg, tex) -> {
			String[] s;
			if(arg.contains("@")) s = arg.split("@");
			else s = new String[]{arg, ""};
			
			TextureAtlas.AtlasRegion t = Core.atlas.find(s[0]);
			if(t.found())packer.add(MultiPacker.PageType.main, s[0] + s[1], DrawFuncs.getOutline(t, s.length > 2 ? Color.valueOf(s[2]) : outlineColor));
		});
	}
	
	@Override
	public void load(){
		ammoInfo = Core.atlas.find(NewHorizon.MOD_NAME + "upgrade-info");
		iconLevel = Core.atlas.find(NewHorizon.MOD_NAME + "level-up");
		
		NHLoader.outlineTex.each((arg, tex) -> {
			String[] s;
			if(arg.contains("@")) s = arg.split("@");
			else s = new String[]{arg, ""};
			NHLoader.outlineTex.put(arg, Core.atlas.find(s[0] + s[1]));
		});
		
		for(UpgradeData data : NHUpgradeDatas.all){
			data.load();
			data.init();
		}
		
		super.load();
	}
	
	@Override
	public void init(){
		super.init();
		deliveryBullet = new DeliveryBulletType();
		if(!Vars.headless)deliveryBullet.region = Core.atlas.find(NewHorizon.configName("mass-deliver-pack"));
	}
	
	@Override
	public boolean isHidden(){
		return true;
	}
}
