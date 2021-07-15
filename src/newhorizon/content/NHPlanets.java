package newhorizon.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.Tmp;
import arc.util.noise.Simplex;
import mindustry.content.Planets;
import mindustry.ctype.ContentList;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.HexMesher;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.Planet;

public class NHPlanets implements ContentList {
    public static Planet midantha;

    @Override
    public void load(){
        midantha = new Planet("midantha", Planets.sun, 3, 1.2f) {{
            generator = new SerpuloPlanetGenerator();
            
            bloom = true;
            visible = true;
            hasAtmosphere = true;
            alwaysUnlocked = true;
            meshLoader = () -> new NHModMesh(
                    this, 6,
                    5, 0.3, 1.7, 1.2, 1.4,
                    1.1f,
                    NHColor.darkEnrFront.cpy().lerp(Color.white, 0.2f),
                    NHColor.darkEnrFront,
                    NHColor.darkEnrColor,
                    NHColor.darkEnrColor.cpy().lerp(Color.black, 0.2f).mul(1.05f),
                    Pal.gray.cpy().lerp(Pal.metalGrayDark, 0.25f).lerp(NHColor.darkEnr, 0.02f),
                    Pal.gray,
                    Pal.darkerGray,
                    Pal.darkestGray.cpy().lerp(Pal.gray, 0.2f),
                    Pal.darkestGray
            );
    
            atmosphereColor = Color.valueOf("3c1b8f");
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
            startSector = 0;
        }};
    }
    
    public static class NHModMesh extends HexMesh{
        public static float waterOffset = 0.05f;
        
        public NHModMesh(Planet planet, int divisions, double octaves, double persistence, double scl, double pow, double mag, float colorScale, Color... colors){
            super(planet, new HexMesher(){
                @Override
                public float getHeight(Vec3 position){
                    position = Tmp.v33.set(position).scl(4f);
                    float height = (Mathf.pow(Simplex.noise3d(123, 7, 0.5f, 1f/3f, position.x, position.y, position.z), 2.3f) + waterOffset) / (1f + waterOffset);
                    return Math.max(height, 0.05f);
                }
    
                @Override
                public Color getColor(Vec3 position){
                    double height = Math.pow(Simplex.noise3d(1, octaves, persistence, scl, position.x, position.y, position.z), pow) * mag;
                    return Tmp.c1.set(colors[Mathf.clamp((int)(height * colors.length), 0, colors.length - 1)]).mul(colorScale);
                }
            }, divisions, Shaders.unlit);
        }
    }
}
