//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.generator.tileentity;

import ic2.core.GuiIC2;
import ic2.core.gui.EfficiencyGauge;
import ic2.core.gui.EnergyGauge;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSolarBlock extends GuiIC2<ContainerSolarPanel> {
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/guisolarpanel.png");

  public GuiSolarBlock(final ContainerSolarPanel container) {
    super(container, 196);

    this.addElement(EnergyGauge.asRedBox(this, 134, 52, container.base));
    this.addElement(EfficiencyGauge.asYellowBox(this, 152, 52, container.base));
  }

  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.field_146289_q.func_78276_b(Localization.translate("ic2.EUStorage.gui.info.armor"), 7, this.ySize - 126 + 3, 4210752);

    //Charge
    this.field_146289_q.func_78276_b(Localization.translate("ic2.item.tooltip.charge") + " " + Math.round(container.base.energy.getEnergy()) + " RF", 7, 20, 4210752);
    //Capacity
    this.field_146289_q.func_78276_b(Localization.translate("ic2.item.tooltip.capacity") + " " + Math.round(container.base.energy.getCapacity()) + " RF", 7, 30, 4210752);
    //Generation
    this.field_146289_q.func_78276_b(Localization.translate("ic2.item.tooltip.production") + " " + container.base.getProduction() + " RF/t", 7, 40, 4210752);
    //Effectivity
    this.field_146289_q.func_78276_b(Localization.translate("ic2.item.tooltip.effectivity") + " " + Math.round(container.base.skyLight * 100) + "%", 7, 50, 4210752);
  }

  protected ResourceLocation getTexture() {
    return background;
  }
}
