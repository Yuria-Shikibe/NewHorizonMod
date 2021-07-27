package newhorizon.content;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.loaders.TextureLoader;
import arc.graphics.Texture;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;

import java.lang.reflect.InvocationTargetException;

public class NHLoader{
	public static ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();
	
	public static Texture loadSprite(String spriteName){
		if(!Vars.headless){
			String name = "sprites/" + spriteName;
			String path = name + ".png";
			
			Texture sprite;
			try{
				sprite = Texture.class.getDeclaredConstructor((Class<?>[])null).newInstance();
			}catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
				sprite = null;
			}
			
			TextureLoader.TextureParameter t = new TextureLoader.TextureParameter();
			t.texture = sprite;
			AssetDescriptor<?> desc = Core.assets.load(path, Texture.class, t);
			desc.errored = Throwable::printStackTrace;
			return sprite;
		}else return null;
	}
}
