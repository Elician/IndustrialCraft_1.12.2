package ic2.core.item.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public final class ItemStackHelper {

    public static NBTTagCompound getTagCompound(ItemStack stack) {
        NBTTagCompound tag = stack.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTag(tag);
        }
        return tag;
    }

    public static void setCoordinates(ItemStack stack, BlockPos pos) {
        NBTTagCompound tag = getTagCompound(stack);
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
    }

    public static ItemStack getStackWithEnergy(Item item, String name, double energy) {
        ItemStack stack = new ItemStack(item);
        NBTTagCompound tag = new NBTTagCompound();
        stack.setTag(tag);
        tag.putDouble(name, energy);
        return stack;
    }
}