package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.game.Team;
import mindustry.ui.Styles;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.cutscene.types.AlertType;
import newhorizon.expand.logic.cutscene.types.HudIcon;
import newhorizon.expand.logic.cutscene.types.RaidControllerType;
import newhorizon.expand.logic.cutscene.types.RaidPreset;

public class EventRaid extends ActionLStatement {
    public RaidControllerType type = RaidControllerType.defaultController;
    public String flag = "raid-executor", timer = "raid-timer";

    public String alertTime = "15", raidTime = "5";

    public RaidPreset raidPreset = RaidPreset.PRESET_RAID_0;

    public HudIcon hudIcon = HudIcon.defaultRaid;
    public AlertType warningSound = AlertType.alarm;
    public String warningText = "default_raid_text";

    public Team team;
    public int bulletType = 0, bulletCount = 5;
    public float sourceX = 0, sourceY = 0, targetX = 0, targetY = 0;
    public float inaccuracy = 40f;

    public String duration = "2";

    public EventRaid(String[] tokens) {}

    public EventRaid() {}

    public String getLStatementName() {
        return "raidevent";
    }

    @Override
    public void build(Table table) {
        rebuild(table);
    }

    public void rebuild(Table table) {
        table.clearChildren();

        buildRowTable(table, t -> {
            t.add(" Raid Controller Type: ");
            t.button(b -> {
                b.label(() -> type.name());
                b.clicked(() -> showSelect(b, RaidControllerType.all, type, cType -> {
                    type = cType;

                    switch (type) {
                        case defaultController -> raidPreset = RaidPreset.PRESET_RAID_0;
                    }

                    rebuild(table);
                }, 2, cell -> cell.size(320, 50)));
            }, Styles.logict, () -> {}).size(320, 40).color(table.color).left().padLeft(2);
        });

        buildRowTable(table, t -> {
            t.add(" Objective Config: < Flag : ");
            fields(t, flag, str -> flag = str).width(180f);
            t.add(" , Objective Flag : ");
            fields(t, timer, str -> timer = str).width(180f);
            t.add(" > ");
        });

        Runnable buildBulletType = () -> {
            buildRowTable(table, t -> {
                t.add(" Raid Bullet Type: ");
                t.button(b -> {
                    b.label(() -> type.name());
                    b.clicked(() -> showSelect(b, RaidPreset.all, raidPreset, item -> raidPreset = item, 1, cell -> cell.size(220, 50)));
                }, Styles.logict, () -> {}).size(220, 40).color(table.color).left().padLeft(2);
            });
        };

        Runnable buildBulletConfig = () -> {
            buildRowTable(table, t -> {

            });
        };

        switch (type) {
            case defaultController -> buildBulletType.run();
            case customController -> {
                buildBulletType.run();
            }


        }
    }
}
