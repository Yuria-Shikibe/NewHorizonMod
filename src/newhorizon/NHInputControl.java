package newhorizon;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.Schematic;
import mindustry.graphics.Layer;
import mindustry.input.DesktopInput;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import newhorizon.expand.map.SchematicUtil;
import newhorizon.expand.map.TerrainSchematic;

import static mindustry.Vars.world;
import static mindustry.input.PlaceMode.breaking;

public class NHInputControl extends DesktopInput {
    public int lastX = -1, lastY = -1;
    public TerrainSchematic tschem;

    public void terrainSelect(){
        Lines.stroke(1f);
        int cursorX = tileX(Core.input.mouseX());
        int cursorY = tileY(Core.input.mouseY());

        if(!Core.scene.hasKeyboard() && mode != breaking){
            if(Core.input.keyDown(KeyCode.z)){
                if (lastX == -1){
                    lastX = cursorX;
                }

                if (lastY == -1){
                    lastY = cursorY;
                }
                Draw.z(Layer.overlayUI);
                drawSelection(lastX, lastY, cursorX, cursorY, Vars.maxSchematicSize);
            }
            if (Core.input.keyRelease(KeyCode.z)){
                int blX, blY, trX, trY, curX, curY;
                curX = Mathf.clamp(cursorX, 0, world.width() - 1);
                curY = Mathf.clamp(cursorY, 0, world.height() - 1);
                blX = Math.min(curX, lastX);
                blY = Math.min(curY, lastY);
                trX = Math.max(curX, lastX);
                trY = Math.max(curY, lastY);
                tschem = new TerrainSchematic(blX, blY, trX, trY);
                lastX = lastY = -1;
            }
            if (Core.input.keyDown(KeyCode.x) && tschem != null){
                int ox = cursorX - tschem.width/2, oy = cursorY - tschem.height/2;
                Draw.z(Layer.overlayUI);
                drawSelection(ox, oy, ox + tschem.width - 1, oy + tschem.height - 1, Vars.maxSchematicSize);
            }
            if (Core.input.keyRelease(KeyCode.x) && tschem != null){
                SchematicUtil.placeTerrainOrigin(tschem, cursorX, cursorY);
            }
            if (Core.input.keyTap(KeyCode.v) && tschem != null){
                Seq<Schematic.Stile> stile = new Seq<>();
                tschem.floor.each(stile::add);
                tschem.overlay.each(stile::add);
                tschem.block.each(stile::add);
                Schematic schematic = new Schematic(stile, new StringMap(), tschem.width, tschem.height);
                Vars.ui.schematics.showInfo(schematic);
            }
        }
        Draw.reset();
    }

    int tileX(float cursorX){
        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
        if(selectedBlock()){
            vec.sub(block.offset, block.offset);
        }
        return World.toTile(vec.x);
    }

    int tileY(float cursorY){
        Vec2 vec = Core.input.mouseWorld(0, cursorY);
        if(selectedBlock()){
            vec.sub(block.offset, block.offset);
        }
        return World.toTile(vec.y);
    }
}
