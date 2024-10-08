package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.ctype.ContentType;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import newhorizon.NHUI;
import newhorizon.expand.game.NHWorldData;

import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;

public class DataFloorPlacer extends Block {
    protected static String divKey = "@@@";

    public DataFloorPlacer(String name) {
        super(name);

        alwaysUnlocked = true;
        destroySound = ambientSound = breakSound = Sounds.none;
        size = 1;
        update = true;
        outputsPayload = true;
        hasPower = false;
        configurable = true;
        clipSize = 120;
        saveConfig = saveData = true;
        rebuildable = false;
        solid = solidifes = false;
        requirements = ItemStack.empty;
        category = Category.logic;
        destroyEffect = Fx.none;
        buildVisibility = BuildVisibility.sandboxOnly;
        config(Block.class, (DataFloorPlacerBuild build, Block block) -> build.terrainBlock = (DataFloor) block);
        config(String.class, (DataFloorPlacerBuild build, String unit) -> {
            String[] s = unit.split(divKey);
            if(s.length < 2)return;
            build.terrainBlock = content.getByName(ContentType.block, s[0]);
            build.terrainData = Byte.parseByte(s[1]);
        });
        configClear((DataFloorPlacerBuild build) -> {
            build.terrainBlock = null;
            build.terrainData = 0;
        });

    }

    @Override
    public boolean isHidden(){
        return !Vars.state.rules.editor;
    }

    @Override
    public boolean canBeBuilt(){
        return Vars.state.rules.editor;
    }

    @Override
    public void drawShadow(Tile tile) {}

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        if(!canBeBuilt())drawPlaceText(Core.bundle.get("mod.ui.cautions.unit-initer"), x, y, valid);
    }

    public class DataFloorPlacerBuild extends Building {
        public DataFloor terrainBlock;
        public byte terrainData;
        public transient boolean placed = false;

        @Override public void onDestroyed(){}

        @Override public void afterDestroyed(){}

        @Override
        public void buildConfiguration(Table table){
            table.table(t -> {
                t.background(Styles.black5).margin(8f);

                //tile variants|tile selection
                Table tileDataSelectTable = new Table();
                Table tileSelectTable = new Table();

                //button group setup
                ButtonGroup<Button> tileDataSelect = new ButtonGroup<>();
                ButtonGroup<Button> tileSelect = new ButtonGroup<>();
                tileDataSelect.setMinCheckCount(0);
                tileDataSelect.setMaxCheckCount(1);
                tileSelect.setMinCheckCount(0);
                tileSelect.setMaxCheckCount(1);

                tileDataSelectTable.marginRight(4f);
                tileSelectTable.marginLeft(4f).marginRight(4f);

                //rebuild left
                Runnable leftRebuild = () -> {
                    tileDataSelect.clear();
                    tileDataSelectTable.clear();
                    if (terrainBlock == null) {
                        tileDataSelectTable.label(() -> "                   < N / A >                   ");
                    }else {
                        for (int i = 0; i < terrainBlock.maxSize; i++){
                            int finalI = i;
                            Button tileData = new Button(Styles.clearNoneTogglei);
                            tileData.table(cont -> cont.image(terrainBlock.splitRegion[finalI])).margin(4);
                            tileData.clicked(() -> terrainData = (byte) (finalI > 127? finalI: finalI - 256));
                            //so the full region should be 256 in width
                            if (i % 8 == 0){tileDataSelectTable.row();}
                            tileDataSelect.add(tileData);
                            tileDataSelectTable.add(tileData);
                        }
                    }
                };

                //rebuild right
                Runnable rightRebuild = () -> {
                    content.blocks().each(block -> {
                        if (block instanceof DataFloor){
                            DataFloor floor = (DataFloor) block;
                            Button tileButton = new Button(Styles.clearNoneTogglei);
                            tileButton.table(cont -> cont.image(floor.region).size(40f)).margin(8);
                            tileButton.clicked(() -> {
                                terrainBlock = floor;
                                terrainData = 0;
                                leftRebuild.run();
                            });
                            tileSelectTable.row();
                            tileSelectTable.add(tileButton);
                            tileSelect.add(tileButton);
                        }
                    });
                };

                //pane setup
                ScrollPane paneLeft = new ScrollPane(tileDataSelectTable, Styles.smallPane);
                ScrollPane paneRight = new ScrollPane(tileSelectTable, Styles.smallPane);
                paneLeft.setFadeScrollBars(false);
                paneLeft.setForceScroll(false, true);
                paneLeft.update(() -> paneLeft.exited(() -> Core.scene.unfocus(paneLeft)));
                paneRight.setFadeScrollBars(false);
                paneRight.setForceScroll(false, true);
                paneRight.update(() -> paneRight.exited(() -> Core.scene.unfocus(paneRight)));

                leftRebuild.run();
                rightRebuild.run();

                t.add(paneLeft).growX().height(NHUI.getHeight() / 4f);
                t.add(paneRight).growX().height(NHUI.getHeight() / 4f);
            });
        }

        public Block type(){
            return content.getByName(ContentType.unit, config().split(divKey)[0]);
        }

        @Override
        public String config(){
            if (terrainBlock != null){
                return terrainBlock.name + divKey + terrainData;
            }else {
                return "<N/A>";
            }
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            super.control(type, p1, p2, p3, p4);
        }

        //this is a very bad idea but i dont have many good choices, so wait until anuke make it possible to save tile.data
        @Override public void updateTile(){
            if(!placed){
                if (tile != null){
                    tile.data = terrainData;
                    if (terrainBlock != null){
                        tile.setOverlay(terrainBlock);
                    }
                    NHWorldData.worldTileData.addTileData(tile.x, tile.y, tile.data);
                }
                placed = true;
                kill();
            }
        }

        @Override public void drawConfigure(){}

        @Override
        public void draw(){
            if(Vars.state.isEditor()){
                if (terrainBlock != null && terrainData < terrainBlock.maxSize){
                    int index = terrainData;
                    if (index < 0) index += 256;
                    Draw.rect(terrainBlock.splitRegion[index], x, y, size * tilesize, size * tilesize);
                }else {
                    super.draw();
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            TypeIO.writeBlock(write, terrainBlock);
            write.b(terrainData);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            terrainBlock = (DataFloor) TypeIO.readBlock(read);
            terrainData = read.b();
        }
    }
}
