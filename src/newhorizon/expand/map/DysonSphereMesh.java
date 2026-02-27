package newhorizon.expand.map;

import arc.graphics.Color;
import arc.graphics.Gl;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.*;
import mindustry.type.Planet;
import newhorizon.content.NHColor;

public class DysonSphereMesh extends PlanetMesh {
    static Mat3D mat = new Mat3D();

    public static final PlanetGrid grid = PlanetGrid.create(0);
    public static final Vec3[] corners = new Vec3[grid.tiles.length];
    public static final int[][] edges = new int[grid.edges.length][2];

    static {
        for (PlanetGrid.Ptile tile : grid.tiles) {
            corners[tile.id] = tile.v;
        }
        for (PlanetGrid.Edge edge : grid.edges) {
            edges[edge.id][0] = edge.tiles[0].id;
            edges[edge.id][1] = edge.tiles[1].id;
        }
    }

    public float speed = 0.32f;

    public DysonSphereMesh(Planet planet, float radius){
        super(planet, MeshBuilder.buildHex(new HexMesher(){
            @Override
            public float getHeight(Vec3 position){
                return 1f;
            }

            @Override
            public void getColor(Vec3 position, Color out){
                if (cornerRange(position, 3)) {
                    out.set(NHColor.darkEnrFront);
                }else if (cornerRange(position, 5, 9)) {
                    out.set(Pal.darkerMetal);
                }else if (!cornerRange(position, 9)){
                    if (edgeRange(position, 3f)) {
                        out.set(NHColor.darkEnrFront);
                    }else if (edgeRange(position, 5f)) {
                        out.set(Pal.darkerMetal);
                    }else {
                        out.a(0);
                    }
                }else {
                    out.a(0);
                }
            }

            /*
            @Override
            public void getEmissiveColor(Vec3 position, Color out) {
                if (cornerRange(position, 2)) {
                    out.set(NHColor.ancientLight);
                }else if (!cornerRange(position, 7)){
                    if (edgeRange(position, 0.5f)) {
                        out.set(NHColor.ancientLight);
                    }
                }
            }

            @Override
            public boolean isEmissive() {
                return true;
            }

             */

            @Override
            public boolean skip(Vec3 position){
                getColor(position, Tmp.c1);
                return Tmp.c1.a == 0;
            }

            public boolean cornerRange(Vec3 vec, float deg) {
                return cornerRange(vec, 0, deg);
            }

            public boolean cornerRange(Vec3 vec, float min, float max) {
                Tmp.v31.set(vec);
                for (Vec3 v: corners) {
                    if (Tmp.v31.angle(v) > min && Tmp.v31.angle(v) < max) return true;
                }
                return false;
            }

            public boolean edgeRange(Vec3 vec, float max){
                return edgeRange(vec, 0, max);
            }

            public boolean edgeRange(Vec3 vec, float min, float max) {
                Tmp.v31.set(vec);
                for (int[] edge : edges) {
                    Tmp.v32.set(corners[edge[0]]).nor();
                    Tmp.v33.set(corners[edge[1]]).nor();
                    if (isOnArcNear(Tmp.v31, Tmp.v32, Tmp.v33,
                            Mathf.degreesToRadians * min / 2f,
                            Mathf.degreesToRadians * max / 2f)) {
                        return true;
                    }
                }
                return false;
            }

            public float arcDistance(Vec3 p, Vec3 a, Vec3 b){
                Vec3 n = a.cpy().crs(b);
                if(n.isZero()) return a.angleRad(p);

                boolean between = (a.cpy().crs(p).dot(n) >= 0) && (p.cpy().crs(b).dot(n) >= 0);

                if(between){
                    Vec3 pProj = p.cpy().sub(n.cpy().scl(p.dot(n))).nor();
                    return (float)Math.acos(Math.min(Math.max(p.dot(pProj), -1f), 1f));
                } else {
                    return Math.min(a.angleRad(p), b.angleRad(p));
                }
            }

            public boolean isOnArcNear(Vec3 p, Vec3 a, Vec3 b, float min, float max){
                return arcDistance(p, a, b) <= max && arcDistance(p, b, a) >= min;
            }

        }, 6, planet.radius, radius), Shaders.clouds);
    }

    public DysonSphereMesh(){}

    public float relRot(){
        return Time.globalTime * speed / 40f;
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        //don't waste performance rendering 0-alpha clouds
        if(params.planet == planet && Mathf.zero(1f - params.uiAlpha, 0.01f)) return;

        preRender(params);
        shader.bind();
        shader.setUniformMatrix4("u_proj", projection.val);
        shader.setUniformMatrix4("u_trans", mat.setToTranslation(planet.position).rotate(Vec3.Y, planet.getRotation() + relRot()).val);
        shader.apply();
        mesh.render(shader, Gl.triangles);
    }

    @Override
    public void preRender(PlanetParams params){
        Shaders.clouds.planet = planet;
        Shaders.clouds.lightDir.set(planet.solarSystem.position).sub(planet.position).rotate(Vec3.Y, planet.getRotation() + relRot()).nor();
        Shaders.clouds.ambientColor.set(planet.solarSystem.lightColor);
        Shaders.clouds.alpha = params.planet == planet ? 1f - params.uiAlpha : 1f;
    }
}
