package newhorizon.expand;

import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.Renderer;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerNode;
import newhorizon.NHGroups;
import newhorizon.NHRenderer;
import newhorizon.NHVars;
import newhorizon.content.NHColor;
import newhorizon.content.NHContent;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;

public class GravityWallSubstation extends PowerNode {
    public Color fromColor = Pal.powerLight.cpy().lerp(Pal.gray, 0.5f),  toColor = Pal.powerLight;
    public GravityWallSubstation(String name) {
        super(name);

        configurable = false;
        autolink = true;
        update = true;
    }

    @Override
    public void init() {
        super.init();
        maxNodes = 9999;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Tile tile = world.tile(x, y);

        if(tile == null || !autolink) return;

        Lines.stroke(1f);
        drawRangeRect(x * tilesize + offset, y * tilesize + offset, laserRange * tilesize);

        getPotentialLinks(tile, player.team(), other -> {
            if (!(other instanceof PowerNodeBuild)) return;
            Draw.color(laserColor1, Renderer.laserOpacity * 0.5f);
            drawLaser(x * tilesize + offset, y * tilesize + offset, other.x, other.y, size, other.block.size);

            Drawf.square(other.x, other.y, other.block.size * tilesize / 2f + 2f, Pal.place);
        });

        getPotentialLinks(tile, player.team(), other -> {
            Drawf.selected(other, Pal.place);
        });

        Draw.reset();

        NHVars.renderer.drawGravityTrap();
    }

    public void drawRangeRect(float x, float y, float range){
        Lines.stroke(3, Pal.gray);
        Lines.square(x, y, range + 1);

        Lines.stroke(1, Vars.player.team().color);
        Lines.square(x, y, range);
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list){
        if(plan.config instanceof Point2[] ps){
            setupColor(1f);
            for(Point2 point : ps){
                int px = plan.x + point.x, py = plan.y + point.y;
                otherReq = null;
                list.each(other -> {
                    if(other.block != null
                            && (px >= other.x - ((other.block.size-1)/2) && py >= other.y - ((other.block.size-1)/2) && px <= other.x + other.block.size/2 && py <= other.y + other.block.size/2)
                            && other != plan && other.block.hasPower){
                        otherReq = other;
                    }
                });

                if(otherReq == null || otherReq.block == null) continue;

                Drawf.selected(otherReq.x, otherReq.y, otherReq.block, Pal.place);
            }
            Draw.color();
        }
    }

    protected boolean overlaps(float srcx, float srcy, Tile other, float range){
        return Intersector.overlaps(Tmp.r1.setCentered(srcx, srcy, range), other.getHitbox(Tmp.r1));
    }

    public boolean overlaps(@Nullable Tile src, @Nullable Tile other){
        if(src == null || other == null) return true;
        return Intersector.overlaps(Tmp.r1.setCentered(src.worldx() + offset, src.worldy() + offset, laserRange * tilesize), Tmp.r2.setSize(size * tilesize).setCenter(other.worldx() + offset, other.worldy() + offset));
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> overlaps(world.tile(point.x, point.y), world.tile(other.x, other.y)));
    }

    @Override
    protected void getPotentialLinks(Tile tile, Team team, Cons<Building> others){
        Boolf<Building> valid =
                other -> other != null && other.tile() != tile && other.block.connectedPower && other.power != null &&
                (other.block.outputsPower || other.block.consumesPower || other.block instanceof PowerNode) &&
                overlaps(tile.x * tilesize + offset, tile.y * tilesize + offset, other.tile(), laserRange * tilesize) && other.team == team;

        tempBuilds.clear();
        graphs.clear();

        //add conducting graphs to prevent double link
        for(var p : Edges.getEdges(size)){
            Tile other = tile.nearby(p);
            if(other != null && other.team() == team && other.build != null && other.build.power != null){
                graphs.add(other.build.power.graph);
            }
        }

        if(tile.build != null && tile.build.power != null){
            graphs.add(tile.build.power.graph);
        }

        var worldRange = laserRange * tilesize;
        var tree = team.data().buildingTree;
        if(tree != null){
            tree.intersect(tile.worldx() - worldRange, tile.worldy() - worldRange, worldRange * 2, worldRange * 2, build -> {
                if(valid.get(build) && !tempBuilds.contains(build)){
                    tempBuilds.add(build);
                }
            });
        }

        tempBuilds.sort((a, b) -> {
            int type = -Boolean.compare(a.block instanceof PowerNode, b.block instanceof PowerNode);
            if(type != 0) return type;
            return Float.compare(a.dst2(tile), b.dst2(tile));
        });

        returnInt = 0;

        tempBuilds.each(valid, t -> {
            if(returnInt ++ < maxNodes){
                graphs.add(t.power.graph);
                others.get(t);
            }
        });
    }

    public class GravityWallSubstationBuild extends PowerNodeBuild {
        public transient GravityTrapField field;

        @Override
        public void tapped() {
            super.tapped();
            Fx.placeBlock.at(this, laserRange * 2);
            Sounds.click.at(this);
            Seq<Point2> points = new Seq<>();
            getPotentialLinks(tile, team, link -> points.add(new Point2(link.tileX() - tile.x, link.tileY() - tile.y)));
            configure(points.toArray(Point2.class));
        }

        @Override
        public void created() {
            super.created();
            if(field != null)field.setPosition(self());
        }

        @Override
        public void updateTile() {
            if (timer(0, 3000f + Mathf.randomSeed(id, 3000))){
                Seq<Point2> points = new Seq<>();
                getPotentialLinks(tile, team, link -> points.add(new Point2(link.tileX() - tile.x, link.tileY() - tile.y)));
                configure(points.toArray(Point2.class));
            }
        }

        @Override
        public void draw(){
            Draw.rect(block.region, x, y);
            if(Mathf.zero(Renderer.laserOpacity) || isPayload()) return;

            Draw.z(Layer.power);
            setupColor(power.graph.getSatisfaction());

            for(int i = 0; i < power.links.size; i++){
                Building link = world.build(power.links.get(i));

                if(!linkValid(this, link)) continue;

                if(link.block instanceof PowerNode && link.id < id) {
                    drawLaser(x, y, link.x, link.y, size, link.block.size);
                }
            }
            Draw.reset();

            Draw.z(NHContent.POWER_AREA);
            Draw.color(toColor, fromColor, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
            Fill.square(x, y, range());

            Draw.z(NHContent.POWER_DYNAMIC);
            Draw.color(toColor, fromColor, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
            Fill.square(x, y, range() * 0.8f + range() * 0.2f * Interp.exp5Out.apply(Time.time / 240f % 1f));
        }

        @Override
        public void drawSelect(){
            drawRangeRect(x, y, range());
            for(int i = 0; i < power.links.size; i++){
                Building link = world.build(power.links.get(i));
                if (link != null) Drawf.selected(link, Pal.power);
            }
        }

        @Override
        public void add(){
            if(added)return;

            Groups.all.add(this);
            Groups.build.add(this);
            this.added = true;

            if(field == null)field = new GravityTrapField(this, this::isValid, range());

            field.add();
        }

        public float range(){
            return laserRange * tilesize;
        }

        public void remove(){
            if(added) NHGroups.gravityTraps.remove(field);

            super.remove();
        }
    }
}
