//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.armor;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IHazmatLike;
import ic2.api.item.IItemHudProvider;
import ic2.core.IC2;
import ic2.core.IC2Potion;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.item.ItemTinCan;
import ic2.core.item.armor.batpack.IBatpack;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.item.utils.EnergyHelper;
import ic2.core.ref.ItemName;
import ic2.core.slot.ArmorSlot;
import ic2.core.slot.SlotArmor;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class ItemArmorMetaQuantumSuit extends ItemArmorElectric implements IBatpack, IJetpack, IHazmatLike, IItemHudProvider {
  private static final int defaultColor = -1;
  protected static final Map<Potion, Integer> potionRemovalCost = new IdentityHashMap<>();
  private float jumpCharge;

  public ItemArmorMetaQuantumSuit(ItemName name, EntityEquipmentSlot armorType) {
    super(name, "meta_quantum", armorType, 50000000, 100000.0, 5);
    if (armorType == EntityEquipmentSlot.FEET) {
      MinecraftForge.EVENT_BUS.register(this);
    }

    potionRemovalCost.put(MobEffects.POISON, 14000);
    potionRemovalCost.put(IC2Potion.radiation, 20000);
    potionRemovalCost.put(MobEffects.WITHER, 30000);
  }

  public boolean canProvideEnergy(ItemStack stack) {
        return true;
    }

  protected boolean hasOverlayTexture() {
    return true;
  }

  public boolean func_82816_b_(ItemStack stack) {
    return this.func_82814_b(stack) != -1;
  }

  public void func_82815_c(ItemStack stack) {
    NBTTagCompound nbt = this.getDisplayNbt(stack, false);
    if (nbt != null && nbt.contains("color", 3)) {
      nbt.remove("color");
      if (nbt.func_82582_d()) {
        stack.getTag().remove("display");
      }

    }
  }

  public int func_82814_b(ItemStack stack) {
    NBTTagCompound nbt = this.getDisplayNbt(stack, false);
    return nbt != null && nbt.contains("color", 3) ? nbt.getInt("color") : -1;
  }

  public void func_82813_b(ItemStack stack, int color) {
    NBTTagCompound nbt = this.getDisplayNbt(stack, true);
    nbt.putInt("color", color);
  }

  private NBTTagCompound getDisplayNbt(ItemStack stack, boolean create) {
    NBTTagCompound nbt = stack.getTag();
    if (nbt == null) {
      if (!create) {
        return null;
      }

      nbt = new NBTTagCompound();
      stack.setTag(nbt);
    }

    NBTTagCompound ret;
    if (!nbt.contains("display", 10)) {
      if (!create) {
        return null;
      }

      ret = new NBTTagCompound();
      nbt.func_74782_a("display", ret);
    } else {
      ret = nbt.getCompound("display");
    }

    return ret;
  }

  public boolean addsProtection(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack stack) {
    return ElectricItem.manager.getCharge(stack) > 0.0;
  }

  public ArmorProperties getProperties(EntityLivingBase entity, ItemStack armor, DamageSource source, double damage, int slot) {
    int energyPerDamage = this.getEnergyPerDamage();
    int damageLimit = Integer.MAX_VALUE;
    if (energyPerDamage > 0) {
      damageLimit = (int) Math.min(damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / (double) energyPerDamage);
    }

    if (source == DamageSource.FALL) {
      if (this.slot == EntityEquipmentSlot.FEET) {
        return new ArmorProperties(10, 1.0, damageLimit);
      }

      if (this.slot == EntityEquipmentSlot.LEGS) {
        return new ArmorProperties(9, 0.8, damageLimit);
      }
    }

    double absorptionRatio = this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio();
    return new ArmorProperties(8, absorptionRatio, damageLimit);
  }

  @SubscribeEvent(
      priority = EventPriority.LOW
  )
  public void onEntityLivingFallEvent(LivingFallEvent event) {
    if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityLivingBase) {
      EntityLivingBase entity = (EntityLivingBase) event.getEntity();
      ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
      if (armor.getItem() == this) {
        int fallDamage = Math.max((int) event.getDistance() - 10, 0);
        double energyCost = this.getEnergyPerDamage() * fallDamage;
        if (energyCost <= ElectricItem.manager.getCharge(armor)) {
          ElectricItem.manager.discharge(armor, energyCost, Integer.MAX_VALUE, true, false, false);
          event.setCanceled(true);
        }
      }
    }

  }

  public double getDamageAbsorptionRatio() {
    return this.slot == EntityEquipmentSlot.CHEST ? 1.2 : 1.0;
  }

  public int getEnergyPerDamage() {
    return 10000;
  }

  @SideOnly(Side.CLIENT)
  public EnumRarity getRarity(ItemStack stack) {
    return EnumRarity.EPIC;
  }

  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

    super.onArmorTick(world, player, stack);

    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    byte toggleTimer = nbtData.getByte("toggleTimer");
    boolean ret = false;
    int skylight;
    switch (this.slot) {
      case HEAD:
        IC2.platform.profilerStartSection("QuantumHelmet");
        int air = player.getAir();

        double chargeAmount = TileEntitySolarGenerator.getSkyLight(player.getEntityWorld(), player.getPosition());
        if (chargeAmount > 0.0) {
          for (int col = 0; col < ArmorSlot.getCount(); ++col) {

            ItemStack stackArmor = player.inventory.armorInventory.get(col);

            if (!EnergyHelper.isEnergyContainerItem(stackArmor)) continue;

            IEnergyContainerItem item = (IEnergyContainerItem) stackArmor.getItem();
            int receive = item.receiveEnergy(stackArmor, (int) (chargeAmount * 50000), false);

            if (receive > 0) ret = true;
          }
        }

        if (ElectricItem.manager.canUse(stack, 1000.0) && air < 100) {
          player.setAir(air + 200);
          ElectricItem.manager.use(stack, 1000.0, null);
          ret = true;
        } else if (air <= 0) {
          IC2.achievements.issueAchievement(player, "starveWithQHelmet");
        }

        if (ElectricItem.manager.canUse(stack, 1000.0) && player.getFoodStats().needFood()) {
          int slot = -1;

          for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
            ItemStack playerStack = player.inventory.mainInventory.get(i);
            if (!StackUtil.isEmpty(playerStack) && playerStack.getItem() == ItemName.filled_tin_can.getInstance()) {
              slot = i;
              break;
            }
          }

          if (slot > -1) {
            ItemStack playerStack = player.inventory.mainInventory.get(slot);
            ItemTinCan can = (ItemTinCan) playerStack.getItem();
            ActionResult<ItemStack> result = can.onEaten(player, playerStack);
            playerStack = result.getResult();
            if (StackUtil.isEmpty(playerStack)) {
              player.inventory.mainInventory.set(slot, StackUtil.emptyStack);
            }

            if (result.getType() == EnumActionResult.SUCCESS) {
              ElectricItem.manager.use(stack, 1000.0, null);
            }

            ret = true;
          }
        } else if (player.getFoodStats().getFoodLevel() <= 0) {
          IC2.achievements.issueAchievement(player, "starveWithQHelmet");
        }

        for (Object o : new LinkedList(player.getActivePotionEffects())) {
          PotionEffect effect = (PotionEffect) o;
          Potion potion = effect.getPotion();
          Integer cost = potionRemovalCost.get(potion);
          if (cost != null) {
            cost = cost * (effect.getAmplifier() + 1);
            if (ElectricItem.manager.canUse(stack, (double) cost)) {
              ElectricItem.manager.use(stack, (double) cost, null);
              IC2.platform.removePotion(player, potion);
            }
          }
        }

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
          BlockPos pos = new BlockPos((int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
          skylight = player.getEntityWorld().func_175671_l(pos);
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
        break;
      case CHEST:
        IC2.platform.profilerStartSection("QuantumBodyarmor");
        player.extinguish();
        IC2.platform.profilerEndSection();
        break;
      case LEGS:
        IC2.platform.profilerStartSection("QuantumLeggings");
        boolean enableQuantumSpeedOnSprint;
        if (IC2.platform.isRendering()) {
          enableQuantumSpeedOnSprint = ConfigUtil.getBool(MainConfig.get(), "misc/quantumSpeedOnSprint");
        } else {
          enableQuantumSpeedOnSprint = true;
        }

        if (ElectricItem.manager.canUse(stack, 1000.0) && (player.onGround || player.isInWater()) && IC2.keyboard.isForwardKeyDown(player) && (enableQuantumSpeedOnSprint && player.isSprinting() || !enableQuantumSpeedOnSprint && IC2.keyboard.isBoostKeyDown(player))) {
          skylight = nbtData.getByte("speedTicker");
          skylight = (byte) (skylight + 1);
          if (skylight >= 10) {
            skylight = 0;
            ElectricItem.manager.use(stack, 1000.0, null);
            ret = true;
          }

          nbtData.putByte("speedTicker", (byte) skylight);
          float speed = 0.22F;
          if (player.isInWater()) {
            speed = 0.1F;
            if (IC2.keyboard.isJumpKeyDown(player)) {
              player.field_70181_x += 0.10000000149011612;
            }
          }

          if (speed > 0.0F) {
            player.func_191958_b(0.0F, 0.0F, 1.0F, speed);
          }
        }

        IC2.platform.profilerEndSection();
        break;
      case FEET:
        IC2.platform.profilerStartSection("QuantumBoots");
        if (IC2.platform.isSimulating()) {
          boolean wasOnGround = nbtData.contains("wasOnGround") ? nbtData.getBoolean("wasOnGround") : true;
          if (wasOnGround && !player.onGround && IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isBoostKeyDown(player)) {
            ElectricItem.manager.use(stack, 4000.0, null);
            ret = true;
          }

          if (player.onGround != wasOnGround) {
            nbtData.putBoolean("wasOnGround", player.onGround);
          }
        } else {
          if (ElectricItem.manager.canUse(stack, 4000.0) && player.onGround) {
            this.jumpCharge = 1.0F;
          }

          if (player.field_70181_x >= 0.0 && this.jumpCharge > 0.0F && !player.isInWater()) {
            if (IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isBoostKeyDown(player)) {
              if (this.jumpCharge == 1.0F) {
                player.field_70159_w *= 3.5;
                player.field_70179_y *= 3.5;
              }

              player.field_70181_x += (this.jumpCharge * 0.3F);
              this.jumpCharge = (float) ((double) this.jumpCharge * 0.75);
            } else if (this.jumpCharge < 1.0F) {
              this.jumpCharge = 0.0F;
            }
          }
        }

        IC2.platform.profilerEndSection();
    }

    if (ret) {
      player.container.detectAndSendChanges();
    }

  }

  public int getItemEnchantability() {
    return 0;
  }

  public boolean drainEnergy(ItemStack pack, int amount) {
    return ElectricItem.manager.discharge(pack, (amount + 6), Integer.MAX_VALUE, true, false, false) > 0.0;
  }

  public float getPower(ItemStack stack) {
    return 1.0F;
  }

  public float getDropPercentage(ItemStack stack) {
    return 0.05F;
  }

  public double getChargeLevel(ItemStack stack) {
    return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
  }

  public boolean isJetpackActive(ItemStack stack) {
    return true;
  }

  public float getHoverMultiplier(ItemStack stack, boolean upwards) {
    return 0.1F;
  }

  public float getWorldHeightDivisor(ItemStack stack) {
    return 0.9F;
  }

  public boolean doesProvideHUD(ItemStack stack) {
    return this.slot == EntityEquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0;
  }

  public HudMode getHudMode(ItemStack stack) {
    return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).getShort("HudMode"));
  }
}
