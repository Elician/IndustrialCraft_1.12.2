package ic2.core.item.tool;

import com.google.common.base.CaseFormat;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class ItemAdvancedDrill extends ItemDrill {
    protected static final Material[] MATERIALS;

    public ItemAdvancedDrill() {
        super(ItemName.advanced_drill, (int) DrillMode.NORMAL.energyCost, HarvestLevel.Iridium, 200000, 500, 2, ItemAdvancedDrill.DrillMode.NORMAL.drillSpeed);
    }

    public static DrillMode readDrillMode(ItemStack stack) {
        return ItemAdvancedDrill.DrillMode.getFromID(StackUtil.getOrCreateNbtData(stack).getInt("toolMode"));
    }

    public static DrillMode readNextDrillMode(ItemStack stack) {
        return ItemAdvancedDrill.DrillMode.getFromID(StackUtil.getOrCreateNbtData(stack).getInt("toolMode") + 1);
    }

    public static void saveDrillMode(ItemStack stack, DrillMode mode) {
        StackUtil.getOrCreateNbtData(stack).putInt("toolMode", mode.ordinal());
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            ItemStack stack = StackUtil.get(player, hand);
            if (!world.isRemote) {
                DrillMode mode = readNextDrillMode(stack);
                saveDrillMode(stack, mode);
                IC2.platform.messagePlayer(player, Localization.translate("advanced_drill.mode"), mode.colour, new Object[]{mode.translationName});
                this.efficiency = mode.drillSpeed;
                this.operationEnergyCost = mode.energyCost;
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        } else {
            return super.onItemRightClick(world, player, hand);
        }
    }

    public static Collection<BlockPos> getBrokenBlocks(EntityPlayer player, RayTraceResult ray) {
        return getBrokenBlocks(player, ray.func_178782_a(), ray.field_178784_b);
    }

    protected static Collection<BlockPos> getBrokenBlocks(EntityPlayer player, BlockPos pos, EnumFacing side) {
        assert side != null;

        int xMove = 1;
        int yMove = 1;
        int zMove = 1;
        switch (side.getAxis()) {
            case X:
                xMove = 0;
                break;
            case Y:
                yMove = 0;
                break;
            case Z:
                zMove = 0;
        }

        World world = player.world;
        Collection<BlockPos> list = new ArrayList<>(9);

        for (int x = pos.getX() - xMove; x <= pos.getX() + xMove; ++x) {
            for (int y = pos.getY() - yMove; y <= pos.getY() + yMove; ++y) {
                for (int z = pos.getZ() - zMove; z <= pos.getZ() + zMove; ++z) {
                    BlockPos potential = new BlockPos(x, y, z);
                    if (canBlockBeMined(world, potential, player, false)) {
                        list.add(potential);
                    }
                }
            }
        }

        return list;
    }

    protected static boolean canBlockBeMined(World world, BlockPos pos, EntityPlayer player, boolean skipEffectivity) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().canHarvestBlock(world, pos, player) && (skipEffectivity || isEffective(state.getMaterial())) && state.getPlayerRelativeBlockHardness(player, world, pos) != 0.0F;
    }

    protected static boolean isEffective(Material material) {
        for (Material option : MATERIALS) {
            if (material == option) {
                return true;
            }
        }

        return false;
    }

    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        World world;
        if (readDrillMode(stack) == ItemAdvancedDrill.DrillMode.BIG_HOLES && !(world = player.world).isRemote) {
            Collection<BlockPos> blocks = getBrokenBlocks(player, this.func_77621_a(world, player, true));
            if (!blocks.contains(pos) && canBlockBeMined(world, pos, player, true)) {
                blocks.add(pos);
            }

            boolean powerRanOut = false;

            for (BlockPos blockPos : blocks) {
                if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
                    powerRanOut = true;
                    break;
                }

                if (world.isBlockLoaded(blockPos)) {
                    IBlockState state = world.getBlockState(blockPos);
                    Block block = state.getBlock();
                    if (!block.isAir(state, world, blockPos)) {
                        int experience;
                        if (player instanceof EntityPlayerMP) {
                            experience = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP) player).interactionManager.getGameType(), (EntityPlayerMP) player, blockPos);
                            if (experience < 0) {
                                return false;
                            }
                        } else {
                            experience = 0;
                        }

                        block.onBlockHarvested(world, blockPos, state, player);
                        if (player.isCreative()) {
                            if (block.removedByPlayer(state, world, blockPos, player, false)) {
                                block.onPlayerDestroy(world, blockPos, state);
                            }
                        } else {
                            if (block.removedByPlayer(state, world, blockPos, player, true)) {
                                block.onPlayerDestroy(world, blockPos, state);
                                block.harvestBlock(world, player, blockPos, state, world.getTileEntity(blockPos), stack);
                                if (experience > 0) {
                                    block.dropXpOnBlockBreak(world, blockPos, experience);
                                }
                            }

                            stack.onBlockDestroyed(world, state, blockPos, player);
                        }

                        world.func_175718_b(2001, blockPos, Block.func_176210_f(state));
                        ((EntityPlayerMP) player).connection.sendPacket(new SPacketBlockChange(world, blockPos));
                    }
                }
            }

            if (powerRanOut) {
                IC2.platform.messagePlayer(player, Localization.translate("advanced_drill.ranOut"));
            }

            return true;
        } else {
            return super.onBlockStartBreak(stack, pos, player);
        }
    }

    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GOLD + Localization.translate("advanced_drill.mode", new Object[]{TextFormatting.WHITE + Localization.translate(readDrillMode(stack).translationName)}));
    }

    static {
        MATERIALS = new Material[]{Material.ROCK, Material.EARTH, /*Material.GROUND,*/ Material.SAND, Material.CLAY};
    }

    public enum DrillMode {
        NORMAL(TextFormatting.DARK_GREEN, 35.0F, 600),
        LOW_POWER(TextFormatting.GOLD, 16.0F, 250),
        FINE(TextFormatting.AQUA, 10.0F, 200),
        BIG_HOLES(TextFormatting.LIGHT_PURPLE, 10.0F, 750);

        public final String translationName;
        public final TextFormatting colour;
        public final double energyCost;
        public final float drillSpeed;
        private static final DrillMode[] VALUES = values();

        DrillMode(TextFormatting colour, float speed, double energyCost) {
            this.translationName = "advanced_drill." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name());
            this.energyCost = energyCost;
            this.drillSpeed = speed;
            this.colour = colour;
        }

        public static DrillMode getFromID(int ID) {
            return VALUES[ID % VALUES.length];
        }
    }
}
