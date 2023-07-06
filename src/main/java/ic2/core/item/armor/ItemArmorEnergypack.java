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
public class ItemArmorEnergypack extends ItemArmorElectric {
  public ItemArmorEnergypack() {
    super(ItemName.energy_pack, ArmorMaterial.DIAMOND, "energypack", EntityEquipmentSlot.CHEST, 8000000.0, 6144.0, 3);
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
