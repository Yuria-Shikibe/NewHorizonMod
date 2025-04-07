package newhorizon.expand.block.distribution.liquid;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.Liquids;
import mindustry.core.Renderer;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.NHGroups;
import newhorizon.NHVars;
import newhorizon.expand.block.distribution.item.AdaptConveyor;
import newhorizon.expand.block.distribution.item.AdaptItemBridge;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.*;

public class AdaptLiquidBridge extends AdaptItemBridge {
    public AdaptLiquidBridge(String name, AdaptConveyor cBlock) {
        super(name, cBlock);

        hasItems = false;
        hasLiquids = true;
        outputsLiquid = true;
        canOverdrive = false;
        group = BlockGroup.liquids;
        envEnabled = Env.any;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (state.rules.mode() == Gamemode.sandbox) return true;

        Seq<GravityTrapField> fields = new Seq<>();
        NHGroups.gravityTraps.intersect(tile.worldx(), tile.worldy(), tilesize, tilesize, fields);
        if (fields.isEmpty()) return false;
        for (GravityTrapField field : fields) {
            if (field.team() != team) return false;
        }
        return true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        NHVars.renderer.drawGravityTrap();
    }
    
    public class AdaptLiquidBridgeBuild extends AdaptItemBridgeBuild {
        @Override
        public void updateTransport(Building other){
            if(warmup >= 0.25f){
                moved |= moveLiquid(other, liquids.current()) > 0.05f;
            }
        }

        @Override
        public void doDump(){
            dumpLiquid(liquids.current(), 1f);
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);
            Draw.z(Layer.power + 0.1f);
            Draw.rect(topRegion, x, y);

            Draw.z(Layer.power);

            Tile other = world.tile(link);
            if(!linkValid(tile, other)) return;
            if(Mathf.zero(Renderer.bridgeOpacity)) return;

            Lines.stroke(4.5f);
            Draw.blend(Blending.additive);
            Draw.color(Liquids.water.color, Pal.gray, 0.45f);
            Draw.alpha(Renderer.bridgeOpacity * 0.75f);
            Lines.line(cBlock.pulseRegions[cBlock.pulseFrame() * 5], x, y, other.worldx(), other.worldy(), false);
            Draw.blend(Blending.normal);

            Draw.color(Liquids.water.color, Color.white, 0.4f);
            Draw.alpha(Renderer.bridgeOpacity * 0.75f);
            Lines.line(cBlock.edgeRegions[0], x, y, other.worldx(), other.worldy(), false);

            float dst = Mathf.dst(x, y, other.worldx(), other.worldy()) - tilesize/4f;
            float ang = Angles.angle(x, y, other.worldx(), other.worldy());
            int seg = Mathf.round(dst / tilesize);

            if (seg == 0) return;
            for (int i = 0; i < seg; i++) {
                Tmp.v1.trns(ang, (dst/seg) * i + tilesize/8f).add(this);
                Tmp.v2.trns(ang, dst/seg).add(Tmp.v1);
                Draw.color(Liquids.water.color, Color.white, 0.7f);
                Draw.alpha(Renderer.bridgeOpacity * 0.75f);
                Lines.stroke(6f);
                Lines.line(cBlock.arrowRegions[cBlock.conveyorFrame()], Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, false);
                Lines.line(cBlock.arrowRegions[cBlock.conveyorFrame() + 16], Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, false);
            }
            Draw.color();

            Draw.reset();
        }
    }
}
