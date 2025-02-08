package newhorizon.expand.cutscene.components;

import arc.scene.ui.layout.Table;

public abstract class ActionTrigger {
    public ActionTrigger() {}

    public void update(){}

    public void trigger(){}

    public void buildTable(Table table){}

    //the only value that was saved
    public void write(String triggerData){}

    public void read(String triggerData){}
}
