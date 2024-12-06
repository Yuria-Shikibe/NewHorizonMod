package newhorizon.expand.units.unitType;

import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.ammo.ItemAmmoType;
import newhorizon.content.NHItems;

public class NHUnitType extends UnitType {
    public NHUnitType(String name) {
        super(name);

        ammoType = new ItemAmmoType(NHItems.presstanium);
    }

    @Override
    public void init() {
        super.init();
        float maxWeaponRange = 0;
        for (Weapon weapon : weapons) {
            if (weapon.range() > maxWeaponRange) {
                maxWeaponRange = weapon.range();
            }
        }
        fogRadius = maxWeaponRange / 8;
    }

    public void setRequirements(ItemStack[] stacks) {
        cachedRequirements = stacks;
        totalRequirements = firstRequirements = ItemStack.mult(stacks, 1 / 15f);
    }
}
