package newhorizon.expand.entities;

import arc.struct.Seq;
import newhorizon.expand.block.defence.QuantumVortexProjector;
import mindustry.gen.Building;
import mindustry.game.Team;

public class SharedShieldFields {
    private static final Seq<SharedShieldField> fields = new Seq<>(true, 16, SharedShieldField.class);
    private static boolean topologyDirty;

    /** Marks the spatial index for a deferred merge pass. */
    public static void markDirty() {
        topologyDirty = true;
    }

    public static SharedShieldField find(Building source) {
        if (source == null) return null;

        // Reuse an existing field and merge every overlapping field into it. This is
        // important after world reloads where transient Building.field references are lost.
        SharedShieldField result = null;
        for (int i = 0; i < fields.size; i++) {
            SharedShieldField field = fields.get(i);
            if (field.hasSource(source)) {
                result = field;
                break;
            }
        }

        // An unpowered projector keeps no active field of its own. If it was
        // already registered, return the old reference so its caller can
        // remove it cleanly; otherwise wait until power is available before
        // creating or joining a shared field.
        if (source.efficiency <= 0.01f) return result;
        // Attach a newly placed projector directly to an already overlapping
        // field. Creating a temporary one-projector field first can make the
        // topology rebuild select that empty field as its state owner, which
        // resets buildup and the broken cooldown.
        if (result == null) {
            for (int i = 0; i < fields.size; i++) {
                SharedShieldField field = fields.get(i);
                if (field.sameTeam(source) && field.overlaps(source)) {
                    result = field;
                    result.add(source);
                    break;
                }
            }
        }
        if (result == null) {
            result = new SharedShieldField();
            fields.add(result);
            result.add(source);
            topologyDirty = true;
        }

        // A powered field is connected by geometric range overlap; warmup does
        // not affect the configured connection radius. Merge transitively so
        // A-B and B-C produce one shared field even when A-C do not overlap.
        boolean merged;
        do {
            merged = false;
            for (int i = fields.size - 1; i >= 0; i--) {
                SharedShieldField other = fields.get(i);
                if (other == result || !sameTeam(result, other)) continue;
                if (overlaps(result, other)) {
                    merge(result, other);
                    merged = true;
                }
            }
        } while (merged);

        assignBuildingFields(result);
        return result;
    }

    private static boolean sameTeam(SharedShieldField a, SharedShieldField b) {
        Team teamA = a.team(), teamB = b.team();
        if (teamA == null || teamA != teamB) return false;

        // Do not merge a field that still contains a stale source from a
        // different team. Such a source can remain for a tick when a building
        // is converted or loaded while the topology index is dirty.
        for (Building source : a.iterable()) {
            if (source.isValid() && source.isAdded() && source.team != teamA) return false;
        }
        for (Building source : b.iterable()) {
            if (source.isValid() && source.isAdded() && source.team != teamA) return false;
        }
        return true;
    }

    private static boolean overlaps(SharedShieldField a, SharedShieldField b) {
        for (Building source : a.iterable()) {
            if (b.overlaps(source)) return true;
        }
        return false;
    }

    private static void merge(SharedShieldField target, SharedShieldField source) {
        // Keep the larger state when joining components. In multiplayer each
        // source can receive the same authoritative shared value before the
        // topology merge; summing here would double-count that value.
        target.buildup = Math.max(target.buildup, source.buildup);
        target.broken |= source.broken;
        target.setCooldownProgress(Math.max(target.cooldownProgress(), source.cooldownProgress()));
        for (Building building : source.iterable()) target.add(building);
        target.buildup = Math.min(target.buildup, Math.max(target.capacity(), 0f));
        source.clear();
        fields.remove(source, true);
        assignBuildingFields(target);
    }

    private static void assignBuildingFields(SharedShieldField field) {
        for (Building building : field.iterable()) {
            if (building instanceof QuantumVortexProjector.QuantumBuild quantum) quantum.field = field;
        }
    }

    public static void remove(SharedShieldField field) {
        fields.remove(field);
        topologyDirty = true;
    }

    public static void update() {
        // Projectors are stationary in normal play, so only rescan topology after a
        // source/field is added or removed. This avoids an O(n^2) pass every frame.
        if (topologyDirty) {
            mergeOverlappingFields();
            topologyDirty = false;
        }
        for (int i = fields.size - 1; i >= 0; i--) {
            if (i >= fields.size) continue;

            SharedShieldField field = fields.get(i);
            field.update();

            if (field.empty()) {
                fields.remove(field, true);
            }
        }
    }

    private static void mergeOverlappingFields() {
        // Rebuild connected components whenever topology changes. This also
        // splits a former bridge field after its middle projector is removed;
        // a merge-only pass would leave the two remaining islands connected.
        Seq<Building> remaining = new Seq<>(false, 16, Building.class);
        Seq<SharedShieldField> oldFields = new Seq<>(false, 16, SharedShieldField.class);
        for (SharedShieldField field : fields) {
            oldFields.add(field);
            for (Building source : field.iterable()) {
                if (source.isValid() && source.isAdded() && source.efficiency > 0.01f && !remaining.contains(source, true)) {
                    remaining.add(source);
                }
            }
        }

        fields.clear();
        Seq<SharedShieldField> reused = new Seq<>(false, 16, SharedShieldField.class);
        while (!remaining.isEmpty()) {
            Building seed = remaining.remove(remaining.size - 1);
            Seq<Building> component = new Seq<>(false, 8, Building.class);
            component.add(seed);

            boolean expanded;
            do {
                expanded = false;
                for (int i = remaining.size - 1; i >= 0; i--) {
                    Building candidate = remaining.get(i);
                    boolean connected = false;
                    for (Building member : component) {
                        if (sameTeam(member, candidate) && SharedShieldField.projectorsOverlap(member, candidate)) {
                            connected = true;
                            break;
                        }
                    }
                    if (connected) {
                        component.add(candidate);
                        remaining.remove(i);
                        expanded = true;
                    }
                }
            } while (expanded);

            SharedShieldField target = null;
            for (Building source : component) {
                if (!(source instanceof QuantumVortexProjector.QuantumBuild quantum)) continue;
                SharedShieldField previous = quantum.field;
                if (previous == null || !oldFields.contains(previous, true) || reused.contains(previous, true)) continue;
                // Prefer the previous field carrying the most sources/state.
                // This keeps damaged/broken cooldown state when a new source
                // was temporarily registered as a separate field.
                if (target == null || previousStateWeight(previous) > previousStateWeight(target)) target = previous;
            }
            if (target == null) target = new SharedShieldField();
            target.clear();
            for (Building source : component) target.add(source);
            reused.add(target);
            fields.add(target);
            assignBuildingFields(target);
        }
    }

    private static float previousStateWeight(SharedShieldField field) {
        float weight = field.sourceCount();
        if (field.broken) weight += 100000f;
        weight += field.buildup * 0.001f;
        weight += field.cooldownProgress() * 0.001f;
        return weight;
    }

    private static boolean sameTeam(Building a, Building b) {
        return a != null && b != null && a.team != null && a.team == b.team;
    }

    public static void clearWorld() {
        // Buildings cache their transient field reference. Clear each source
        // list as well, otherwise a post-reload building can retain an
        // apparently valid field that is no longer in this registry.
        for (SharedShieldField field : fields) {
            for (Building source : field.iterable()) {
                if (source instanceof QuantumVortexProjector.QuantumBuild quantum) quantum.field = null;
            }
            field.clear();
        }
        fields.clear();
        topologyDirty = false;
    }

    public static Iterable<SharedShieldField> all() {
        return fields;
    }
}
