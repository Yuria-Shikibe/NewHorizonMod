package newhorizon.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.graphics.MultiPacker;
import mindustry.ui.Cicon;
import mindustry.world.Block;
import newhorizon.NewHorizon;
import newhorizon.feature.UpgradeData;
import newhorizon.func.DrawFuncs;

public class NHContent extends Block{
	public TextureRegion
			iconLevel, ammoInfo;
	
	
	
	public NHContent(){
		super("specific-content-loader");
		this.outlineIcon = true;
		this.outlineColor = DrawFuncs.outlineColor;
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		super.createIcons(packer);
		if(Vars.mobile)return;
		packer.add(MultiPacker.PageType.editor, this.name + "-icon-editor", Core.atlas.getPixmap((TextureAtlas.AtlasRegion)this.icon(Cicon.full)));
		if (!this.synthetic()) {
			PixmapRegion image = Core.atlas.getPixmap((TextureAtlas.AtlasRegion)this.icon(Cicon.full));
			this.mapColor.set(image.getPixel(image.width / 2, image.height / 2));
		}
		
		this.getGeneratedIcons();
		
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
		
		ammoInfo = Core.atlas.find(NewHorizon.NHNAME + "upgrade-info");
		iconLevel = region = Core.atlas.find(NewHorizon.NHNAME + "level-up");
		
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
	public boolean isHidden(){
		return true;
	}
}
