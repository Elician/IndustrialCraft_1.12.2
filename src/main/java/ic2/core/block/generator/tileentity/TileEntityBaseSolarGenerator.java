//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.generator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.network.GuiSynced;
import ic2.core.util.BiomeUtil;
import ic2.core.util.Util;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TileEntityBaseSolarGenerator extends TileEntityBaseGenerator {

  @GuiSynced
  public float skyLight;
  private int ticker;
  private final int max_production;

  @GuiSynced
  public double production;

  public TileEntityBaseSolarGenerator(int max_production, int tier) {
    super(max_production, tier, max_production * 1200);
    this.ticker = IC2.random.nextInt(10);
    this.max_production = max_production;
  }

  protected void onLoaded() {
    super.onLoaded();
    this.updateSunVisibility();
  }

  public int getProduction() {
    return (int) production;
  }

  public boolean gainEnergy() {
    if (++this.ticker % 10 == 0) {
      this.updateSunVisibility();
    }

    if (this.skyLight > 0.0F) {
      this.energy.addEnergy(this.production);
      return true;
    } else {
      return false;
    }
  }

  public boolean gainFuel() {
    return false;
  }

  public void updateSunVisibility() {
    this.skyLight = getSkyLight(this.getWorld(), this.pos.up());
    this.production = max_production * (double) this.skyLight;
  }

  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {

    tooltip.add(Localization.translate("ic2.item.tooltip.production") + " " + max_production + " RF/t");
    tooltip.add(Localization.translate("ic2.item.tooltip.capacity") + " " + this.energy.getCapacity() + " RF");

    tooltip.add("");

    super.addInformation(stack, tooltip, advanced);

  }

  public static float getSkyLight(World world, BlockPos pos) {
    if (world.dimension.isNether()) {
      return 0.0F;
    } else {
      float sunBrightness = Util.limit((float) Math.cos(world.getCelestialAngleRadians(1.0F)) * 2.0F + 0.2F, 0.0F, 1.0F);
      if (!BiomeDictionary.hasType(BiomeUtil.getBiome(world, pos), Type.SANDY)) {
        sunBrightness *= 1.0F - world.getRainStrength(1.0F) * 5.0F / 16.0F;
        sunBrightness *= 1.0F - world.getThunderStrength(1.0F) * 5.0F / 16.0F;
        sunBrightness = Util.limit(sunBrightness, 0.0F, 1.0F);
      }

      return (float) world.func_175642_b(EnumSkyBlock.SKY, pos) / 15.0F * sunBrightness;
    }
  }

  public boolean needsFuel() {
    return false;
  }

  public ContainerBase<? extends TileEntityBaseGenerator> getGuiContainer(EntityPlayer player) {
    return new ContainerSolarPanel(player, this);
  }

  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return new GuiSolarBlock(new ContainerSolarPanel(player, this));
  }

  public boolean getGuiState(String name) {
    if (name.equals("sunlight")) {
      return this.skyLight > 0.0F;
    } else {
      return super.getGuiState(name);
    }
  }

  protected boolean delayActiveUpdate() {
    return true;
  }
}
