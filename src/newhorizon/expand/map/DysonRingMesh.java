package newhorizon.expand.map;

import arc.graphics.Color;
import arc.graphics.Gl;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.util.Time;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.PlanetMesh;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;

public class DysonRingMesh extends PlanetMesh {
    static Mat3D mat = new Mat3D();
    static Rand rand = new Rand();

    public Vec3 flipAngle = new Vec3();
    public float flipValue = 0;
    public float flipSpeed = 0f;
    public float rotateSpeed = 0.5f;

    public DysonRingMesh(Planet planet, float radius, float height, int seed, Color color, Color color2) {
        super(planet, CylinderRingMeshBuilder.build(radius, height, 120, color, color2), Shaders.clouds);
        rand.setSeed(seed);
        flipValue = rand.random(-180, 180);
        flipAngle.setToRandomDirection(rand);
        flipSpeed = rand.random(0.3f, 0.7f);
        rotateSpeed = rand.random(1f, 3f);
    }

    public DysonRingMesh(){}

    public float flipRot() {
        return Time.globalTime * flipSpeed / 40f + flipValue;
    }

    public float relRot(){
        return Time.globalTime * rotateSpeed / 40f;
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        //don't waste performance rendering 0-alpha clouds
        if(params.planet == planet && Mathf.zero(1f - params.uiAlpha, 0.01f)) return;

        preRender(params);
        shader.bind();
        shader.setUniformMatrix4("u_proj", projection.val);
        shader.setUniformMatrix4("u_trans", mat
                .setToTranslation(planet.position)
                .rotate(flipAngle, flipRot())
                .rotate(Vec3.Y, planet.getRotation() + relRot()).val);
        shader.apply();
        mesh.render(shader, Gl.triangles);
    }

    @Override
    public void preRender(PlanetParams params){
        Shaders.clouds.planet = planet;
        Shaders.clouds.lightDir.set(planet.solarSystem.position).sub(planet.position)
                .rotate(flipAngle, flipRot())
                .rotate(Vec3.Y, planet.getRotation() + relRot())
                .nor();
        Shaders.clouds.ambientColor.set(planet.solarSystem.lightColor);
        Shaders.clouds.alpha = params.planet == planet ? 1f - params.uiAlpha : 1f;
    }
}
