package newhorizon.expand.block.stream;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.core.Renderer;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import newhorizon.content.NHContent;
import newhorizon.util.func.MathUtil;

import static mindustry.Vars.world;

public class StreamBeam {
    public float lastStrokeScale, lastOutput;
    public Color lastColor = Color.clear.cpy();
    public static final Rand rand = new Rand();

    public Liquid currentLiquid;
    public Building source, target;
    public int distance;
    public boolean clog;
    public int beamLength = 5;

    public int offsetX, offsetY, rotationOffset;
    public float amountCap = -1f;
    public Liquid filter;

    public StreamBeam(Building source) {
        this.source = source;
        offsetX = offsetY = 0;
    }

    public void update(){
        if (source == null) return;

        target = null;
        clog = false;
        distance = beamLength;

        for (int i = 0; i <= beamLength; i++){
            Building building = world.build(
                    source.tileX() + Geometry.d4x(getRotation()) * (i + 1),
                    source.tileY() + Geometry.d4y(getRotation()) * (i + 1)
            );
            if (building instanceof StreamBeamBuild sbb){
                target = building;
                distance = i;
                clog = !sbb.acceptStream(this);
                break;
            }
        }

        getCurrentLiquid();
        lastStrokeScale = Mathf.lerpDelta(lastStrokeScale, beamStrokeScale(), 0.05f);
        transportLiquid();
    }

    public Effect getEffect() {
        float len = target == null? (distance + 1): distance;
        float length = len * 8f;

        return new Effect(length, e -> {
            rand.setSeed(e.id);
            Tmp.v1.set(4f + length * e.fin(), rand.random(-3f * lastStrokeScale, 3f * lastStrokeScale)).rotate(e.rotation);
            float threshold = distance / len;
            Draw.color(e.color);
            Draw.alpha(1 - Mathf.curve(e.fin(), threshold, 1f));
            Lines.stroke(0.5f);
            Lines.lineAngle(Tmp.v1.x + e.x, Tmp.v1.y + e.y, e.rotation, 2f);
        });
    }

    public void getCurrentLiquid(){
        currentLiquid = null;
        if (source.liquids != null){
            if (filter != null){
                currentLiquid = filter;
            }else if (source.liquids.currentAmount() > 0.01f){
                currentLiquid = source.liquids.current();
            }
        }
        if (currentLiquid != null){
            lastColor.lerp(currentLiquid.color, 0.1f * Time.delta);
        }else {
            lastColor.lerp(Color.white, 0.05f * Time.delta);
        }
    }

    public void transportLiquid(){
        if (currentLiquid == null) return;

        if (source.liquids != null && source.liquids.get(currentLiquid) > 0.01f){
            float cap = amountCap > 0? amountCap * source.edelta(): 5;
            float maxAmount = Math.min(cap, source.liquids.get(currentLiquid));

            if (target != null && target.liquids != null && target instanceof StreamBeamBuild sbb){
                float maxAccept = Math.min(target.block.liquidCapacity - target.liquids.get(currentLiquid), maxAmount);
                if (sbb.acceptStream(this)) {
                    target.handleLiquid(source, currentLiquid, maxAccept);
                    sbb.handleStream(this);
                }
            }
            lastOutput = maxAmount;
            source.liquids.remove(currentLiquid, maxAmount);
        }

        //if (Mathf.chanceDelta(lastOutput / 0.2f)) {
        //    getEffect().at(source.x, source.y, getRotation() * 90f, currentLiquid.color);
        //}
    }

    public int getRotation(){
        return source == null? 0: (source.rotation + rotationOffset) % 4;
    }

    public void draw(){
        if (source == null) return;

        Draw.z(Layer.blockOver);

        if (clog) {
            Draw.color(Pal.remove);
            Draw.rect(Icon.warning.getRegion(), source.x, source.y, 4f, 4f);
        }

        /*
        float alpha = Renderer.bridgeOpacity;
        float stroke = MathUtil.timeValue(4f, 6f, 2f) * lastStrokeScale;
        float length = distance * 8f;

        Tmp.c1.set(lastColor).a(alpha);

        Tmp.v1.setZero().add(4f,               -stroke/2f).rotate(getRotation() * 90).add(source);
        Tmp.v2.setZero().add(4f + 8f,          -stroke/2f).rotate(getRotation() * 90).add(source);
        Tmp.v3.setZero().add(4f + length - 8f, -stroke/2f).rotate(getRotation() * 90).add(source);
        Tmp.v4.setZero().add(4f + length,      -stroke/2f).rotate(getRotation() * 90).add(source);
        Tmp.v5.setZero().add(4f + length + 8f, -stroke/2f).rotate(getRotation() * 90).add(source);
        Tmp.v6.setZero().add(0, stroke).rotate(getRotation() * 90);

        Lines.stroke(stroke);

        float colorBits = Tmp.c1.toFloatBits();
        float clearBits = Color.clear.toFloatBits();

        if (distance > 0){
            Fill.quad(Tmp.v1.x, Tmp.v1.y, colorBits, Tmp.v1.x + Tmp.v6.x, Tmp.v1.y + Tmp.v6.y, colorBits,
                    Tmp.v2.x + Tmp.v6.x, Tmp.v2.y + Tmp.v6.y, clearBits, Tmp.v2.x, Tmp.v2.y, clearBits);
            Fill.quad(Tmp.v1.x, Tmp.v1.y, colorBits, Tmp.v1.x + Tmp.v6.x, Tmp.v1.y + Tmp.v6.y, colorBits,
                    Tmp.v4.x + Tmp.v6.x, Tmp.v4.y + Tmp.v6.y, colorBits, Tmp.v4.x, Tmp.v4.y, colorBits);
            if (target == null) {
                Fill.quad(Tmp.v4.x, Tmp.v4.y, colorBits, Tmp.v4.x + Tmp.v6.x, Tmp.v4.y + Tmp.v6.y, colorBits,
                        Tmp.v5.x + Tmp.v6.x, Tmp.v5.y + Tmp.v6.y, clearBits, Tmp.v5.x, Tmp.v5.y, clearBits);
            }else if (distance > 0){
                Fill.quad(Tmp.v4.x, Tmp.v4.y, colorBits, Tmp.v4.x + Tmp.v6.x, Tmp.v4.y + Tmp.v6.y, colorBits,
                        Tmp.v3.x + Tmp.v6.x, Tmp.v3.y + Tmp.v6.y, clearBits, Tmp.v3.x, Tmp.v3.y, clearBits);
            }
        }

         */
        float length = distance * 8f;
        float scale = MathUtil.timeValue(0.75f, 1f, 2f) * lastStrokeScale;

        Tmp.v1.setZero().add(4f, 0f).rotate(getRotation() * 90).add(source);
        Tmp.v2.setZero().add(4f + length, 0f).rotate(getRotation() * 90).add(source);

        Draw.alpha(1f);
        Draw.color(lastColor);
        Drawf.laser(NHContent.beamLaser, NHContent.beamLaserEnd, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, scale);
        Draw.color();
        Drawf.laser(NHContent.beamLaserInner, NHContent.beamLaserInnerEnd, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, scale);
    }

    public float beamStrokeScale(){
        if (source == null || source.liquids == null || currentLiquid == null) return 0f;
        float value = Mathf.clamp(lastOutput / (0.5f * Time.delta));
        return value < 0.01f ? 0: Mathf.sqrt(value);
    }

    public Point2 calculateRotatedPosition(Point2 pos, int blockSize, int rotation) {
        int shift = (blockSize + 1) % 2;
        int px = pos.x, py = pos.y;

        return switch (rotation) {
            case 1 -> new Point2(-py + shift, px);
            case 2 -> new Point2(-px + shift, -py + shift);
            case 3 -> new Point2(py, -px + shift);
            default -> new Point2(px, py); // default rotation 0
        };
    }
}
