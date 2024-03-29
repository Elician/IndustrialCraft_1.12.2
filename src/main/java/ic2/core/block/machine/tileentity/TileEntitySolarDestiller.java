//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.generator.tileentity.TileEntityBaseSolarGenerator;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.block.invslot.InvSlot.InvSide;
import ic2.core.block.invslot.InvSlotConsumableLiquid.OpType;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.block.machine.gui.GuiSolarDestiller;
import ic2.core.ref.FluidName;
import ic2.core.util.BiomeUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySolarDestiller extends TileEntityInventory implements IHasGui, IUpgradableBlock {
  public final FluidTank inputTank;
  public final FluidTank outputTank;
  private int tickrate;
  private int updateTicker;
  private float skyLight;
  public final InvSlotOutput wateroutputSlot;
  public final InvSlotOutput destiwateroutputSlott;
  public final InvSlotConsumableLiquidByList waterinputSlot;
  public final InvSlotConsumableLiquidByTank destiwaterinputSlot;
  public final InvSlotUpgrade upgradeSlot;
  protected final Fluids fluids = this.addComponent(new Fluids(this));

  public final int waitMBukkitHot;
  public final int waitMBukkitCold;
  public final int waitMBukkitDefault;

  public TileEntitySolarDestiller() {
    this(10000, 72, 288, 144);
  }

  public TileEntitySolarDestiller(int capacity, int hotMB, int coldMB, int defaultMB) {

    this.waitMBukkitHot = hotMB;
    this.waitMBukkitCold = coldMB;
    this.waitMBukkitDefault = defaultMB;

    this.inputTank = this.fluids.addTankInsert("inputTank", capacity, Fluids.fluidPredicate(FluidRegistry.WATER));
    this.outputTank = this.fluids.addTankExtract("outputTank", capacity);

    this.waterinputSlot = new InvSlotConsumableLiquidByList(this, "waterInput", Access.I, 1, InvSide.TOP, OpType.Drain, FluidRegistry.WATER);
    this.destiwaterinputSlot = new InvSlotConsumableLiquidByTank(this, "destilledWaterInput", Access.I, 1, InvSide.BOTTOM, OpType.Fill, this.outputTank);
    this.wateroutputSlot = new InvSlotOutput(this, "waterOutput", 1);
    this.destiwateroutputSlott = new InvSlotOutput(this, "destilledWaterOutput", 1);
    this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 3);
  }

  protected void onLoaded() {
    super.onLoaded();
    this.tickrate = this.getTickRate();
    this.updateTicker = IC2.random.nextInt(this.tickrate);
  }

  protected void updateEntityServer() {
    super.updateEntityServer();
    this.waterinputSlot.processIntoTank(this.inputTank, this.wateroutputSlot);
    if (++this.updateTicker >= this.tickrate) {
      this.updateSunVisibility();
      if (this.canWork()) {
        this.inputTank.drainInternal(1, true);
        this.outputTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 1), true);
      }

      this.updateTicker = 0;
    }

    this.destiwaterinputSlot.processFromTank(this.outputTank, this.destiwateroutputSlott);
    this.upgradeSlot.tick();
  }

  public void updateSunVisibility() {
    this.skyLight = TileEntityBaseSolarGenerator.getSkyLight(this.getWorld(), this.pos.up());
  }

  public ContainerBase<TileEntitySolarDestiller> getGuiContainer(EntityPlayer player) {
    return new ContainerSolarDestiller(player, this);
  }

  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return new GuiSolarDestiller(new ContainerSolarDestiller(player, this));
  }

  public void onGuiClosed(EntityPlayer player) {
  }

  public int getTickRate() {
    Biome biome = BiomeUtil.getBiome(this.getWorld(), this.pos);
    if (BiomeDictionary.hasType(biome, Type.HOT)) {
      return this.waitMBukkitHot;
    } else {
      return BiomeDictionary.hasType(biome, Type.COLD) ? this.waitMBukkitCold : this.waitMBukkitDefault;
    }
  }

  public int gaugeLiquidScaled(int i, int tank) {
    switch (tank) {
      case 0:
        if (this.inputTank.getFluidAmount() <= 0) {
          return 0;
        }

        return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
      case 1:
        if (this.outputTank.getFluidAmount() <= 0) {
          return 0;
        }

        return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
      default:
        return 0;
    }
  }

  public boolean canWork() {
    return this.inputTank.getFluidAmount() > 0 && this.outputTank.getFluidAmount() < this.outputTank.getCapacity() && (double) this.skyLight > 0.5;
  }

  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing);
  }

  public double getEnergy() {
    return 40.0;
  }

  public boolean useEnergy(double amount) {
    return true;
  }
}
