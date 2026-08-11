package newhorizon.expand.map;

import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Mesh;
import arc.graphics.VertexAttribute;
import arc.graphics.g3d.Camera3D;
import arc.graphics.g3d.PlaneBatch3D;
import arc.graphics.g3d.VertexBatch3D;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import arc.math.geom.Ray;
import arc.math.geom.Vec3;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Shaders;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;
import mindustry.type.Sector;

import java.nio.FloatBuffer;

import static mindustry.graphics.g3d.PlanetRenderer.outlineRad;

/** A campaign planet whose selectable sectors are projected onto a ring world's star-facing inner wall. */
public class RingWorldPlanet extends Planet {
    private static final Mat3D sectorTransform = new Mat3D();
    private static final float sqrt3 = Mathf.sqrt(3f);

    public final int columns;
    public final int rows;
    public final float innerRadius;
    public final float outerRadius;
    public final float halfWidth;
    public final float campaignHalfWidth;
    public final float hexSize;

    public float panelScale = 0.985f;
    // Shared inward offset for picking, sector fills, borders and labels.
    public float campaignDepth = 1.35f;
    public float sourceLatitude = 32f;
    public float maxCameraPitch = 22f;
    public float campaignCameraMinDistance = 16f;
    public float campaignCameraZoomDistance = 6f;

    private final Vec3 intersection = new Vec3();

    public RingWorldPlanet(String name, Planet parent, float innerRadius, float outerRadius, float halfWidth, int columns, int rows) {
        super(name, parent, 1f);
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.halfWidth = halfWidth;
        this.campaignHalfWidth = halfWidth * 0.73f;
        this.columns = columns;
        this.rows = rows;
        this.hexSize = Mathf.PI2 * innerRadius / (columns * 1.5f);

        grid = new RingWorldGrid(this);
        sectors.ensureCapacity(grid.tiles.length);
        for (int i = 0; i < grid.tiles.length; i++) {
            sectors.add(new Sector(this, grid.tiles[i]));
        }
        sectorApproxRadius = sectors.first().tile.v.dst(sectors.first().tile.corners[0].v);

        // The ring and its parent star share a center; it is a megastructure,
        // not a conventional orbiting satellite.
        orbitRadius = 0f;
        orbitTime = Float.POSITIVE_INFINITY;
        drawOrbit = false;
        if (parent != null) parent.updateTotalRadius();
    }

    @Override
    public float getRotation() {
        return 0f;
    }

    public float centerU(int id) {
        return (id % columns) * 1.5f * hexSize;
    }

    public float centerV(int id) {
        int column = id % columns;
        int row = id / columns;
        float stagger = (column & 1) == 0 ? -0.25f : 0.25f;
        return (row - (rows - 1f) / 2f + stagger) * sqrt3 * hexSize;
    }

    public Vec3 getSectorCenter(int id, float radial, Vec3 out) {
        return mapSurface(centerU(id), centerV(id), radial, out);
    }

    public Vec3 getSectorCorner(int id, int corner, float radial, float scale, Vec3 out) {
        float angle = corner * 60f * Mathf.degreesToRadians;
        float u = centerU(id) + Mathf.cos(angle) * hexSize * scale;
        float v = centerV(id) + Mathf.sin(angle) * hexSize * scale;
        return mapSurface(u, Mathf.clamp(v, -campaignHalfWidth, campaignHalfWidth), radial, out);
    }

    public Vec3 getSectorDetailPoint(int id, float localU, float localV, float radial, Vec3 out) {
        return mapSurface(centerU(id) + localU, Mathf.clamp(centerV(id) + localV, -campaignHalfWidth, campaignHalfWidth), radial, out);
    }

    public Vec3 getSurfacePoint(float u, float v, float radial, Vec3 out) {
        return mapSurface(u, v, radial, out);
    }

    public Vec3 getSourcePoint(int id, Vec3 out) {
        return mapSource(centerU(id), centerV(id), out);
    }

    public Vec3 getSourcePoint(float u, float v, Vec3 out) {
        return mapSource(u, Mathf.clamp(v, -campaignHalfWidth, campaignHalfWidth), out);
    }

    public Vec3 getSourceCorner(int id, int corner, Vec3 out) {
        float angle = corner * 60f * Mathf.degreesToRadians;
        float u = centerU(id) + Mathf.cos(angle) * hexSize;
        float v = centerV(id) + Mathf.sin(angle) * hexSize;
        return mapSource(u, Mathf.clamp(v, -campaignHalfWidth, campaignHalfWidth), out);
    }

    private Vec3 mapSurface(float u, float v, float radial, Vec3 out) {
        float angle = u / innerRadius;
        return out.set(Mathf.cos(angle) * radial, v, Mathf.sin(angle) * radial);
    }

    private Vec3 mapSource(float u, float v, Vec3 out) {
        float longitude = u / innerRadius;
        float latitude = v / campaignHalfWidth * sourceLatitude * Mathf.degreesToRadians;
        float latitudeCos = Mathf.cos(latitude);
        return out.set(latitudeCos * Mathf.cos(longitude), Mathf.sin(latitude), latitudeCos * Mathf.sin(longitude)).nor();
    }

    @Override
    public Sector getSector(Ray ray) {
        return getSector(ray, innerRadius);
    }

    @Override
    public Sector getSector(Ray ray, float ignoredRadius) {
        Vec3 hit = intersectCylinder(ray, true);
        if (hit == null) return null;

        float hitAngle = Mathf.atan2(hit.z - position.z, hit.x - position.x);
        if (hitAngle < 0f) hitAngle += Mathf.PI2;
        float hitU = hitAngle * innerRadius;
        float hitV = hit.y - position.y;
        float circumference = Mathf.PI2 * innerRadius;

        Sector nearest = null;
        float nearestDst = Float.POSITIVE_INFINITY;
        for (Sector sector : sectors) {
            float signedDu = centerU(sector.id) - hitU;
            if (signedDu > circumference / 2f) signedDu -= circumference;
            if (signedDu < -circumference / 2f) signedDu += circumference;
            float du = Math.abs(signedDu);
            float dv = centerV(sector.id) - hitV;
            float dst = du * du + dv * dv;
            if (insideSectorHex(signedDu, dv)) return sector;
            if (dst < nearestDst) {
                nearestDst = dst;
                nearest = sector;
            }
        }
        return nearestDst <= hexSize * hexSize * 1.35f ? nearest : null;
    }

    @Override
    public Vec3 intersect(Ray ray, float ignoredRadius) {
        return intersectCylinder(ray, false);
    }

    private Vec3 intersectCylinder(Ray ray, boolean campaignFaceOnly) {
        float ox = ray.origin.x - position.x;
        float oy = ray.origin.y - position.y;
        float oz = ray.origin.z - position.z;
        float dx = ray.direction.x;
        float dy = ray.direction.y;
        float dz = ray.direction.z;

        float a = dx * dx + dz * dz;
        if (a < 0.000001f) return null;

        float b = 2f * (ox * dx + oz * dz);
        float surfaceRadius = campaignFaceOnly ? innerRadius - campaignDepth : innerRadius;
        float c = ox * ox + oz * oz - surfaceRadius * surfaceRadius;
        float discriminant = b * b - 4f * a * c;
        if (discriminant < 0f) return null;

        float root = Mathf.sqrt(discriminant);
        float t1 = (-b - root) / (2f * a);
        float t2 = (-b + root) / (2f * a);
        if (t1 > t2) {
            float swap = t1;
            t1 = t2;
            t2 = swap;
        }

        float result = validIntersection(t1, ox, oy, oz, dx, dy, dz, campaignFaceOnly) ? t1 :
                validIntersection(t2, ox, oy, oz, dx, dy, dz, campaignFaceOnly) ? t2 : -1f;
        if (result < 0f) return null;
        return intersection.set(ray.direction).scl(result).add(ray.origin);
    }

    private boolean validIntersection(float t, float ox, float oy, float oz, float dx, float dy, float dz, boolean campaignFaceOnly) {
        if (t <= 0f) return false;
        float y = oy + dy * t;
        float allowedWidth = campaignFaceOnly ? campaignHalfWidth : halfWidth;
        if (Math.abs(y) > allowedWidth + 0.04f) return false;
        if (!campaignFaceOnly) return true;

        float x = ox + dx * t;
        float z = oz + dz * t;
        // Only the surface whose inward normal faces the camera contains sectors.
        return dx * -x + dz * -z < 0f;
    }

    @Override
    public void fill(VertexBatch3D batch, Sector sector, Color color, float offset) {
        float radial = innerRadius - campaignDepth - offset;
        Vec3 center = getSectorCenter(sector.id, radial, Tmp.v33);
        for (int i = 0; i < 6; i++) {
            getSectorCorner(sector.id, i, radial, panelScale, Tmp.v31);
            getSectorCorner(sector.id, (i + 1) % 6, radial, panelScale, Tmp.v32);
            batch.tri(center, Tmp.v31, Tmp.v32, color);
        }
    }

    @Override
    public void drawBorders(VertexBatch3D batch, Sector sector, Color base, float alpha) {
        Color color = Tmp.c1.set(base).a((base.a + 0.25f + Mathf.absin(Time.globalTime, 5f, 0.25f)) * alpha);
        // Keep the generic hover/border layer behind the selected Pal.accent
        // outline so the original yellow rim remains fully readable.
        drawSelection(batch, sector, color, 0.035f, -0.04f);
    }

    @Override
    public void drawSelection(VertexBatch3D batch, Sector sector, Color color, float stroke, float length) {
        boolean selectedAccent = Math.abs(color.r - Pal.accent.r) < 0.08f &&
                Math.abs(color.g - Pal.accent.g) < 0.08f && Math.abs(color.b - Pal.accent.b) < 0.08f;
        float visibleStroke = selectedAccent ? stroke : Math.max(0.02f, stroke);
        float radial = innerRadius - campaignDepth - 0.08f - length;
        float innerScale = Math.max(0.55f, panelScale - visibleStroke / hexSize);
        for (int i = 0; i < 6; i++) {
            getSectorCorner(sector.id, i, radial, panelScale, Tmp.v31);
            getSectorCorner(sector.id, (i + 1) % 6, radial, panelScale, Tmp.v32);
            getSectorCorner(sector.id, (i + 1) % 6, radial - 0.002f, innerScale, Tmp.v33);
            getSectorCorner(sector.id, i, radial - 0.002f, innerScale, Tmp.v34);
            batch.quad(Tmp.v31, Tmp.v32, Tmp.v33, Tmp.v34, color);
        }
    }

    @Override
    public void renderSectors(VertexBatch3D batch, Camera3D cam, PlanetParams params) {
        batch.proj().mul(getTransform(sectorTransform));
        if (params.renderer != null) params.renderer.renderSectors(this);

        Vec3 hit = intersectCylinder(cam.getMouseRay(), true);
        Shaders.planetGrid.mouse.lerp(hit == null ? Vec3.Zero : Tmp.v31.set(hit).sub(position), 0.2f);

        Shader shader = Shaders.planetGrid;
        shader.bind();
        shader.setUniformMatrix4("u_proj", cam.combined.val);
        shader.setUniformMatrix4("u_trans", getTransform(sectorTransform).val);
        shader.apply();
        gridMesh.render(shader, Gl.lines);
    }

    @Override
    public Vec3 lookAt(Sector sector, Vec3 out) {
        getSectorCenter(sector.id, innerRadius, out);
        return out.nor();
    }

    @Override
    public Vec3 project(Sector sector, Camera3D cam, Vec3 out) {
        return cam.project(getSectorCenter(sector.id, innerRadius - campaignDepth - 0.02f, out).add(position));
    }

    @Override
    public void setPlane(Sector sector, PlaneBatch3D projector) {
        Vec3 origin = getSectorCenter(sector.id, innerRadius - campaignDepth - 0.025f, Tmp.v33).add(position);
        float angle = centerU(sector.id) / innerRadius;
        projector.setPlane(origin, Tmp.v32.set(Vec3.Y),
                Tmp.v31.set(-Mathf.sin(angle), 0f, Mathf.cos(angle)).nor());
    }

    @Override
    public void drawArc(VertexBatch3D batch, Vec3 a, Vec3 b, Color from, Color to, float length, float timeScale, int pointCount) {
        drawRingArc(batch, a, b, from, to, length, timeScale, pointCount);
    }

    @Override
    public void drawArcLine(VertexBatch3D batch, Vec3 a, Vec3 b, Color from, Color to, float length, float timeScale, int pointCount, float stroke) {
        drawRingArc(batch, a, b, from, to, length, timeScale, pointCount);
    }

    private void drawRingArc(VertexBatch3D batch, Vec3 a, Vec3 b, Color from, Color to, float length, float timeScale, int pointCount) {
        Sector start = sectors.min(sector -> sector.tile.v.dst2(a));
        Sector end = sectors.min(sector -> sector.tile.v.dst2(b));
        float circumference = Mathf.PI2 * innerRadius;
        float startU = centerU(start.id);
        float deltaU = centerU(end.id) - startU;
        if (deltaU > circumference / 2f) deltaU -= circumference;
        if (deltaU < -circumference / 2f) deltaU += circumference;
        float startV = centerV(start.id);
        float deltaV = centerV(end.id) - startV;

        for (int i = 0; i <= pointCount; i++) {
            float f = i / (float) pointCount;
            float radial = innerRadius - campaignDepth - Mathf.sin(f * Mathf.PI) * length;
            mapSurface(startU + deltaU * f, startV + deltaV * f, radial, Tmp.v31);
            batch.vertex(Tmp.v31, Tmp.c1.set(from).lerp(to, (f + Time.globalTime / timeScale) % 1f));
        }
        batch.flush(Gl.lineStrip);
    }

    public Mesh buildGridMesh(Color color) {
        Mesh mesh = new Mesh(true, sectors.size * 12, 0, VertexAttribute.position3, VertexAttribute.color);
        FloatBuffer buffer = mesh.getVerticesBuffer();
        buffer.clear();
        float packedColor = color.toFloatBits();
        float radial = innerRadius - campaignDepth - 0.015f;

        for (Sector sector : sectors) {
            for (int i = 0; i < 6; i++) {
                getSectorCorner(sector.id, i, radial, panelScale, Tmp.v31);
                getSectorCorner(sector.id, (i + 1) % 6, radial, panelScale, Tmp.v32);
                buffer.put(Tmp.v31.x).put(Tmp.v31.y).put(Tmp.v31.z).put(packedColor);
                buffer.put(Tmp.v32.x).put(Tmp.v32.y).put(Tmp.v32.z).put(packedColor);
            }
        }

        buffer.flip();
        return mesh;
    }

    /** Keeps the campaign camera inside the ring and limits pitch to the habitable band. */
    public void constrainCamera(Vec3 cameraOffset) {
        float length = cameraOffset.len();
        if (length < 0.0001f) return;

        float maxY = Mathf.sin(maxCameraPitch * Mathf.degreesToRadians) * length;
        float y = Mathf.clamp(cameraOffset.y, -maxY, maxY);
        float horizontal = Mathf.sqrt(Math.max(0f, length * length - y * y));
        float currentHorizontal = Mathf.sqrt(cameraOffset.x * cameraOffset.x + cameraOffset.z * cameraOffset.z);
        if (currentHorizontal < 0.0001f) {
            cameraOffset.set(horizontal, y, 0f);
        } else {
            float scale = horizontal / currentHorizontal;
            cameraOffset.set(cameraOffset.x * scale, y, cameraOffset.z * scale);
        }
    }

    /** Places the camera on the star-facing side of a sector and points it toward the inner wall. */
    public void applyCampaignCamera(Camera3D camera, Vec3 cameraOffset, float zoom) {
        float horizontal = Mathf.sqrt(cameraOffset.x * cameraOffset.x + cameraOffset.z * cameraOffset.z);
        // PlanetDialog's spherical drag direction is reversed when the camera
        // is looking outward from inside the ring. Mirror the horizontal axis
        // so screen-space dragging retains vanilla behavior.
        Vec3 radial = Tmp.v31.set(cameraOffset.x, 0f, -cameraOffset.z).nor();
        float targetY = horizontal < 0.0001f ? 0f :
                Mathf.clamp(cameraOffset.y / horizontal * innerRadius, -campaignHalfWidth, campaignHalfWidth);
        Vec3 target = Tmp.v33.set(radial.x * (innerRadius - campaignDepth - 0.12f), targetY,
                radial.z * (innerRadius - campaignDepth - 0.12f)).add(position);
        float wallDistance = campaignCameraMinDistance + Math.max(0f, zoom - minZoom) * campaignCameraZoomDistance;
        float cameraRadius = Math.max(radius + 1f, innerRadius - wallDistance);

        camera.position.set(position).add(radial.x * cameraRadius, targetY, radial.z * cameraRadius);
        camera.up.set(Vec3.Y);
        camera.lookAt(target);
    }

    private boolean insideSectorHex(float localU, float localV) {
        float u = Math.abs(localU), v = Math.abs(localV);
        float radius = hexSize * 1.015f;
        return u <= radius && v <= sqrt3 * radius * 0.5f && sqrt3 * u + v <= sqrt3 * radius;
    }
}
