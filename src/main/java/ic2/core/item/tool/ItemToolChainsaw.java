package ic2.core.item.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolChainsaw extends ItemElectricTool implements IHitSoundOverride {
    public ItemToolChainsaw() {
        super(ItemName.chainsaw, 400, HarvestLevel.Iron, EnumSet.of(ToolClass.Axe, ToolClass.Sword, ToolClass.Shears));
        this.maxCharge = 120000;
        this.transferLimit = 500;
        this.tier = 1;
        this.efficiency = 12.0F;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            return super.onItemRightClick(world, player, hand);
        } else {
            if (IC2.keyboard.isModeSwitchKeyDown(player)) {
                NBTTagCompound compoundTag = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
                if (compoundTag.getBoolean("disableShear")) {
                    compoundTag.putBoolean("disableShear", false);
                    IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
                } else {
                    compoundTag.putBoolean("disableShear", true);
                    IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.noShear");
                }
            }

            return super.onItemRightClick(world, player, hand);
        }
    }

    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        if (slot != EntityEquipmentSlot.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        } else {
            Multimap<String, AttributeModifier> ret = HashMultimap.create();
            if (ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
                ret.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.attackSpeed, 0));
                ret.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, "Tool modifier", 9.0, 0));
            }

            return ret;
        }
    }

    public boolean hitEntity(ItemStack itemstack, EntityLivingBase entityliving, EntityLivingBase attacker) {
        ElectricItem.manager.use(itemstack, this.operationEnergyCost, attacker);
        if (attacker instanceof EntityPlayer && entityliving instanceof EntityCreeper && entityliving.getHealth() <= 0.0F) {
            IC2.achievements.issueAchievement((EntityPlayer)attacker, "killCreeperChainsaw");
        }

        return true;
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (IC2.platform.isSimulating()) {
            Entity entity = event.getTarget();
            EntityPlayer player = event.getEntityPlayer();
            ItemStack itemstack = player.inventory.getStackInSlot(player.inventory.currentItem);
            if (itemstack.getItem() == this && entity instanceof IShearable && !StackUtil.getOrCreateNbtData(itemstack).getBoolean("disableShear") && ElectricItem.manager.use(itemstack, this.operationEnergyCost, player)) {
                IShearable target = (IShearable)entity;
                World world = entity.getEntityWorld();
                BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
                if (target.isShearable(itemstack, world, pos)) {
                    List<ItemStack> drops = target.onSheared(itemstack, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack));

                    EntityItem ent;
                    for(Iterator var9 = drops.iterator(); var9.hasNext(); ent.field_70179_y += (double)((random.nextFloat() - random.nextFloat()) * 0.1F)) {
                        ItemStack stack = (ItemStack)var9.next();
                        ent = entity.entityDropItem(stack, 1.0F);
                        ent.field_70181_x += random.nextFloat() * 0.05F;
                        ent.field_70159_w += (random.nextFloat() - random.nextFloat()) * 0.1F;
                    }
                }
            }

        }
    }

    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        if (!IC2.platform.isSimulating()) {
            return false;
        } else if (StackUtil.getOrCreateNbtData(itemstack).getBoolean("disableShear")) {
            return false;
        } else {
            World world = player.getEntityWorld();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof IShearable) {
                IShearable target = (IShearable)block;
                if (target.isShearable(itemstack, world, pos) && ElectricItem.manager.use(itemstack, this.operationEnergyCost, player)) {
                    List<ItemStack> drops = target.onSheared(itemstack, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack));

                    for (ItemStack stack : drops) {
                        StackUtil.dropAsEntity(world, pos, stack);
                    }

                    player.addStat(StatList.func_188055_a(block), 1);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                    return true;
                }
            }

            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public String getHitSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public String getBreakSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
        return null;
    }

    protected String getIdleSound(EntityLivingBase player, ItemStack stack) {
        return "Tools/Chainsaw/ChainsawIdle.ogg";
    }

    protected String getStopSound(EntityLivingBase player, ItemStack stack) {
        return "Tools/Chainsaw/ChainsawStop.ogg";
    }
}
