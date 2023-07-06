//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.item.block;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.wiring.CableType;
import ic2.core.item.ItemIC2;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCable extends ItemIC2 implements IMultiItem<CableType>, IBoxable {
  private final List<ItemStack> variants = new ArrayList<>();

  public ItemCable() {
    super(ItemName.cable);
    this.func_77627_a(true);
    CableType[] var1 = CableType.values;

    for (CableType type : var1) {
      for (int insulation = 0; insulation <= type.maxInsulation; ++insulation) {
        this.variants.add(getCable(type, insulation));
      }
    }

  }

  @SideOnly(Side.CLIENT)
  public void registerModels(ItemName name) {
    final ResourceLocation loc = Util.getName(this);
    ModelLoader.setCustomMeshDefinition(this, stack -> ItemCable.getModelLocation(loc, stack));
    Iterator var3 = this.variants.iterator();

    while(var3.hasNext()) {
      ItemStack stack = (ItemStack)var3.next();
      ModelBakery.registerItemVariants(this, getModelLocation(loc, stack));
    }

  }

  static ModelResourceLocation getModelLocation(ResourceLocation loc, ItemStack stack) {
    return new ModelResourceLocation(new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + getName(stack)), (String)null);
  }

  public ItemStack getItemStack(CableType type) {
    return getCable(type, 0);
  }

  public ItemStack getItemStack(String variant) {
    int pos = 0;
    CableType type = null;

    int insulation;
    int nextPos;
    for(insulation = 0; pos < variant.length(); pos = nextPos + 1) {
      nextPos = variant.indexOf(44, pos);
      if (nextPos == -1) {
        nextPos = variant.length();
      }

      int sepPos = variant.indexOf(58, pos);
      if (sepPos == -1 || sepPos >= nextPos) {
        return null;
      }

      String key = variant.substring(pos, sepPos);
      String value = variant.substring(sepPos + 1, nextPos);
      if (key.equals("type")) {
        type = CableType.get(value);
        if (type == null) {
          IC2.log.warn(LogCategory.Item, "Invalid cable type: %s", value);
        }
      } else if (key.equals("insulation")) {
        try {
          insulation = Integer.parseInt(value);
        } catch (NumberFormatException var10) {
          IC2.log.warn(LogCategory.Item, "Invalid cable insulation: %s", value);
        }
      }
    }

    if (type == null) {
      return null;
    } else if (insulation >= 0 && insulation <= type.maxInsulation) {
      return getCable(type, insulation);
    } else {
      IC2.log.warn(LogCategory.Item, "Invalid cable insulation: %d", insulation);
      return null;
    }
  }

  public String getVariant(ItemStack stack) {
    if (stack == null) {
      throw new NullPointerException("null stack");
    } else if (stack.getItem() != this) {
      throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this);
    } else {
      CableType type = getCableType(stack);
      int insulation = getInsulation(stack);
      return "type:" + type.getName() + ",insulation:" + insulation;
    }
  }

  public static ItemStack getCable(CableType type, int insulation) {
    ItemStack ret = new ItemStack(ItemName.cable.getInstance(), 1, type.getId());
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(ret);
    nbt.putByte("type", (byte)type.ordinal());
    nbt.putByte("insulation", (byte)insulation);
    return ret;
  }

  private static CableType getCableType(ItemStack stack) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    int type = nbt.getByte("type") & 255;
    return type < CableType.values.length ? CableType.values[type] : CableType.copper;
  }

  private static int getInsulation(ItemStack stack) {
    CableType type = getCableType(stack);
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    int insulation = nbt.getByte("insulation") & 255;
    return Math.min(insulation, type.maxInsulation);
  }

  private static String getName(ItemStack stack) {
    CableType type = getCableType(stack);
    int insulation = getInsulation(stack);
    return type.getName(insulation, null);
  }

  public String getTranslationKey(ItemStack stack) {
    return super.getTranslationKey(stack) + "." + getName(stack);
  }

  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, World world, List<String> info, ITooltipFlag b) {
  }

  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

    return EnumActionResult.FAIL;

  }

  public void fillItemGroup(CreativeTabs tab, NonNullList<ItemStack> itemList) {
    if (this.isInGroup(tab)) {
      List<ItemStack> variants = new ArrayList<>(this.variants);
      if (IC2.version.isClassic()) {
        variants.remove(11);
      }

      itemList.addAll(variants);
    }
  }

  public Set<CableType> getAllTypes() {
    return EnumSet.allOf(CableType.class);
  }

  public Set<ItemStack> getAllStacks() {
    return new HashSet<>(this.variants);
  }

  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
}
