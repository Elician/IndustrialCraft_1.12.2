package ic2.core.block.wiring;

import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityElectricMFSU extends TileEntityElectricBlock {

    public TileEntityElectricMFSU() {
        super(4, 10000, 100000000);
    }
}
