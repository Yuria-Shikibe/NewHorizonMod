package newhorizon.expand.eventsys.custome;

import arc.scene.ui.layout.Table;

import java.lang.reflect.Field;

public class ParserableField extends CostumeField{
	public ParserableField(Field field){
		super(field);
	}
	
	@Override
	public void buildTable(Table table){
		super.buildTable(table);
	}
	
	@Override
	public void apply(Object tgt, Object val) throws IllegalAccessException{
		super.apply(tgt, val);
	}
}
