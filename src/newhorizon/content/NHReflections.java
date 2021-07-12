package newhorizon.content;

import arc.struct.ObjectMap;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.ctype.MappableContent;

public class NHReflections{
	public static ObjectMap<String, MappableContent>[] contentNameMap;
	public static void load(){
		contentNameMap = Reflect.get(Vars.content, "contentNameMap");
	}
}
