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
public class ItemArmorAdvEnergypack extends ItemArmorBatpackBase {
    public ItemArmorAdvEnergypack(int capacity) {
        super(ItemName.advanced_energypack, ArmorMaterial.LEATHER, "advenergypack", capacity, 4096.0, 4);
    }
}
