package newhorizon.content;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.ctype.UnlockableContent;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import newhorizon.expand.units.unitType.NHUnitType;

public class NHTechTree {
    public static ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();

    public static void load() {
        unitBuildCost.each((u, is) -> {
            if (u instanceof NHUnitType) {
                ((NHUnitType) u).setRequirements(is);
            }
        });

        //TechNode root = nodeRoot("new-horizon", NHPlanets.midantha, () -> {
        //	node(NHSectorPresents.abandonedOutpost, ItemStack.with(), () -> {});
        //});
        //
        //root.planet = NHPlanets.midantha;
        //root.children.each(c -> c.planet = NHPlanets.midantha);
    }

    @SuppressWarnings("all")
    public class TechTreeNodeContent extends StatusEffect {
        public Seq<UnlockableContent> unlockables;
        public TechTreeNodeContent(String name) {
            super(name);
        }

        @Override
        public void onUnlock() {
            unlockables.each(UnlockableContent::quietUnlock);
        }
    }
}
