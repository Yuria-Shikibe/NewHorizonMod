package newhorizon.content;

import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.util.Log;
import mindustry.ctype.ContentList;
import newhorizon.NewHorizon;

public class NHLoader implements ContentList{
	public static final ObjectMap<String, NHIconGenerator.IconSet> fullIconNeeds = new ObjectMap<>();
	public static final ObjectMap<String, TextureRegion> outlineTex = new ObjectMap<>();
	public static final ObjectMap<String, TextureRegion> needBeLoad = new ObjectMap<>();
	public static NHContent content;
	public static NHIconGenerator iconGenerator;
	
	public static void putNeedLoad(String name, TextureRegion textureRegion){
		needBeLoad.put(name, textureRegion);
	}
	
	public static void put(String name, NHIconGenerator.IconSet set){
		NHLoader.fullIconNeeds.put(name, set);
	}
	
	public static void put(String name){
		NHLoader.outlineTex.put(NewHorizon.NHNAME + name, null);
	}
	
	public static void put(String... args){
		for(String name : args)put(name);
	}
	
	@Override
	public void load(){
		content = new NHContent();
	}
	
	public void loadLast(){
		iconGenerator = new NHIconGenerator();
	}
}
