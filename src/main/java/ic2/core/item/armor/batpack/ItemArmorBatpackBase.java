package ic2.core.item.armor.batpack;

import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class ItemArmorBatpackBase extends ItemArmorElectric implements IBatpack {

  public ItemArmorBatpackBase(ItemName item, ArmorMaterial material, String name, int capacity, double transferLimit, int tier) {
    super(item, material, name, EntityEquipmentSlot.CHEST, capacity, transferLimit, tier);
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
