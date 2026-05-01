package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;
import mindustry.world.draw.DrawBlock;

public class DrawDrillMineBeam extends DrawBlock {
    public float shooterOffset = 12f, shooterExtendOffset = 1.8f, shooterMoveRange = 5.2f, shootY = 1.55f;
    public float moveScale = 60f, moveScaleRand = 20f;
    public float laserScl = 0.2f, laserAlpha = 0.75f, laserAlphaSine = 0.2f;
    public Color laserColor = Color.valueOf("f58349");
    public int particles = 25;
    public float particleLife = 40f, particleRad = 9.75f, particleLen = 4f;
    public TextureRegion laserRegion, laserEndRegion;

    @Override
    public void load(Block block) {
        laserRegion = Core.atlas.find("minelaser");
        laserEndRegion = Core.atlas.find("minelaser-end");
    }

    @Override
    public void draw(Building build) {
        if (build instanceof Drill.DrillBuild drill){
            float timeDrilled = drill.timeDrilled / 2.5f;
            float xOffset = Mathf.randomSeed(build.id, -moveScaleRand, moveScaleRand);
            float yOffset = Mathf.randomSeed(build.id >> 2, -moveScaleRand, moveScaleRand);
            float yStart = Mathf.randomSeed(build.id >> 1, moveScale);

            float moveX = Mathf.sin(timeDrilled, moveScale + xOffset, shooterMoveRange) + build.x;
            float moveY = Mathf.sin(timeDrilled + yStart, moveScale + yOffset, shooterMoveRange) + build.y;

            float stroke = laserScl * drill.warmup;
            Draw.mixcol(laserColor, Mathf.absin(4f, 0.6f));
            Draw.alpha(laserAlpha + Mathf.absin(8f, laserAlphaSine));
            Draw.blend(Blending.additive);
            Drawf.laser(laserRegion, laserEndRegion,
                    build.x + (-shooterOffset + drill.warmup * shooterExtendOffset + shootY), moveY,
                    build.x - (-shooterOffset + drill.warmup * shooterExtendOffset + shootY), moveY,
                    stroke);
            Drawf.laser(laserRegion, laserEndRegion,
                    moveX, build.y + (-shooterOffset + drill.warmup * shooterExtendOffset + shootY),
                    moveX, build.y - (-shooterOffset + drill.warmup * shooterExtendOffset + shootY),
                    stroke);

            Draw.color(drill.dominantItem.color);

            float sine = 1f + Mathf.sin(6f, 0.1f);

            Lines.stroke(stroke / laserScl / 2f);
            Lines.circle(moveX, moveY, stroke * 12f * sine);
            Fill.circle(moveX, moveY, stroke * 8f * sine);

            rand.setSeed(build.id);
            float base = (Time.time / particleLife);
            for (int i = 0; i < particles; i++) {
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f);
                float len = Mathf.randomSeed(rand.nextLong(), particleRad * 0.8f, particleRad * 1.1f) * Interp.pow2Out.apply(fin);
                Lines.lineAngle(moveX + Angles.trnsx(angle, len), moveY + Angles.trnsy(angle, len), angle, particleLen * fout * stroke / laserScl);
            }

            Draw.blend();
            Draw.reset();
        }
    }
}
