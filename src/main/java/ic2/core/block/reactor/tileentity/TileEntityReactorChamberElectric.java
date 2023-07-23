
package ic2.core.block.reactor.tileentity;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import com.google.common.base.Supplier;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.util.StackUtil;
import java.util.Arrays;
import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityReactorChamberElectric extends TileEntityBlock implements IEnergyProvider, IInventory, IReactorChamber, IEnergyEmitter {
    public final Redstone redstone = this.addComponent(new Redstone(this));
    protected final Fluids fluids = this.addComponent(new Fluids(this));
    private TileEntityNuclearReactorElectric reactor;
    private long lastReactorUpdate;

    public TileEntityReactorChamberElectric() {
        this.fluids.addUnmanagedTankHook(() -> {
            TileEntityNuclearReactorElectric reactor = TileEntityReactorChamberElectric.this.getReactor();
            return (reactor == null ? Collections.emptySet() : Arrays.asList(reactor.inputTank, reactor.outputTank));
        });
    }

    protected void onLoaded() {
        super.onLoaded();
        this.updateRedstoneLink();
    }

    private void updateRedstoneLink() {
        if (!this.getWorld().isRemote) {
            TileEntityNuclearReactorElectric reactor = this.getReactor();
            if (reactor != null) {
                this.redstone.linkTo(reactor.redstone);
            }
        }
    }

    protected void updateEntityServer() {

        TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor == null) return;

        for (EnumFacing facing : EnumFacing.values()) {

            BlockPos pos = this.pos.offset(facing);
            TileEntity te = this.getWorld().getTileEntity(pos);

            if (te == null) {
                reactor.clearProvider(this, facing);
                continue;
            }
            if (te instanceof IEnergyReceiver) {
                IEnergyReceiver rcv = (IEnergyReceiver) te;
                if (rcv.canConnectEnergy(facing.getOpposite()) && reactor.getOfferedEnergy() > 0) {
                    if (reactor.energyProvider == null) {
                        reactor.energyProvider = this;
                    }

                    if (reactor.energyProviderFacing == null) {
                        reactor.energyProviderFacing = facing;
                    }

                    if (reactor.energyProvider.equals(this) && reactor.energyProviderFacing.equals(facing)) {
                        rcv.receiveEnergy(facing.getOpposite(), (int) reactor.getOfferedEnergy(), false);
                    }
                } else {
                    reactor.clearProvider(this, facing);
                }
            } else {
                reactor.clearProvider(this, facing);
            }
        }
    }

    @Override
    public int extractEnergy(EnumFacing enumFacing, int maxExtract, boolean simulate) {
        return Math.min((int) this.getReactor().getOfferedEnergy(), maxExtract);
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

    @SideOnly(Side.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            TileEntityNuclearReactorElectric.showHeatEffects(this.getWorld(), this.pos, reactor.getHeat());
        }

    }

    protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            World world = this.getWorld();
            return reactor.getBlockType().func_180639_a(world, reactor.getPos(), world.getBlockState(reactor.getPos()), player, hand, side, hitX, hitY, hitZ);
        } else {
            return false;
        }
    }

    protected void onNeighborChange(Block neighbor, BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        this.lastReactorUpdate = 0L;
        if (this.getReactor() == null) {
            this.destoryChamber(true);
        }

    }

    public void destoryChamber(boolean wrench) {
        World world = this.getWorld();
        world.func_175698_g(this.pos);

        for (ItemStack drop : this.getSelfDrops(0, wrench)) {
            StackUtil.dropAsEntity(world, this.pos, drop);
        }

    }

    public String func_70005_c_() {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null ? reactor.func_70005_c_() : "<null>";
    }

    public boolean hasCustomName() {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null && reactor.hasCustomName();
    }

    public ITextComponent getDisplayName() {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null ? reactor.getDisplayName() : new TextComponentString("<null>"));
    }

    public int getSizeInventory() {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null ? reactor.getSizeInventory() : 0;
    }

    public boolean isEmpty() {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor == null || reactor.isEmpty();
    }

    public ItemStack getStackInSlot(int index) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null ? reactor.getStackInSlot(index) : null;
    }

    public ItemStack decrStackSize(int index, int count) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null ? reactor.decrStackSize(index, count) : null;
    }

    public ItemStack removeStackFromSlot(int index) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null ? reactor.removeStackFromSlot(index) : null;
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.setInventorySlotContents(index, stack);
        }

    }

    public int getInventoryStackLimit() {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null ? reactor.getInventoryStackLimit() : 0;
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null && reactor.isUsableByPlayer(player);
    }

    public void openInventory(EntityPlayer player) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.openInventory(player);
        }

    }

    public void closeInventory(EntityPlayer player) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.closeInventory(player);
        }

    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null && reactor.isItemValidForSlot(index, stack);
    }

    public int func_174887_a_(int id) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null ? reactor.func_174887_a_(id) : 0;
    }

    public void func_174885_b(int id, int value) {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.func_174885_b(id, value);
        }

    }

    public int func_174890_g() {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null ? reactor.func_174890_g() : 0;
    }

    public void clear() {
        TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.clear();
        }

    }

    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
        return true;
    }

    public TileEntityNuclearReactorElectric getReactorInstance() {
        return this.reactor;
    }

    public boolean isWall() {
        return false;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (super.hasCapability(capability, facing)) {
            return super.getCapability(capability, facing);
        } else {
            return this.reactor != null ? this.reactor.getCapability(capability, facing) : null;
        }
    }

    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return super.hasCapability(capability, facing) || this.reactor != null && this.reactor.hasCapability(capability, facing);
    }

    private TileEntityNuclearReactorElectric getReactor() {
        long time = this.getWorld().getGameTime();
        if (time != this.lastReactorUpdate) {
            this.updateReactor();
            this.lastReactorUpdate = time;
        } else if (this.reactor != null && this.reactor.isRemoved()) {
            this.reactor = null;
        }

        return this.reactor;
    }

    private void updateReactor() {
        World world = this.getWorld();
        this.reactor = null;

        for (EnumFacing facing : EnumFacing.BY_INDEX) {
            TileEntity te = world.getTileEntity(this.pos.offset(facing));
            if (te instanceof TileEntityNuclearReactorElectric) {
                this.reactor = (TileEntityNuclearReactorElectric) te;
                break;
            }
        }

    }
}
