//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.generator.tileentity;

import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.ConfigUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityGenerator extends TileEntityBaseGenerator implements IGuiValueProvider {
    public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);
    @GuiSynced
    public int totalFuel = 0;

    public TileEntityGenerator() {
        super(Math.round(50.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/generator")), 1, 25000);
    }

    @SideOnly(Side.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        if (this.getActive()) {
            TileEntityIronFurnace.showFlames(this.getWorld(), this.pos, this.getFacing());
        }

    }

    public double getFuelRatio() {
        return this.fuel <= 0 ? 0.0 : (double)this.fuel / (double)this.totalFuel;
    }

    public boolean gainFuel() {
        int fuelValue = this.fuelSlot.consumeFuel() / 4;
        if (fuelValue == 0) {
            return false;
        } else {
            this.fuel += fuelValue;
            this.totalFuel = fuelValue;
            return true;
        }
    }

    public boolean isConverting() {
        return this.fuel > 0;
    }

    public String getOperationSoundFile() {
        return "Generators/GeneratorLoop.ogg";
    }

    public double getGuiValue(String name) {
        if ("fuel".equals(name)) {
            return this.getFuelRatio();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void read(NBTTagCompound nbt) {
        super.read(nbt);
        this.totalFuel = nbt.getInt("totalFuel");
    }

    public NBTTagCompound write(NBTTagCompound nbt) {
        super.write(nbt);
        nbt.putInt("totalFuel", this.totalFuel);
        return nbt;
    }
}
