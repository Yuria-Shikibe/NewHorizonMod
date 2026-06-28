package newhorizon.expand.game;

import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.type.Item;
import mindustry.type.UnitType;
import newhorizon.expand.logic.ThreatLevel;

import static mindustry.Vars.content;

public class DefaultRaidStrength {
    private static final int MAX_TIER = 8;

    public static final int ITEM_AMOUNT_CAP = 10000;
    public static final int UNIT_COUNT_CAP = 100;

    private static final float
            ITEM_WEIGHT = 0.55f,
            UNIT_WEIGHT = 0.45f,
            ITEM_VARIETY_BONUS = 4f,
            UNIT_VARIETY_BONUS = 6f;

    /**
     * Tier lower bounds from calibration: when threat milestone x = tier + 2,
     * every item type with threat <= x - 2 contributes at ITEM_AMOUNT_CAP each (with variety bonus).
     */
    private static final float[] TIER_MIN = {
            0f,
            0f,
            300f,
            800f,
            1200f,
            1800f,
            2800f,
            3600f,
            5600f
    };

    public static int maxTier() {
        return MAX_TIER;
    }

    public static float evaluate(Team team) {
        if (team == null) return 0f;
        return evaluateCoreItems(team) * ITEM_WEIGHT + evaluateUnits(team) * UNIT_WEIGHT;
    }

    public static float evaluateCoreItems(Team team) {
        if (team == null) return 0f;
        float sum = 0f;
        int varieties = 0;

        for (Item item : content.items()) {
            if (item == null) continue;

            int[] total = {0};
            team.cores().each(core -> total[0] += core.items.get(item));
            if (total[0] <= 0) continue;

            varieties++;
            sum += Mathf.sqrt(cappedAmount(total[0])) * itemWeight(item);
        }

        if (varieties > 1) sum += Mathf.sqrt(varieties) * ITEM_VARIETY_BONUS;
        return sum;
    }

    public static float evaluateUnits(Team team) {
        if (team == null) return 0f;

        IntMap<Integer> counts = new IntMap<>();
        Groups.unit.each(u -> {
            if (u == null || !u.isValid() || u.team != team || u.type == null) return;
            int id = u.type.id;
            counts.put(id, counts.get(id, 0) + 1);
        });

        float sum = 0f;
        for (IntMap.Entry<Integer> entry : counts) {
            UnitType type = content.unit(entry.key);
            if (type == null) continue;
            sum += Mathf.sqrt(cappedCount(entry.value)) * unitWeight(type);
        }

        if (counts.size > 1) sum += Mathf.sqrt(counts.size) * UNIT_VARIETY_BONUS;
        return sum;
    }

    public static int toTier(Team team) {
        return toTier(team, MAX_TIER);
    }

    public static int toTier(Team team, int maxTier) {
        return scoreToTier(evaluate(team), maxTier);
    }

    public static int scoreToTier(float score) {
        return scoreToTier(score, MAX_TIER);
    }

    public static int scoreToTier(float score, int maxTier) {
        if (score <= 0f || maxTier <= 1) return 1;
        maxTier = Mathf.clamp(maxTier, 1, MAX_TIER);
        for (int tier = maxTier; tier >= 1; tier--) {
            if (score >= tierMin(tier)) return tier;
        }
        return 1;
    }

    public static float tierMin(int tier) {
        tier = Mathf.clamp(tier, 1, MAX_TIER);
        return TIER_MIN[tier];
    }

    public static float nextTierMin(int currentTier) {
        if (currentTier >= MAX_TIER) return -1f;
        return tierMin(currentTier + 1);
    }

    public static float nextTierMin(Team team) {
        return nextTierMin(toTier(team));
    }

    public static float itemWeight(Item item) {
        int threat = 0;
        for (IntMap.Entry<Seq<Item>> entry : ThreatLevel.threatMap) {
            if (entry.value.contains(item)) threat = Math.max(threat, entry.key);
        }
        if (threat > 0) return threat;
        return Mathf.sqrt(Math.max(item.cost, 1f)) * 0.15f;
    }

    private static int cappedAmount(int amount) {
        return Math.min(Math.max(amount, 0), ITEM_AMOUNT_CAP);
    }

    private static int cappedCount(int count) {
        return Math.min(Math.max(count, 0), UNIT_COUNT_CAP);
    }

    private static float unitWeight(UnitType type) {
        if (type.internal) return 0.5f;
        float power = type.health * 0.002f;
        if (type.weapons.size > 0 && type.weapons.first().bullet != null) {
            power += type.weapons.first().bullet.damage * 0.01f;
        }
        return power;
    }
}
