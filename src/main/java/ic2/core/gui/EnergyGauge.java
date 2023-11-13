package ic2.core.gui;

import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;
import ic2.core.gui.Gauge.GaugePropertyBuilder.GaugeOrientation;
import ic2.core.init.Localization;
import ic2.core.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

public class EnergyGauge extends Gauge<EnergyGauge> {
  private Energy energy = null;

  public static EnergyGauge asBar(GuiIC2<?> gui, int x, int y, TileEntityBlock te) {
    return new EnergyGauge(gui, x, y, te, EnergyGauge.EnergyGaugeStyle.Bar);
  }

  public static EnergyGauge asBolt(GuiIC2<?> gui, int x, int y, TileEntityBlock te) {
    return new EnergyGauge(gui, x, y, te, EnergyGauge.EnergyGaugeStyle.Bolt);
  }

  public static EnergyGauge asRedBox(GuiIC2<?> gui, int x, int y,  TileEntityBlock te) {
    return new EnergyGauge(gui, x, y, te, EnergyGauge.EnergyGaugeStyle.SolarCharge);
  }

  public EnergyGauge(GuiIC2<?> gui, int x, int y, EnergyGaugeStyle style) {
    super(gui, x, y, style.properties);
  }

  public EnergyGauge(GuiIC2<?> gui, int x, int y, TileEntityBlock te, EnergyGaugeStyle style) {
    super(gui, x, y, style.properties);
    this.energy = te.getComponent(Energy.class);
  }

  protected List<String> getToolTip() {
    List<String> ret = super.getToolTip();
    ret.add(
        Util.toSiString(this.energy.getEnergy(), 4) + '/' +
            Util.toSiString(this.energy.getCapacity(), 4) + ' ' +
            Localization.translate("ic2.generic.text.EU")
    );
    return ret;
  }

  protected List<String> getCleanToolTip() {
    return super.getToolTip();
  }

  protected double getRatio() {
    return this.energy.getFillRatio();
  }

  public static enum EnergyGaugeStyle {
    Bar((new Gauge.GaugePropertyBuilder(132, 43, 24, 9, GaugeOrientation.Right))
        .withBackground(-4, -11, 32, 32, 128, 0)
        .build()),
    Bolt((new Gauge.GaugePropertyBuilder(116, 65, 7, 13, GaugeOrientation.Up))
        .withBackground(-4, -1, 16, 16, 96, 64)
        .build()),
    StirlingBar((new Gauge.GaugePropertyBuilder(176, 15, 58, 14, GaugeOrientation.Right))
        .withTexture(new ResourceLocation("ic2", "textures/gui/GUIStirlingGenerator.png"))
        .withBackground(59, 33)
        .build()),
    SolarEfficiency((new Gauge.GaugePropertyBuilder(32, 64, 16, 48, GaugeOrientation.Up))
        .withTexture(new ResourceLocation("ic2", "textures/gui/elements.png"))
        .withBackground(16, 64)
        .build()),

    SolarCharge((new Gauge.GaugePropertyBuilder(0, 64, 16, 48, GaugeOrientation.Up))
        .withTexture(new ResourceLocation("ic2", "textures/gui/elements.png"))
        .withBackground(16, 64)
        .build());

    private static final Map<String, EnergyGaugeStyle> map = getMap();
    public final String name;
    public final Gauge.GaugeProperties properties;

    private EnergyGaugeStyle(Gauge.GaugeProperties properties) {
      this.name = this.name().toLowerCase(Locale.ENGLISH);
      this.properties = properties;
    }

    public static EnergyGaugeStyle get(String name) {
      return map.get(name);
    }

    private static Map<String, EnergyGaugeStyle> getMap() {
      EnergyGaugeStyle[] values = values();
      Map<String, EnergyGaugeStyle> ret = new HashMap<>(values.length);

      for (EnergyGaugeStyle style : values) {
        ret.put(style.name, style);
      }

      return ret;
    }
  }
}
