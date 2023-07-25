//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.IPatternStorage;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotScannable;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.block.invslot.InvSlot.InvSide;
import ic2.core.block.machine.container.ContainerScanner;
import ic2.core.block.machine.gui.GuiScanner;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuGraph;
import ic2.core.uu.UuIndex;
import java.util.Iterator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityScanner extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener {
    private ItemStack currentStack;
    private ItemStack pattern;
    private final int energyusecycle;
    public int progress;
    public final int duration;
    public final InvSlotConsumable inputSlot;
    public final InvSlot diskSlot;
    private State state;
    public double patternUu;
    public double patternEu;

    public TileEntityScanner() {
        super(2000000, 4);
        this.currentStack = StackUtil.emptyStack;
        this.pattern = StackUtil.emptyStack;
        this.energyusecycle = 256;
        this.progress = 0;
        this.duration = 6000;
        this.state = TileEntityScanner.State.IDLE;
        this.inputSlot = new InvSlotScannable(this, "input", 1);
        this.diskSlot = new InvSlotConsumableId(this, "disk", Access.IO, 1, InvSide.ANY, ItemName.crystal_memory.getInstance());
    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean newActive = false;
        if (this.progress < 6000) {
            if (!this.inputSlot.isEmpty() && (StackUtil.isEmpty(this.currentStack) || StackUtil.checkItemEquality(this.currentStack, this.inputSlot.get()))) {
                if (this.getPatternStorage() == null && this.diskSlot.isEmpty()) {
                    this.state = TileEntityScanner.State.NO_STORAGE;
                    this.reset();
                } else if (this.energy.getEnergy() >= 256.0) {
                    if (StackUtil.isEmpty(this.currentStack)) {
                        this.currentStack = StackUtil.copyWithSize(this.inputSlot.get(), 1);
                    }

                    this.pattern = UuGraph.find(this.currentStack);
                    if (StackUtil.isEmpty(this.pattern)) {
                        this.state = TileEntityScanner.State.FAILED;
                    } else if (this.isPatternRecorded(this.pattern)) {
                        this.state = TileEntityScanner.State.ALREADY_RECORDED;
                        this.reset();
                    } else {
                        newActive = true;
                        this.state = TileEntityScanner.State.SCANNING;
                        this.energy.useEnergy(256.0);
                        ++this.progress;
                        if (this.isDone()) {
                            this.refreshInfo();
                            if (this.patternUu != Double.POSITIVE_INFINITY) {
                                this.state = TileEntityScanner.State.COMPLETED;
                                this.inputSlot.consume(1, false, true);
                                this.markDirty();
                            } else {
                                this.state = TileEntityScanner.State.FAILED;
                            }
                        }
                    }
                } else {
                    this.state = TileEntityScanner.State.NO_ENERGY;
                }
            } else {
                this.state = TileEntityScanner.State.IDLE;
                this.reset();
            }
        } else if (StackUtil.isEmpty(this.pattern)) {
            this.state = TileEntityScanner.State.IDLE;
            this.progress = 0;
        }

        this.setActive(newActive);
    }

    public void reset() {
        this.progress = 0;
        this.currentStack = StackUtil.emptyStack;
        this.pattern = StackUtil.emptyStack;
    }

    private boolean isPatternRecorded(ItemStack stack) {
        if (!this.diskSlot.isEmpty() && this.diskSlot.get().getItem() instanceof ItemCrystalMemory) {
            ItemStack crystalMemory = this.diskSlot.get();
            if (StackUtil.checkItemEquality(((ItemCrystalMemory)crystalMemory.getItem()).readItemStack(crystalMemory), stack)) {
                return true;
            }
        }

        IPatternStorage storage = this.getPatternStorage();
        if (storage == null) {
            return false;
        } else {
            Iterator var3 = storage.getPatterns().iterator();

            ItemStack stored;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                stored = (ItemStack)var3.next();
            } while(!StackUtil.checkItemEquality(stored, stack));

            return true;
        }
    }

    private void record() {
        if (!StackUtil.isEmpty(this.pattern) && this.patternUu != Double.POSITIVE_INFINITY) {
            if (!this.savetoDisk(this.pattern)) {
                IPatternStorage storage = this.getPatternStorage();
                if (storage == null) {
                    this.state = TileEntityScanner.State.TRANSFER_ERROR;
                    return;
                }

                if (!storage.addPattern(this.pattern)) {
                    this.state = TileEntityScanner.State.TRANSFER_ERROR;
                    return;
                }
            }

            this.reset();
        } else {
            this.reset();
        }
    }

    public void read(NBTTagCompound nbttagcompound) {
        super.read(nbttagcompound);
        this.progress = nbttagcompound.getInt("progress");
        NBTTagCompound contentTag = nbttagcompound.getCompound("currentStack");
        this.currentStack = new ItemStack(contentTag);
        contentTag = nbttagcompound.getCompound("pattern");
        this.pattern = new ItemStack(contentTag);
        int stateIdx = nbttagcompound.getInt("state");
        this.state = stateIdx < TileEntityScanner.State.values().length ? TileEntityScanner.State.values()[stateIdx] : TileEntityScanner.State.IDLE;
        this.refreshInfo();
    }

    public NBTTagCompound write(NBTTagCompound nbt) {
        super.write(nbt);
        nbt.putInt("progress", this.progress);
        NBTTagCompound contentTag;
        if (!StackUtil.isEmpty(this.currentStack)) {
            contentTag = new NBTTagCompound();
            this.currentStack.write(contentTag);
            nbt.func_74782_a("currentStack", contentTag);
        }

        if (!StackUtil.isEmpty(this.pattern)) {
            contentTag = new NBTTagCompound();
            this.pattern.write(contentTag);
            nbt.func_74782_a("pattern", contentTag);
        }

        nbt.putInt("state", this.state.ordinal());
        return nbt;
    }

    public ContainerBase<TileEntityScanner> getGuiContainer(EntityPlayer player) {
        return new ContainerScanner(player, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
        return new GuiScanner(new ContainerScanner(player, this));
    }

    public void onGuiClosed(EntityPlayer player) {
    }

    public IPatternStorage getPatternStorage() {
        World world = this.getWorld();
        EnumFacing[] var2 = EnumFacing.BY_INDEX;

        for (EnumFacing dir : var2) {
            TileEntity target = world.getTileEntity(this.pos.offset(dir));
            if (target instanceof IPatternStorage) {
                return (IPatternStorage) target;
            }
        }

        return null;
    }

    public boolean savetoDisk(ItemStack stack) {
        if (!this.diskSlot.isEmpty() && stack != null) {
            if (this.diskSlot.get().getItem() instanceof ItemCrystalMemory) {
                ItemStack crystalMemory = this.diskSlot.get();
                ((ItemCrystalMemory)crystalMemory.getItem()).writecontentsTag(crystalMemory, stack);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void onNetworkEvent(EntityPlayer player, int event) {
        switch (event) {
            case 0:
                this.reset();
                break;
            case 1:
                if (this.isDone()) {
                    this.record();
                }
        }

    }

    private void refreshInfo() {
        if (!StackUtil.isEmpty(this.pattern)) {
            this.patternUu = UuIndex.instance.getInBuckets(this.pattern);
        }

    }

    public int getPercentageDone() {
        return 100 * this.progress / 6000;
    }

    public int getSubPercentageDoneScaled(int width) {
        return width * (100 * this.progress % 6000) / 6000;
    }

    public boolean isDone() {
        return this.progress >= 6000;
    }

    public State getState() {
        return this.state;
    }

    public enum State {
        IDLE,
        SCANNING,
        COMPLETED,
        FAILED,
        NO_STORAGE,
        NO_ENERGY,
        TRANSFER_ERROR,
        ALREADY_RECORDED;

        State() {
        }
    }
}
