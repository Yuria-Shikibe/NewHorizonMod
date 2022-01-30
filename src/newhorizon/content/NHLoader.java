package newhorizon.content;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.loaders.TextureLoader;
import arc.graphics.Texture;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;

public class NHLoader{
	public static ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();
	
	public static void loadSprite(String spriteName){
		if(!Vars.headless){
			try{
				String name = "textures/" + spriteName;
				String path = name + ".png";
				
				Texture texture = new Texture(0, 0);
				
				TextureLoader.TextureParameter loader = new TextureLoader.TextureParameter();
//				loader.texture = texture;
				
				AssetDescriptor<Texture> desc = Core.assets.load(path, Texture.class, loader);
				desc.errored = Throwable::printStackTrace;
				
				Core.assets.finishLoadingAsset(desc);
//				return texture;
			}catch(Throwable t){
				throw t;
			}
		}
	}
}
