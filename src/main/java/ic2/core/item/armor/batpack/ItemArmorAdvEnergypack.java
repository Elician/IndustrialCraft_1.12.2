//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor.batpack;

import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemArmorAdvEnergypack extends ItemArmorElectric implements IBatpack {
    public ItemArmorAdvEnergypack(int capacity) {
        super(ItemName.advanced_energypack, ArmorMaterial.LEATHER, "advenergypack", EntityEquipmentSlot.CHEST, capacity, 4096.0, 4);
    }

    public boolean canProvideEnergy(ItemStack stack) {
        return true;
    }

    public double getDamageAbsorptionRatio() {
        return 0.0;
    }

    public int getEnergyPerDamage() {
        return 0;
    }
}
