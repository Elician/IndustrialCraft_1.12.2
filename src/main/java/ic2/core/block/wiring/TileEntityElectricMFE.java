package ic2.core.block.wiring;

import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityElectricMFE extends TileEntityElectricBlock {

    public TileEntityElectricMFE() {
        super(3, 8000, 10000000);
    }
}
