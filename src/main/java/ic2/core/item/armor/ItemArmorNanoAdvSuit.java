package ic2.core.item.armor;

import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ItemArmorNanoAdvSuit extends ItemArmorNanoSuit {
    public ItemArmorNanoAdvSuit(ItemName name, EntityEquipmentSlot armorType) {
        super(name, "nano_adv", armorType, 6000000, 6000);
    }

    @Override
    public double getDamageAbsorptionRatio() {
        return 0.95;
    }

    @Override
    public int getEnergyPerDamage() {
        return 3400;
    }
}
