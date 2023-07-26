//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.tool;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IMiningDrill;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.Iterator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDrill extends ItemElectricTool implements IMiningDrill, IHitSoundOverride {
  public ItemDrill(ItemName name, int operationEnergyCost, HarvestLevel harvestLevel, int maxCharge, int transferLimit, int tier, float efficiency) {
    super(name, operationEnergyCost, harvestLevel, EnumSet.of(ToolClass.Pickaxe, ToolClass.Shovel));

    this.maxCharge = maxCharge;
    this.transferLimit = transferLimit;
    this.tier = tier;
    this.efficiency = efficiency;
  }

  @SideOnly(Side.CLIENT)
  public String getHitSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
    return null;
  }

  @SideOnly(Side.CLIENT)
  public String getBreakSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
    if (player.abilities.isCreativeMode) {
      return null;
    } else {
      IBlockState state = world.getBlockState(pos);
      float hardness = state.getBlockHardness(world, pos);
      return !(hardness > 1.0F) && !(hardness < 0.0F) ? "Tools/Drill/DrillSoft.ogg" : "Tools/Drill/DrillHard.ogg";
    }
  }

  public float getDestroySpeed(ItemStack stack, IBlockState state) {
    float speed = super.getDestroySpeed(stack, state);
    EntityPlayer player = getPlayerHoldingItem(stack);
    if (player != null) {
      if (player.func_70055_a(Material.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
        speed *= 5.0F;
      }

      if (!player.onGround) {
        speed *= 5.0F;
      }
    }

    return speed;
  }

  private static EntityPlayer getPlayerHoldingItem(ItemStack stack) {
    if (IC2.platform.isRendering()) {
      EntityPlayer player = IC2.platform.getPlayerInstance();
      if (player != null && player.inventory.getCurrentItem() == stack) {
        return player;
      }
    } else {

      for (net.minecraft.entity.player.EntityPlayerMP entityPlayerMP : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
        EntityPlayer player = entityPlayerMP;
        if (player.inventory.getCurrentItem() == stack) {
          return player;
        }
      }
    }

    return null;
  }

  @Override
  public boolean isBookEnchantable(ItemStack itemStack1, ItemStack itemStack2) {
    return true;
  }

  @Override
  public int getItemEnchantability() {
    return this.field_77862_b.func_77995_e();
  }

  public int energyUse(ItemStack stack, World world, BlockPos pos, IBlockState state) {
    if (stack.getItem() == ItemName.drill.getInstance()) {
      return 15;
    } else if (stack.getItem() == ItemName.diamond_drill.getInstance()) {
      return 70;
    } else if (stack.getItem() == ItemName.iridium_drill.getInstance()) {
      return 800;
    } else {
      throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
    }
  }

  public int breakTime(ItemStack stack, World world, BlockPos pos, IBlockState state) {
    if (stack.getItem() == ItemName.drill.getInstance()) {
      return 170;
    } else if (stack.getItem() == ItemName.diamond_drill.getInstance()) {
      return 70;
    } else if (stack.getItem() == ItemName.iridium_drill.getInstance()) {
      return 20;
    } else {
      throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
    }
  }

  public boolean breakBlock(ItemStack stack, World world, BlockPos pos, IBlockState state) {
    if (stack.getItem() == ItemName.drill.getInstance()) {
      return this.tryUsePower(stack, 150.0);
    } else if (stack.getItem() == ItemName.diamond_drill.getInstance()) {
      return this.tryUsePower(stack, 250.0);
    } else if (stack.getItem() == ItemName.iridium_drill.getInstance()) {
      return this.tryUsePower(stack, 800.0);
    } else {
      throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
    }
  }

  @Override
  public int getMaxEnergyStored(ItemStack itemStack) {

    int level = 0;

    Enchantment enchantment = Enchantment.getEnchantmentByID(12);

    if (enchantment != null) {
      level = EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack);
    }

    return this.maxCharge + (level * level * 10000);

  }
}
