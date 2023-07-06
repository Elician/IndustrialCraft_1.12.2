//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IItemHudProvider;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmorNanoSuit extends ItemArmorElectric implements IItemHudProvider {
  public ItemArmorNanoSuit(ItemName name, EntityEquipmentSlot armorType) {
    super(name, "nano", armorType, 2000000.0, 2000.0, 3);
    if (armorType == EntityEquipmentSlot.FEET) {
      MinecraftForge.EVENT_BUS.register(this);
    }

  }

  public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
    if (source == DamageSource.FALL && this.slot == EntityEquipmentSlot.FEET) {
      int energyPerDamage = this.getEnergyPerDamage();
      int damageLimit = Integer.MAX_VALUE;
      if (energyPerDamage > 0) {
        damageLimit = (int)Math.min(damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / (double)energyPerDamage);
      }

      return new ISpecialArmor.ArmorProperties(10, damage < 8.0 ? 1.0 : 0.875, damageLimit);
    } else {
      return super.getProperties(player, armor, source, damage, slot);
    }
  }

  @SubscribeEvent(
    priority = EventPriority.LOW
  )
  public void onEntityLivingFallEvent(LivingFallEvent event) {
    if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityLivingBase) {
      EntityLivingBase entity = (EntityLivingBase)event.getEntity();
      ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
      if (armor != null && armor.getItem() == this) {
        int fallDamage = (int)event.getDistance() - 3;
        if (fallDamage >= 8) {
          return;
        }

        double energyCost = this.getEnergyPerDamage() * fallDamage;
        if (energyCost <= ElectricItem.manager.getCharge(armor)) {
          ElectricItem.manager.discharge(armor, energyCost, Integer.MAX_VALUE, true, false, false);
          event.setCanceled(true);
        }
      }
    }

  }

  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    byte toggleTimer = nbtData.getByte("toggleTimer");
    boolean ret = false;
    if (this.slot == EntityEquipmentSlot.HEAD) {
      IC2.platform.profilerStartSection("NanoHelmet");
      boolean Nightvision = nbtData.getBoolean("Nightvision");
      short hubmode = nbtData.getShort("HudMode");
      if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
        toggleTimer = 10;
        Nightvision = !Nightvision;
        if (IC2.platform.isSimulating()) {
          nbtData.putBoolean("Nightvision", Nightvision);
          if (Nightvision) {
            IC2.platform.messagePlayer(player, "Nightvision enabled.");
          } else {
            IC2.platform.messagePlayer(player, "Nightvision disabled.");
          }
        }
      }

      if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isHudModeKeyDown(player) && toggleTimer == 0) {
        toggleTimer = 10;
        if (hubmode == HudMode.getMaxMode()) {
          hubmode = 0;
        } else {
          ++hubmode;
        }

        if (IC2.platform.isSimulating()) {
          nbtData.putShort("HudMode", hubmode);
          IC2.platform.messagePlayer(player, Localization.translate(HudMode.getFromID(hubmode).getTranslationKey()), new Object[0]);
        }
      }

      if (IC2.platform.isSimulating() && toggleTimer > 0) {
        --toggleTimer;
        nbtData.putByte("toggleTimer", toggleTimer);
      }

      if (Nightvision && IC2.platform.isSimulating() && ElectricItem.manager.use(stack, 1.0, player)) {
        BlockPos pos = new BlockPos((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
        int skylight = player.getEntityWorld().func_175671_l(pos);
        if (skylight > 5) {
          IC2.platform.removePotion(player, MobEffects.NIGHT_VISION);
          player.func_70690_d(new PotionEffect(MobEffects.BLINDNESS, 100, 0, true, true));
        } else {
          IC2.platform.removePotion(player, MobEffects.BLINDNESS);
          player.func_70690_d(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, true));
        }

        ret = true;
      }

      IC2.platform.profilerEndSection();
    }

    if (ret) {
      player.container.detectAndSendChanges();
    }

  }

  public double getDamageAbsorptionRatio() {
    return 0.9;
  }

  public int getEnergyPerDamage() {
    return 3500;
  }

  @SideOnly(Side.CLIENT)
  public EnumRarity getRarity(ItemStack stack) {
    return EnumRarity.UNCOMMON;
  }

  public boolean doesProvideHUD(ItemStack stack) {
    return this.slot == EntityEquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0;
  }

  public HudMode getHudMode(ItemStack stack) {
    return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).getShort("HudMode"));
  }
}
