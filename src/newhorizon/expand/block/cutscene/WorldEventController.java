package newhorizon.expand.block.cutscene;

import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.cutscene.components.WorldActionEvent;
import newhorizon.expand.cutscene.event.RaidEvent;

public class WorldEventController extends Block {
    public WorldEventController(String name) {
        super(name);
        configurable = true;
        destructible = true;
        solid = true;

        update = true;

        category = Category.logic;
        buildVisibility = BuildVisibility.sandboxOnly;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class WorldEventControllerBuild extends Building implements CutsceneTrigger{
        public boolean active;
        public float progress;

        public WorldActionEvent event;
        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            table.button("t", this::activate);
        }

        @Override
        public void created() {
            super.created();
            event = new RaidEvent(Team.crux, x, y, closestCore().x, closestCore().y, 800f, 30f, 200f, 4, 0, 0, 300f);
        }

        public void update(){
            if (!active) return;
            //when active, start event related update.
            if (event != null){
                progress += Time.delta;
                if (progress >= event.waitDuration){
                    event.trigger();
                    deactivate();
                }
            }else {
                deactivate();
            }
        }

        @Override
        public void draw() {
            super.draw();
        }

        @Override
        public void activate() {
            active = true;
            progress = 0f;
            if (event != null) event.activate();
        }

        public void deactivate(){
            active = false;
            progress = 0f;
        }
    }
}
