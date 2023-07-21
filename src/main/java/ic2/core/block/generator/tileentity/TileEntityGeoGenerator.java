

package ic2.core.block.generator.tileentity;

import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.block.invslot.InvSlot.InvSide;
import ic2.core.block.invslot.InvSlotConsumableLiquid.OpType;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.ConfigUtil;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class TileEntityGeoGenerator extends TileEntityBaseGenerator {
    private static final int fluidPerTick = 2;
    public final InvSlotConsumableLiquid fluidSlot;
    public final InvSlotOutput outputSlot;
    @GuiSynced
    protected final FluidTank fluidTank;
    protected final Fluids fluids = this.addComponent(new Fluids(this));

    public TileEntityGeoGenerator() {
        super(100.0, 1, 40000);
        this.fluidTank = this.fluids.addTankInsert("fluid", 10000, Fluids.fluidPredicate(FluidRegistry.LAVA));
        this.production = Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/geothermal"));
        this.fluidSlot = new InvSlotConsumableLiquidByTank(this, "fluidSlot", Access.I, 1, InvSide.ANY, OpType.Drain, this.fluidTank);
        this.outputSlot = new InvSlotOutput(this, "output", 1);
    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot)) {
            this.markDirty();
        }

    }

    public boolean gainFuel() {
        boolean dirty = false;
        FluidStack ret = this.fluidTank.drainInternal(fluidPerTick, false);
        if (ret != null && ret.amount >= fluidPerTick) {
            this.fluidTank.drainInternal(fluidPerTick, true);
            ++this.fuel;
            dirty = true;
        }

        return dirty;
    }

    public String getOperationSoundFile() {
        return "Generators/GeothermalLoop.ogg";
    }

    protected void onBlockBreak() {
        super.onBlockBreak();
        FluidEvent.fireEvent(new FluidEvent.FluidSpilledEvent(new FluidStack(FluidRegistry.LAVA, 1000), this.getWorld(), this.pos));
    }
}
