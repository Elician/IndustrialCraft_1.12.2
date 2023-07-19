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
public class ItemArmorEnergypack extends ItemArmorElectric implements IBatpack {
  public ItemArmorEnergypack(int capacity) {
    super(ItemName.energy_pack, ArmorMaterial.LEATHER, "energypack", EntityEquipmentSlot.CHEST, capacity, 6000, 3);
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
