package newhorizon.interfaces;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import mindustry.gen.Buildingc;
import newhorizon.NewHorizon;
import newhorizon.feature.UpgradeData.DataEntity;
import newhorizon.func.DrawFuncs;

import static mindustry.Vars.tilesize;

public interface Scalablec extends Buildingc{
    void resetUpgrade();
    void setLinkPos(int i);
    boolean isContiunous();
    boolean isConnected();
    
    default void drawConnected(){
        if(!isConnected())return;
        DrawFuncs.drawConnected(getX(), getY(), block().size * tilesize, getColor());
    }
    default void drawMode(){
        Draw.reset();
        float
                len = block().size * tilesize / 2f - tilesize,
                x = getX(),
                y = getY();

        Draw.rect(getData().type().icon, x - len, y + len);
        Draw.color(getColor());
        Draw.rect(NewHorizon.configName("upgrade-icon-outline"), x - len, y + len);
        Draw.reset();
    }
    Upgraderc upgraderc();
    
    void setData(DataEntity baseData);
    
    DataEntity getData();
    
    Color getColor();

}

