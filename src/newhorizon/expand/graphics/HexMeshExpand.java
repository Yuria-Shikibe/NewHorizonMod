package newhorizon.expand.graphics;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.Tmp;
import arc.util.noise.Simplex;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.HexMesher;
import mindustry.graphics.g3d.MeshBuilder;
import mindustry.graphics.g3d.PlanetMesh;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;

public class HexMeshExpand extends PlanetMesh {

    public HexMeshExpand(Planet planet, int divisions, double octaves, double persistence, double scl, double pow, double mag, float colorScale, float sizeScale, Color... colors){
        super(planet, MeshBuilder.buildHex(new HexMesher(){

            @Override
            public float getHeight(Vec3 position){
                return 0;
            }

            @Override
            public Color getColor(Vec3 position){
                double height = Math.pow(Simplex.noise3d(0, octaves, persistence, scl, position.x, position.y, position.z), pow) * mag;
                return Tmp.c1.set(colors[Mathf.clamp((int)(height * colors.length), 0, colors.length - 1)]).mul(colorScale);
            }
        }, divisions, false, planet.radius * sizeScale, 0.2f), Shaders.unlit);
    }


    @Override
    public void preRender(PlanetParams params){
        Shaders.planet.planet = planet;
        Shaders.planet.lightDir.set(planet.solarSystem.position).sub(planet.position).rotate(Vec3.Y, planet.getRotation()).nor();
        Shaders.planet.ambientColor.set(planet.solarSystem.lightColor);
    }
}
