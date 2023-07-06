//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor;

import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemArmorAdvBatpack extends ItemArmorElectric {
  public ItemArmorAdvBatpack() {
    super(ItemName.advanced_batpack, ArmorMaterial.IRON, "advbatpack", EntityEquipmentSlot.CHEST, 2000000.0, 4096.0, 2);
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
