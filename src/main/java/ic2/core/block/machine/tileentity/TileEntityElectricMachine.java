
package ic2.core.block.machine.tileentity;

import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.block.invslot.InvSlot.InvSide;
import net.minecraft.util.EnumFacing;

public abstract class TileEntityElectricMachine extends TileEntityInventory implements IEnergyReceiver {
    protected final Energy energy;
    public final InvSlotDischarge dischargeSlot;

    public TileEntityElectricMachine(int maxEnergy, int tier) {
        this(maxEnergy, tier, true);
    }

    public TileEntityElectricMachine(int maxEnergy, int tier, boolean allowRedstone) {
        this.dischargeSlot = new InvSlotDischarge(this, Access.NONE, tier, allowRedstone, InvSide.ANY);
        this.energy = this.addComponent(Energy.asBasicSink(this, maxEnergy, tier).addManagedSlot(this.dischargeSlot));
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
        return this.energy.getCapacity() > 0;
    }

}
