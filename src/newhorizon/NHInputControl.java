package newhorizon;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.core.World;
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
    public @Nullable Block terrainBlock;
    public byte terrainData;
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
                Core.app.setClipboardText(SchematicUtil.writeBase64(tschem));
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

            if (Core.input.keyDown(KeyCode.e)){
                if (world.tile(cursorX, cursorY) != null && terrainBlock != null){
                    world.tile(cursorX, cursorY).data = terrainData;
                    world.tile(cursorX, cursorY).setFloor((Floor) terrainBlock);
                }
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
