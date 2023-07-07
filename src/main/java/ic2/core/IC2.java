//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core;

import ic2.api.energy.EnergyNet;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.tile.ExplosionWhitelist;
import ic2.api.tile.IWrenchable;
import ic2.api.util.Keys;
import ic2.core.EnhancedOverlay.Segment;
import ic2.core.apihelper.ApiHelper;
import ic2.core.audio.AudioManager;
import ic2.core.block.BlockIC2Fluid;
import ic2.core.block.EntityDynamite;
import ic2.core.block.EntityItnt;
import ic2.core.block.EntityNuke;
import ic2.core.block.EntityStickyDynamite;
import ic2.core.block.comp.Components;
import ic2.core.block.comp.Obscuration.ObscurationComponentEventHandler;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityCompressor;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityExtractor;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.block.steam.TileEntityCokeKiln;
import ic2.core.command.CommandIc2;
import ic2.core.crop.IC2Crops;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.init.BlocksItems;
import ic2.core.init.Ic2Loot;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.init.Rezepte;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.EntityBoatCarbon;
import ic2.core.item.EntityBoatElectric;
import ic2.core.item.EntityBoatRubber;
import ic2.core.item.EntityIC2Boat;
import ic2.core.item.GatewayElectricItemManager;
import ic2.core.item.ItemIC2Boat;
import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.item.tfbp.Tfbp;
import ic2.core.item.tool.EntityMiningLaser;
import ic2.core.item.tool.EntityParticle;
import ic2.core.item.type.CellType;
import ic2.core.item.type.CraftingItemType;
import ic2.core.network.NetworkManager;
import ic2.core.profile.ProfileManager;
import ic2.core.profile.Version;
import ic2.core.recipe.OreDictionaryEntries;
import ic2.core.recipe.RecipeInputFactory;
import ic2.core.recipe.ScrapboxRecipeManager;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.ItemInfo;
import ic2.core.util.Keyboard;
import ic2.core.util.Log;
import ic2.core.util.LogCategory;
import ic2.core.util.PriorityExecutor;
import ic2.core.util.RotationUtil;
import ic2.core.util.SideGateway;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.uu.UuIndex;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.FogMode;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import org.lwjgl.opengl.GL11;

@Mod(
  modid = "ic2",
  name = "IndustrialCraft 2",
  version = "2.8.221-ex112",
  acceptedMinecraftVersions = "[1.12]",
  useMetadata = true,
  certificateFingerprint = "de041f9f6187debbc77034a344134053277aa3b0",
  dependencies = "required-after:forge@[13.20.0.2206,)",
  guiFactory = "ic2.core.gui.Ic2GuiFactory"
)
public class IC2 implements IFuelHandler {
  public static final String VERSION = "2.8.221-ex112";
  public static final String MODID = "ic2";
  public static final String RESOURCE_DOMAIN = "ic2";
  private static IC2 instance;
  @SidedProxy(
    clientSide = "ic2.core.PlatformClient",
    serverSide = "ic2.core.Platform"
  )
  public static Platform platform;
  public static SideGateway<NetworkManager> network;
  @SidedProxy(
    clientSide = "ic2.core.util.KeyboardClient",
    serverSide = "ic2.core.util.Keyboard"
  )
  public static Keyboard keyboard;
  @SidedProxy(
    clientSide = "ic2.core.audio.AudioManagerClient",
    serverSide = "ic2.core.audio.AudioManager"
  )
  public static AudioManager audioManager;
  public static Log log;
  public static IC2Achievements achievements;
  public static TickHandler tickHandler;
  public static Random random;
  public static boolean suddenlyHoes;
  public static boolean seasonal;
  public static boolean initialized;
  public static Version version;
  public static final CreativeTabIC2 tabIC2;
  public static final int setBlockNotify = 1;
  public static final int setBlockUpdate = 2;
  public static final int setBlockNoUpdateFromClient = 4;
  public final PriorityExecutor threadPool = new PriorityExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 2));

  public IC2() {
    instance = this;
    Info.ic2ModInstance = this;
  }

  public static IC2 getInstance() {
    return instance;
  }

  @EventHandler
  public void load(FMLPreInitializationEvent event) {
    long startTime = System.nanoTime();
    log = new Log(event.getModLog());
    log.debug(LogCategory.General, "Starting pre-init.");
    MainConfig.load();
    ProfileManager.init();
    Localization.preInit(event.getSourceFile());
    tickHandler = new TickHandler();
    audioManager.initialize();
    Recipes.inputFactory = new RecipeInputFactory();
    ElectricItem.manager = new GatewayElectricItemManager();
    ElectricItem.rawManager = new ElectricItemManager();
    Info.itemInfo = new ItemInfo();
    Keys.instance = keyboard;
    Components.init();
    BlocksItems.init();
    Blocks.OBSIDIAN.func_149752_b(60.0F);
    Blocks.ENCHANTING_TABLE.func_149752_b(60.0F);
    Blocks.ENDER_CHEST.func_149752_b(60.0F);
    Blocks.ANVIL.func_149752_b(60.0F);
    Blocks.WATER.func_149752_b(30.0F);
    Blocks.field_150358_i.func_149752_b(30.0F);
    Blocks.LAVA.func_149752_b(30.0F);
    ExplosionWhitelist.addWhitelistedBlock(Blocks.BEDROCK);
    ScrapboxRecipeManager.setup();
    Tfbp.init();
    TileEntityCanner.init();
    TileEntityCompressor.init();
    TileEntityExtractor.init();
    TileEntityMacerator.init();
    TileEntityRecycler.init();
    TileEntityCentrifuge.init();
    TileEntityMatter.init();
    TileEntityMetalFormer.init();
    TileEntitySemifluidGenerator.init();
    TileEntityOreWashing.init();
    TileEntityFluidHeatGenerator.init();
    TileEntityBlockCutter.init();
    TileEntityBlastFurnace.init();
    TileEntityLiquidHeatExchanger.init();
    TileEntityFermenter.init();
    TileEntityElectrolyzer.init();
    TileEntityCokeKiln.init();
    Rezepte.registerRecipes();
    EntityIC2Boat.init();
    MinecraftForge.EVENT_BUS.register(this);
    Rezepte.registerWithSorter();
    String[] var4 = OreDictionary.getOreNames();
    int var5 = var4.length;

    for(int var6 = 0; var6 < var5; ++var6) {
      String oreName = var4[var6];
      Iterator var8 = OreDictionary.getOres(oreName).iterator();

      while(var8.hasNext()) {
        ItemStack ore = (ItemStack)var8.next();
        this.registerOre(new OreDictionary.OreRegisterEvent(oreName, ore));
      }
    }

    OreDictionaryEntries.load();
    EnergyNet.instance = EnergyNetGlobal.create();
    IC2Crops.init();
    IC2Potion.init();
    ApiHelper.preload();
    achievements = new IC2Achievements();
    Ic2Loot.init();
    EntityRegistry.registerModEntity(getIdentifier("mining_laser"), EntityMiningLaser.class, "MiningLaser", 0, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("dynamite"), EntityDynamite.class, "Dynamite", 1, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("sticky_dynamite"), EntityStickyDynamite.class, "StickyDynamite", 2, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("itnt"), EntityItnt.class, "Itnt", 3, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("nuke"), EntityNuke.class, "Nuke", 4, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("carbon_boat"), EntityBoatCarbon.class, "BoatCarbon", 5, this, 80, 3, true);
    EntityRegistry.registerModEntity(getIdentifier("rubber_boat"), EntityBoatRubber.class, "BoatRubber", 6, this, 80, 3, true);
    EntityRegistry.registerModEntity(getIdentifier("electric_boat"), EntityBoatElectric.class, "BoatElectric", 7, this, 80, 3, true);
    EntityRegistry.registerModEntity(getIdentifier("particle"), EntityParticle.class, "Particle", 8, this, 160, 1, true);
    if (Util.inDev()) {
      EntityRegistry.registerModEntity(getIdentifier("beam"), ic2.core.block.beam.EntityParticle.class, "Beam", 9, this, 160, 1, true);
    }

    EntityRegistry.registerModEntity(getIdentifier("fireproof_item"), ItemIC2Boat.FireproofItem.class, "FireproofItem", 10, this, 80, 1, false);
    int d = Integer.parseInt((new SimpleDateFormat("Mdd")).format(new Date()));
    suddenlyHoes = (double)d > Math.cbrt(6.4E7) && (double)d < Math.cbrt(6.5939264E7);
    seasonal = (double)d > Math.cbrt(1.089547389E9) && (double)d < Math.cbrt(1.338273208E9);
    GameRegistry.registerWorldGenerator(new Ic2WorldDecorator(), 0);
    GameRegistry.registerFuelHandler(this);
    MinecraftForge.EVENT_BUS.register(new IC2BucketHandler());
    TeBlock.registerTeMappings();
    ObscurationComponentEventHandler.init();
    platform.preInit();
    initialized = true;
    log.debug(LogCategory.General, "Finished pre-init after %d ms.", new Object[]{(System.nanoTime() - startTime) / 1000000L});
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    long startTime = System.nanoTime();
    log.debug(LogCategory.General, "Starting init.");
    ScrapboxRecipeManager.load();
    new ChunkLoaderLogic();
    TeBlock.buildDummies();
    IC2Crops.ensureInit();
    log.debug(LogCategory.General, "Finished init after %d ms.", new Object[]{(System.nanoTime() - startTime) / 1000000L});
  }

  @EventHandler
  public void modsLoaded(FMLPostInitializationEvent event) {
    long startTime = System.nanoTime();
    log.debug(LogCategory.General, "Starting post-init.");
    if (!initialized) {
      platform.displayError("IndustrialCraft 2 has failed to initialize properly.", new Object[0]);
    }

    Rezepte.loadFailedRecipes();
    Iterator var4 = ConfigUtil.asRecipeInputList(MainConfig.get(), "misc/additionalValuableOres").iterator();

    Iterator var6;
    while(var4.hasNext()) {
      IRecipeInput input = (IRecipeInput)var4.next();
      var6 = input.getInputs().iterator();

      while(var6.hasNext()) {
        ItemStack stack = (ItemStack)var6.next();
        OreValues.add(stack, 1);
      }
    }

    if (loadSubModule("bcIntegration")) {
      log.debug(LogCategory.SubModule, "BuildCraft integration module loaded.");
    }

    List<IRecipeInput> purgedRecipes = new ArrayList();
    purgedRecipes.addAll(ConfigUtil.asRecipeInputList(MainConfig.get(), "recipes/purge"));
    if (ConfigUtil.getBool(MainConfig.get(), "balance/disableEnderChest")) {
      purgedRecipes.add(Recipes.inputFactory.forStack(new ItemStack(Blocks.ENDER_CHEST)));
    }

    List<IRecipe> recipesToPurge = new ArrayList();
    var6 = ForgeRegistries.RECIPES.iterator();

    while(true) {
      while(true) {
        ItemStack output;
        IRecipe recipe;
        do {
          do {
            if (!var6.hasNext()) {
              Stream var10000 = recipesToPurge.stream().map(IForgeRegistryEntry::getRegistryName);
              IForgeRegistryModifiable var10001 = (IForgeRegistryModifiable)ForgeRegistries.RECIPES;
//              var10000.forEach(var10001::remove);
              if (ConfigUtil.getBool(MainConfig.get(), "recipes/smeltToIc2Items")) {
                Map<ItemStack, ItemStack> smeltingMap = FurnaceRecipes.func_77602_a().func_77599_b();
                Iterator var22 = smeltingMap.entrySet().iterator();

                label74:
                while(true) {
                  Map.Entry entry;
                  do {
                    if (!var22.hasNext()) {
                      break label74;
                    }

                    entry = (Map.Entry)var22.next();
                    output = (ItemStack)entry.getValue();
                  } while(StackUtil.isEmpty(output));

                  boolean found = false;
                  int[] var11 = OreDictionary.getOreIDs(output);
                  int var12 = var11.length;

                  for(int var13 = 0; var13 < var12; ++var13) {
                    int oreId = var11[var13];
                    String oreName = OreDictionary.getOreName(oreId);
                    Iterator var16 = OreDictionary.getOres(oreName).iterator();

                    while(var16.hasNext()) {
                      ItemStack ore = (ItemStack)var16.next();
                      ore.getItem();
                      if (Util.getName(ore.getItem()).getNamespace().equals("ic2")) {
                        entry.setValue(StackUtil.copyWithSize(ore, StackUtil.getSize(output)));
                        found = true;
                        break;
                      }
                    }

                    if (found) {
                      break;
                    }
                  }
                }
              }

              TileEntityRecycler.initLate();
              JetpackAttachmentRecipe.init();
              JetpackHandler.init();
              UuIndex.instance.init();
              UuIndex.instance.refresh(true);
              platform.onPostInit();
              log.debug(LogCategory.General, "Finished post-init after %d ms.", new Object[]{(System.nanoTime() - startTime) / 1000000L});
              log.info(LogCategory.General, "%s version %s loaded.", new Object[]{"ic2", "2.8.221-ex112"});
              return;
            }

            recipe = (IRecipe)var6.next();
            output = recipe.getRecipeOutput();
          } while(StackUtil.isEmpty(output));
        } while(recipe.getRegistryName().getNamespace() == "ic2");

        Iterator var9 = purgedRecipes.iterator();

        while(var9.hasNext()) {
          IRecipeInput input = (IRecipeInput)var9.next();
          if (input.matches(output)) {
            recipesToPurge.add(recipe);
            break;
          }
        }
      }
    }
  }

  @EventHandler
  public void finished(FMLLoadCompleteEvent event) {
  }

  private static boolean loadSubModule(String name) {
    log.debug(LogCategory.SubModule, "Loading %s submodule: %s.", new Object[]{"ic2", name});

    try {
      Class<?> subModuleClass = IC2.class.getClassLoader().loadClass("ic2." + name + ".SubModule");
      return (Boolean)subModuleClass.getMethod("init").invoke((Object)null);
    } catch (Throwable var2) {
      log.debug(LogCategory.SubModule, "Submodule %s not loaded.", new Object[]{name});
      return false;
    }
  }

  @EventHandler
  public void serverStart(FMLServerStartingEvent event) {
    event.registerServerCommand(new CommandIc2());
  }

  public int getBurnTime(ItemStack stack) {
    if (!BlockName.sapling.hasItemStack()) {
      return 0;
    } else {
      if (stack != null) {
        Item item = stack.getItem();
        if (StackUtil.checkItemEquality(stack, BlockName.sapling.getItemStack())) {
          return 80;
        }

        if (item == Items.field_151120_aE) {
          return 50;
        }

        if (item == Item.getItemFromBlock(Blocks.CACTUS)) {
          return 50;
        }

        if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap))) {
          return 350;
        }

        if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
          return 3150;
        }

        if (item == ItemName.fluid_cell.getInstance()) {
          FluidStack fs = FluidUtil.getFluidContained(stack);
          if (fs != null && fs.getFluid() == FluidRegistry.LAVA) {
            int ret = TileEntityFurnace.func_145952_a(new ItemStack(Items.LAVA_BUCKET));
            return ret * fs.amount / 1000;
          }
        } else if (StackUtil.checkItemEquality(stack, ItemName.cell.getItemStack(CellType.lava))) {
          return TileEntityFurnace.func_145952_a(new ItemStack(Items.LAVA_BUCKET));
        }
      }

      return 0;
    }
  }

  @SubscribeEvent
  public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
  }

  @SubscribeEvent
  public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
    if (platform.isSimulating()) {
      keyboard.removePlayerReferences(event.player);
    }

  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    WorldData.onWorldUnload(event.getWorld());
  }

  public static void explodeMachineAt(World world, int x, int y, int z, boolean noDrop) {
    ExplosionIC2 explosion = new ExplosionIC2(world, (Entity)null, 0.5 + (double)x, 0.5 + (double)y, 0.5 + (double)z, 2.5F, 0.75F);
    explosion.destroy(x, y, z, noDrop);
    explosion.doExplosion();
  }

  public static int getSeaLevel(World world) {
    return world.dimension.func_76557_i();
  }

  public static int getWorldHeight(World world) {
    return world.func_72800_K();
  }

  @SubscribeEvent
  public void registerOre(OreDictionary.OreRegisterEvent event) {
    String oreClass = event.getName();
    ItemStack ore = event.getOre();
    if (ore.getItem() instanceof ItemBlock) {
      int multiplier = 1;
      if (oreClass.startsWith("dense")) {
        multiplier *= 3;
        oreClass = oreClass.substring("dense".length());
      }

      int value = 0;
      if (oreClass.equals("oreCoal")) {
        value = 1;
      } else if (!oreClass.equals("oreCopper") && !oreClass.equals("oreTin") && !oreClass.equals("oreLead") && !oreClass.equals("oreQuartz")) {
        if (!oreClass.equals("oreIron") && !oreClass.equals("oreGold") && !oreClass.equals("oreRedstone") && !oreClass.equals("oreLapis") && !oreClass.equals("oreSilver")) {
          if (!oreClass.equals("oreUranium") && !oreClass.equals("oreGemRuby") && !oreClass.equals("oreGemGreenSapphire") && !oreClass.equals("oreGemSapphire") && !oreClass.equals("oreRuby") && !oreClass.equals("oreGreenSapphire") && !oreClass.equals("oreSapphire")) {
            if (!oreClass.equals("oreDiamond") && !oreClass.equals("oreEmerald") && !oreClass.equals("oreTungsten")) {
              if (oreClass.startsWith("ore")) {
                value = 1;
              }
            } else {
              value = 5;
            }
          } else {
            value = 4;
          }
        } else {
          value = 3;
        }
      } else {
        value = 2;
      }

      if (value > 0 && multiplier >= 1) {
        OreValues.add(ore, value * multiplier);
      }

    }
  }

  @SubscribeEvent
  public void onLivingSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
    if (seasonal && (event.getEntityLiving() instanceof EntityZombie || event.getEntityLiving() instanceof EntitySkeleton) && event.getEntityLiving().getEntityWorld().rand.nextFloat() < 0.1F) {
      EntityLiving entity = (EntityLiving)event.getEntityLiving();
      EntityEquipmentSlot[] var3 = EntityEquipmentSlot.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
        EntityEquipmentSlot slot = var3[var5];
        entity.setDropChance(slot, Float.NEGATIVE_INFINITY);
      }

      if (entity instanceof EntityZombie) {
        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemName.nano_saber.getItemStack());
      }

      if (entity.getEntityWorld().rand.nextFloat() < 0.1F) {
        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemName.quantum_helmet.getItemStack());
        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemName.quantum_chestplate.getItemStack());
        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemName.quantum_leggings.getItemStack());
        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemName.quantum_boots.getItemStack());
      } else {
        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemName.nano_helmet.getItemStack());
        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemName.nano_chestplate.getItemStack());
        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemName.nano_leggings.getItemStack());
        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemName.nano_boots.getItemStack());
      }
    }

  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void onViewRenderFogDensity(EntityViewRenderEvent.FogDensity event) {
    if (event.getState().getBlock() instanceof BlockIC2Fluid) {
      event.setCanceled(true);
      Fluid fluid = ((BlockIC2Fluid)event.getState().getBlock()).getFluid();
      GlStateManager.func_187430_a(FogMode.EXP);
      event.setDensity((float)Util.map((double)Math.abs(fluid.getDensity()), 20000.0, 2.0));
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void onViewRenderFogColors(EntityViewRenderEvent.FogColors event) {
    if (event.getState().getBlock() instanceof BlockIC2Fluid) {
      int color = ((BlockIC2Fluid)event.getState().getBlock()).getColor();
      event.setRed((float)(color >>> 16 & 255) / 255.0F);
      event.setGreen((float)(color >>> 8 & 255) / 255.0F);
      event.setBlue((float)(color & 255) / 255.0F);
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void renderEnhancedOverlay(DrawBlockHighlightEvent event) {
    ItemStack inHand = StackUtil.get(event.getPlayer(), EnumHand.MAIN_HAND);
    if (event.getSubID() == 0 && event.getTarget().field_72313_a == Type.BLOCK && inHand.getItem() instanceof IEnhancedOverlayProvider) {
      World world = event.getPlayer().world;
      BlockPos blockPos = event.getTarget().func_178782_a();
      EnumFacing side = event.getTarget().field_178784_b;
      if (((IEnhancedOverlayProvider)inHand.getItem()).providesEnhancedOverlay(world, blockPos, side, event.getPlayer(), inHand)) {
        GL11.glPushMatrix();
        EnhancedOverlay.transformToFace(event.getPlayer(), blockPos, side, event.getPartialTicks());
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.5F);
        GL11.glBegin(1);
        GL11.glVertex3d(0.5, 0.0, -0.25);
        GL11.glVertex3d(-0.5, 0.0, -0.25);
        GL11.glVertex3d(0.5, 0.0, 0.25);
        GL11.glVertex3d(-0.5, 0.0, 0.25);
        GL11.glVertex3d(0.25, 0.0, -0.5);
        GL11.glVertex3d(0.25, 0.0, 0.5);
        GL11.glVertex3d(-0.25, 0.0, -0.5);
        GL11.glVertex3d(-0.25, 0.0, 0.5);
        GL11.glVertex3d(0.5, 0.0, -0.5);
        GL11.glVertex3d(-0.5, 0.0, -0.5);
        GL11.glVertex3d(0.5, 0.0, 0.5);
        GL11.glVertex3d(-0.5, 0.0, 0.5);
        GL11.glVertex3d(0.5, 0.0, -0.5);
        GL11.glVertex3d(0.5, 0.0, 0.5);
        GL11.glVertex3d(-0.5, 0.0, -0.5);
        GL11.glVertex3d(-0.5, 0.0, 0.5);
        GL11.glEnd();
        GlStateManager.func_179147_l();
        GlStateManager.func_187428_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.func_179090_x();
        GlStateManager.func_179132_a(false);
        Map<EnumFacing, Runnable> additionalRenders = new EnumMap(EnumFacing.class);
        if (!(world.getBlockState(blockPos).getBlock() instanceof IWrenchable)) {
          EnhancedOverlay.forFace(side).drawArea(Segment.forRayTrace(event.getTarget()), Tessellator.getInstance().getBuffer(), 0, 0, 0);
        } else {
          EnumFacing hoveredSpin = RotationUtil.rotateByRay(event.getTarget());
          IWrenchable block = (IWrenchable)world.getBlockState(blockPos).getBlock();
          List<EnhancedOverlay.Segment> skippedSegments = new ArrayList();
          EnhancedOverlay.Segment[] var10 = Segment.values();
          int var11 = var10.length;

          for(int var12 = 0; var12 < var11; ++var12) {
            EnhancedOverlay.Segment segment = var10[var12];
            EnumFacing spin;
            switch (segment) {
              case CENTRE:
                spin = side;
                break;
              case TOP:
                if (side.getAxis().func_176720_b()) {
                  spin = EnumFacing.NORTH;
                } else {
                  spin = EnumFacing.UP;
                }
                break;
              case BOTTOM:
                if (side.getAxis().func_176720_b()) {
                  spin = EnumFacing.SOUTH;
                } else {
                  spin = EnumFacing.DOWN;
                }
                break;
              case LEFT:
                if (side.getAxis().func_176720_b()) {
                  spin = EnumFacing.WEST;
                } else {
                  spin = side.rotateY();
                }
                break;
              case RIGHT:
                if (side.getAxis().func_176720_b()) {
                  spin = EnumFacing.EAST;
                } else {
                  spin = side.rotateYCCW();
                }
                break;
              case TOP_LEFT:
              case TOP_RIGHT:
              case BOTTOM_LEFT:
              case BOTTOM_RIGHT:
                spin = side.getOpposite();
                break;
              default:
                throw new IllegalStateException("Unexpected segment: " + segment);
            }

            if (block.canSetFacing(world, blockPos, spin, event.getPlayer())) {
              byte red;
              short green;
              short blue;
              if (hoveredSpin == spin) {
                blue = 0;
                red = 0;
                green = 255;
              } else {
                green = 0;
                red = 0;
                blue = 255;
              }

              EnhancedOverlay.forFace(side).drawArea(segment, Tessellator.getInstance().getBuffer(), red, green, blue);
              if (hoveredSpin == spin) {
                if (side.getOpposite() == spin) {
                  EnumFacing[] edges = null;
                  EnumFacing[] sides = null;
                  switch (side.getAxis()) {
                    case X:
                      edges = new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH};
                      break;
                    case Y:
                      sides = Plane.HORIZONTAL.func_179516_a();
                      break;
                    case Z:
                      sides = Plane.VERTICAL.func_179516_a();
                      edges = new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST};
                  }

                  EnumFacing[] var20;
                  int var21;
                  int var22;
                  EnumFacing face;
                  if (edges != null) {
                    var20 = edges;
                    var21 = edges.length;

                    for(var22 = 0; var22 < var21; ++var22) {
                      face = var20[var22];
                      EnumFacing finalFace1 = face;
                      additionalRenders.put(face, () -> {
                        GlStateManager.func_179131_c((float)red / 255.0F, (float)green / 255.0F, (float)blue / 255.0F, 0.5F);
                        EnhancedOverlay.drawArea(finalFace1, new EnhancedOverlay.Segment[]{Segment.TOP_LEFT, Segment.TOP, Segment.TOP_RIGHT, Segment.BOTTOM_LEFT, Segment.BOTTOM, Segment.BOTTOM_RIGHT});
                      });
                    }
                  }

                  if (sides != null) {
                    var20 = sides;
                    var21 = sides.length;

                    for(var22 = 0; var22 < var21; ++var22) {
                      face = var20[var22];
                      EnumFacing finalFace = face;
                      additionalRenders.put(face, () -> {
                        GlStateManager.func_179131_c((float)red / 255.0F, (float)green / 255.0F, (float)blue / 255.0F, 0.5F);
                        EnhancedOverlay.drawArea(finalFace, new EnhancedOverlay.Segment[]{Segment.TOP_LEFT, Segment.LEFT, Segment.BOTTOM_LEFT, Segment.TOP_RIGHT, Segment.RIGHT, Segment.BOTTOM_RIGHT});
                      });
                    }
                  }
                } else if (segment == Segment.CENTRE) {
                  additionalRenders.put(spin, () -> {
                    GlStateManager.func_179131_c((float)red / 255.0F, (float)green / 255.0F, (float)blue / 255.0F, 0.5F);
                    EnhancedOverlay.drawArea(spin, (EnhancedOverlay.Segment[])skippedSegments.toArray(new EnhancedOverlay.Segment[skippedSegments.size()]));
                  });
                } else {
                  additionalRenders.put(spin, () -> {
                    EnhancedOverlay.forFace(spin).drawSide(Tessellator.getInstance().getBuffer(), red, green, blue);
                  });
                }
              }
            } else {
              skippedSegments.add(segment);
            }
          }
        }

        Runnable r = (Runnable)additionalRenders.remove(side);
        if (r != null) {
          r.run();
        }

        GL11.glPopMatrix();
        Iterator var25 = additionalRenders.entrySet().iterator();

        while(var25.hasNext()) {
          Map.Entry<EnumFacing, Runnable> entry = (Map.Entry)var25.next();
          GlStateManager.func_179094_E();
          EnhancedOverlay.transformToFace(event.getPlayer(), blockPos, (EnumFacing)entry.getKey(), event.getPartialTicks());
          ((Runnable)entry.getValue()).run();
          GlStateManager.func_179121_F();
        }

        GlStateManager.func_179132_a(true);
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
      }
    }

  }

  public static ResourceLocation getIdentifier(String name) {
    return new ResourceLocation("ic2", name);
  }

  static {
    try {
      (new BlockPos(1, 2, 3)).add(2, 3, 4);
    } catch (Throwable var1) {
      throw new Error("IC2 is incompatible with this environment, use the normal IC2 version, not the dev one.", var1);
    }

    instance = null;
    network = new SideGateway("ic2.core.network.NetworkManager", "ic2.core.network.NetworkManagerClient");
    random = new Random();
    suddenlyHoes = false;
    seasonal = false;
    initialized = false;
    version = ProfileManager.selected.style;
    tabIC2 = new CreativeTabIC2();
  }
}