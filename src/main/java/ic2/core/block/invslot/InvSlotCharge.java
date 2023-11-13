
package ic2.core.block.invslot;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.energy.tile.IChargingSlot;
import ic2.api.item.ElectricItem;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.block.invslot.InvSlot.InvSide;
import ic2.core.util.StackUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InvSlotCharge extends InvSlot implements IChargingSlot {
  public int tier;

  public InvSlotCharge(IInventorySlotHolder<?> base1, int tier) {
    super(base1, "charge", Access.IO, 1, InvSide.TOP);
    this.tier = tier;
  }

  public boolean accepts(ItemStack stack) {
    return stack.getItem() instanceof IEnergyContainerItem;
  }

  public double charge(double amount) {
    if (amount <= 0.0) {
      throw new IllegalArgumentException("Amount must be > 0.");
    } else {
      ItemStack stack = this.get(0);

      if (StackUtil.isEmpty(stack)) return 0.0;

      IEnergyContainerItem item = (IEnergyContainerItem) stack.getItem();
      return item.receiveEnergy(stack, (int) amount, false);
    }
  }

  public void setTier(int tier1) {
    this.tier = tier1;
  }
}
