package ic2.core.item.armor.batpack;

import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemArmorAdvBatpack extends ItemArmorElectric implements IBatpack {
  public ItemArmorAdvBatpack(int capacity) {
    super(ItemName.advanced_batpack, ArmorMaterial.LEATHER, "advbatpack", EntityEquipmentSlot.CHEST, capacity, 4096.0, 2);
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
