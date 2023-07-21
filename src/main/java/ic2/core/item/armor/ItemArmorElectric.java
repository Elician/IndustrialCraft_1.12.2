//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor;

import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.util.EnergyContainerItemWrapper;
import com.google.common.collect.Iterables;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.IPseudoDamageItem;
import ic2.core.item.utils.EnergyHelper;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

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

  public ItemArmorElectric(ItemName name, ArmorMaterial armorMaterial, String armorName, EntityEquipmentSlot armorType, double maxCharge, double transferLimit, int tier) {
    super(name, armorMaterial, armorName, armorType, null);
    this.maxCharge = maxCharge;
    this.tier = tier;
    this.transferLimit = transferLimit;
    this.func_77656_e(27);
    this.func_77625_d(1);
    this.setNoRepair();
  }

  public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
    double energyReceived = Math.min(this.maxCharge - this.getEnergyStored(stack), maxReceive);

    if (!simulate) {
      ElectricItem.manager.charge(stack, energyReceived, maxReceive, true, false);
    }

    return (int) energyReceived;
  }

  public int extractEnergy(ItemStack stack, int maxReceive, boolean simulate) {
    double energyCost = Math.min(this.getEnergyStored(stack), maxReceive);

    if (!simulate) {
      ElectricItem.manager.discharge(stack, energyCost, Integer.MAX_VALUE, true, false, false);
    }

    return (int) energyCost;
  }


  @Override
  public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {

    if (!this.canProvideEnergy(stack)) return;
    if (slot != EntityEquipmentSlot.CHEST.getSlotIndex()) return;

    Iterable<ItemStack> equipment;
    EntityPlayer player = (EntityPlayer) entity;

    equipment = Iterables.concat(Arrays.asList(player.inventory.mainInventory, player.inventory.armorInventory, player.inventory.offHandInventory));

    for (ItemStack equipmentStack : equipment) {
      if (equipmentStack.equals(stack)) {
        continue;
      }
      if (EnergyHelper.isEnergyContainerItem(equipmentStack)) {
        extractEnergy(stack, ((IEnergyContainerItem) equipmentStack.getItem()).receiveEnergy(equipmentStack, getEnergyStored(stack), false), false);
      }
    }
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {

    return new EnergyContainerItemWrapper(stack, this);
  }

  public int getEnergyStored(ItemStack stack) {
    return (int) ElectricItem.manager.getCharge(stack);
  }

  public int getMaxEnergyStored(ItemStack stack) {
    return (int) this.getMaxCharge(stack);
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
    List<String> info = new LinkedList<>();
    info.add(ElectricItem.manager.getToolTip(stack));
    info.add(Localization.translate("ic2.item.tooltip.PowerTier", this.tier));
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
        damageLimit = (int)Math.min(damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / (double)energyPerDamage);
      }

      return new ISpecialArmor.ArmorProperties(0, absorptionRatio, damageLimit);
    }
  }

  public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
    return ElectricItem.manager.getCharge(armor) >= (double)this.getEnergyPerDamage() ? (int)Math.round(20.0 * this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio()) : 0;
  }

  public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
    ElectricItem.manager.discharge(stack, (damage * this.getEnergyPerDamage()), Integer.MAX_VALUE, true, false, false);
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
