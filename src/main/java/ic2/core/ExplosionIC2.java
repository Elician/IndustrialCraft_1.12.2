

package ic2.core;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.flag.Flags;
import ic2.api.event.ExplosionEvent;
import ic2.api.tile.ExplosionWhitelist;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

public class ExplosionIC2 extends Explosion {
  private final World worldObj;
  private final Entity exploder;
  private final double explosionX;
  private final double explosionY;
  private final double explosionZ;
  private final int mapHeight;
  private final float power;
  private final float explosionDropRate;
  private final Type type;
  private final int radiationRange;
  private final EntityLivingBase igniter;
  private final Random rng;
  private final double maxDistance;
  private final int areaSize;
  private final int areaX;
  private final int areaZ;
  private final DamageSource damageSource;
  private final List<EntityDamage> entitiesInRange;
  private final long[][] destroyedBlockPositions;
  private ChunkCache chunkCache;
  private static final double dropPowerLimit = 8.0;
  private static final double damageAtDropPowerLimit = 32.0;
  private static final double accelerationAtDropPowerLimit = 0.7;
  private static final double motionLimit = 60.0;
  private static final int secondaryRayCount = 5;
  private static final int bitSetElementSize = 2;

  public ExplosionIC2(World world, Entity entity, double x, double y, double z, float power, float drop) {
    this(world, entity, x, y, z, power, drop, ExplosionIC2.Type.Normal);
  }

  public ExplosionIC2(World world, Entity entity, double x, double y, double z, float power, float drop, Type type) {
    this(world, entity, x, y, z, power, drop, type, null, 0);
  }

  public ExplosionIC2(World world, Entity entity, BlockPos pos, float power, float drop, Type type) {
    this(world, entity, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, power, drop, type);
  }

  public ExplosionIC2(World world, Entity entity, double x, double y, double z, float power1, float drop, Type type1, EntityLivingBase igniter1, int radiationRange1) {
    super(world, entity, x, y, z, power1, false, false);
    this.rng = new Random();
    this.entitiesInRange = new ArrayList<>();
    this.worldObj = world;
    this.exploder = entity;
    this.explosionX = x;
    this.explosionY = y;
    this.explosionZ = z;
    this.mapHeight = IC2.getWorldHeight(world);
    this.power = power1;
    this.explosionDropRate = drop;
    this.type = type1;
    this.igniter = igniter1;
    this.radiationRange = radiationRange1;
    this.maxDistance = (double) this.power / 0.4;
    int maxDistanceInt = (int) Math.ceil(this.maxDistance);
    this.areaSize = maxDistanceInt * 2;
    this.areaX = Util.roundToNegInf(x) - maxDistanceInt;
    this.areaZ = Util.roundToNegInf(z) - maxDistanceInt;
    if (this.isNuclear()) {
      this.damageSource = IC2DamageSource.getNukeSource(this);
    } else {
      this.damageSource = DamageSource.causeExplosionDamage(this);
    }

    this.destroyedBlockPositions = new long[this.mapHeight][];
  }

  public ExplosionIC2(World world, Entity entity, BlockPos pos, int i, float f, Type heat) {
    this(world, entity, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, (float) i, f, heat);
  }

  public void doExplosion() {
    this.doExplosion(null);
  }

  public void doExplosion(String exploder_owner_uuid) {
    if (!(this.power <= 0.0F)) {
      ExplosionEvent event = new ExplosionEvent(this.worldObj, this.exploder, this.getPosition(), this.power, this.igniter, this.radiationRange, this.maxDistance);
      if (!MinecraftForge.EVENT_BUS.post(event)) {
        int range = this.areaSize / 2;
        BlockPos pos = new BlockPos(this.getPosition());
        BlockPos start = pos.add(-range, -range, -range);
        BlockPos end = pos.add(range, range, range);
        this.chunkCache = new ChunkCache(this.worldObj, start, end, 0);
        List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(start, end));
        Iterator var7 = entities.iterator();

        while (true) {
          Entity entity;
          do {
            if (!var7.hasNext()) {
              boolean entitiesAreInRange = !this.entitiesInRange.isEmpty();
              if (entitiesAreInRange) {
                this.entitiesInRange.sort(Comparator.comparingInt(a -> a.distance));
              }

              int steps = (int) Math.ceil(Math.PI / Math.atan(1.0 / this.maxDistance));
              BlockPos.MutableBlockPos tmpPos = new BlockPos.MutableBlockPos();

              for (int phi_n = 0; phi_n < 2 * steps; ++phi_n) {
                for (int theta_n = 0; theta_n < steps; ++theta_n) {
                  double phi = 6.283185307179586 / (double) steps * (double) phi_n;
                  double theta = Math.PI / (double) steps * (double) theta_n;
                  this.shootRay(this.explosionX, this.explosionY, this.explosionZ, phi, theta, (double) this.power, entitiesAreInRange && phi_n % 8 == 0 && theta_n % 8 == 0, tmpPos);
                }
              }

              double reduction;
              EntityDamage entry;
              double distance;
              for (Iterator var31 = this.entitiesInRange.iterator(); var31.hasNext(); entity.field_70179_y += entry.motionZ * reduction) {
                entry = (EntityDamage) var31.next();
                entity = entry.entity;
                entity.attackEntityFrom(this.damageSource, (float) entry.damage);
                if (entity instanceof EntityPlayer) {
                  EntityPlayer player = (EntityPlayer) entity;
                  if (this.isNuclear() && this.igniter != null && player == this.igniter && player.getHealth() <= 0.0F) {
                    IC2.achievements.issueAchievement(player, "dieFromOwnNuke");
                  }
                }

                distance = Util.square(entry.motionX) + Util.square(entity.field_70181_x) + Util.square(entity.field_70179_y);
                reduction = distance > 3600.0 ? Math.sqrt(3600.0 / distance) : 1.0;
                entity.field_70159_w += entry.motionX * reduction;
                entity.field_70181_x += entry.motionY * reduction;
              }

              int poisonLength;
              int index;
              if (this.isNuclear() && this.radiationRange >= 1) {
                List<EntityLiving> entitiesInRange = this.worldObj.func_72872_a(EntityLiving.class, new AxisAlignedBB(this.explosionX - (double) this.radiationRange, this.explosionY - (double) this.radiationRange, this.explosionZ - (double) this.radiationRange, this.explosionX + (double) this.radiationRange, this.explosionY + (double) this.radiationRange, this.explosionZ + (double) this.radiationRange));

                for (EntityLiving entity3 : entitiesInRange) {
                  if (!ItemArmorHazmat.hasCompleteHazmat(entity3)) {
                    distance = entity3.func_70011_f(this.explosionX, this.explosionY, this.explosionZ);
                    index = (int) (120.0 * ((double) this.radiationRange - distance));
                    poisonLength = (int) (80.0 * ((double) (this.radiationRange / 3) - distance));
                    if (index >= 0) {
                      entity3.func_70690_d(new PotionEffect(MobEffects.HUNGER, index, 0));
                    }

                    if (poisonLength >= 0) {
                      IC2Potion.radiation.applyTo(entity3, poisonLength, 0);
                    }
                  }
                }
              }

              (IC2.network.get(true)).initiateExplosionEffect(this.worldObj, this.getPosition(), this.type);
              Random rng = this.worldObj.rand;
              boolean doDrops = this.worldObj.getGameRules().func_82766_b("doTileDrops");
              Map<XZposition, Map<ItemComparableItemStack, DropData>> blocksToDrop = new HashMap<>();

              for (int y = 0; y < this.destroyedBlockPositions.length; ++y) {

                long[] bitSet = this.destroyedBlockPositions[y];
                Block block;

                if (bitSet == null) continue;

                for (index = -2; (index = nextSetIndex(index + 2, bitSet, 2)) != -1;) {
                  poisonLength = index / 2;
                  int z = poisonLength / this.areaSize;
                  int x = poisonLength - z * this.areaSize;
                  x += this.areaX;
                  z += this.areaZ;
                  tmpPos.func_181079_c(x, y, z);
                  IBlockState state = this.chunkCache.getBlockState(tmpPos);
                  block = state.getBlock();
                  if (this.power < 20.0F) {
                  }

                  if (doDrops && block.canDropFromExplosion(this) && getAtIndex(index, bitSet, 2) == 1) {

                    for (ItemStack stack : StackUtil.getDrops(this.worldObj, tmpPos, state, block, 0)) {
                      if (!(rng.nextFloat() > this.explosionDropRate)) {
                        XZposition xZposition = new XZposition(x / 2, z / 2);
                        Map<ItemComparableItemStack, DropData> map = blocksToDrop.get(xZposition);
                        if (map == null) {
                          map = new HashMap<>();
                          blocksToDrop.put(xZposition, map);
                        }

                        ItemComparableItemStack isw = new ItemComparableItemStack(stack, false);
                        DropData data = (DropData) ((Map) map).get(isw);
                        if (data == null) {
                          data = new DropData(StackUtil.getSize(stack), y);
                          ((Map) map).put(isw.copy(), data);
                        } else {
                          data.add(StackUtil.getSize(stack), y);
                        }
                      }
                    }
                  }

                  Location location = new Location((Extent) this.worldObj, x, y, z);

                  Claim claim = GriefDefender.getCore().getClaimAt(location);

                  if (claim != null && !claim.isWilderness()) {
                    if (exploder_owner_uuid != null) {
                      if (!claim.isUserTrusted(UUID.fromString(exploder_owner_uuid), TrustTypes.BUILDER)) continue;
                    }

                    Set<Context> contexts = new HashSet<>();

                    contexts.add(new Context("source", "ic2:te:22"));

                    boolean is_allow_explosions = claim.getFlagPermissionValue(Flags.EXPLOSION_BLOCK, contexts).asBoolean();

                    if (!is_allow_explosions) continue;
                  }

                  block.onBlockExploded(this.worldObj, tmpPos, this);

                }
              }

              for (Map.Entry<XZposition, Map<ItemComparableItemStack, DropData>> xZpositionMapEntry : blocksToDrop.entrySet()) {
                Map.Entry<XZposition, Map<ItemComparableItemStack, DropData>> entryS = (Map.Entry) xZpositionMapEntry;
                XZposition xZposition = (XZposition) entryS.getKey();
                Iterator var47 = ((Map) entryS.getValue()).entrySet().iterator();

                while (var47.hasNext()) {
                  Map.Entry<ItemComparableItemStack, DropData> entry2 = (Map.Entry) var47.next();
                  ItemComparableItemStack isw = (ItemComparableItemStack) entry2.getKey();

                  int stackSize;
                  for (int count = ((DropData) entry2.getValue()).n; count > 0; count -= stackSize) {
                    stackSize = Math.min(count, 64);
                    EntityItem entityitem = new EntityItem(this.worldObj, (double) (((float) xZposition.x + this.worldObj.rand.nextFloat()) * 2.0F), (double) ((DropData) entry2.getValue()).maxY + 0.5, (double) (((float) xZposition.z + this.worldObj.rand.nextFloat()) * 2.0F), isw.toStack(stackSize));
                    entityitem.setDefaultPickupDelay();
                    this.worldObj.addEntity0(entityitem);
                  }
                }
              }

              return;
            }

            entity = (Entity) var7.next();
          } while (!(entity instanceof EntityLivingBase) && !(entity instanceof EntityItem));

          int distance = (int) (Util.square(entity.posX - this.explosionX) + Util.square(entity.posY - this.explosionY) + Util.square(entity.posZ - this.explosionZ));
          double health = getEntityHealth(entity);
          this.entitiesInRange.add(new EntityDamage(entity, distance, health));
        }
      }
    }
  }

  public void destroy(int x, int y, int z, boolean noDrop) {
    this.destroyUnchecked(x, y, z, noDrop);
  }

  private void destroyUnchecked(int x, int y, int z, boolean noDrop) {
    int index = (z - this.areaZ) * this.areaSize + (x - this.areaX);
    index *= 2;
    long[] array = this.destroyedBlockPositions[y];
    if (array == null) {
      array = makeArray(Util.square(this.areaSize), 2);
      this.destroyedBlockPositions[y] = array;
    }

    if (noDrop) {
      setAtIndex(index, array, 3);
    } else {
      setAtIndex(index, array, 1);
    }

  }

  private void shootRay(double x, double y, double z, double phi, double theta, double power1, boolean killEntities, BlockPos.MutableBlockPos tmpPos) {
    double deltaX = Math.sin(theta) * Math.cos(phi);
    double deltaY = Math.cos(theta);
    double deltaZ = Math.sin(theta) * Math.sin(phi);
    int step = 0;

    while (true) {
      int blockY = Util.roundToNegInf(y);
      if (blockY < 0 || blockY >= this.mapHeight) {
        break;
      }

      int blockX = Util.roundToNegInf(x);
      int blockZ = Util.roundToNegInf(z);
      tmpPos.func_181079_c(blockX, blockY, blockZ);
      IBlockState state = this.chunkCache.getBlockState(tmpPos);
      Block block = state.getBlock();
      double absorption = this.getAbsorption(block, tmpPos);
      if (absorption < 0.0) {
        break;
      }

      if (absorption > 1000.0 && !ExplosionWhitelist.isBlockWhitelisted(block)) {
        absorption = 0.5;
      } else {
        if (absorption > power1) {
          break;
        }

        if (block == Blocks.STONE || block != Blocks.AIR && !block.isAir(state, this.worldObj, tmpPos)) {
          this.destroyUnchecked(blockX, blockY, blockZ, power1 > 8.0);
        }
      }

      if (killEntities && (step + 4) % 8 == 0 && !this.entitiesInRange.isEmpty() && power1 >= 0.25) {
        this.damageEntities(x, y, z, step, power1);
      }

      if (absorption > 10.0) {
        for (int i = 0; i < 5; ++i) {
          this.shootRay(x, y, z, this.rng.nextDouble() * 2.0 * Math.PI, this.rng.nextDouble() * Math.PI, absorption * 0.4, false, tmpPos);
        }
      }

      power1 -= absorption;
      x += deltaX;
      y += deltaY;
      z += deltaZ;
      ++step;
    }

  }

  private double getAbsorption(Block block, BlockPos pos) {
    double ret = 0.5;
    if (block != Blocks.AIR && !block.isAir(block.getDefaultState(), this.worldObj, pos)) {
      if ((block == Blocks.WATER || block == Blocks.field_150358_i) && this.type != ExplosionIC2.Type.Normal) {
        ++ret;
      } else {
        float resistance = block.getExplosionResistance(this.worldObj, pos, this.exploder, this);
        if (resistance < 0.0F) {
          return (double) resistance;
        }

        double extra = (double) (resistance + 4.0F) * 0.3;
        if (this.type != ExplosionIC2.Type.Heat) {
          ret += extra;
        } else {
          ret += extra * 6.0;
        }
      }

      return ret;
    } else {
      return ret;
    }
  }

  private void damageEntities(double x, double y, double z, int step, double power) {
    int index;
    int i;
    if (step != 4) {
      i = Util.square(step - 5);
      int indexStart = 0;
      int indexEnd = this.entitiesInRange.size() - 1;

      do {
        index = (indexStart + indexEnd) / 2;
        int distance = ((EntityDamage) this.entitiesInRange.get(index)).distance;
        if (distance < i) {
          indexStart = index + 1;
        } else if (distance > i) {
          indexEnd = index - 1;
        } else {
          indexEnd = index;
        }
      } while (indexStart < indexEnd);
    } else {
      index = 0;
    }

    int distanceMax = Util.square(step + 5);

    for (i = index; i < this.entitiesInRange.size(); ++i) {
      EntityDamage entry = (EntityDamage) this.entitiesInRange.get(i);
      if (entry.distance >= distanceMax) {
        break;
      }

      Entity entity = entry.entity;
      if (Util.square(entity.posX - x) + Util.square(entity.posY - y) + Util.square(entity.posZ - z) <= 25.0) {
        double damage = 4.0 * power;
        entry.damage += damage;
        entry.health -= damage;
        double dx = entity.posX - this.explosionX;
        double dy = entity.posY - this.explosionY;
        double dz = entity.posZ - this.explosionZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        entry.motionX += dx / distance * 0.0875 * power;
        entry.motionY += dy / distance * 0.0875 * power;
        entry.motionZ += dz / distance * 0.0875 * power;
        if (entry.health <= 0.0) {
          entity.attackEntityFrom(this.damageSource, (float) entry.damage);
          if (!entity.isAlive()) {
            this.entitiesInRange.remove(i);
            --i;
          }
        }
      }
    }

  }

  public EntityLivingBase getExplosivePlacedBy() {
    return this.igniter;
  }

  private boolean isNuclear() {
    return this.type == ExplosionIC2.Type.Nuclear;
  }

  private static double getEntityHealth(Entity entity) {
    return entity instanceof EntityItem ? 5.0 : Double.POSITIVE_INFINITY;
  }

  private static long[] makeArray(int size, int step) {
    return new long[(size * step + 8 - step) / 8];
  }

  private static int nextSetIndex(int start, long[] array, int step) {
    int offset = start % 8;

    for (int i = start / 8; i < array.length; ++i) {
      long aval = array[i];

      for (int j = offset; j < 8; j += step) {
        int val = (int) (aval >> j & (long) ((1 << step) - 1));
        if (val != 0) {
          return i * 8 + j;
        }
      }

      offset = 0;
    }

    return -1;
  }

  private static int getAtIndex(int index, long[] array, int step) {
    return (int) (array[index / 8] >>> index % 8 & (long) ((1 << step) - 1));
  }

  private static void setAtIndex(int index, long[] array, int value) {
    array[index / 8] |= (long) (value << index % 8);
  }

  private static class EntityDamage {
    final Entity entity;
    final int distance;
    double health;
    double damage;
    double motionX;
    double motionY;
    double motionZ;

    EntityDamage(Entity entity, int distance, double health) {
      this.entity = entity;
      this.distance = distance;
      this.health = health;
    }
  }

  public static enum Type {
    Normal,
    Heat,
    Electrical,
    Nuclear;

    private Type() {
    }
  }

  private static class DropData {
    int n;
    int maxY;

    DropData(int n1, int y) {
      this.n = n1;
      this.maxY = y;
    }

    public DropData add(int n1, int y) {
      this.n += n1;
      if (y > this.maxY) {
        this.maxY = y;
      }

      return this;
    }
  }

  private static class XZposition {
    int x;
    int z;

    XZposition(int x1, int z1) {
      this.x = x1;
      this.z = z1;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof XZposition)) {
        return false;
      } else {
        XZposition xZposition = (XZposition) obj;
        return xZposition.x == this.x && xZposition.z == this.z;
      }
    }

    public int hashCode() {
      return this.x * 31 ^ this.z;
    }
  }
}
