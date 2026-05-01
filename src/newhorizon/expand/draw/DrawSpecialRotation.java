package newhorizon.expand.draw;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.IntMap;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import newhorizon.util.graphic.DrawFunc;

public class DrawSpecialRotation extends DrawBlock {
    public String suffix = "";
    public float x = 0f, y = 0f;
    public float rotateSpeed = 1f;
    public float spinTime = 60f;
    public float smooth = 0.15f;
    public float rotateAngle = 90f;

    public TextureRegion rotator;

    private final IntMap<Float> baseAngles = new IntMap<>();
    private final IntMap<Float> lastCycles = new IntMap<>();

    public DrawSpecialRotation(float rotateSpeed, String suffix) {
        this.rotateSpeed = rotateSpeed;
        this.suffix = suffix;
    }

    public DrawSpecialRotation(float rotateSpeed, String suffix, float spinTime, float smooth) {
        this.rotateSpeed = rotateSpeed;
        this.suffix = suffix;
        this.spinTime = spinTime;
        this.smooth = smooth;
    }

    public DrawSpecialRotation(float rotateSpeed, String suffix, float spinTime, float smooth, float rotateAngle) {
        this.rotateSpeed = rotateSpeed;
        this.suffix = suffix;
        this.spinTime = spinTime;
        this.smooth = smooth;
        this.rotateAngle = rotateAngle;
    }

    @Override
    public void load(Block block) {
        rotator = Core.atlas.find(block.name + suffix);
    }

    private float rotation(Building build) {
        float total = build.totalProgress() * rotateSpeed;
        float cycle = (float)Math.floor(total / spinTime);
        float progress = DrawFunc.cycle(total, 0f, spinTime);

        if (!lastCycles.containsKey(build.id)) {
            lastCycles.put(build.id, cycle);
            baseAngles.put(build.id, 0f);
        } else {
            float last = lastCycles.get(build.id, cycle);
            if (cycle > last) {
                baseAngles.put(build.id, baseAngles.get(build.id, 0f) + rotateAngle);
                lastCycles.put(build.id, cycle);
            } else if (cycle < last) {
                baseAngles.put(build.id, 0f);
                lastCycles.put(build.id, cycle);
            }
        }

        return baseAngles.get(build.id, 0f) + DrawFunc.rotator_90(progress, smooth) * (rotateAngle / 90f);
    }

    @Override
    public void draw(Building build) {
        Drawf.spinSprite(rotator, build.x + x, build.y + y, rotation(build));
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        Drawf.spinSprite(rotator, plan.drawx() + x, plan.drawy() + y, 0f);
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[]{rotator};
    }
}