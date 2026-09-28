package com.kiancars.carmod.item;

import com.kiancars.carmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * A cheap bucket made of planks. Like vanilla there are three items (empty,
 * water, lava) and scooping swaps one for another; the durability carries
 * over each time. Every scoop costs 1 durability, and carrying lava burns
 * 1 durability every second until the bucket falls apart.
 */
public class WoodenBucketItem extends BucketItem {

    public static final int MAX_DURABILITY = 16;
    private static final int SCOOP_COST = 1;

    public WoodenBucketItem(Fluid content, Item.Properties properties) {
        super(content, properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player,
                this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        BlockPos next = pos.relative(face);
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(next, face, stack)) {
            return InteractionResult.FAIL;
        }

        if (this.content == Fluids.EMPTY) {
            return scoop(level, player, stack, pos);
        }

        BlockState state = level.getBlockState(pos);
        BlockPos target = canBlockContainFluid(player, level, pos, state) ? pos : next;
        if (!this.emptyContents(player, level, target, hit, stack)) {
            return InteractionResult.FAIL;
        }
        this.checkExtraContent(player, level, stack, target);
        player.awardStat(Stats.ITEM_USED.get(this));
        ItemStack empty = carryDurability(stack, new ItemStack(ModItems.WOODEN_BUCKET.get()), 0, player);
        return InteractionResult.SUCCESS.heldItemTransformedTo(ItemUtils.createFilledResult(stack, player, empty));
    }

    private InteractionResult scoop(Level level, Player player, ItemStack stack, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        Item filledItem;
        if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
            filledItem = ModItems.WOODEN_WATER_BUCKET.get();
        } else if (fluid.is(FluidTags.LAVA) && fluid.isSource()) {
            filledItem = ModItems.WOODEN_LAVA_BUCKET.get();
        } else {
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) {
            return InteractionResult.FAIL;
        }
        if (pickup.pickupBlock(player, level, pos, state).isEmpty()) {
            return InteractionResult.FAIL;
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        pickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        ItemStack filled = carryDurability(stack, new ItemStack(filledItem), SCOOP_COST, player);
        return InteractionResult.SUCCESS.heldItemTransformedTo(ItemUtils.createFilledResult(stack, player, filled));
    }

    /** Copies wear from the old bucket to the new one; returns empty if that breaks it. */
    private static ItemStack carryDurability(ItemStack from, ItemStack to, int extraWear, Player player) {
        if (player.hasInfiniteMaterials()) {
            return to;
        }
        int wear = from.getDamageValue() + extraWear;
        if (wear >= to.getMaxDamage()) {
            player.playSound(SoundEvents.ITEM_BREAK.value(), 0.8F, 0.8F);
            return ItemStack.EMPTY;
        }
        to.setDamageValue(wear);
        return to;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (this.content != Fluids.LAVA || level.getGameTime() % 20 != 0) {
            return;
        }
        if (entity instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }
        int wear = stack.getDamageValue() + 1;
        if (wear >= stack.getMaxDamage()) {
            stack.setCount(0);
            level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.0F);
        } else {
            stack.setDamageValue(wear);
        }
    }
}
