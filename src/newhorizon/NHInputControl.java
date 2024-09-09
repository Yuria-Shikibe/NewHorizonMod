package newhorizon;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Log;
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
                SchematicUtil.placeTerrain(tschem, cursorX, cursorY);
            }
            if (Core.input.keyRelease(KeyCode.x) && tschem == null){
                TerrainSchematic testInput = SchematicUtil.readBase64("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");

                SchematicUtil.placeTerrain(testInput, cursorX, cursorY);
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
