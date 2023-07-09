//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.recipe.BasicMachineRecipeManager;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityMacerator extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> {
    public static List<Map.Entry<ItemStack, ItemStack>> recipes = new Vector<>();

    public TileEntityMacerator() {
        super(120, 20000, 1);
        this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.macerator);
    }

    public static void init() {
        Recipes.macerator = new BasicMachineRecipeManager();
    }

    @SideOnly(Side.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        World world = this.getWorld();
        if (this.getActive() && world.rand.nextInt(8) == 0) {
            for(int i = 0; i < 4; ++i) {
                double x = (double)this.pos.getX() + 0.5 + (double)world.rand.nextFloat() * 0.6 - 0.3;
                double y = (double)(this.pos.getY() + 1) + (double)world.rand.nextFloat() * 0.2 - 0.1;
                double z = (double)this.pos.getZ() + 0.5 + (double)world.rand.nextFloat() * 0.6 - 0.3;
                world.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0, 0.0, 0.0, new int[0]);
            }
        }

    }

    public String getStartSoundFile() {
        return "Machines/MaceratorOp.ogg";
    }

    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }

    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
