package newhorizon.expand.eventsys.custome;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;

public abstract class Customizer<T>{
	public T target;
	public String targetName = "";
	
	public Seq<CostumeField> targetFields = new Seq<>();
	
	public abstract void construct();
	public abstract T obtain();
	public abstract void buildTable(Table table);
}
