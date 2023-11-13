package ic2.core.gui;

import ic2.core.GuiIC2;
import ic2.core.block.generator.tileentity.TileEntityBaseSolarGenerator;

import java.util.List;

public class EfficiencyGauge extends EnergyGauge {

  private final TileEntityBaseSolarGenerator te;

  public static EnergyGauge asYellowBox(GuiIC2<?> gui, int x, int y, TileEntityBaseSolarGenerator sg) {
    return new EfficiencyGauge(gui, x, y, sg);
  }

  public EfficiencyGauge(GuiIC2<?> gui, int x, int y, TileEntityBaseSolarGenerator sg) {
    super(gui, x, y, EnergyGauge.EnergyGaugeStyle.SolarEfficiency);
    this.te = sg;
  }

  protected List<String> getToolTip() {
    List<String> ret = super.getCleanToolTip();
    ret.add(
        Math.round(te.skyLight * 100) + " %"
    );
    return ret;
  }

  protected double getRatio() {
    return te.skyLight;
  }
}
