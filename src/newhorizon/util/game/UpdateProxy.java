package newhorizon.util.game;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Nullable;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Building;
import mindustry.input.MobileInput;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;
import newhorizon.expand.block.environment.OreVein;

import static mindustry.Vars.world;

public class UpdateProxy {

    private static final Interval timer = new Interval(10);
    private static final ObjectFloatMap<Item> oreCount = new ObjectFloatMap<>();
    private static final Seq<Item> itemArray = new Seq<>();
    private static final Seq<Tile> tempTiles = new Seq<>();
    private static @Nullable Item returnItem;
    private static int returnCount;

    private static final Seq<Drill> registerDrills = new Seq<>();

    public static void init(){
        Vars.content.blocks().each(block -> {
            if (block instanceof Drill){
                registerDrills.add((Drill) block);
            }
        });

        Thread thread = new Thread(() -> {
            Events.run(EventType.Trigger.draw, () -> {
                var input = Vars.control.input;
                if (input.block instanceof Drill drill){
                    Tile tile = null;
                    if (Vars.mobile){
                        for(BuildPlan plan : input.selectPlans) {
                            if(!plan.breaking && plan == ((MobileInput)input).lastPlaced && plan.block != null){
                                tile = plan.tile();
                            }
                        }
                    }else {
                        Vec2 vec = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
                        if (input.selectedBlock()) vec.sub(input.block.offset, input.block.offset);
                        int worldX = World.toTile(vec.x), worldY = World.toTile(vec.y);
                        tile = world.tile(worldX, worldY);
                    }
                    if (tile != null) {
                        countOreForDrill(drill, tile);
                        updateDrillStat(drill, tile);
                    }
                }
            });
            Events.run(EventType.Trigger.afterGameUpdate, () -> {
                if (timer.get(0, 60)) {
                    Vars.state.teams.getActive().each(teamData -> {
                        registerDrills.each(drill -> {
                            Seq<Building> buildings = teamData.buildingTypes.get(drill);
                            if (buildings != null && !buildings.isEmpty()) {
                                teamData.buildingTypes.get(drill).each(building -> {
                                    if (building instanceof Drill.DrillBuild drillBuild) {
                                        countOreForDrill(drill, drillBuild.tile);
                                        updateDrill(drillBuild);
                                    }
                                });
                            }
                        });
                    });
                };
            });
        });


        Events.run(EventType.Trigger.beforeGameUpdate, () -> {
            Vars.state.teams.getActive().each(teamData -> {
                registerDrills.each(drill -> {
                    Seq<Building> buildings = teamData.buildingTypes.get(drill);
                    if (buildings != null && !buildings.isEmpty()) {
                        teamData.buildingTypes.get(drill).each(building -> {
                            building.enabled = false;
                        });
                    }
                });
            });
        });

        Events.run(EventType.Trigger.afterGameUpdate, () -> {
            Vars.state.teams.getActive().each(teamData -> {
                registerDrills.each(drill -> {
                    Seq<Building> buildings = teamData.buildingTypes.get(drill);
                    if (buildings != null && !buildings.isEmpty()) {
                        teamData.buildingTypes.get(drill).each(building -> {
                            building.enabled = true;
                        });
                    }
                });
            });
        });

        thread.setPriority(Thread.NORM_PRIORITY - 1);
        thread.setDaemon(true);
        thread.start();
    }

    public static void countOreForDrill(Drill drill, Tile tile){
        returnItem = null;
        returnCount = 0;

        oreCount.clear();
        itemArray.clear();

        for(Tile other : tile.getLinkedTilesAs(drill, tempTiles)){
            if(drill.canMine(other)){
                if (other.overlay() instanceof OreVein ore) {
                    oreCount.increment(drill.getDrop(other), 0, ore.density);
                }else {
                    oreCount.increment(drill.getDrop(other), 0, 1f);
                }
            }
        }

        for(Item item : oreCount.keys()){
            itemArray.add(item);
        }

        itemArray.sort((item1, item2) -> {
            int type = Boolean.compare(!item1.lowPriority, !item2.lowPriority);
            if(type != 0) return type;
            int amounts = Integer.compare(Mathf.round(oreCount.get(item1, 0)), Mathf.round(oreCount.get(item2, 0)));
            if(amounts != 0) return amounts;
            return Integer.compare(item1.id, item2.id);
        });

        if(itemArray.size == 0){
            return;
        }

        returnItem = itemArray.peek();
        returnCount = Mathf.round(oreCount.get(itemArray.peek(), 0));
    }

    public static void updateDrillStat(Drill drill, Tile tile){
        Reflect.set(Drill.class, drill, "returnItem", returnItem);
        Reflect.set(Drill.class, drill, "returnCount", 100);
        drill.drawPlaceText(returnCount + "", tile.x, tile.y + 1, true);
    }

    public static void updateDrill(Drill.DrillBuild building){
        building.dominantItems = returnCount;
        building.dominantItem = returnItem;
    }
}
