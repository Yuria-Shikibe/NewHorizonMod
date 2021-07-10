package newhorizon.content;

import arc.graphics.Color;
import mindustry.content.Planets;
import mindustry.ctype.ContentList;
import mindustry.graphics.g3d.HexMesh;
import mindustry.type.Planet;

public class NHPlanets implements ContentList {
    public static Planet eolutch;

    @Override
    public void load(){
        Planets.sun.radius += 12;
        Planets.serpulo.orbitRadius += 22f;
        eolutch = new Planet("serpulo", Planets.sun, 3, 4.0F) {
            {
                this.meshLoader = () -> {
                    return new HexMesh(this, 9);
                };
                this.atmosphereColor = Color.valueOf("E3EDCD");
                this.atmosphereRadIn = 0.06F;
                this.atmosphereRadOut = 0.7F;
                this.startSector = 15;
            }
        };
    }
}
