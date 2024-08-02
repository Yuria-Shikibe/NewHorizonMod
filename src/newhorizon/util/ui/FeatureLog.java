package newhorizon.util.ui;

import arc.Core;
import arc.func.Cons;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import mindustry.ctype.UnlockableContent;

public class FeatureLog{
	public UnlockableContent content;
	
	public String title, description;
	public featureType type;
	public TextureRegion icon;
	
	public boolean important = false;
	public Cons<Table> modifier = null;

	public boolean isContent;

	public enum featureType{

		ADJUST("adjust"),
		IMPROVE("improve"),
		BALANCE("balance"),
		FEATURE("feature"),
		CONTENT("content"),
		FIX("fix"),
		IMPORTANT("important"),;

		public final String name;
		public final String localizedName;

		featureType(String s) {
			name = s;
			localizedName = Core.bundle.get("nh.new-feature." + s);
		}
	}
			
	
	public FeatureLog(UnlockableContent content){
		title = content.localizedName;
		description = content.description;
		icon = content.fullIcon;
		type = featureType.CONTENT;
		
		this.content = content;

		isContent = true;
	}
	
	public FeatureLog(String title, String description, featureType type, TextureRegion icon){
		this.title = title;
		this.description = description;
		this.type = type;
		this.icon = icon;
	}
	
	public FeatureLog(String title, String description, featureType type, UnlockableContent content){
		this.content = content;
		this.title = title;
		this.description = description;
		this.type = type;
		this.icon = content.fullIcon;
	}

	public FeatureLog(int index, featureType type, TextureRegion icon){
		this.title = type.name + index;
		this.description = type.name + index + "-desc";
		this.type = type;
		this.icon = icon;
	}

	public FeatureLog(int index, featureType type, UnlockableContent content){
		this.content = content;
		this.title = type.name + index;
		this.description = type.name + index + "-desc";
		this.type = type;
		this.icon = content.fullIcon;
	}

    public String getLocalizedTitle(){
        return isContent? content.localizedName: Core.bundle.get("nh.new-feature." + title);
    }

    public String getLocalizedDescription(){
        return isContent? content.description: Core.bundle.get("nh.new-feature." + description);
    }
}
