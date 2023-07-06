//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor;

import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class ItemArmorBatpack extends ItemArmorElectric {
  public ItemArmorBatpack() {
    super(ItemName.batpack, ArmorMaterial.LEATHER, "batpack", EntityEquipmentSlot.CHEST, 120000.0, 512.0, 1);
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
