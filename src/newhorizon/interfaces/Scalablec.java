package newhorizon.interfaces;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Buildingc;

import mindustry.graphics.Pal;
import newhorizon.NewHorizon;
import newhorizon.data.*;

import static mindustry.Vars.tilesize;

public interface Scalablec extends Buildingc{
    void resetUpgrade();
    boolean isContiunous();
    boolean isConnected();

    default void drawMode(){
        Draw.reset();
        float
                len = block().size * tilesize / 2f - tilesize,
                x = getX(),
                y = getY();

        Draw.rect(getAmmoData().icon, x - len, y + len);
        Draw.color(getColor());
        Draw.rect(NewHorizon.NHNAME + "upgrade-icon-outline", x - len, y + len);
        Draw.reset();
    }
    default void drawConnected(){
        if(!isConnected())return;
        Draw.reset();
        float
                sin = Mathf.absin(Time.time, 6f, 1f),
                x = getX(),
                y = getY();

        for(int i = 0; i < 4; i++){
            float length = tilesize * block().size / 2f + 3 + sin;
            Tmp.v1.trns(i * 90, -length);
            Draw.color(Pal.gray);
            Draw.rect(NewHorizon.NHNAME + "linked-arrow-back", x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
            Draw.color(getColor());
            Draw.rect(NewHorizon.NHNAME + "linked-arrow", 	 x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
        }
        Draw.reset();
    }

    Upgraderc upgraderc();
    
    void setBaseData(UpgradeBaseData baseData);
    void setAmmoData(UpgradeAmmoData baseData);
    
    UpgradeBaseData getBaseData();
    UpgradeAmmoData getAmmoData();
    Color getColor();

}

