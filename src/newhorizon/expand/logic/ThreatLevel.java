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
        threatMap.put(1, Seq.with(Items.copper, Items.lead, Items.sand, Items.beryllium, NHItems.silicar));
        threatMap.put(2, Seq.with(NHItems.hardLight, Items.graphite, Items.silicon, Items.titanium, Items.scrap, Items.coal));
        threatMap.put(3, Seq.with(Items.metaglass, Items.tungsten, Items.pyratite, Items.sporePod));
        threatMap.put(4, Seq.with(NHItems.presstanium, Items.thorium,NHItems.juniorProcessor, Items.blastCompound, Items.oxide));
        threatMap.put(5, Seq.with(NHItems.zeta, Items.carbide, NHItems.multipleSteel,Items.plastanium, NHItems.metalOxhydrigen, NHItems.fusionEnergy,Items.fissileMatter ));
        threatMap.put(6, Seq.with( NHItems.thermoCorePositive, NHItems.thermoCoreNegative));
        threatMap.put(7, Seq.with(Items.phaseFabric, Items.surgeAlloy));
        threatMap.put(8, Seq.with(NHItems.seniorProcessor));
        threatMap.put(9, Seq.with(NHItems.irayrondPanel, NHItems.setonAlloy));
        threatMap.put(10, Seq.with(NHItems.ancimembrane, NHItems.nodexPlate, NHItems.darkEnergy));
        threatMap.put(12, Seq.with(NHItems.hyperProcessor, NHItems.hadronicomp));
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
