package ic2.core.block.generator.tileentity;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityConversionGenerator extends TileEntityInventory implements IEnergyProvider, IHasGui, IEnergySource {
    private static final NumberFormat FORMAT = new DecimalFormat("#.#");
    @GuiSynced
    private double lastProduction;
    @GuiSynced
    private double maxProduction;
    private double production;
    private boolean registeredToEnet;

    public TileEntityConversionGenerator() {
    }

    @Override
    public int extractEnergy(EnumFacing enumFacing, int maxExtract, boolean simulate) {
        return (int) Math.min(production, maxExtract);
    }

    @Override
    public int getEnergyStored(EnumFacing enumFacing) {
        return 0;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing enumFacing) {
        return 0;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing enumFacing) {
        return true;
    }

    protected void updateEntityServer() {

        for (EnumFacing facing : EnumFacing.values()) {

            BlockPos pos = this.pos.offset(facing);
            TileEntity te = this.getWorld().getTileEntity(pos);
            if (te == null) continue;
            if (te instanceof IEnergyReceiver) {
                IEnergyReceiver rcv = (IEnergyReceiver) te;
                if (rcv.canConnectEnergy(facing.getOpposite()) && maxProduction > 0) {
                    double amount = rcv.receiveEnergy(facing.getOpposite(), (int) maxProduction, false);
                    this.drawEnergy(amount);
                }
            }
        }

        super.updateEntityServer();
        this.lastProduction = this.production;
        this.production = 0.0;
        this.setActive(this.maxProduction > 0.0);

    }

    protected void onUnloaded() {
        super.onUnloaded();
        if (this.registeredToEnet && !this.world.isRemote) {
            EnergyNet.instance.removeTile(this);
            this.registeredToEnet = false;
        }

    }

    protected void onLoaded() {
        super.onLoaded();
        if (!this.registeredToEnet && !this.world.isRemote) {
            EnergyNet.instance.addTile(this);
            this.registeredToEnet = true;
        }

    }

    public String getProduction() {
        return FORMAT.format(this.lastProduction);
    }

    public String getMaxProduction() {
        return FORMAT.format(this.maxProduction);
    }

    public ContainerBase<TileEntityConversionGenerator> getGuiContainer(EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
        return DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }

    public void onGuiClosed(EntityPlayer player) {
    }

    protected abstract int getEnergyAvailable();

    protected abstract void drawEnergyAvailable(int var1);

    protected abstract double getMultiplier();

    public double getOfferedEnergy() {
        return this.maxProduction = (double)this.getEnergyAvailable() * this.getMultiplier();
    }

    public void drawEnergy(double amount) {
        this.production += amount;
        this.drawEnergyAvailable((int)Math.ceil(amount / this.getMultiplier()));
    }

    public int getSourceTier() {
        return Math.max(EnergyNet.instance.getTierFromPower(this.maxProduction), 2);
    }

    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
        return side != this.getFacing();
    }
}
