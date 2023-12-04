package ic2.core.item.armor.batpack;

import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@NotClassic
public class ItemArmorAdvBatpack extends ItemArmorBatpackBase {
  public ItemArmorAdvBatpack(int capacity) {
    super(ItemName.advanced_batpack, ArmorMaterial.LEATHER, "advbatpack", capacity, 4096.0, 2);
  }
}
