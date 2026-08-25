package newhorizon.expand.entities;

import arc.struct.Seq;
import mindustry.gen.Building;

public class SharedShieldFields {
    private static final Seq<SharedShieldField> fields = new Seq<>(false, 16);

    public static SharedShieldField find(Building source) {
        for (int i = 0; i < fields.size; i++) {
            SharedShieldField field = fields.get(i);
            if (field.contains(source.x, source.y) || field.hasSource(source)) return field;
        }

        SharedShieldField field = new SharedShieldField();
        field.add(source);
        fields.add(field);
        return field;
    }

    public static void remove(SharedShieldField field) {
        fields.remove(field);
    }

    public static void update() {
        for (int i = fields.size - 1; i >= 0; i--) {
            if (i >= fields.size) continue;

            SharedShieldField field = fields.get(i);
            int beforeSize = fields.size;
            field.update();

            if (field.empty()) {
                for (int j = Math.min(beforeSize, fields.size) - 1; j >= i; j--) {
                    if (fields.items[j] == field) {
                        fields.remove(j);
                        break;
                    }
                }
            }
        }
    }

    public static void clearWorld() {
        fields.clear();
    }
}
