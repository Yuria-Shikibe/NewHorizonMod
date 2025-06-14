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
        threatMap.put(0, Seq.with(Items.sand, Items.scrap, Items.sporePod));
        threatMap.put(1, Seq.with(Items.copper, Items.lead, Items.coal));
        threatMap.put(2, Seq.with(Items.metaglass, Items.graphite, Items.silicon));
        threatMap.put(3, Seq.with(Items.thorium, Items.beryllium, Items.blastCompound, Items.pyratite));
        threatMap.put(4, Seq.with(NHItems.presstanium, NHItems.juniorProcessor, Items.thorium, Items.tungsten));
        threatMap.put(5, Seq.with(NHItems.multipleSteel, NHItems.zeta, Items.plastanium, Items.oxide));
        threatMap.put(6, Seq.with(NHItems.fusionEnergy, NHItems.metalOxhydrigen, Items.phaseFabric, Items.surgeAlloy, Items.carbide));
        threatMap.put(7, Seq.with(NHItems.seniorProcessor, NHItems.irayrondPanel));
        threatMap.put(8, Seq.with(NHItems.thermoCorePositive, NHItems.thermoCoreNegative, NHItems.setonAlloy));
        threatMap.put(9, Seq.with(NHItems.darkEnergy));
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
