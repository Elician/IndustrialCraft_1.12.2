//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor.batpack;

import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class ItemArmorBatpack extends ItemArmorElectric {
  public ItemArmorBatpack(int capacity) {
    super(ItemName.batpack, ArmorMaterial.LEATHER, "batpack", EntityEquipmentSlot.CHEST, capacity, 512.0, 1);
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
