package ic2.core.item.tool;

import com.google.common.base.CaseFormat;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.Platform;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;

import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolAdvChainsaw extends ItemElectricTool {

    public ItemToolAdvChainsaw() {
        super(ItemName.advchainsaw, 1000, HarvestLevel.Iron, EnumSet.of(ToolClass.Axe, ToolClass.Sword, ToolClass.Shears));
        this.maxCharge = 160000;
        this.transferLimit = 2000;
        this.tier = 2;
        this.efficiency = 30.0F;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && IC2.keyboard.isModeSwitchKeyDown(player)) {
            ItemStack stack = StackUtil.get(player, hand);
            NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
            if (nbt.getBoolean("disableShear")) {
                nbt.putBoolean("disableShear", false);
                Platform.messageTranslationPlayer(player, "ic2.advchainsaw.shear", TextFormatting.DARK_GREEN, Localization.translate("gravisuite.message.on"));
            } else {
                nbt.putBoolean("disableShear", true);
                Platform.messageTranslationPlayer(player, "ic2.advchainsaw.shear", TextFormatting.DARK_RED, Localization.translate("gravisuite.message.off"));
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        } else {
            return super.onItemRightClick(world, player, hand);
        }
    }

    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        ElectricItem.manager.use(stack, this.operationEnergyCost, attacker);
        if (attacker instanceof EntityPlayer && target instanceof EntityCreeper && target.getHealth() <= 0.0F) {
            IC2.achievements.issueAchievement((EntityPlayer)attacker, "killCreeperChainsaw");
        }

        return true;
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.world.isRemote) {
            Entity entity = event.getTarget();
            ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
            if (stack.getItem() == this && entity instanceof IShearable && !StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear") && ElectricItem.manager.use(stack, this.operationEnergyCost, player)) {
                IShearable target = (IShearable)entity;
                BlockPos pos = new BlockPos(entity);
                if (target.isShearable(stack, entity.world, pos)) {
                    List<ItemStack> drops = target.onSheared(stack, entity.world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));

                    Random itemRand = new Random();

                    EntityItem item;
                    for(Iterator<ItemStack> var8 = drops.iterator(); var8.hasNext(); item.field_70179_y += ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.1F)) {
                        ItemStack drop = var8.next();
                        item = entity.entityDropItem(drop, 1.0F);
                        item.field_70181_x += itemRand.nextFloat() * 0.05F;
                        item.field_70159_w += (itemRand.nextFloat() - itemRand.nextFloat()) * 0.1F;
                    }
                }
            }

        }
    }

    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        World world = player.world;
        if (world.isRemote) {
            return false;
        } else if (StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear")) {
            return false;
        } else {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof IShearable) {
                IShearable target = (IShearable)block;
                if (target.isShearable(stack, world, pos) && ElectricItem.manager.use(stack, this.operationEnergyCost, player)) {
                    List<ItemStack> drops = target.onSheared(stack, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));

                    for (ItemStack drop : drops) {
                        StackUtil.dropAsEntity(world, pos, drop);
                    }

                    player.addStat(Objects.requireNonNull(StatList.func_188055_a(block)), 1);
                }
            }

            return false;
        }
    }

    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        if (StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear")) {
            tooltip.add(TextFormatting.DARK_RED + Localization.translate("ic2.advchainsaw.shear", new Object[]{Localization.translate("ic2.message.off")}));
        } else {
            tooltip.add(TextFormatting.DARK_GREEN + Localization.translate("ic2.advchainsaw.shear", new Object[]{Localization.translate("ic2.message.on")}));
        }

    }

    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        if (slot != EntityEquipmentSlot.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        } else {
            Multimap<String, AttributeModifier> ret = HashMultimap.create();
            if (ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
                ret.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.attackSpeed, 0));
                ret.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, "Tool modifier", 13.0, 0));
            }

            return ret;
        }
    }

    protected String getIdleSound(EntityLivingBase player, ItemStack stack) {
        return "Tools/Chainsaw/ChainsawIdle.ogg";
    }

    protected String getStopSound(EntityLivingBase player, ItemStack stack) {
        return "Tools/Chainsaw/ChainsawStop.ogg";
    }
}
