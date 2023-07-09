//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class BaseElectricItem extends ItemIC2 implements IEnergyContainerItem, IPseudoDamageItem, IElectricItem, IItemHudInfo {
    public static final boolean logIncorrectItemDamaging = ConfigUtil.getBool(MainConfig.get(), "debug/logIncorrectItemDamaging");
    protected final double maxCharge;
    protected final double transferLimit;
    protected final int tier;

    public BaseElectricItem(ItemName name, double maxCharge, double transferLimit, int tier) {
        super(name);
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
        this.func_77656_e(27);
        this.func_77625_d(1);
        this.setNoRepair();
    }

    public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        double energyReceived = Math.min(this.maxCharge - this.getEnergyStored(stack), maxReceive);

        if (!simulate) {
            ElectricItem.manager.charge(stack, energyReceived, maxReceive, true, false);
        }

        return (int) energyReceived;
    }

    public int extractEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        double energyCost = Math.min(this.getEnergyStored(stack), maxReceive);

        if (!simulate) {
            ElectricItem.manager.discharge(stack, energyCost, Integer.MAX_VALUE, true, false, false);
        }

        return (int) energyCost;
    }

    public int getEnergyStored(ItemStack stack) {
        return (int) ElectricItem.manager.getCharge(stack);
    }

    public int getMaxEnergyStored(ItemStack stack) {
        return (int) this.maxCharge;
    }

    public boolean canProvideEnergy(ItemStack stack) {
        return false;
    }

    public double getMaxCharge(ItemStack stack) {
        return this.maxCharge;
    }

    public int getTier(ItemStack stack) {
        return this.tier;
    }

    public double getTransferLimit(ItemStack stack) {
        return this.transferLimit;
    }

    public List<String> getHudInfo(ItemStack stack, boolean advanced) {
        List<String> info = new LinkedList<>();
        info.add(ElectricItem.manager.getToolTip(stack));
        return info;
    }

    public void fillItemGroup(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (this.isInGroup(tab)) {
            ElectricItemManager.addChargeVariants(this, subItems);
        }
    }

    public void setDamage(ItemStack stack, int damage) {
        int prev = this.getDamage(stack);
        if (damage != prev && logIncorrectItemDamaging) {
            IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", new Object[]{damage - prev});
        }

    }

    public void setStackDamage(ItemStack stack, int damage) {
        super.setDamage(stack, damage);
    }
}
