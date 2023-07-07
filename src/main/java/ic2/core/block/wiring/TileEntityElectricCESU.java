package ic2.core.block.wiring;

import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityElectricCESU extends TileEntityElectricBlock {
    public TileEntityElectricCESU() {
        super(2, 1000, 1000000);
    }
}
