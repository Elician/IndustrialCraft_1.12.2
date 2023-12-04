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
public class ItemArmorEnergypack extends ItemArmorBatpackBase {
  public ItemArmorEnergypack(int capacity) {
    super(ItemName.energy_pack, ArmorMaterial.LEATHER, "energypack", capacity, 6000, 3);
  }
}
