package newhorizon.expand.block.distribution.track;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.TargetPriority;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.Autotiler;
import mindustry.world.blocks.distribution.Duct;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NewHorizon;
import newhorizon.content.NHItems;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class TrackRail extends Duct implements Autotiler {
    public float itemPerSecond = 5f;

    public TextureRegion[] edgeRegions, baseRegions, arrowRegions;


    public TrackRail(String name) {
        super(name);
        requirements(Category.distribution, with(NHItems.presstanium, 1));

        conveyorPlacement = false;
        underBullets = true;

        group = BlockGroup.transportation;
        priority = TargetPriority.transport;

        ambientSound = Sounds.conveyor;
        ambientSoundVolume = 0.004f;

        drawTeamOverlay = false;

    }

    @Override
    public void init() {
        super.init();
        speed = framePeriod();
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.itemsMoved, itemPerSecond, StatUnit.itemsSecond);
    }

    //frame period in a circle. eg 10 ips -> 6 ticks a circle
    public float framePeriod(){
        return 60f / itemPerSecond;
    }

    public int conveyorFrame(){return (int)((((Time.time) % framePeriod()) / framePeriod()) * 16);}

    public int pulseFrame(){return (int) ((Time.time/4f) % 4f);}

    @Override
    public void load() {
        super.load();
        edgeRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-edge"), 36, 36, 1);
        arrowRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-arrow"), 32, 32, 1);
        baseRegions = SpriteUtil.splitRegionArray(Core.atlas.find(NewHorizon.name("track-rail-pulse")), 32, 32, 3);
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return (otherblock.outputsItems() || (lookingAt(tile, rotation, otherx, othery, otherblock) && otherblock instanceof TrackRail))
                && lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
    }

    public boolean blends(Building self, Building other){
        if (other == null) return false;
        return blends(self.tile(), self.rotation, other.tileX(), other.tileY(), other.rotation, other.block);
    }

    public class TrackRailBuild extends DuctBuild{
        public float itemProgress(){
            return progress / framePeriod();
        }

        @Override
        public void draw(){
            float rotation = rotdeg();
            int r = this.rotation;

            //draw extra ducts facing this one for tiling purposes
            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = r - i;
                    float rot = i == 0 ? rotation : (dir)*90;
                    drawAt(x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, 0, rot, i != 0 ? SliceMode.bottom : SliceMode.top);
                }
            }

            //draw item
            if(current != null){
                Draw.z(Layer.blockUnder + 0.1f);
                Tmp.v1.set(Geometry.d4x(recDir) * tilesize / 2f, Geometry.d4y(recDir) * tilesize / 2f)
                        .lerp(Geometry.d4x(r) * tilesize / 2f, Geometry.d4y(r) * tilesize / 2f,
                                Mathf.clamp(itemProgress()));

                Draw.rect(current.fullIcon, x + Tmp.v1.x, y + Tmp.v1.y, itemSize, itemSize);
            }

            Draw.scl(xscl, yscl);
            drawAt(x, y, blendbits, rotation, SliceMode.none);
            Draw.reset();
        }

        @Override
        protected void drawAt(float x, float y, int bits, float rotation, SliceMode slice){
            Draw.blend(Blending.additive);
            Draw.z(Layer.blockUnder);
            Draw.color(team.color);
            Draw.rect(baseRegions[bits], x, y, rotation);

            Draw.z(Layer.blockUnder + 0.01f);
            Draw.rect(arrowRegions[conveyorFrame()], x, y, rotation);
            boolean backDraw = true;

            if (blends(this, right())) {Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg() + 90);backDraw = false;}
            if (blends(this, back())) {Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg());backDraw = false;}
            if (blends(this, left())) {Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg() - 90);backDraw = false;}
            if (backDraw){Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg());}

            Draw.blend();

            Draw.z(Layer.blockUnder + 0.02f);
            Draw.rect(edgeRegions[bits], x, y, rotation);
        }

        @Override
        public void updateTile(){
            if (current != null && progress < framePeriod()){
                progress += edelta();
            }

            if(current != null && next != null){
                if(progress >= framePeriod() && moveForward(current)){
                    items.remove(current, 1);
                    current = null;
                    progress %= framePeriod();
                }
            }

            if(current == null && items.total() > 0){
                current = items.first();
            }
        }
    }
}
