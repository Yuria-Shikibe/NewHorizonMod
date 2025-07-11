package newhorizon.expand.logic;

import arc.struct.IntMap;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.type.Item;
import newhorizon.content.NHItems;

public class ThreatLevel {
    //a threat map for automatic threat.
    public static IntMap<Seq<Item>> threatMap = new IntMap<>();

    public static void init() {
        threatMap.clear();
        threatMap.put(1, Seq.with(Items.copper, Items.lead));
        threatMap.put(2, Seq.with(Items.graphite, Items.silicon, Items.beryllium));
        threatMap.put(3, Seq.with(Items.metaglass, Items.titanium));
        threatMap.put(4, Seq.with(NHItems.presstanium, NHItems.juniorProcessor));
        threatMap.put(5, Seq.with(Items.thorium, Items.plastanium, NHItems.metalOxhydrigen));
        threatMap.put(6, Seq.with(NHItems.zeta, Items.carbide));
        threatMap.put(7, Seq.with(NHItems.multipleSteel, Items.phaseFabric, Items.surgeAlloy));
        threatMap.put(8, Seq.with(NHItems.seniorProcessor));
        threatMap.put(9, Seq.with(NHItems.irayrondPanel, NHItems.setonAlloy));
        threatMap.put(10, Seq.with(NHItems.ancimembrane, NHItems.upgradeSort));
    }

    public static int getTeamThreat(Team team) {
        int threat = 0;
        for (IntMap.Entry<Seq<Item>> entry : threatMap) {
            for (Item item : entry.value) {
                if (team.items().has(item)) {
                    threat = Math.max(entry.key, threat);
                }
            }
        }
        return threat;
    }
}
