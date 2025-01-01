package newhorizon.expand.block.inner;

import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.production.factory.MultiBlockEntity;
import newhorizon.util.annotation.NotNull;

/**
 * Inner building that are linked to a specific building.
 * Handle items, liquids, damage and so on a passed to the main building to handle.
 * NEVER SUPPOSED TO USE OUTSIDE MULTI BLOCK!
 * */
public class LinkBlock extends Block {
    public LinkBlock(String name) {
        super(name);

        update = false;
        squareSprite = false;

        destructible = true;
        breakable = false;
        solid = true;
        rebuildable = false;

        hasItems = true;
        hasLiquids = true;

        buildVisibility = BuildVisibility.hidden;
    }

    public boolean canBreak(Tile tile){
        return false;
    }

    @Override
    public boolean isHidden(){
        return true;
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    public class LinkBuild extends Building {
        /**Linked Build. Can't be null*/
        public @NotNull Building linkBuild;

        public void updateLink(Building link){
            if (link instanceof MultiBlockEntity){
                linkBuild = link;
                items = link.items;
                liquids = link.liquids;
            }
        }

        @Override
        public void draw() {}

        @Override
        public void drawSelect() {
            if (linkBuild != null) {
                linkBuild.drawSelect();
            }
        }

        @Override
        public TextureRegion getDisplayIcon() {
            return linkBuild == null? this.block.uiIcon: linkBuild.getDisplayIcon();
        }

        @Override
        public String getDisplayName() {
            String name = linkBuild == null? this.block.localizedName: linkBuild.block.localizedName;
            return this.team == Team.derelict ? name + "\n" + Core.bundle.get("block.derelict") : name + (this.team != Vars.player.team() && !this.team.emoji.isEmpty() ? " " + this.team.emoji : "");
        }

        @Override
        public void displayBars(Table table) {
            if (linkBuild != null){
                for (Func<Building, Bar> buildingBarFunc : linkBuild.block.listBars()) {
                    Bar result = buildingBarFunc.get(linkBuild);
                    if (result != null) {
                        table.add(result).growX();
                        table.row();
                    }
                }
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (linkBuild != null){
                return linkBuild.acceptItem(source, item);
            }else {
                return false;
            }
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            if (linkBuild != null){
                return linkBuild.acceptStack(item, amount, source);
            }else {
                return 0;
            }
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (linkBuild != null){
                return linkBuild.acceptLiquid(source, liquid);
            }else {
                return false;
            }
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            //todo
            return super.acceptPayload(source, payload);
        }

        @Override
        public float handleDamage(float amount) {
            if (linkBuild != null){
                return linkBuild.handleDamage(amount);
            }else {
                return 0;
            }
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (linkBuild != null){
                linkBuild.handleItem(source, item);
            }
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source) {
            if (linkBuild != null) {
                linkBuild.handleStack(item, amount, source);
            }
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            if (linkBuild != null) {
                linkBuild.handleLiquid(source, liquid, amount);
            }
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            if (linkBuild != null) {
                linkBuild.handlePayload(source, payload);
            }
        }

        @Override
        public void handleString(Object value) {
            if (linkBuild != null) {
                linkBuild.handleString(value);
            }
        }

        @Override
        public void handleUnitPayload(Unit unit, Cons<Payload> grabber) {
            if (linkBuild != null) {
                linkBuild.handleUnitPayload(unit, grabber);
            }
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            if (linkBuild != null){
                ((MultiBlockEntity)linkBuild).updateLinkProximity();
            }
        }

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
        }

        @Override
        public void onProximityRemoved() {
            super.onProximityRemoved();
        }
    }
}