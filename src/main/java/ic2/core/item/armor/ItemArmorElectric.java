//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.IPseudoDamageItem;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ISpecialArmor;

public abstract class ItemArmorElectric extends ItemArmorIC2 implements IEnergyContainerItem, ISpecialArmor, IPseudoDamageItem, IElectricItem, IItemHudInfo {
  protected final double maxCharge;
  protected final double transferLimit;
  protected final int tier;

  public ItemArmorElectric(ItemName name, String armorName, EntityEquipmentSlot armorType, double maxCharge, double transferLimit, int tier) {
    super(name, ArmorMaterial.DIAMOND, armorName, armorType, null);
    this.maxCharge = maxCharge;
    this.tier = tier;
    this.transferLimit = transferLimit;
    this.func_77656_e(27);
    this.func_77625_d(1);
    this.setNoRepair();
  }

  public int receiveEnergy(ItemStack var1, int var2, boolean var3) {

  }

  public int extractEnergy(ItemStack var1, int var2, boolean var3) {

  }

  public int getEnergyStored(ItemStack var1) {

  }

  public int getMaxEnergyStored(ItemStack var1) {
    
  }

  public int getItemEnchantability() {
    return 0;
  }

  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
    return false;
  }

  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList();
    info.add(ElectricItem.manager.getToolTip(stack));
    info.add(Localization.translate("ic2.item.tooltip.PowerTier", new Object[]{this.tier}));
    return info;
  }

  public void fillItemGroup(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (this.isInGroup(tab)) {
      ElectricItemManager.addChargeVariants(this, subItems);
    }
  }

  public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
    if (source.isUnblockable()) {
      return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
    } else {
      double absorptionRatio = this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio();
      int energyPerDamage = this.getEnergyPerDamage();
      int damageLimit = Integer.MAX_VALUE;
      if (energyPerDamage > 0) {
        damageLimit = (int)Math.min((double)damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / (double)energyPerDamage);
      }

      return new ISpecialArmor.ArmorProperties(0, absorptionRatio, damageLimit);
    }
  }

  public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
    return ElectricItem.manager.getCharge(armor) >= (double)this.getEnergyPerDamage() ? (int)Math.round(20.0 * this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio()) : 0;
  }

  public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
    ElectricItem.manager.discharge(stack, (double)(damage * this.getEnergyPerDamage()), Integer.MAX_VALUE, true, false, false);
  }

  public boolean canProvideEnergy(ItemStack stack) {
    return false;
  }

  public double getMaxCharge(ItemStack stack) {
    return this.maxCharge;
  }

  public int getTier(ItemStack stack) {
    return this.tier;
  }

  public double getTransferLimit(ItemStack stack) {
    return this.transferLimit;
  }

  public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
    return false;
  }

  public void setDamage(ItemStack stack, int damage) {
    int prev = this.getDamage(stack);
    if (damage != prev && BaseElectricItem.logIncorrectItemDamaging) {
      IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", new Object[]{damage - prev});
    }

  }

  public void setStackDamage(ItemStack stack, int damage) {
    super.setDamage(stack, damage);
  }

  public abstract double getDamageAbsorptionRatio();

  public abstract int getEnergyPerDamage();

  protected final double getBaseAbsorptionRatio() {
    switch (this.slot) {
      case HEAD:
        return 0.15;
      case CHEST:
        return 0.4;
      case LEGS:
        return 0.3;
      case FEET:
        return 0.15;
      default:
        return 0.0;
    }
  }
}
