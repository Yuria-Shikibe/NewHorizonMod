package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.game.Team;
import mindustry.ui.Styles;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.cutscene.types.AlertType;
import newhorizon.expand.logic.cutscene.types.HudIcon;
import newhorizon.expand.logic.cutscene.types.RaidControllerType;

public class EventRaid extends ActionLStatement {
    public RaidControllerType type = RaidControllerType.defaultController;
    public String flag = "raid-executor", timer = "raid-timer";

    public String alertTime = "15", raidTime = "5";

    public String raidType = "PRESET_RAID_0";

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
            t.add(" Controller Type: ");
            t.button(b -> {
                b.label(() -> type.name());
                b.clicked(() -> showSelect(b, RaidControllerType.all, type, s -> {
                    type = s;
                    rebuild(table);
                }, 2, cell -> cell.size(320, 50)));
            }, Styles.logict, () -> {}).size(320, 40).color(table.color).left().padLeft(2);
        });

        buildRowTable(table, t -> {
            t.add(" Duration: ");
            fields(t, duration, str -> duration = str);
        });
    }
}
