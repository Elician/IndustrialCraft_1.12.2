package ic2.core.item;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBattery extends BaseElectricItem {
    private static int maxLevel = 4;

    public ItemBattery(ItemName name, double maxCharge, double transferLimit, int tier) {
        super(name, maxCharge, transferLimit, tier);
        this.func_77625_d(16);
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomMeshDefinition(this, stack -> {
            int damage = stack.getDamage();
            int maxDamage = stack.getMaxDamage() - 1;
            int level;
            if (maxDamage > 0) {
                level = Util.limit((damage * ItemBattery.maxLevel + maxDamage / 2) / maxDamage, 0, ItemBattery.maxLevel);
            } else {
                level = 0;
            }

            return ItemIC2.getModelLocation(name, Integer.toString(ItemBattery.maxLevel - level));
        });

        for(int level = 0; level <= maxLevel; ++level) {
            ModelBakery.registerItemVariants(this, getModelLocation(name, Integer.toString(level)));
        }

    }

    public boolean canProvideEnergy(ItemStack stack) {
        return true;
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (!world.isRemote && StackUtil.getSize(stack) == 1) {
//            if (getEnergyStored(stack) > 0.0) {
//                boolean transferred = false;
//
//                for(int i = 0; i < 9; ++i) {
//                    ItemStack target = player.inventory.mainInventory.get(i);
//
//                    if (EnergyHelper.isEnergyContainerItem(target)) {
//                        double transfer = extractEnergy(stack, ((IEnergyContainerItem) target.getItem()).receiveEnergy(target, getEnergyStored(stack), false), false);
//                        if (transfer > 0) {
//                            transferred = true;
//                        }
//                    }
//
//                    if (target != stack && !(ElectricItem.manager.discharge(target, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true, true) > 0.0)) {
//                        double transfer = ElectricItem.manager.discharge(stack, 2.0 * this.transferLimit, Integer.MAX_VALUE, true, true, true);
//                        if (!(transfer <= 0.0)) {
//                            transfer = ElectricItem.manager.charge(target, transfer, this.tier, true, false);
//                            if (!(transfer <= 0.0)) {
//                                ElectricItem.manager.discharge(stack, transfer, Integer.MAX_VALUE, true, true, false);
//                                transferred = true;
//                            }
//                        }
//                    }
//                }
//            }

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        } else {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
    }
}
