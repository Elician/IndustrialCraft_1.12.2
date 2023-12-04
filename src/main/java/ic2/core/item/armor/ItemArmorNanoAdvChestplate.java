package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IC2Items;
import ic2.core.item.armor.batpack.IBatpack;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemArmorNanoAdvChestplate extends ItemArmorJetpackAdvElectric implements IBatpack {
    protected static final ItemStack WATER_CELL = IC2Items.getItem("fluid_cell", "water");
    protected static final ItemStack EMPTY_CELL = IC2Items.getItem("fluid_cell");
    protected byte ticker;

    public ItemArmorNanoAdvChestplate(ItemName item) {
        super(item, "advnanochestplate");
    }

    public void onArmorTick(World world, EntityPlayer player, ItemStack armour) {
        super.onArmorTick(world, player, armour);
        byte var10002 = this.ticker;
        this.ticker = (byte)(var10002 + 1);
        if (var10002 % 20 == 0 && player.isBurning() && ElectricItem.manager.canUse(armour, 50000.0)) {

            for (ItemStack stack : player.inventory.mainInventory) {
                if (!StackUtil.isEmpty(stack) && StackUtil.checkItemEquality(WATER_CELL, stack.copy()) && StackUtil.storeInventoryItem(EMPTY_CELL, player, false)) {
                    stack.shrink(1);
                    ElectricItem.manager.discharge(stack, 50000.0, Integer.MAX_VALUE, true, false, false);
                    player.extinguish();
                    break;
                }
            }
        }

    }

    public int getEnergyPerDamage() {
        return 3400;
    }

    public double getDamageAbsorptionRatio() {
        return 0.95;
    }
}
