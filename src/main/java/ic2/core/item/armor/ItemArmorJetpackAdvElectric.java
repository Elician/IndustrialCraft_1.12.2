package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.Platform;
import ic2.core.init.Localization;
import ic2.core.item.armor.jetpack.IBoostingJetpack;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemArmorJetpackAdvElectric extends ItemArmorElectric implements IBoostingJetpack {

    public ItemArmorJetpackAdvElectric() {

        super(ItemName.jetpack_advelectric, ArmorMaterial.DIAMOND, "advjetpack", EntityEquipmentSlot.CHEST, 8000000, 4000, 3);

        this.func_77656_e(27);
        this.func_77625_d(1);
        this.setNoRepair();
    }

    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    public static boolean isJetpackOn(ItemStack stack) {
        return StackUtil.getOrCreateNbtData(stack).getBoolean("isFlyActive");
    }

    public static boolean isHovering(ItemStack stack) {
        return StackUtil.getOrCreateNbtData(stack).getBoolean("hoverMode");
    }

    public static boolean switchJetpack(ItemStack stack) {
        NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        boolean newMode;
        nbt.putBoolean("isFlyActive", newMode = !nbt.getBoolean("isFlyActive"));
        return newMode;
    }

    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        byte toggleTimer = nbt.getByte("toggleTimer");
        if (IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
            toggleTimer = 10;
            nbt.putByte("toggleTimer", (byte)10);
            if (!world.isRemote) {
                String mode;
                if (switchJetpack(stack)) {
                    mode = TextFormatting.DARK_GREEN + Localization.translate("jetpack.message.on");
                } else {
                    mode = TextFormatting.DARK_RED + Localization.translate("jetpack.message.off");
                }

                Platform.messageTranslationPlayer(player, "jetpack.message.switch", TextFormatting.YELLOW, mode);
            }
        }

        if (toggleTimer > 0 && !isJetpackOn(stack)) {
            --toggleTimer;
            nbt.putByte("toggleTimer", toggleTimer);
        }

    }

    public boolean isJetpackActive(ItemStack stack) {
        return isJetpackOn(stack);
    }

    public double getChargeLevel(ItemStack stack) {
        return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
    }

    public float getPower(ItemStack stack) {
        return 1.0F;
    }

    public float getDropPercentage(ItemStack stack) {
        return 0.05F;
    }

    public float getBaseThrust(ItemStack stack, boolean hover) {
        return hover ? 0.65F : 0.3F;
    }

    public float getBoostThrust(EntityPlayer player, ItemStack stack, boolean hover) {
        return IC2.keyboard.isBoostKeyDown(player) && ElectricItem.manager.getCharge(stack) >= 60.0 ? (hover ? 0.07F : 0.09F) : 0.0F;
    }

    public boolean useBoostPower(ItemStack stack, float boostAmount) {
        return ElectricItem.manager.discharge(stack, 60.0, Integer.MAX_VALUE, true, false, false) > 0.0;
    }

    public float getWorldHeightDivisor(ItemStack stack) {
        return 1.0F;
    }

    public float getHoverMultiplier(ItemStack stack, boolean upwards) {
        return 0.2F;
    }

    public float getHoverBoost(EntityPlayer player, ItemStack stack, boolean up) {
        if (IC2.keyboard.isBoostKeyDown(player) && ElectricItem.manager.getCharge(stack) >= 60.0) {
            if (!player.onGround) {
                ElectricItem.manager.discharge(stack, 60.0, Integer.MAX_VALUE, true, false, false);
            }

            return 2.0F;
        } else {
            return 1.0F;
        }
    }

    public boolean drainEnergy(ItemStack pack, int amount) {
        return ElectricItem.manager.discharge(pack, (amount * 6), Integer.MAX_VALUE, true, false, false) > 0.0;
    }

    public boolean canProvideEnergy(ItemStack stack) {
        return true;
    }

    public int getEnergyPerDamage() {
        return 0;
    }

    public double getDamageAbsorptionRatio() {
        return 0.0;
    }
}
