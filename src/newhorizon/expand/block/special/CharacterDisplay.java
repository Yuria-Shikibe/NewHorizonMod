package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.CharacterOverlay;

import static mindustry.Vars.*;

public class CharacterDisplay extends Block {
    public TextureRegion maskRegion;
    public TextureRegion[] letterRegions;

    public String queueText = "";

    public CharacterDisplay(String name) {
        super(name);

        size = 2;
        sync = true;
        update = true;
        rotate = false;
        saveConfig = true;
        configurable = true;
        logicConfigurable = true;
        selectionRows = selectionColumns = 8;

        config(Integer.class, (SpriteDisplayBuild build, Integer color) -> build.displayColor = color);
        config(Integer[].class, (SpriteDisplayBuild build, Integer[] character) -> {
            try {
                build.displayColor = character[0];
                build.displayCharacter = character[1];
            } catch (Exception e) {
                Log.err(e);
            }
        });
        config(String.class, (SpriteDisplayBuild build, String packed) ->{
            String[] split = packed.split("@");
            try {
                build.displayColor = Strings.parseInt(split[0]);
                build.displayCharacter = Strings.parseInt(split[1]);
            } catch (Exception e) {
                Log.err(e);
            }
        });
        configClear((SpriteDisplayBuild build) -> {
            build.displayCharacter = -1;
            build.displayColor = Color.white.rgba();
        });
    }

    @Override
    public void placeEnded(Tile tile, Unit builder, int rotation, Object config) {
        if (tile.build != null){
            if (queueText.isEmpty()) return;
            tile.build.configure(CharacterOverlay.charToData(queueText.charAt(0)));
            queueText = queueText.substring(1);
        }
    }

    @Override
    public void load() {
        super.load();
        maskRegion = Core.atlas.find(name + "-mask");
        letterRegions = new TextureRegion[64];
        for (int i = 0; i < 64; i++) {
            letterRegions[i] = Core.atlas.find("character-overlay" + i);
        }
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
        if (plan.config instanceof UnlockableContent content) {
            Draw.rect(content.uiIcon, plan.drawx(), plan.drawy());
        }
    }

    public class SpriteDisplayBuild extends Building {
        public int displayCharacter;
        public int displayColor = Color.white.rgba();

        @Override
        public void buildConfiguration(Table table) {
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            group.clear();
            Table color = new Table();
            color.table(input -> {
                TextField text = new TextField(queueText);
                text.update(() -> queueText = text.getText());
                input.add(text).growX();
                input.button(Icon.pick, Styles.clearNonei, () -> ui.picker.show(
                        Tmp.c1.set(displayColor), c -> configure(c.rgba() + "@" + displayCharacter)));
            }).growX().row();

            Table cont = new Table().top();
            cont.defaults().size(40);
            cont.clearChildren();

            int i = 0;

            for(int region = 0; region < letterRegions.length; region++){
                int finalRegion = region;
                TextureRegion character = letterRegions[region];
                ImageButton button = cont.button(Tex.whiteui, Styles.clearNoneTogglei, (character.width + character.height) / 2f, () -> control.input.config.hideConfig()).group(group).get();
                button.changed(() -> configure(displayColor + "@" + (button.isChecked() ? finalRegion : -1)));
                button.getStyle().imageUp = new TextureRegionDrawable(character);
                button.update(() -> button.setChecked(displayCharacter == finalRegion));

                if(i++ % 8 == 7) cont.row();
            }

            Table main = new Table(Styles.black6);
            main.add(color).growX().row();
            main.add(cont).maxHeight(40 * 8);
            table.top().add(main);
        }

        @Override
        public String config() {
            return displayColor + "@" + displayCharacter;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            Tmp.c1.set(displayColor);

            if (displayCharacter >= 0 && displayCharacter < letterRegions.length) {
                Draw.z(Layer.blockOver);
                Draw.rect(maskRegion, x, y);
                TextureRegion character = letterRegions[displayCharacter];
                Draw.color(Tmp.c1);
                Draw.rect(character, x, y, character.width * 0.16f * size, character.height * 0.16f * size);
            }

            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(displayCharacter);
            write.i(displayColor);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            displayCharacter = read.i();
            displayColor = read.i();
        }
    }
}
