package ic2.core.item.utils;

import cofh.redstoneflux.api.IEnergyContainerItem;
import net.minecraft.item.ItemStack;

public class EnergyHelper {
  public static boolean isEnergyContainerItem(ItemStack container) {

    return !container.isEmpty() && (container.getItem() instanceof IEnergyContainerItem);
  }
}
