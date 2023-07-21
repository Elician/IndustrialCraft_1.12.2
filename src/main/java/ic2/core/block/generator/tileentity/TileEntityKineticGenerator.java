//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.generator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@NotClassic
public class TileEntityKineticGenerator extends TileEntityConversionGenerator {
    private final double euPerKu = 0.85 * (double)ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/Kinetic");
    protected IKineticSource source;

    public TileEntityKineticGenerator() {
    }

    protected void onLoaded() {
        super.onLoaded();
        this.updateSource();
    }

    protected void setFacing(EnumFacing facing) {
        super.setFacing(facing);
        this.updateSource();
    }

    protected void onNeighborChange(Block neighbor, BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        if (this.getPos().offset(this.getFacing()).equals(neighborPos)) {
            this.updateSource();
        }

    }

    protected void updateSource() {
        if (this.source == null || ((TileEntity)this.source).isRemoved()) {
            TileEntity te = this.world.getTileEntity(this.pos.offset(this.getFacing()));
            if (te instanceof IKineticSource) {
                this.source = (IKineticSource)te;
            } else {
                this.source = null;
            }
        }

    }

    protected int getEnergyAvailable() {
        if (this.source != null) {
            assert !((TileEntity)this.source).isRemoved();

            return this.source.drawKineticEnergy(this.getFacing().getOpposite(), this.source.getConnectionBandwidth(this.getFacing().getOpposite()), true);
        } else {
            return 0;
        }
    }

    protected void drawEnergyAvailable(int amount) {
        if (this.source != null) {
            assert !((TileEntity)this.source).isRemoved();

            this.source.drawKineticEnergy(this.getFacing().getOpposite(), amount, false);
        } else {
            assert false;
        }

    }

    protected double getMultiplier() {
        return this.euPerKu;
    }
}
