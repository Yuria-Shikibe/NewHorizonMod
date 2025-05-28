package newhorizon.content.bullets;

import arc.struct.IntMap;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.entities.bullet.BulletType;
import newhorizon.expand.bullets.AdaptBulletType;

public class OverrideBullets {
    public static IntMap<BulletType> bulletReplacement = new IntMap<>();

    public static void load(){
        //check for all vanilla bullets
        int lastSize = Vars.content.getBy(ContentType.bullet).size;
        for(int i = 0; i < lastSize; i++){
            if (Vars.content.getBy(ContentType.bullet).get(i).isModded()) break;
            bulletReplacement.put(i, new AdaptBulletType());
        }
    }

    public static BulletType getReplacement(BulletType bullet){
        return bulletReplacement.get(bullet.id, bullet);
    }
}
