package newhorizon.expand.units.probe;

import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.IntSet;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.ai.RtsAI;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;
import mindustry.entities.units.AIController;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Unit;

final class ProbeRtsTeam {
    private final Teams.TeamData data;
    private final RtsAI ai;
    private final IntSet members = new IntSet();
    private final Interval timer = new Interval();

    ProbeRtsTeam(Team team) {
        data = new Teams.TeamData(team);
        data.unitTree = new QuadTree<>(Vars.world.getQuadBounds(new Rect()));
        ai = new RtsAI(data);
    }

    void add(Unit unit) {
        if (!members.add(unit.id())) return;
        data.units.add(unit);
        if (unit.controller() instanceof AIController) {
            CommandAI command = new CommandAI();
            unit.controller(command);
            command.command = UnitCommand.moveCommand;
        }
    }

    void update() {
        if (!timer.get(0, 120f)) return;
        data.unitTree.clear();
        for (int index = data.units.size - 1; index >= 0; index--) {
            Unit unit = data.units.get(index);
            if (!unit.isValid() || unit.team != data.team) {
                members.remove(unit.id());
                data.units.remove(index);
            } else if (unit.controller() instanceof CommandAI) {
                data.unitTree.insert(unit);
            }
        }
        if (data.units.isEmpty()) return;

        Rules.TeamRule rules = data.team.rules();
        int minSquad = rules.rtsMinSquad;
        int maxSquad = rules.rtsMaxSquad;
        float minWeight = rules.rtsMinWeight;
        rules.rtsMinSquad = 5;
        rules.rtsMaxSquad = 20;
        rules.rtsMinWeight = Math.min(minWeight, 0.1f);
        try {
            ai.update();
        } finally {
            rules.rtsMinSquad = minSquad;
            rules.rtsMaxSquad = maxSquad;
            rules.rtsMinWeight = minWeight;
        }
    }
}
