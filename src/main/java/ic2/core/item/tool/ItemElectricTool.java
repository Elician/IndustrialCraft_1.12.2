package ic2.core.item.tool;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.init.Localization;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.IPseudoDamageItem;
import ic2.core.item.ItemToolIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ItemElectricTool extends ItemToolIC2 implements IEnergyContainerItem, IPseudoDamageItem, IItemHudInfo, IElectricItem {
    public double operationEnergyCost;
    public int maxCharge;
    public int transferLimit;
    public int tier;
    protected AudioSource audioSource;
    protected boolean wasEquipped;

    protected ItemElectricTool(ItemName name, int operationEnergyCost) {
        this(name, operationEnergyCost, HarvestLevel.Iron, Collections.emptySet());
    }

    protected ItemElectricTool(ItemName name, int operationEnergyCost, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses) {
        this(name, 2.0F, -3.0F, operationEnergyCost, harvestLevel, toolClasses, new HashSet<>());
    }

    private ItemElectricTool(ItemName name, float damage, float speed, int operationEnergyCost, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
        super(name, damage, speed, harvestLevel, toolClasses, mineableBlocks);
        this.operationEnergyCost = operationEnergyCost;
        this.func_77656_e(27);
        this.setNoRepair();
    }

    public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset) {
        ElectricItem.manager.use(StackUtil.get(player, hand), 0.0, player);
        return super.func_180614_a(player, world, pos, hand, side, xOffset, yOffset, zOffset);
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ElectricItem.manager.use(StackUtil.get(player, hand), 0.0, player);
        return super.onItemRightClick(world, player, hand);
    }

    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return !ElectricItem.manager.canUse(stack, this.operationEnergyCost) ? 1.0F : super.getDestroySpeed(stack, state);
    }

    public boolean hitEntity(ItemStack itemstack, EntityLivingBase entityliving, EntityLivingBase entityliving1) {
        return true;
    }

    public int getItemEnchantability() {
        return 0;
    }

    public boolean isRepairable() {
        return false;
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

    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase user) {
        if (state.getBlockHardness(world, pos) != 0.0F) {
            if (user != null) {
                ElectricItem.manager.use(stack, this.operationEnergyCost, user);
            } else {
                ElectricItem.manager.discharge(stack, this.operationEnergyCost, this.tier, true, false, false);
            }
        }

        return true;
    }

    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2) {
        return false;
    }

    public void fillItemGroup(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (this.isInGroup(tab)) {
            ElectricItemManager.addChargeVariants(this, subItems);
        }
    }

    public List<String> getHudInfo(ItemStack stack, boolean advanced) {
        List<String> info = new LinkedList<>();
        info.add(ElectricItem.manager.getToolTip(stack));
        info.add(Localization.translate("ic2.item.tooltip.PowerTier", this.tier));
        return info;
    }

    protected ItemStack getItemStack(double charge) {
        ItemStack ret = new ItemStack(this);
        ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
        return ret;
    }

    public void inventoryTick(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {
        boolean isEquipped = flag && entity instanceof EntityLivingBase;
        if (IC2.platform.isRendering()) {
            if (isEquipped && !this.wasEquipped) {
                String initSound;
                if (this.audioSource == null) {
                    initSound = this.getIdleSound((EntityLivingBase)entity, itemstack);
                    if (initSound != null) {
                        this.audioSource = IC2.audioManager.createSource(entity, PositionSpec.Hand, initSound, true, false, IC2.audioManager.getDefaultVolume());
                    }
                }

                if (this.audioSource != null) {
                    this.audioSource.play();
                }

                initSound = this.getStartSound((EntityLivingBase)entity, itemstack);
                if (initSound != null) {
                    IC2.audioManager.playOnce(entity, PositionSpec.Hand, initSound, true, IC2.audioManager.getDefaultVolume());
                }
            } else if (!isEquipped && this.audioSource != null) {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase theEntity = (EntityLivingBase)entity;
                    ItemStack stack = theEntity.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                    if (stack == null || stack.getItem() != this || stack == itemstack) {
                        this.removeAudioSource();
                        String sound = this.getStopSound(theEntity, itemstack);
                        if (sound != null) {
                            IC2.audioManager.playOnce(entity, PositionSpec.Hand, sound, true, IC2.audioManager.getDefaultVolume());
                        }
                    }
                }
            } else if (this.audioSource != null) {
                this.audioSource.updatePosition();
            }

            this.wasEquipped = isEquipped;
        }

    }

    protected void removeAudioSource() {
        if (this.audioSource != null) {
            this.audioSource.stop();
            this.audioSource.remove();
            this.audioSource = null;
        }

    }

    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        this.removeAudioSource();
        return true;
    }

    protected String getIdleSound(EntityLivingBase player, ItemStack stack) {
        return null;
    }

    protected String getStopSound(EntityLivingBase player, ItemStack stack) {
        return null;
    }

    protected String getStartSound(EntityLivingBase player, ItemStack stack) {
        return null;
    }

    public void setDamage(ItemStack stack, int damage) {
        int prev = this.getDamage(stack);
        if (damage != prev && BaseElectricItem.logIncorrectItemDamaging) {
            IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", damage - prev);
        }

    }

    public void setStackDamage(ItemStack stack, int damage) {
        super.setDamage(stack, damage);
    }

    @Override
    public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        double energyReceived = Math.min(this.getMaxEnergyStored(stack) - this.getEnergyStored(stack), maxReceive);

        if (!simulate) {
            ElectricItem.manager.charge(stack, energyReceived, maxReceive, true, false);
        }

        return (int) energyReceived;
    }

    @Override
    public int extractEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        double energyCost = Math.min(this.getEnergyStored(stack), maxReceive);

        if (!simulate) {
            ElectricItem.manager.discharge(stack, energyCost, Integer.MAX_VALUE, true, false, false);
        }

        return (int) energyCost;
    }

    @Override
    public int getEnergyStored(ItemStack itemStack) {
        return (int) ElectricItem.manager.getCharge(itemStack);
    }

    @Override
    public int getMaxEnergyStored(ItemStack itemStack) {
        return (int) ElectricItem.manager.getMaxCharge(itemStack);
    }
}
