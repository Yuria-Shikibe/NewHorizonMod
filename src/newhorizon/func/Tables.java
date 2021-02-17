package newhorizon.func;

import arc.func.Cons;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Cicon;
import newhorizon.block.special.JumpGate;

import static mindustry.Vars.state;
import static newhorizon.func.TableFuncs.LEN;

public class Tables{
	public static class UnitSetTable extends Table{
		public UnitSetTable(JumpGate.UnitSet set, Cons<Table> stat){
			super();
			if(set.type.locked() && !state.rules.infiniteResources && state.isCampaign()){
				table(Tex.clear, t2 -> {
					t2.table(Tex.clear, table2 -> table2.image(Icon.lock).size(LEN).center()).left().size(LEN + TableFuncs.OFFSET * 1.5f).pad(TableFuncs.OFFSET);
					
					t2.pane(table2 -> table2.add("[gray]Need to be researched.").left().row()).size(LEN * 6f, LEN).left().pad(TableFuncs.OFFSET);
					
					t2.table(table2 -> table2.image(Icon.lock).size(LEN).center()).height(LEN + TableFuncs.OFFSET).disabled(b -> true).growX().left().pad(TableFuncs.OFFSET);
				}).fillX().growY().padBottom(TableFuncs.OFFSET / 2).row();
			}else{
				table(Tex.clear, t2 -> {
					t2.table(Tex.clear, table2 -> {
						TableFuncs.tableImageShrink(set.type.icon(Cicon.xlarge), LEN, table2);
					}).left().size(LEN + TableFuncs.OFFSET * 1.5f).pad(TableFuncs.OFFSET);
					
					t2.pane(table2 -> {
						table2.add("[lightgray]Summon: [accent]" + set.type.localizedName + "[lightgray]; Level: [accent]" + set.level + "[].").left().row();
						table2.add("[lightgray]NeededTime: [accent]" + TableFuncs.format(set.costTime() / 60) + "[lightgray] sec[]").left().row();
					}).size(LEN * 6f, LEN).left().pad(TableFuncs.OFFSET);
					
					t2.table(stat).height(LEN + TableFuncs.OFFSET).growX().left().pad(TableFuncs.OFFSET);
				}).fillX().growY().padBottom(TableFuncs.OFFSET / 2).row();
			}
		}
	}
}
