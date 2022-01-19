package newhorizon.util.ui;

import arc.graphics.g2d.TextureRegion;
import mindustry.ctype.UnlockableContent;

public class LatestFeature{
	public UnlockableContent content;
	
	public String title, description, type;
	public TextureRegion icon;
	
	public LatestFeature(UnlockableContent content){
		title = content.localizedName;
		description = content.description;
		icon = content.fullIcon;
		type = content.getContentType().toString();
		
		this.content = content;
	}
	
	public LatestFeature(String title, String description, String type, TextureRegion icon){
		this.title = title;
		this.description = description;
		this.type = type;
		this.icon = icon;
	}
	
	public LatestFeature(String title, String description, String type, UnlockableContent content){
		this.content = content;
		this.title = title;
		this.description = description;
		this.type = type;
		this.icon = content.fullIcon;
	}
}
