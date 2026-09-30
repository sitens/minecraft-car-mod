package com.kiancars.carmod.block;

import com.kiancars.carmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * A pedestal that holds one junk food. The food it holds is remembered in a
 * block state number ({@link #FOOD}): 0 means empty, 1-15 means "the Nth
 * junk food in the list". Twelve altars in a ring, each holding a different
 * junk food, plus a TNT blast in the middle, summons the hOesaC Final Boss
 * (see {@code com.kiancars.carmod.ritual.SummonRitual}).
 * <p>
 * Right-click with a junk food to put it on the altar; right-click with an
 * empty hand to take it back.
 */
public class AltarBlock extends Block {

    public static final int FOOD_KINDS = 15;
    public static final IntegerProperty FOOD = IntegerProperty.create("food", 0, FOOD_KINDS);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 3, 15),
            Block.box(5, 3, 5, 11, 9, 11),
            Block.box(2, 9, 2, 14, 12, 14));

    public AltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FOOD, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FOOD);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Which junk food (1-based, matching {@link #FOOD}) an item is, or 0 if it isn't junk food. */
    public static int foodIndexOf(ItemStack stack) {
        List<? extends net.neoforged.neoforge.registries.DeferredItem<Item>> foods = ModItems.junkFoods();
        for (int i = 0; i < foods.size() && i < FOOD_KINDS; i++) {
            if (stack.is(foods.get(i).get())) {
                return i + 1;
            }
        }
        return 0;
    }

    public static ItemStack foodStack(int index) {
        return new ItemStack(ModItems.junkFoods().get(index - 1).get());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        int index = foodIndexOf(stack);
        if (index == 0 || state.getValue(FOOD) != 0) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(FOOD, index), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.6F, 1.4F);
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        int index = state.getValue(FOOD);
        if (index == 0) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(FOOD, 0), Block.UPDATE_ALL);
            ItemStack food = foodStack(index);
            if (!player.getInventory().add(food)) {
                player.drop(food, false);
            }
            player.displayClientMessage(Component.translatable("message.carmod.altar_took", food.getHoverName()), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        int index = state.getValue(FOOD);
        if (index != 0 && !level.isClientSide() && !player.preventsBlockDrops()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, foodStack(index)));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
