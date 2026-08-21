package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;
import mindustry.world.draw.DrawBlock;

public class DrawRotator extends DrawBlock {
    public String suffix = "-rotator";
    public TextureRegion rotator;
    public float x, y;
    public float rotateSpeed = 1.25f;
    public float primaryRotation = 0;
    public boolean usesSpinDraw = false;
    /** Uses a drill's warmup timer for rotation, so it accelerates and decelerates with mining. */
    public boolean useDrillWarmup = false;

    public DrawRotator(boolean usesSpinDraw) {
        this.usesSpinDraw = usesSpinDraw;
    }

    public DrawRotator(float rotateSpeed, boolean usesSpinDraw) {
        this.rotateSpeed = rotateSpeed;
        this.usesSpinDraw = usesSpinDraw;
    }

    public DrawRotator(float rotateSpeed, String suffix) {
        this.suffix = suffix;
        this.rotateSpeed = rotateSpeed;
    }

    public DrawRotator(float rotateSpeed, float primaryRotation, String suffix) {
        this.suffix = suffix;
        this.rotateSpeed = rotateSpeed;
        this.primaryRotation = primaryRotation;
    }

    public DrawRotator(float rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
    }

    public DrawRotator(float rotateSpeed, float primaryRotation) {
        this.rotateSpeed = rotateSpeed;
        this.primaryRotation = primaryRotation;
    }

    public DrawRotator() {

    }

    @Override
    public void draw(Building build) {
        float progress = build.totalProgress();
        if (useDrillWarmup && build instanceof Drill.DrillBuild drill) {
            progress = drill.timeDrilled;
        }

        if (usesSpinDraw)
            Drawf.spinSprite(rotator, build.x + x, build.y + y, progress * rotateSpeed + primaryRotation);
        else Draw.rect(rotator, build.x + x, build.y + y, progress * rotateSpeed + primaryRotation);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(rotator, plan.drawx() + x, plan.drawy() + y, primaryRotation);
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[]{rotator};
    }

    @Override
    public void load(Block block) {
        rotator = Core.atlas.find(block.name + suffix);
    }
}
