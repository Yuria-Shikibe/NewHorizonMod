package newhorizon.expand.block.inner;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.meta.BuildVisibility;

public class PlaceholderBlock extends Block {
    public PlaceholderBlock(String name) {
        super(name);

        update = true;
        squareSprite = false;

        destructible = true;
        breakable = false;
        solid = true;
        rebuildable = false;

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
    public class PlaceholderBuild extends Building{
        //check for next tick
        public boolean checkTile = false;
        public Tile linkTile;
        public ConstructBlock.ConstructBuild linkBuild;

        public void updateLink(Tile tile){
            linkTile = tile;
            if (tile.build instanceof ConstructBlock.ConstructBuild){
                linkBuild = (ConstructBlock.ConstructBuild) tile.build;
            }
        }

        @Override
        public void draw() {}

        @Override
        public void updateTile() {
            super.updateTile();
            if (!checkTile && linkBuild == null){
                if (linkTile != null){
                    updateLink(linkTile);
                    checkTile = true;
                }
            }
            if (linkBuild == null || !linkBuild.isAdded()){
                tile.removeNet();
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
        public float handleDamage(float amount) {
            if (linkBuild != null){
                return linkBuild.handleDamage(amount);
            }else {
                return 0;
            }
        }
    }
}
