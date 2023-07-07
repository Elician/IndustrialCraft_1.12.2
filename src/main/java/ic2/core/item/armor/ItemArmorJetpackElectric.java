//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.ItemName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;

public class ItemArmorJetpackElectric extends ItemArmorElectric implements IJetpack {
    public ItemArmorJetpackElectric() {
        super(ItemName.jetpack_electric, ArmorMaterial.IRON, "jetpack", EntityEquipmentSlot.CHEST, 120000, 250, 1);
        this.func_77656_e(27);
        this.func_77625_d(1);
        this.setNoRepair();
    }

    public boolean drainEnergy(ItemStack pack, int amount) {
        return ElectricItem.manager.discharge(pack, (amount + 6), Integer.MAX_VALUE, true, false, false) > 0.0;
    }

    public float getPower(ItemStack stack) {
        return 0.7F;
    }

    public float getDropPercentage(ItemStack stack) {
        return 0.05F;
    }

    public boolean isJetpackActive(ItemStack stack) {
        return true;
    }

    public double getChargeLevel(ItemStack stack) {
        return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
    }

    public float getHoverMultiplier(ItemStack stack, boolean upwards) {
        return 0.1F;
    }

    public float getWorldHeightDivisor(ItemStack stack) {
        return 1.28F;
    }

    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
        return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
    }

    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return 0;
    }

    public int getEnergyPerDamage() {
        return 0;
    }

    public double getDamageAbsorptionRatio() {
        return 0.0;
    }
}
