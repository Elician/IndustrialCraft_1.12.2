package ic2.core.block.wiring;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.EnergyNet;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.tile.IEnergyStorage;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.block.invslot.InvSlot.InvSide;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.ref.TeBlock.DefaultDrop;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityElectricBlock extends TileEntityInventory implements ITickable, IEnergyProvider, IHasGui, INetworkClientTileEntityEventListener, IEnergyStorage {
  protected double output;
  public byte redstoneMode = 0;
  public static byte redstoneModes = 7;
  public final InvSlotCharge chargeSlot;
  public final InvSlotDischarge dischargeSlot;
  public final Energy energy;
  public final Redstone redstone;
  public final RedstoneEmitter rsEmitter;

  public static void initialize() {
    GameRegistry.registerTileEntity(TileEntityElectricBlock.class, "ic2:storage_cell");
  }

  public int receiveEnergy(EnumFacing enumFacing, int maxReceive, boolean simulate) {
    if (!this.energy.getSinkDirs().contains(enumFacing)) return 0;

    int energyReceived = Math.min(this.getCapacity() - this.getStored(), maxReceive);

    if (!simulate) {
      this.addEnergy(energyReceived);
    }

    return energyReceived;
  }

  public int extractEnergy(EnumFacing enumFacing, int maxExtract, boolean simulate) {
    if (!this.energy.getSourceDirs().contains(enumFacing)) return 0;

    int energyExtracted = Math.min(this.getStored(), maxExtract);

    this.energy.useEnergy(energyExtracted, simulate);

    return energyExtracted;
  }

  public boolean canConnectEnergy(EnumFacing enumFacing) {
    return true;
  }

  public int getEnergyStored(EnumFacing enumFacing) {
    return this.getStored();
  }

  public int getMaxEnergyStored(EnumFacing enumFacing) {
    return this.getCapacity();
  }

  public TileEntityElectricBlock(int tier, int output, int maxStorage) {
    this.output = output;
    this.chargeSlot = new InvSlotCharge(this, tier);
    this.dischargeSlot = new InvSlotDischarge(this, Access.IO, tier, InvSide.BOTTOM);
    this.energy = this.addComponent((new Energy(this, maxStorage, EnumSet.complementOf(EnumSet.of(EnumFacing.DOWN)), EnumSet.of(EnumFacing.DOWN), tier, tier, true)).addManagedSlot(this.chargeSlot).addManagedSlot(this.dischargeSlot));
    this.rsEmitter = this.addComponent(new RedstoneEmitter(this));
    this.redstone = this.addComponent(new Redstone(this));
    this.comparator.setUpdate(this.energy::getComparatorValue);
  }

  public void read(NBTTagCompound nbt) {
    this.superReadFromNBT(nbt);
    this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
  }

  protected final void superReadFromNBT(NBTTagCompound nbt) {
    super.read(nbt);
    this.redstoneMode = nbt.getByte("redstoneMode");
  }

  public NBTTagCompound write(NBTTagCompound nbt) {
    super.write(nbt);
    nbt.putByte("redstoneMode", this.redstoneMode);
    return nbt;
  }

  protected void updateEntityServer() {
    super.updateEntityServer();
    this.energy.setSendingEnabled(this.shouldEmitEnergy());
    this.rsEmitter.setLevel(this.shouldEmitRedstone() ? 15 : 0);
  }

  public ContainerBase<? extends TileEntityElectricBlock> getGuiContainer(EntityPlayer player) {
    return new ContainerElectricBlock(player, this);
  }

  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return new GuiElectricBlock(new ContainerElectricBlock(player, this));
  }

  public void onGuiClosed(EntityPlayer player) {
  }

  public void setFacing(EnumFacing facing) {
    super.setFacing(facing);
    this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
  }

  protected final void superSetFacing(EnumFacing facing) {
    super.setFacing(facing);
  }

  protected boolean shouldEmitRedstone() {
    switch (this.redstoneMode) {
      case 1:
        return this.energy.getEnergy() >= this.energy.getCapacity() - this.output * 20.0;
      case 2:
        return this.energy.getEnergy() > this.output && this.energy.getEnergy() < this.energy.getCapacity() - this.output;
      case 3:
        return this.energy.getEnergy() < this.energy.getCapacity() - this.output;
      case 4:
        return this.energy.getEnergy() < this.output;
      default:
        return false;
    }
  }

  protected boolean shouldEmitEnergy() {
    boolean redstone = this.redstone.hasRedstoneInput();
    if (this.redstoneMode == 5) {
      return !redstone;
    } else if (this.redstoneMode != 6) {
      return true;
    } else {
      return !redstone || this.energy.getEnergy() > this.energy.getCapacity() - this.output * 20.0;
    }
  }

  public void onNetworkEvent(EntityPlayer player, int event) {
    ++this.redstoneMode;
    if (this.redstoneMode >= redstoneModes) {
      this.redstoneMode = 0;
    }

    IC2.platform.messagePlayer(player, this.getRedstoneMode());
  }

  public String getRedstoneMode() {
    return this.redstoneMode < redstoneModes && this.redstoneMode >= 0 ? Localization.translate("ic2.EUStorage.gui.mod.redstone" + this.redstoneMode) : "";
  }

  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    super.onPlaced(stack, placer, facing);
    if (!this.getWorld().isRemote) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      this.energy.addEnergy(nbt.getDouble("energy"));
    }

  }

  public void onUpgraded() {
    this.rerender();
  }

  protected ItemStack adjustDrop(ItemStack drop, boolean wrench) {
    drop = super.adjustDrop(drop, wrench);
    if (wrench || this.teBlock.getDefaultDrop() == DefaultDrop.Self) {
      double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
      double totalEnergy = this.energy.getEnergy();
      if (retainedRatio > 0.0 && totalEnergy > 0.0) {
        NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
        nbt.putDouble("energy", totalEnergy * retainedRatio);
      }
    }

    return drop;
  }

  public int getOutput() {
    return (int)this.output;
  }

  public double getOutputEnergyUnitsPerTick() {
    return this.output;
  }

  public void setStored(int energy) {
  }

  public int addEnergy(int amount) {
    this.energy.addEnergy(amount);
    return amount;
  }

  public int getStored() {
    return (int)this.energy.getEnergy();
  }

  public int getCapacity() {
    return (int)this.energy.getCapacity();
  }

  public boolean isTeleporterCompatible(EnumFacing side) {
    return true;
  }

  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, tooltip, advanced);
    tooltip.add(String.format("%s %.0f %s %s %d %s", Localization.translate("ic2.item.tooltip.Output"), EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier()), Localization.translate("ic2.generic.text.EUt"), Localization.translate("ic2.item.tooltip.Capacity"), this.getCapacity(), Localization.translate("ic2.generic.text.EU")));
    tooltip.add(Localization.translate("ic2.item.tooltip.Store") + " " + (long)StackUtil.getOrCreateNbtData(stack).getDouble("energy") + " " + Localization.translate("ic2.generic.text.EU"));
  }
}
