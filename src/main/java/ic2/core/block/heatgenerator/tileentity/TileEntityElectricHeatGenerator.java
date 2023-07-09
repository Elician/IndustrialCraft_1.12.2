//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.heatgenerator.tileentity;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiElectricHeatGenerator;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityElectricHeatGenerator extends TileEntityHeatSourceInventory implements IEnergyReceiver, IHasGui {
    private boolean newActive;
    public final InvSlotDischarge dischargeSlot;
    public final InvSlotConsumable coilSlot;
    protected final Energy energy;
    public static final double outputMultiplier = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/electric");

    public TileEntityElectricHeatGenerator() {
        this.coilSlot = new InvSlotConsumableItemStack(this, "CoilSlot", 10, ItemName.crafting.getItemStack(CraftingItemType.coil));
        this.coilSlot.setStackSizeLimit(1);
        this.dischargeSlot = new InvSlotDischarge(this, Access.NONE, 4);
        this.energy = this.addComponent(Energy.asBasicSink(this, 40000.0, 3).addManagedSlot(this.dischargeSlot));
        this.newActive = false;
    }

    @Override
    public int receiveEnergy(EnumFacing enumFacing, int maxReceive, boolean simulate) {
        int energyReceived = (int) Math.min(this.energy.getCapacity() - this.energy.getEnergy(), maxReceive);

        if (!simulate) {
            this.energy.addEnergy(energyReceived);
        }

        return energyReceived;
    }

    @Override
    public int getEnergyStored(EnumFacing enumFacing) {
        return (int) this.energy.getEnergy();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing enumFacing) {
        return (int) this.energy.getCapacity();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing enumFacing) {
        return true;
    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
        }

    }

    public ContainerBase<TileEntityElectricHeatGenerator> getGuiContainer(EntityPlayer player) {
        return new ContainerElectricHeatGenerator(player, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
        return new GuiElectricHeatGenerator(new ContainerElectricHeatGenerator(player, this));
    }

    public void onGuiClosed(EntityPlayer player) {
    }

    protected int fillHeatBuffer(int maxAmount) {
        int amount = Math.min(maxAmount, (int)(this.energy.getEnergy() / outputMultiplier));
        if (amount > 0) {
            this.energy.useEnergy((double)amount / outputMultiplier);
            this.newActive = true;
        } else {
            this.newActive = false;
        }

        return amount;
    }

    public int getMaxHeatEmittedPerTick() {
        int counter = 0;

        for(int i = 0; i < this.coilSlot.size(); ++i) {
            if (!this.coilSlot.isEmpty(i)) {
                ++counter;
            }
        }

        return counter * 10;
    }

    public final float getChargeLevel() {
        return (float)Math.min(1.0, this.energy.getFillRatio());
    }
}
