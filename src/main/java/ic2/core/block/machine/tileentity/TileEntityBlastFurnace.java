//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.item.type.IngotResourceType;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityBlastFurnace extends TileEntityInventory implements IUpgradableBlock, IHasGui, IGuiValueProvider {
    public int heat = 0;
    public static int maxHeat = 15000;
    @GuiSynced
    public float guiHeat;
    protected final Redstone redstone;
    protected final Fluids fluids;
    protected int progress = 0;
    protected int progressNeeded = 300;
    @GuiSynced
    protected float guiProgress;
    public final InvSlotProcessableGeneric inputSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotConsumableLiquidByList tankInputSlot;
    public final InvSlotOutput tankOutputSlot;
    public final InvSlotUpgrade upgradeSlot;
    @GuiSynced
    public final FluidTank fluidTank;

    public TileEntityBlastFurnace() {
        this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.blastfurnace);
        this.outputSlot = new InvSlotOutput(this, "output", 2) {
            public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
                if (player != null && ItemName.ingot.getItemStack(IngotResourceType.steel).isItemEqual(stack)) {
                    IC2.achievements.issueAchievement(player, "acquireRefinedIron");
                }

            }
        };
        this.tankInputSlot = new InvSlotConsumableLiquidByList(this, "cellInput", 1, FluidName.air.getInstance());
        this.tankOutputSlot = new InvSlotOutput(this, "cellOutput", 1);
        this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
        this.redstone = this.addComponent(new Redstone(this));
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankInsert("fluid", 6000, Fluids.fluidPredicate(FluidName.air.getInstance()));
    }

    public static void init() {
        Recipes.blastfurnace = new BasicMachineRecipeManager();
    }

    public void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        this.heatup();
        MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.getOutput();
        if (result != null && this.isHot()) {
            this.setActive(true);
            if (result.getRecipe().getMetaData().getInt("fluid") <= this.fluidTank.getFluidAmount()) {
                ++this.progress;
                this.fluidTank.drainInternal(result.getRecipe().getMetaData().getInt("fluid"), true);
            }

            this.progressNeeded = result.getRecipe().getMetaData().getInt("duration");
            if (this.progress >= result.getRecipe().getMetaData().getInt("duration")) {
                this.operateOnce(result, result.getOutput());
                needsInvUpdate = true;
                this.progress = 0;
            }
        } else {
            if (result == null) {
                this.progress = 0;
            }

            this.setActive(false);
        }

        if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity()) {
            this.gainFluid();
        }

        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        this.guiProgress = (float)this.progress / (float)this.progressNeeded;
        this.guiHeat = (float)this.heat / (float)maxHeat;
        if (needsInvUpdate) {
            super.markDirty();
        }

    }

    public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, Collection<ItemStack> processResult) {
        this.inputSlot.consume(result);
        this.outputSlot.add(processResult);
    }

    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
        if (this.inputSlot.isEmpty()) {
            return null;
        } else {
            MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = this.inputSlot.process();
            if (output != null && output.getRecipe().getMetaData() != null) {
                return this.outputSlot.canAdd(output.getOutput()) ? output : null;
            } else {
                return null;
            }
        }
    }

    public boolean gainFluid() {
        return this.tankInputSlot.processIntoTank(this.fluidTank, this.tankOutputSlot);
    }

    public void read(NBTTagCompound nbt) {
        super.read(nbt);
        this.heat = nbt.getInt("heat");
        this.progress = nbt.getInt("progress");
    }

    public NBTTagCompound write(NBTTagCompound nbt) {
        super.write(nbt);
        nbt.putInt("heat", this.heat);
        nbt.putInt("progress", this.progress);
        return nbt;
    }

    private void heatup() {
        int heatRequested = 0;
        int gainhU = 0;
        if ((!this.inputSlot.isEmpty() || this.progress >= 1) && this.heat <= maxHeat) {
            heatRequested = maxHeat - this.heat + 100;
        } else if (this.redstone.hasRedstoneInput() && this.heat <= maxHeat) {
            heatRequested = maxHeat - this.heat + 100;
        }

        if (heatRequested > 0) {
            EnumFacing dir = this.getFacing();
            TileEntity te = this.getWorld().getTileEntity(this.pos.offset(dir));
            if (te instanceof IHeatSource) {
                gainhU = ((IHeatSource)te).drawHeat(dir.getOpposite(), heatRequested, false);
                this.heat += gainhU;
            }

            if (gainhU == 0) {
                this.heat -= Math.min(this.heat, 1);
            }
        } else {
            this.heat -= Math.min(this.heat, 1);
        }

    }

    public boolean isHot() {
        return this.heat >= maxHeat;
    }

    public ContainerBase<TileEntityBlastFurnace> getGuiContainer(EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
        return DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }

    public void onGuiClosed(EntityPlayer player) {
    }

    public double getGuiValue(String name) {
        if (name.equals("progress")) {
            return this.guiProgress;
        } else if (name.equals("heat")) {
            return this.guiHeat;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public double getEnergy() {
        return 0.0;
    }

    public boolean useEnergy(double amount) {
        return false;
    }

    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming);
    }
}
