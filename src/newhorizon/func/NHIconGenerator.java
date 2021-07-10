//package newhorizon.func;
//
//import arc.Core;
//import arc.graphics.Color;
//import arc.graphics.Pixmap;
//import arc.graphics.g2d.PixmapRegion;
//import arc.graphics.g2d.TextureAtlas;
//import arc.struct.ObjectMap;
//import arc.util.Log;
//import mindustry.Vars;
//import mindustry.ctype.ContentType;
//import mindustry.ctype.UnlockableContent;
//import mindustry.game.Team;
//import mindustry.graphics.MultiPacker;
//import mindustry.type.UnitType;
//import mindustry.type.Weapon;
//
//public class NHIconGenerator extends UnlockableContent{
//		public NHIconGenerator generator;
//	public static void initLoad(){
//		generator = new NHIconGenerator("");
//	}
//
//	private static final ObjectMap<String, IconSet> fullIconNeeds = new ObjectMap<>();
//
//	public static void put(String name, IconSet set){
//		fullIconNeeds.put(name, set);
//	}
//
//	public NHIconGenerator(String name){
//		super(name);
//	}
//
//
//
//	@Override
//	public ContentType getContentType(){
//		return ContentType.error;
//	}
//
//	public static class IconSet{
//		public final UnitType type;
//		public final Weapon[] weapons;
//
//		public IconSet(UnitType type, Weapon[] weapons){
//			this.type = type;
//			this.weapons = weapons;
//		}
//	}
//}
