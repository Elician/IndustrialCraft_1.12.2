package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioManager;
import ic2.core.audio.AudioSource;
import ic2.core.audio.FutureSound;
import ic2.core.audio.PositionSpec;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.network.NetworkManager;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityInduction extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider, INetworkTileEntityEventListener {
    private static final short maxHeat = 10000;
    public final InvSlotProcessableSmelting inputSlotA = new InvSlotProcessableSmelting(this, "inputA", 1);
    public final InvSlotProcessableSmelting inputSlotB = new InvSlotProcessableSmelting(this, "inputB", 1);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
    public final InvSlotOutput outputSlotA = new InvSlotOutput(this, "outputA", 1);
    public final InvSlotOutput outputSlotB = new InvSlotOutput(this, "outputB", 1);
    protected final Redstone redstone = this.addComponent(new Redstone(this));
    protected AudioSource audioSource;
    protected FutureSound startingSound;
    protected String finishingSound;
    @GuiSynced
    public short heat = 0;
    @GuiSynced
    public short progress = 0;

    public TileEntityInduction() {
        super(50000, 2);
        this.comparator.setUpdate(() -> this.heat * 15 / 10000);
    }

    public void read(NBTTagCompound nbt) {
        super.read(nbt);
        this.heat = nbt.getShort("heat");
        this.progress = nbt.getShort("progress");
    }

    public NBTTagCompound write(NBTTagCompound nbt) {
        super.write(nbt);
        nbt.putShort("heat", this.heat);
        nbt.putShort("progress", this.progress);
        return nbt;
    }

    protected void onUnloaded() {
        super.onUnloaded();
        if (IC2.platform.isRendering()) {
            if (this.startingSound != null) {
                if (!this.startingSound.isComplete()) {
                    this.startingSound.cancel();
                }

                this.startingSound = null;
            }

            if (this.finishingSound != null) {
                IC2.audioManager.removeSource(this.finishingSound);
                this.finishingSound = null;
            }

            if (this.audioSource != null) {
                IC2.audioManager.removeSources(this);
                this.audioSource = null;
            }
        }

    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        boolean newActive = this.getActive();
        if (this.heat == 0) {
            newActive = false;
        }

        if (this.progress >= 4000) {
            this.operate();
            needsInvUpdate = true;
            this.progress = 0;
            newActive = false;
        }

        boolean canOperate = this.canOperate();
        if ((canOperate || this.redstone.hasRedstoneInput()) && this.energy.useEnergy(1.0)) {
            if (this.heat < 10000) {
                ++this.heat;
            }

            newActive = true;
        } else {
            this.heat = (short)(this.heat - Math.min(this.heat, 4));
        }

        if (newActive && this.progress != 0) {
            if (!canOperate || this.energy.getEnergy() < 15.0) {
                if (!canOperate) {
                    this.progress = 0;
                }

                newActive = false;
                (IC2.network.get(true)).initiateTileEntityEvent(this, 1, true);
            }
        } else if (canOperate) {
            if (this.energy.getEnergy() >= 15.0) {
                newActive = true;
                (IC2.network.get(true)).initiateTileEntityEvent(this, 0, true);
            }
        } else {
            if (needsInvUpdate) {
                (IC2.network.get(true)).initiateTileEntityEvent(this, 3, true);
            }

            this.progress = 0;
        }

        if (newActive && canOperate) {
            this.progress = (short)(this.progress + this.heat / 30);
            this.energy.useEnergy(60);
        }

        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        if (needsInvUpdate) {
            this.markDirty();
        }

        if (newActive != this.getActive()) {
            this.setActive(newActive);
        }

    }

    public String getHeat() {
        return this.heat * 100 / 10000 + "%";
    }

    public int gaugeProgressScaled(int i) {
        return i * this.progress / 4000;
    }

    public void operate() {
        this.operate(this.inputSlotA, this.outputSlotA);
        this.operate(this.inputSlotB, this.outputSlotB);
    }

    public void operate(InvSlotProcessableSmelting inputSlot, InvSlotOutput outputSlot) {
        if (this.canOperate(inputSlot, outputSlot)) {
            MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = inputSlot.process();
            outputSlot.add(result.getOutput());
            inputSlot.consume(result);
        }
    }

    public boolean canOperate() {
        return this.canOperate(this.inputSlotA, this.outputSlotA) || this.canOperate(this.inputSlotB, this.outputSlotB);
    }

    public boolean canOperate(InvSlotProcessableSmelting inputSlot, InvSlotOutput outputSlot) {
        if (inputSlot.isEmpty()) {
            return false;
        } else {
            MachineRecipeResult<? extends ItemStack, ? extends ItemStack, ? extends ItemStack> result = inputSlot.process();
            return result != null && outputSlot.canAdd(result.getOutput());
        }
    }

    public ContainerBase<TileEntityInduction> getGuiContainer(EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
        return DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }

    public void onGuiClosed(EntityPlayer player) {
    }

    public double getEnergy() {
        return this.energy.getEnergy();
    }

    public boolean useEnergy(double amount) {
        return this.energy.useEnergy(amount);
    }

    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }

    public double getGuiValue(String name) {
        if ("progress".equals(name)) {
            return (double)this.gaugeProgressScaled(1000) / 1000.0;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getStartingSoundFile() {
        return "Machines/Induction Furnace/InductionStart.ogg";
    }

    public String getStartSoundFile() {
        return "Machines/Induction Furnace/InductionLoop.ogg";
    }

    public String getInterruptSoundFile() {
        return "Machines/Induction Furnace/InductionStop.ogg";
    }

    public void onNetworkEvent(int event) {
        if (this.audioSource == null && this.getStartSoundFile() != null) {
            this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, this.getStartSoundFile(), true, false, IC2.audioManager.getDefaultVolume());
        }

        switch (event) {
            case 0:
                if (this.startingSound == null) {
                    if (this.finishingSound != null) {
                        IC2.audioManager.removeSource(this.finishingSound);
                        this.finishingSound = null;
                    }

                    String source = IC2.audioManager.playOnce(this, PositionSpec.Center, this.getStartingSoundFile(), false, IC2.audioManager.getDefaultVolume());
                    if (this.audioSource != null) {
                        AudioManager var10000 = IC2.audioManager;
                        AudioSource var10005 = this.audioSource;
                        var10005.getClass();
                        var10000.chainSource(source, this.startingSound = new FutureSound(var10005::play));
                    }
                }
                break;
            case 1:
            case 3:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    if (this.startingSound != null) {
                        if (!this.startingSound.isComplete()) {
                            this.startingSound.cancel();
                        }

                        this.startingSound = null;
                    }

                    this.finishingSound = IC2.audioManager.playOnce(this, PositionSpec.Center, this.getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume());
                }
            case 2:
        }

    }
}
