//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor.batpack;

import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmorLappack extends ItemArmorElectric implements IBatpack {
    public ItemArmorLappack(int capacity) {
        super(ItemName.lappack, ArmorMaterial.LEATHER, "lappack", EntityEquipmentSlot.CHEST, capacity, 2500.0, 5);
    }

    public boolean canProvideEnergy(ItemStack stack) {
        return true;
    }

    public double getDamageAbsorptionRatio() {
        return 0.0;
    }

    public int getEnergyPerDamage() {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }
}
