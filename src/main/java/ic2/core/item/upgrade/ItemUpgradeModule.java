

package ic2.core.item.upgrade;

import com.google.common.base.Predicate;
import ic2.api.item.ElectricItem;
import ic2.api.item.ICustomDamageItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.api.upgrade.IFullUpgrade;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.state.IIdProvider;
import ic2.core.gui.dynamic.DynamicHandHeldContainer;
import ic2.core.init.Localization;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.item.ItemIC2;
import ic2.core.item.ItemMulti;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUpgradeModule extends ItemMulti<ItemUpgradeModule.UpgradeType> implements IFullUpgrade, IHandHeldSubInventory, IItemHudInfo {
    private static final DecimalFormat decimalformat = new DecimalFormat("0.##");
    private static final List<StackUtil.AdjacentInv> emptyInvList = Collections.emptyList();
    private static final List<LiquidUtil.AdjacentFluidHandler> emptyFhList = Collections.emptyList();

    public ItemUpgradeModule() {
        super(ItemName.upgrade, UpgradeType.class);
        this.func_77627_a(true);

        for (UpgradeType type : ItemUpgradeModule.UpgradeType.values()) {
            UpgradeRegistry.register(new ItemStack(this, 1, type.getId()));
        }

    }

    @SideOnly(Side.CLIENT)
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomMeshDefinition(this, stack -> {
            UpgradeType type = ItemUpgradeModule.this.getType(stack);
            if (type == null) {
                return new ModelResourceLocation("builtin/missing", "missing");
            } else {
                EnumFacing dir;
                return type.directional && (dir = ItemUpgradeModule.getDirection(stack)) != null ? ItemIC2.getModelLocation(name, type.getName() + '_' + dir.getName()) : ItemIC2.getModelLocation(name, type.getName());
            }
        });
        Iterator var2 = this.typeProperty.getAllowedValues().iterator();

        while(true) {
            UpgradeType type;
            do {
                if (!var2.hasNext()) {
                    return;
                }

                type = (UpgradeType)var2.next();
                ModelBakery.registerItemVariants(this, getModelLocation(name, type.getName()));
            } while(!type.directional);

            for (EnumFacing dir : EnumFacing.BY_INDEX) {
                ModelBakery.registerItemVariants(this, getModelLocation(name, type.getName() + '_' + dir.getName()));
            }
        }
    }

    public List<String> getHudInfo(ItemStack stack, boolean advanced) {
        List<String> info = new LinkedList<>();
        info.add("Machine Upgrade");
        return info;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        UpgradeType type = this.getType(stack);
        if (type != null) {
            String side;
            switch (type) {
                case overclocker:
                case overclocker_advanced:
                case overclocker_ultimate:
                case overclocker_maximum:
                case overclocker_infinity:
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.overclocker.time", decimalformat.format(100.0 * Math.pow(this.getProcessTimeMultiplier(stack, null), StackUtil.getSize(stack)))));
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.overclocker.power", decimalformat.format(100.0 * Math.pow(this.getEnergyDemandMultiplier(stack, null), StackUtil.getSize(stack)))));
                    break;
                case transformer:
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.transformer", this.getExtraTier(stack, null) * StackUtil.getSize(stack)));
                    break;
                case energy_storage:
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.storage", this.getExtraEnergyStorage(stack, null) * StackUtil.getSize(stack)));
                    break;
                case ejector:
                case advanced_ejector:
                    side = getSideName(stack);
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(side)));
                    break;
                case pulling:
                case advanced_pulling:
                    side = getSideName(stack);
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(side)));
                    break;
                case fluid_ejector:
                    side = getSideName(stack);
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(side)));
                    break;
                case fluid_pulling:
                    side = getSideName(stack);
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(side)));
                    break;
                case redstone_inverter:
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.redstone"));
                    break;
                case remote_interface:
                    tooltip.add(Localization.translate("ic2.tooltip.upgrade.remote_interface", StackUtil.getSize(stack)));
            }

        }
    }

    private static String getSideName(ItemStack stack) {
        EnumFacing dir = getDirection(stack);
        if (dir == null) {
            return "ic2.tooltip.upgrade.ejector.anyside";
        } else {
            switch (dir) {
                case WEST:
                    return "ic2.dir.west";
                case EAST:
                    return "ic2.dir.east";
                case DOWN:
                    return "ic2.dir.bottom";
                case UP:
                    return "ic2.dir.top";
                case NORTH:
                    return "ic2.dir.north";
                case SOUTH:
                    return "ic2.dir.south";
                default:
                    throw new RuntimeException("invalid dir: " + dir);
            }
        }
    }

    public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset) {
        ItemStack stack = StackUtil.get(player, hand);
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return EnumActionResult.PASS;
        } else if (type.directional) {
            int dir = 1 + side.ordinal();
            NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
            if (nbtData.getByte("dir") == dir) {
                nbtData.putByte("dir", (byte)0);
            } else {
                nbtData.putByte("dir", (byte)dir);
            }

            if (IC2.platform.isRendering()) {
                switch (type) {
                    case ejector:
                    case advanced_ejector:
                        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(getSideName(stack))), new Object[0]);
                        break;
                    case pulling:
                    case advanced_pulling:
                        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(getSideName(stack))), new Object[0]);
                        break;
                    case fluid_ejector:
                        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(getSideName(stack))), new Object[0]);
                        break;
                    case fluid_pulling:
                        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(getSideName(stack))), new Object[0]);
                }
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.PASS;
        }
    }

    public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
        UpgradeType type = this.getType(stack);
        if (type != null) {
            switch (type) {
                case advanced_ejector:
                case advanced_pulling:
                    if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof DynamicHandHeldContainer) {
                        HandHeldInventory base = (HandHeldInventory)((DynamicHandHeldContainer)player.openContainer).base;
                        if (base instanceof HandHeldAdvancedUpgrade && base.isThisContainer(stack)) {
                            base.saveAsThrown(stack);
                            player.closeScreen();
                        }
                    }
            }
        }

        return true;
    }

    public boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> types) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return false;
        } else {
            switch (type) {
                case overclocker_advanced:
                case overclocker_ultimate:
                case overclocker_maximum:
                case overclocker_infinity:
                case overclocker:
                    return types.contains(UpgradableProperty.Processing) || types.contains(UpgradableProperty.Augmentable);
                case transformer:
                    return types.contains(UpgradableProperty.Transformer);
                case energy_storage:
                    return types.contains(UpgradableProperty.EnergyStorage);
                case ejector:
                case advanced_ejector:
                    return types.contains(UpgradableProperty.ItemProducing);
                case pulling:
                case advanced_pulling:
                    return types.contains(UpgradableProperty.ItemConsuming);
                case fluid_ejector:
                    return types.contains(UpgradableProperty.FluidProducing);
                case fluid_pulling:
                    return types.contains(UpgradableProperty.FluidConsuming);
                case redstone_inverter:
                    return types.contains(UpgradableProperty.RedstoneSensitive);
                case remote_interface:
                    return types.contains(UpgradableProperty.RemotelyAccessible);
                default:
                    return false;
            }
        }
    }

    public int getAugmentation(ItemStack stack, IUpgradableBlock parent) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return 0;
        } else {
            switch (type) {
                case overclocker_advanced:
                case overclocker_ultimate:
                case overclocker_maximum:
                case overclocker_infinity:
                case overclocker:
                    return 1;
                default:
                    return 0;
            }
        }
    }

    public int getExtraProcessTime(ItemStack stack, IUpgradableBlock parent) {
        return 0;
    }

    public double getProcessTimeMultiplier(ItemStack stack, IUpgradableBlock parent) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return 1.0;
        } else {
            switch (type) {
                case overclocker:
                    return 0.9;
                case overclocker_advanced:
                    return 0.7;
                case overclocker_ultimate:
                    return 0.5;
                case overclocker_maximum:
                    return 0.3;
                case overclocker_infinity:
                    return 0.0;
                default:
                    return 1.0;
            }
        }
    }

    public int getExtraEnergyDemand(ItemStack stack, IUpgradableBlock parent) {
        return 0;
    }

    public double getEnergyDemandMultiplier(ItemStack stack, IUpgradableBlock parent) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return 1.0;
        } else {
            switch (type) {
                case overclocker:
                    return 2.1;
                case overclocker_advanced:
                    return 1.9;
                case overclocker_ultimate:
                    return 1.6;
                case overclocker_maximum:
                    return 1.3;
                case overclocker_infinity:
                    return 0.01;
                default:
                    return 1.0;
            }
        }
    }

    public int getExtraEnergyStorage(ItemStack stack, IUpgradableBlock parent) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return 0;
        } else {
            switch (type) {
                case energy_storage:
                    return 20000;
                default:
                    return 0;
            }
        }
    }

    public double getEnergyStorageMultiplier(ItemStack stack, IUpgradableBlock parent) {
        return 1.0;
    }

    public int getExtraTier(ItemStack stack, IUpgradableBlock parent) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return 0;
        } else {
            switch (type) {
                case transformer:
                    return 1;
                default:
                    return 0;
            }
        }
    }

    public boolean modifiesRedstoneInput(ItemStack stack, IUpgradableBlock parent) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return false;
        } else {
            switch (type) {
                case redstone_inverter:
                    return true;
                default:
                    return false;
            }
        }
    }

    public int getRedstoneInput(ItemStack stack, IUpgradableBlock parent, int externalInput) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return externalInput;
        } else {
            switch (type) {
                case redstone_inverter:
                    return 15 - externalInput;
                default:
                    return externalInput;
            }
        }
    }

    public int getRangeAmplification(ItemStack stack, IUpgradableBlock parent, int existingRange) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return existingRange;
        } else {
            switch (type) {
                case remote_interface:
                    return existingRange << 1;
                default:
                    return existingRange;
            }
        }
    }

    public boolean onTick(ItemStack stack, IUpgradableBlock parent) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return false;
        } else {
            int size = StackUtil.getSize(stack);
            TileEntity te = (TileEntity)parent;
            boolean ret = false;
            int amount;
            Iterator var8;
            LiquidUtil.AdjacentFluidHandler fh;
            StackUtil.AdjacentInv inv;
            switch (type) {
                case ejector:
                    amount = (int)Math.pow(4.0, Math.min(4, size - 1));
                    var8 = getTargetInventories(stack, te).iterator();

                    while(var8.hasNext()) {
                        inv = (StackUtil.AdjacentInv)var8.next();
                        StackUtil.transfer(te, inv.te, inv.dir, amount);
                    }

                    return ret;
                case advanced_ejector:
                    amount = (int)Math.pow(4.0, Math.min(4, size - 1));
                    var8 = getTargetInventories(stack, te).iterator();

                    while(var8.hasNext()) {
                        inv = (StackUtil.AdjacentInv)var8.next();
                        StackUtil.transfer(te, inv.te, inv.dir, amount, stackChecker(stack));
                    }

                    return ret;
                case pulling:
                    amount = (int)Math.pow(4.0, Math.min(4, size - 1));
                    var8 = getTargetInventories(stack, te).iterator();

                    while(var8.hasNext()) {
                        inv = (StackUtil.AdjacentInv)var8.next();
                        StackUtil.transfer(inv.te, te, inv.dir.getOpposite(), amount);
                    }

                    return ret;
                case advanced_pulling:
                    amount = (int)Math.pow(4.0, Math.min(4, size - 1));
                    var8 = getTargetInventories(stack, te).iterator();

                    while(var8.hasNext()) {
                        inv = (StackUtil.AdjacentInv)var8.next();
                        StackUtil.transfer(inv.te, te, inv.dir.getOpposite(), amount, stackChecker(stack));
                    }

                    return ret;
                case fluid_ejector:
                    if (!LiquidUtil.isFluidTile(te, null)) {
                        return false;
                    }

                    amount = (int)(50.0 * Math.pow(4.0, Math.min(4, size - 1)));
                    var8 = getTargetFluidHandlers(stack, te).iterator();

                    while(var8.hasNext()) {
                        fh = (LiquidUtil.AdjacentFluidHandler)var8.next();
                        LiquidUtil.transfer(te, fh.dir, fh.handler, amount);
                    }

                    return ret;
                case fluid_pulling:
                    if (!LiquidUtil.isFluidTile(te, null)) {
                        return false;
                    }

                    amount = (int)(50.0 * Math.pow(4.0, Math.min(4, size - 1)));
                    var8 = getTargetFluidHandlers(stack, te).iterator();

                    while(var8.hasNext()) {
                        fh = (LiquidUtil.AdjacentFluidHandler)var8.next();
                        LiquidUtil.transfer(fh.handler, fh.dir.getOpposite(), te, amount);
                    }

                    return ret;
                default:
                    return false;
            }
        }
    }

    private static Predicate<ItemStack> stackChecker(final ItemStack stack) {
        return new Predicate<ItemStack>() {
            private boolean hasInitialised = false;
            private Set<ItemStack> filters;
            private Settings meta;
            private Settings damage;
            private Settings energy;
            private NbtSettings nbt;

            private void initalise() {
                assert !this.hasInitialised;

                NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
                this.filters = this.getFilterStacks(tag);
                this.meta = new Settings(HandHeldAdvancedUpgrade.getTag(tag, "meta"));
                this.damage = null;
                this.nbt = NbtSettings.getFromNBT(HandHeldAdvancedUpgrade.getTag(tag, "nbt").getByte("type"));
                this.energy = new Settings(HandHeldAdvancedUpgrade.getTag(tag, "energy"));
                this.hasInitialised = true;
            }

            private Set<ItemStack> getFilterStacks(NBTTagCompound nbt) {
                Set<ItemStack> ret = new HashSet<>();
                NBTTagList contentList = nbt.getList("Items", 10);

                for(int tag = 0; tag < contentList.func_74745_c(); ++tag) {
                    NBTTagCompound slotNbt = contentList.getCompound(tag);
                    int slot = slotNbt.getByte("Slot");
                    if (slot >= 0 && slot < 9) {
                        ItemStack filter = new ItemStack(slotNbt);
                        if (!StackUtil.isEmpty(filter)) {
                            ret.add(filter);
                        }
                    }
                }

                return ret;
            }

            private boolean checkMeta(ItemStack stackx, ItemStack filter) {
                assert this.meta.active;

                assert this.meta.comparison == ComparisonType.DIRECT;

                return stackx.func_77960_j() == filter.func_77960_j();
            }

            private boolean checkDamage(ItemStack stackx, ItemStack filter, boolean customStack) {
                assert this.damage.active;

                assert this.damage.comparison == ComparisonType.DIRECT;

                return customStack && filter.getItem() instanceof ICustomDamageItem ? ((ICustomDamageItem)stackx.getItem()).getCustomDamage(stackx) == ((ICustomDamageItem)filter.getItem()).getCustomDamage(filter) : filter.getDamage() == stackx.getDamage();
            }

            private boolean checkNBT(ItemStack stackx, ItemStack filter) {
                switch (this.nbt) {
                    case IGNORED:
                        return true;
                    case FUZZY:
                        return StackUtil.checkNbtEquality(stackx.getTag(), filter.getTag());
                    case EXACT:
                        return StackUtil.checkNbtEqualityStrict(stackx, filter);
                    default:
                        throw new IllegalStateException("Unexpected NBT state: " + this.nbt);
                }
            }

            private boolean checkEnergy(ItemStack stackx, ItemStack filter) {
                assert this.energy.active;

                assert this.energy.comparison == ComparisonType.DIRECT;

                return filter.getItem() instanceof IElectricItem && Util.isSimilar(ElectricItem.manager.getCharge(stackx), ElectricItem.manager.getCharge(filter));
            }

            public boolean apply(ItemStack stackx) {
                if (!this.hasInitialised) {
                    this.initalise();
                }

                boolean checkMeta;
                if (!this.meta.comparison.ignoreFilters()) {
                    if (!this.meta.doComparison(stackx.func_77960_j())) {
                        return false;
                    }

                    checkMeta = false;
                } else {
                    checkMeta = this.meta.active;
                }

                boolean customStack = stackx.getItem() instanceof ICustomDamageItem;
                boolean checkDamage = false;
                boolean checkEnergy;
                if (!this.energy.comparison.ignoreFilters()) {
                    if (!(stackx.getItem() instanceof IElectricItem) || !this.energy.doComparison((int)ElectricItem.manager.getCharge(stackx))) {
                        return false;
                    }

                    checkEnergy = false;
                } else {
                    checkEnergy = this.energy.active;
                    if (checkEnergy && !(stackx.getItem() instanceof IElectricItem)) {
                        return false;
                    }
                }

                Iterator var6 = this.filters.iterator();

                ItemStack filter;
                do {
                    do {
                        do {
                            do {
                                do {
                                    if (!var6.hasNext()) {
                                        return this.filters.isEmpty() && this.meta.active && !checkMeta && this.energy.active && !checkEnergy;
                                    }

                                    filter = (ItemStack)var6.next();
                                } while(filter.getItem() != stackx.getItem());
                            } while(checkMeta && !this.checkMeta(stackx, filter));
                        } while(checkDamage && !this.checkDamage(stackx, filter, customStack));
                    } while(!this.checkNBT(stackx, filter));
                } while(checkEnergy && !this.checkEnergy(stackx, filter));

                return true;
            }
        };
    }

    private static List<StackUtil.AdjacentInv> getTargetInventories(ItemStack stack, TileEntity parent) {
        EnumFacing dir = getDirection(stack);
        if (dir == null) {
            return StackUtil.getAdjacentInventories(parent);
        } else {
            StackUtil.AdjacentInv inv = StackUtil.getAdjacentInventory(parent, dir);
            return inv == null ? emptyInvList : Collections.singletonList(inv);
        }
    }

    private static List<LiquidUtil.AdjacentFluidHandler> getTargetFluidHandlers(ItemStack stack, TileEntity parent) {
        EnumFacing dir = getDirection(stack);
        if (dir == null) {
            return LiquidUtil.getAdjacentHandlers(parent);
        } else {
            LiquidUtil.AdjacentFluidHandler fh = LiquidUtil.getAdjacentHandler(parent, dir);
            return fh == null ? emptyFhList : Collections.singletonList(fh);
        }
    }

    public Collection<ItemStack> onProcessEnd(ItemStack stack, IUpgradableBlock parent, Collection<ItemStack> output) {
        return output;
    }

    public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return null;
        } else {
            switch (type) {
                case advanced_ejector:
                case advanced_pulling:
                    return new HandHeldAdvancedUpgrade(player, stack);
                default:
                    return null;
            }
        }
    }

    public IHasGui getSubInventory(EntityPlayer player, ItemStack stack, int ID) {
        UpgradeType type = this.getType(stack);
        if (type == null) {
            return null;
        } else {
            switch (type) {
                case advanced_ejector:
                case advanced_pulling:
                    return HandHeldAdvancedUpgrade.delegate(player, stack, ID);
                default:
                    return null;
            }
        }
    }

    private static EnumFacing getDirection(ItemStack stack) {
        int rawDir = StackUtil.getOrCreateNbtData(stack).getByte("dir");
        return rawDir >= 1 && rawDir <= 6 ? EnumFacing.BY_INDEX[rawDir - 1] : null;
    }

    public enum UpgradeType implements IIdProvider {
        overclocker(false),
        overclocker_advanced(false),
        overclocker_ultimate(false),
        overclocker_maximum(false),
        overclocker_infinity(false),

        transformer(false),
        energy_storage(false),
        redstone_inverter(false),
        ejector(true),
        @NotClassic
        advanced_ejector(true),
        pulling(true),
        @NotClassic
        advanced_pulling(true),
        fluid_ejector(true),
        fluid_pulling(true),
        @NotClassic
        remote_interface(false);

        public final boolean directional;

        UpgradeType(boolean directional) {
            this.directional = directional;
        }

        public String getName() {
            return this.name();
        }

        public int getId() {
            return this.ordinal();
        }
    }
}
