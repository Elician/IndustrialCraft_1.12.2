
package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.IC2;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.recipe.BasicListRecipeManager;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityRecycler extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> {
    public TileEntityRecycler() {
        super(5, 250, 1);
        this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.recycler);
    }

    public static void init() {
        Recipes.recycler = new RecyclerRecipeManager();
        Recipes.recyclerWhitelist = new BasicListRecipeManager();
        Recipes.recyclerBlacklist = new BasicListRecipeManager();
    }

    public static void initLate() {
        IRecipeInput input;

        Iterator<IRecipeInput> var0 = ConfigUtil.asRecipeInputList(MainConfig.get(), "balance/recyclerBlacklist").iterator();

        while(var0.hasNext()) {
            input = var0.next();
            Recipes.recyclerBlacklist.add(input);
        }

        var0 = ConfigUtil.asRecipeInputList(MainConfig.get(), "balance/recyclerWhitelist").iterator();

        while(var0.hasNext()) {
            input = var0.next();
            Recipes.recyclerWhitelist.add(input);
        }

    }

    public static int recycleChance() {
        return 6;
    }

    public String getStartSoundFile() {
        return "Machines/RecyclerOp.ogg";
    }

    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }

    public static boolean getIsItemBlacklisted(ItemStack aStack) {
        if (Recipes.recyclerWhitelist.isEmpty()) {
            return Recipes.recyclerBlacklist.contains(aStack);
        } else {
            return !Recipes.recyclerWhitelist.contains(aStack);
        }
    }

    public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, Collection<ItemStack> processResult) {
        this.inputSlot.consume(result);
        if (IC2.random.nextInt(recycleChance()) == 0) {
            this.outputSlot.add(processResult);
        }

    }

    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }

    private static class RecyclerRecipeManager implements IBasicMachineRecipeManager {
        public RecyclerRecipeManager() {
        }

        public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
            return false;
        }

        public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, ItemStack... outputs) {
            return false;
        }

        public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
            if (StackUtil.isEmpty(input)) {
                return null;
            } else {
                RecipeOutput ret = new RecipeOutput(null, new ArrayList<>(getOutput(input)));
                if (adjustInput) {
                    input.shrink(1);
                }

                return ret;
            }
        }

        private static Collection<ItemStack> getOutput(ItemStack input) {
            return TileEntityRecycler.getIsItemBlacklisted(input) ? Collections.emptyList() : Collections.singletonList(ItemName.crafting.getItemStack(CraftingItemType.scrap));
        }

        public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
            return StackUtil.isEmpty(input) ? null : (new MachineRecipe(Recipes.inputFactory.forStack(input, 1), getOutput(input))).getResult(StackUtil.copyWithSize(input, StackUtil.getSize(input) - 1));
        }

        public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
            throw new UnsupportedOperationException();
        }

        public boolean isIterable() {
            return false;
        }
    }
}
