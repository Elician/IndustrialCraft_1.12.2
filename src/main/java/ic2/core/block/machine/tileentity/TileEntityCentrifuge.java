//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.BasicMachineRecipeManager;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@NotClassic
public class TileEntityCentrifuge extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> {
    protected final Redstone redstone;
    public static final short maxHeat = 15000;
    @GuiSynced
    public short heat = 0;
    @GuiSynced
    public short workheat = 10000;

    public TileEntityCentrifuge() {
        super(550, 50000, 3, 2);
        this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.centrifuge);
        this.redstone = this.addComponent(new Redstone(this));
    }

    public static void init() {
        Recipes.centrifuge = new BasicMachineRecipeManager();
    }

    public void read(NBTTagCompound nbt) {
        super.read(nbt);
        this.heat = nbt.getShort("heat");
    }

    public NBTTagCompound write(NBTTagCompound nbt) {
        super.write(nbt);
        nbt.putShort("heat", this.heat);
        return nbt;
    }

    public double getHeatRatio() {
        return (double) this.heat / (double) this.workheat;
    }

    private static short min(short a, short b) {
        return a <= b ? a : b;
    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean heating = false;
        if (this.energy.canUseEnergy(1.0)) {
            short heatRequested = Short.MIN_VALUE;
            MachineRecipeResult<? extends IRecipeInput, ? extends Collection<ItemStack>, ? extends ItemStack> output = super.getOutput();
            if (output != null && !this.redstone.hasRedstoneInput()) {
                heatRequested = min((short) 5000, output.getRecipe().getMetaData().getShort("minHeat"));
                this.workheat = heatRequested;
                if (this.heat > heatRequested) {
                    this.heat = heatRequested;
                }
            } else if (this.heat <= 5000 && this.redstone.hasRedstoneInput()) {
                heatRequested = 5000;
                this.workheat = heatRequested;
            }

            if (this.heat - 1 < heatRequested) {
                this.energy.useEnergy(1.0);
                heating = true;
            }
        }

        if (heating) {
            ++this.heat;
        } else {
            this.heat = (short) (this.heat - Math.min(this.heat, 1));
        }

    }

    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
        MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getOutput();
        if (ret != null) {
            if (ret.getRecipe().getMetaData() == null) {
                return null;
            }

            if (ret.getRecipe().getMetaData().getInt("minHeat") > this.heat) {
                return null;
            }
        }

        return ret;
    }

    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(
                UpgradableProperty.Processing,
                UpgradableProperty.RedstoneSensitive,
                UpgradableProperty.Transformer,
                UpgradableProperty.EnergyStorage,
                UpgradableProperty.ItemConsuming,
                UpgradableProperty.ItemProducing
        );
    }

    public double getGuiValue(String name) {
        return "heat".equals(name) ? (double) this.heat / (double) this.workheat : super.getGuiValue(name);
    }
}
