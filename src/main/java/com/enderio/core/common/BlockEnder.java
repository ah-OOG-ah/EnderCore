package com.enderio.core.common;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class BlockEnder<T extends TileEntityBase> extends Block {

  protected final @Nullable Class<? extends T> teClass;
  protected final @Nonnull String name;

  protected BlockEnder(@Nonnull String name, @Nullable Class<? extends T> teClass) {
    this(name, teClass, new Material(MapColor.IRON));
  }

  protected BlockEnder(@Nonnull String name, @Nullable Class<? extends T> teClass, @Nonnull Material mat) {
    super(mat);
    this.teClass = teClass;
    
    this.name = name;
    setHardness(0.5F);
    setUnlocalizedName(name);
    setRegistryName(name);
    setSoundType(SoundType.METAL);
    setHarvestLevel("pickaxe", 0);
  }

  protected void init() {
    GameRegistry.register(this);   
    if (teClass != null) {
      GameRegistry.registerTileEntity(teClass, name + "TileEntity");
    }
    GameRegistry.register(createItemBlock());
  }
  
  protected ItemBlock createItemBlock() {
    ItemBlock ib = new ItemBlock(this);
    ib.setRegistryName(getName());
    return ib;
  }

  @Override
  public boolean hasTileEntity(@Nonnull IBlockState state) {
    return teClass != null;
  }

  @Override
  public @Nonnull TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
    if (teClass != null) {
      try {
        T te = teClass.newInstance();
        te.init();
        return te;
      } catch (Exception e) {
        throw new RuntimeException("Could not create tile entity for block " + name + " for class " + teClass, e);
      }
    }
    throw new RuntimeException(
        "Cannot create a TileEntity for a block that doesn't have a TileEntity. This is not a problem with EnderCore, this is caused by the caller.");
  }

  /* Subclass Helpers */

  
  
  @Override
  public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn,
      @Nonnull EnumHand hand, @Nullable ItemStack heldItem, @Nonnull EnumFacing side,
      float hitX, float hitY, float hitZ) {
    if (playerIn.isSneaking()) {
      return false;
    }
    TileEntity te = worldIn.getTileEntity(pos);
    if (te instanceof ITankAccess) {
      if (FluidUtil.fillInternalTankFromPlayerHandItem(worldIn, pos, playerIn, hand, (ITankAccess) te)) {
        return true;
      }
      if (FluidUtil.fillPlayerHandItemFromInternalTank(worldIn, pos, playerIn, hand, (ITankAccess) te)) {
        return true;
      }
    }

    return openGui(worldIn, pos, playerIn, side);
  }


  protected boolean openGui(World world, BlockPos pos, EntityPlayer entityPlayer, EnumFacing side) {
    return false;
  }

  public boolean doNormalDrops(IBlockAccess world, BlockPos pos) {
    return true;
  }

  
  @Override
  public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
    if (willHarvest) {
      return true;
    }
    return super.removedByPlayer(state, world, pos, player, willHarvest);
  }

  @Override
  public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te,
      @Nullable ItemStack stack) {
    super.harvestBlock(worldIn, player, pos, state, te, stack);
    worldIn.setBlockToAir(pos);
  }

  @Override
  public @Nonnull List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
    if (doNormalDrops(world, pos)) {
      return super.getDrops(world, pos, state, fortune);
    }
    return Lists.newArrayList(getNBTDrop(world, pos, getTileEntity(world, pos)));
  }

  public ItemStack getNBTDrop(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable T te) {
    int meta = damageDropped(world.getBlockState(pos));
    ItemStack itemStack = new ItemStack(this, 1, meta);
    processDrop(world, pos, te, itemStack);
    return itemStack;
  }

  protected void processDrop(IBlockAccess world, BlockPos pos, @Nullable T te, ItemStack drop) {
  }

  @SuppressWarnings("unchecked")
  protected @Nullable T getTileEntity(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    if (teClass != null) {
      TileEntity te = world.getTileEntity(pos);
      final Class<? extends T> teClass2 = teClass;
      if (teClass2 != null) {
        if (teClass2.isInstance(te)) {
          return (T) te;
        }
      } else {
        // TODO handle null value
      }
    }
    return null;
  }

  protected boolean shouldDoWorkThisTick(@Nonnull World world, @Nonnull BlockPos pos, int interval) {
    T te = getTileEntity(world, pos);
    if (te == null) {
      return world.getTotalWorldTime() % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval);
    }
  }

  protected boolean shouldDoWorkThisTick(@Nonnull World world, @Nonnull BlockPos pos, int interval, int offset) {
    T te = getTileEntity(world, pos);
    if (te == null) {
      return (world.getTotalWorldTime() + offset) % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval, offset);
    }
  }

  public @Nonnull String getName() {
    return name;
  }
  
  
}