package com.kiancars.carmod.block;

import com.kiancars.carmod.entity.HoesacBossEntity;
import com.kiancars.carmod.registry.ModTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A thin iron spike, two blocks tall. Anything that walks into it gets hurt
 * (3 hearts, straight through armor and potion effects). It reuses the
 * vanilla two-block-tall plant logic for placing and breaking both halves,
 * but can stand on any solid block instead of only dirt.
 */
public class TackBlock extends DoublePlantBlock {

    public static final MapCodec<TackBlock> CODEC = simpleCodec(TackBlock::new);

    /** 3 hearts. */
    public static final float DAMAGE = 6.0F;
    /** 2 hearts, for the Final Boss. */
    public static final float BOSS_DAMAGE = 4.0F;
    private static final long HIT_COOLDOWN_TICKS = 20;
    private static final String LAST_HIT_KEY = "carmod_last_tack_hit";

    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    public TackBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean intersects) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity living)) {
            return;
        }
        // Standing in both halves at once calls this twice a tick; the
        // cooldown keeps it to one hit per second.
        long now = serverLevel.getGameTime();
        long lastHit = living.getPersistentData().getLongOr(LAST_HIT_KEY, Long.MIN_VALUE / 2);
        if (now - lastHit < HIT_COOLDOWN_TICKS) {
            return;
        }
        living.getPersistentData().putLong(LAST_HIT_KEY, now);
        if (living instanceof HoesacBossEntity boss) {
            boss.hurtServer(serverLevel, serverLevel.damageSources().source(ModTags.TACK_DAMAGE), BOSS_DAMAGE);
            boss.onTackHit(pos.getCenter());
            return;
        }
        living.hurtServer(serverLevel, serverLevel.damageSources().source(ModTags.TACK_DAMAGE), DAMAGE);
    }
}
