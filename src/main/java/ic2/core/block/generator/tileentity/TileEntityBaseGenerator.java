//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.generator.tileentity;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityBaseGenerator extends TileEntityInventory implements IEnergyProvider, IHasGui {
    public final InvSlotCharge chargeSlot;
    protected final Energy energy;
    @GuiSynced
    public int fuel = 0;
    protected double production;
    private int ticksSinceLastActiveUpdate;
    private int activityMeter = 0;
    public AudioSource audioSource;

    public TileEntityBaseGenerator(double production, int tier, int maxStorage) {
        this.production = production;
        this.ticksSinceLastActiveUpdate = IC2.random.nextInt(256);
        this.chargeSlot = new InvSlotCharge(this, 1);
        this.energy = this.addComponent(Energy.asBasicSource(this, maxStorage, tier).addManagedSlot(this.chargeSlot));
    }

    @Override
    public int extractEnergy(EnumFacing enumFacing, int maxExtract, boolean simulate) {
        int energyExtracted = Math.min((int) this.energy.getEnergy(), maxExtract);

        this.energy.useEnergy(energyExtracted, simulate);

        return energyExtracted;
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

    public void read(NBTTagCompound nbttagcompound) {
        super.read(nbttagcompound);
        this.fuel = nbttagcompound.getInt("fuel");
    }

    public NBTTagCompound write(NBTTagCompound nbt) {
        super.write(nbt);
        nbt.putInt("fuel", this.fuel);
        return nbt;
    }

    protected void onUnloaded() {
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }

        super.onUnloaded();
    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.needsFuel()) {
            needsInvUpdate = this.gainFuel();
        }

        boolean newActive = this.gainEnergy();
        if (needsInvUpdate) {
            this.markDirty();
        }

        if (!this.delayActiveUpdate()) {
            this.setActive(newActive);
        } else {
            if (this.ticksSinceLastActiveUpdate % 256 == 0) {
                this.setActive(this.activityMeter > 0);
                this.activityMeter = 0;
            }

            if (newActive) {
                ++this.activityMeter;
            } else {
                --this.activityMeter;
            }

            ++this.ticksSinceLastActiveUpdate;
        }

        for (EnumFacing facing : EnumFacing.values()) {

            BlockPos pos = this.pos.offset(facing);
            TileEntity te = this.getWorld().getTileEntity(pos);
            if (te == null) continue;
            if (te instanceof IEnergyReceiver) {
                IEnergyReceiver rcv = (IEnergyReceiver) te;
                if (rcv.canConnectEnergy(facing.getOpposite()) && this.energy.getEnergy() > 0) {
                    int maxAmount = rcv.receiveEnergy(facing.getOpposite(), (int) this.energy.getEnergy(), false);
                    this.energy.useEnergy(maxAmount);
                }
            }
        }

    }

    public boolean gainEnergy() {
        if (this.isConverting()) {
            this.energy.addEnergy(this.production);
            --this.fuel;
            return true;
        } else {
            return false;
        }
    }

    public boolean isConverting() {
        return !this.needsFuel() && this.energy.getFreeEnergy() >= this.production;
    }

    public boolean needsFuel() {
        return this.fuel <= 0 && this.energy.getFreeEnergy() >= this.production;
    }

    public abstract boolean gainFuel();

    public String getOperationSoundFile() {
        return null;
    }

    protected boolean delayActiveUpdate() {
        return false;
    }

    public void onGuiClosed(EntityPlayer player) {
    }

    public ContainerBase<? extends TileEntityBaseGenerator> getGuiContainer(EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
        return DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }

    public void onNetworkUpdate(String field) {
        if (field.equals("active")) {
            if (this.audioSource == null && this.getOperationSoundFile() != null) {
                this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, this.getOperationSoundFile(), true, false, IC2.audioManager.getDefaultVolume());
            }

            if (this.getActive()) {
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
            } else if (this.audioSource != null) {
                this.audioSource.stop();
            }
        }

        super.onNetworkUpdate(field);
    }
}
