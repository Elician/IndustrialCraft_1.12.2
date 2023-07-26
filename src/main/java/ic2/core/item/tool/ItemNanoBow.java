package ic2.core.item.tool;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.util.Keys;
import ic2.core.IC2;
import ic2.core.Platform;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.item.EntityTechArrow;
import ic2.core.item.IPseudoDamageItem;
import ic2.core.item.ItemIC2;
import ic2.core.item.utils.ItemStackHelper;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class ItemNanoBow extends ItemBow implements IEnergyContainerItem, IElectricItem, IItemModelProvider, IPseudoDamageItem {
    static final int NORMAL = 1;
    static final int RAPID = 2;
    static final int SPREAD = 3;
    static final int SNIPER = 4;
    static final int FLAME = 5;
    static final int EXPLOSIVE = 6;
    static final int[] CHARGE = { 1300, 700, 1700, 5000, 900, 3400 };
    static final String[] MODE = { "normal", "rapidfire", "spread", "sniper", "flame", "explosive" };

    public ItemNanoBow() {
        super();

        ItemName name = ItemName.nano_bow;

        this.func_77637_a(CreativeTabs.TOOLS);

        super.func_77625_d(1);
        super.func_77637_a(IC2.tabIC2);

        this.func_77655_b(name.name());
        BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
        name.setInstance(this);

        super.func_77664_n();

        super.func_77656_e(27);
        super.setNoRepair();
    }

    public void setStackDamage(ItemStack stack, int damage) {
        super.setDamage(stack, damage);
    }

    public String getTranslationKey() {
        return "ic2." + super.getTranslationKey().substring(5);
    }

    public String getTranslationKey(ItemStack itemStack) {
        return this.getTranslationKey();
    }

    public String func_77657_g(ItemStack itemStack) {
        return this.getTranslationKey(itemStack);
    }

    public String func_77653_i(ItemStack itemStack) {
        return Localization.translate(this.getTranslationKey(itemStack));
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
        if (!(entity instanceof EntityPlayer)) return;

        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        int mode = nbt.getInt("bowMode");

        EntityPlayer player = (EntityPlayer) entity;
        int charge = getUseDuration(stack) - timeLeft;
        charge = ForgeEventFactory.onArrowLoose(stack, world, player, charge, true);
        if (charge < 0) return;

        if (mode == RAPID) charge = charge * 2;
        float f = getArrowVelocity(charge);
        if (f < 0.1) return;

        if (!world.isRemote) {
            EntityTechArrow arrow = new EntityTechArrow(world, player);
            arrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, f * 3.0F, 1.0F);

            if (f == 1.5F)
                arrow.setIsCritical(true);

            int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
            if (j > 0)
                arrow.setDamage(arrow.getDamage() + j * 0.5D + 0.5D);
            if (mode == NORMAL && arrow.getIsCritical())
                j += 3;
            else if (mode == RAPID && arrow.getIsCritical())
                j += 1;
            else if (mode == SNIPER && arrow.getIsCritical())
                j += 8;
            if (j > 0)
                arrow.setDamage(arrow.getDamage() + j * 0.5D + 0.5D);
            /*
             * if (IC2CA.nanoBowBoost > 0) arrow.setDamage(arrow.getDamage() +
             * IC2CA.nanoBowBoost * 0.5D + 0.5D);
             */

            int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
            if (mode == NORMAL && arrow.getIsCritical())
                k += 1;
            else if (mode == SNIPER && arrow.getIsCritical())
                k += 5;
            if (k > 0)
                arrow.setKnockbackStrength(k);

            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0)
                arrow.setFire(100);
            if (mode == FLAME && arrow.getIsCritical())
                arrow.setFire(2000);
            if (mode == EXPLOSIVE && arrow.getIsCritical())
                arrow.setExplosive(true);

            arrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;

            switch (mode) {
                case NORMAL:
                case RAPID:
                case SNIPER:
                case FLAME:
                case EXPLOSIVE:
                    this.discharge(stack, CHARGE[mode - 1], player);
                    world.addEntity0(arrow);
                    break;
                case SPREAD:
                    this.discharge(stack, 1500, player);
                    world.addEntity0(arrow);
                    if (arrow.getIsCritical()) {
                        EntityTechArrow arrow2 = new EntityTechArrow(world, player);
                        arrow2.shoot(player, player.rotationPitch + 2.0F, player.rotationYaw, 0.0F, f * 3.0F, 1.0F);
                        arrow2.setIsCritical(true);
                        arrow2.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
                        EntityTechArrow arrow3 = new EntityTechArrow(world, player);
                        arrow3.shoot(player, player.rotationPitch - 2.0F, player.rotationYaw, 0.0F, f * 3.0F, 1.0F);
                        arrow3.setIsCritical(true);
                        arrow3.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
                        EntityTechArrow arrow4 = new EntityTechArrow(world, player);
                        arrow4.shoot(player, player.rotationPitch, player.rotationYaw + 2.0F, 0.0F, f * 3.0F, 1.0F);
                        arrow4.setIsCritical(true);
                        arrow4.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
                        EntityTechArrow arrow5 = new EntityTechArrow(world, player);
                        arrow5.shoot(player, player.rotationPitch, player.rotationYaw - 2.0F, 0.0F, f * 3.0F, 1.0F);
                        arrow5.setIsCritical(true);
                        arrow5.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
                        world.addEntity0(arrow2);
                        world.addEntity0(arrow3);
                        world.addEntity0(arrow4);
                        world.addEntity0(arrow5);
                    }
                    break;
            }
        }
        Random itemRand = new Random();
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
        player.addStat(StatList.func_188057_b(this));
    }

    public static float getArrowVelocity(int charge) {
        float f = charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.5F);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        switch (nbt.getInt("bowMode")) {
            case SNIPER:
            case EXPLOSIVE:
                return 144000;
            case RAPID:
                return 18000;
            default:
                return 72000;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ActionResult<ItemStack> result = ForgeEventFactory.onArrowNock(stack, world, player, hand, true);
        if (result != null) return result;

        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        int mode = nbt.getInt("bowMode");
        if (!world.isRemote && isModeSwitchKeyDown(player) && nbt.getByte("toggleTimer") == 0) {
            byte toggle = 10;
            nbt.putByte("toggleTimer", toggle);

            mode++;
			/*if (mode == RAPID && !IC2CA.rapidFireMode)
				mode++;
			if (mode == SPREAD && !IC2CA.spreadMode)
				mode++;
			if (mode == SNIPER && !IC2CA.sniperMode)
				mode++;
			if (mode == FLAME && !IC2CA.flameMode)
				mode++;
			if (mode == EXPLOSIVE && !IC2CA.explosiveMode)
				mode++;*/
            if (mode > EXPLOSIVE) {
                mode -= EXPLOSIVE;
            }
            nbt.putInt("bowMode", mode);

            Platform.messageTranslationPlayer(player, "ic2.nano_bow.mode_enabled", TextFormatting.GOLD, Localization.translate("ic2.nano_bow.mode." + MODE[mode - 1]));

            return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));
        }

        if (canUse(stack, CHARGE[mode - 1])) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        byte toggle = nbt.getByte("toggleTimer");
        if (toggle > 0)
            nbt.putByte("toggleTimer", --toggle);
        int mode = nbt.getInt("bowMode");
        if (mode == 0)
            nbt.putInt("bowMode", 1);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        int mode = nbt.getInt("bowMode");
        if (mode == RAPID) {
            int j = getUseDuration(stack) - count;
            if ((j >= 10) && (canUse(stack, CHARGE[RAPID - 1])))
                player.stopActiveHand();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        IElectricItem item = (IElectricItem) stack.getItem();
        if (!nbt.contains("loaded")) {
            if (nbt.getInt("tier") == 0)
                nbt.putInt("tier", item.getTier(stack));
            if (nbt.getInt("transferLimit") == 0)
                nbt.putDouble("transferLimit", item.getTransferLimit(stack));
            if (nbt.getInt("maxCharge") == 0)
                nbt.putDouble("maxCharge", item.getMaxCharge(stack));
            nbt.putBoolean("loaded", true);
        }
        if (nbt.getInt("transferLimit") != item.getTransferLimit(stack))
            tooltip.add(String.format(I18n.format("info.transferspeed"), nbt.getDouble("transferLimit")));
        if (nbt.getInt("tier") != item.getTier(stack))
            tooltip.add(String.format(I18n.format("info.chargingtier"), nbt.getInt("tier")));
    }

    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		/*if (!isInCreativeTab(tab))
			return;
		ItemStack charged = CrossModLoader.getCrossMod(ModIDs.IC2).getChargedStack(new ItemStack(this, 1));
		items.add(charged);
		items.add(new ItemStack(this, 1, getMaxDamage()));*/
    }

    protected void discharge(ItemStack stack, double amount, EntityLivingBase player) {
        this.extractEnergy(stack, (int) amount, false);
    }

    protected boolean canUse(ItemStack stack, double amount) {
        return ElectricItem.manager.canUse(stack, amount);
    }

    protected boolean isModeSwitchKeyDown(EntityPlayer player) {
        return Keys.instance.isModeSwitchKeyDown(player);
    }

    // IElectricItem
    @Override
    public boolean canProvideEnergy(ItemStack stack) {
        return false;
    }

    @Override
    public double getMaxCharge(ItemStack stack) {
        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        if (nbt.getInt("maxCharge") == 0)
            nbt.putInt("maxCharge", getDefaultMaxCharge());
        return nbt.getInt("maxCharge");
    }

    @Override
    public int getTier(ItemStack stack) {
        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        if (nbt.getInt("tier") == 0)
            nbt.putInt("tier", getDefaultTier());
        return nbt.getInt("tier");
    }

    @Override
    public double getTransferLimit(ItemStack stack) {
        NBTTagCompound nbt = ItemStackHelper.getTagCompound(stack);
        if (nbt.getInt("transferLimit") == 0)
            nbt.putInt("transferLimit", getDefaultTransferLimit());
        return nbt.getInt("transferLimit");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    // IItemUpgradeable
    // @Override
    public int getDefaultMaxCharge() {
        return 200000;
    }

    // @Override
    public int getDefaultTier() {
        return 2;
    }

    // @Override
    public int getDefaultTransferLimit() {
        return 500;
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(ItemName name) {
        ItemIC2.registerModel(this, 0, name, null);
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