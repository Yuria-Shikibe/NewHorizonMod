package newhorizon.expand.map.planet;

import arc.graphics.Color;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.g3d.GenericMesh;
import mindustry.graphics.g3d.MatMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;
import newhorizon.expand.graphics.HexMeshExpand;

public class CeitoPlanet extends Planet {
    public CeitoPlanet() {
        super("ceito", null, 2);

        bloom = true;
        accessible = false;

        meshLoader = () -> {
            Seq<GenericMesh> meshes = new Seq<>();

            meshes.add(new MatMesh(
                new HexMeshExpand(
                    this, 3,
                    5, 0.3, 1.7, 1.2, 1,
                    1.1f, 0.5f,
                    Color.valueOf("a7cef9"),
                    Color.valueOf("c1dbf6"),
                    Color.valueOf("a4b8ca"),
                    Color.valueOf("95c2ee"),
                    Color.valueOf("b3e5f9"),
                    Color.valueOf("a5cae8")
                ),
                new Mat3D().setToTranslation(Tmp.v31.set(new Vec3(0.5, -0.5, 0)).setLength(1.2f)))
            );

            meshes.add(new MatMesh(
                new HexMeshExpand(
                    this, 4,
                    5, 0.3, 1.7, 1.2, 1,
                    1.1f, 0.8f,
                    Color.valueOf("ff7a38"),
                    Color.valueOf("ff9638"),
                    Color.valueOf("ffc64c"),
                    Color.valueOf("ffc64c"),
                    Color.valueOf("ffe371"),
                    Color.valueOf("f4ee8e")
                ),
                new Mat3D().setToTranslation(Tmp.v31.set(new Vec3(-0.5, 0.5, 0)).setLength(1.8f)))
            );

            return new MultiMesh(meshes.toArray(GenericMesh.class));
        };
    }

    @Override
    public void draw(PlanetParams params, Mat3D projection, Mat3D transform){
        mesh.render(params, projection, transform.setToTranslation(position).rotate(Vec3.Y, Time.time / 20));
    }
}
