package newhorizon.expand.block.production.drill;

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
import arc.math.Rand;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.graphics.Drawf;
import mindustry.type.Category;
import mindustry.type.Item;
import newhorizon.content.NHItems;
import newhorizon.util.func.NHFunc;

import static mindustry.type.ItemStack.with;
import static newhorizon.util.func.NHFunc.rand;

public class BeamDrill extends AdaptDrill {
    public TextureRegion laser;
    public TextureRegion laserEnd;

    public float shooterOffset = 12f;
    public float shooterExtendOffset = 1.8f;
    public float shooterMoveRange = 5.2f;
    public float shootY = 1.55f;

    public float shadowOffset = 2f;

    public float moveScale = 60f;
    public float moveScaleRand = 20f;
    public float laserScl = 0.2f;

    public Color laserColor = Color.valueOf("f58349");
    public Color arcColor = Color.valueOf("f2d585");
    public float laserAlpha = 0.75f;
    public float laserAlphaSine = 0.2f;

    public float coolSpeed = 0.03f;

    public int particles = 25;
    public float particleLife = 40f, particleRad = 9.75f, particleStroke = 1.8f, particleLen = 4f;

    public BeamDrill() {
        super("beam-mining-facility");
        requirements(Category.production, with(Items.copper, 25, Items.lead, 20));
        mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten, NHItems.zeta});

        mineSpeed = 7.5f;
        mineCount = 3;

        powerConsBase = 300f;
    }

    public void load(){
        super.load();
        laser = Core.atlas.find("minelaser");
        laserEnd = Core.atlas.find("minelaser-end");

    }

    public class BeamDrillBuild extends AdaptDrillBuild{
        public Rand rand = new Rand();
        @Override
        public void drawMining(){
            float timeDrilled = Time.time / 1.5f;
            float
                moveX = Mathf.sin(timeDrilled, moveScale + Mathf.randomSeed(id, -moveScaleRand, moveScaleRand), shooterMoveRange) + x,
                moveY = Mathf.sin(timeDrilled + Mathf.randomSeed(id >> 1, moveScale), moveScale + Mathf.randomSeed(id >> 2, -moveScaleRand, moveScaleRand), shooterMoveRange) + y;

            float stroke = laserScl * warmup;
            Draw.mixcol(laserColor, Mathf.absin(4f, 0.6f));
            Draw.alpha(laserAlpha + Mathf.absin(8f, laserAlphaSine));
            Draw.blend(Blending.additive);
            Drawf.laser(laser, laserEnd, x + (-shooterOffset + warmup * shooterExtendOffset + shootY), moveY, x - (-shooterOffset + warmup * shooterExtendOffset + shootY), moveY, stroke);
            Drawf.laser(laser, laserEnd, moveX, y + (-shooterOffset + warmup * shooterExtendOffset + shootY), moveX, y - (-shooterOffset + warmup * shooterExtendOffset + shootY), stroke);

            Draw.color(arcColor);

            float sine = 1f + Mathf.sin(6f, 0.f);

            Fill.circle(moveX, moveY, stroke * 8f * sine);
            Lines.stroke(stroke / laserScl / 2f);
            Lines.circle(moveX, moveY, stroke * 12f * sine);

            rand.setSeed(id);
            float base = (Time.time / particleLife);
            for(int i = 0; i < particles; i++){
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