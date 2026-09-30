package com.kiancars.carmod.ritual;

import com.kiancars.carmod.block.AltarBlock;
import com.kiancars.carmod.entity.HoesacBossEntity;
import com.kiancars.carmod.registry.ModBlocks;
import com.kiancars.carmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * The hOesaC Final Boss summoning ritual.
 * <pre>
 *   C A A A C      C = cobblestone corner
 *   A . . . A      A = altar holding a junk food (12 altars, all different kinds)
 *   A . T . A      . = the 3x3 patch of open ground in the middle
 *   A . . . A      T = TNT, which can be any of the 9 middle squares
 *   C A A A C
 * </pre>
 * Everything sits on the same level. When TNT explodes in the middle, the
 * altars' junk food is used up and the boss drops in.
 */
public final class SummonRitual {

    /** Returns true (and starts the boss fight) if the explosion was a correct ritual. */
    public static boolean tryStart(ServerLevel level, Vec3 explosionCenter) {
        BlockPos cell = BlockPos.containing(explosionCenter);
        // The TNT can be in any of the 9 middle squares, so try each one as the ring's center.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos center = cell.offset(dx, 0, dz);
                if (ringIsComplete(level, center)) {
                    start(level, center);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean ringIsComplete(ServerLevel level, BlockPos center) {
        Set<Integer> kinds = new HashSet<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.max(Math.abs(x), Math.abs(z)) != 2) {
                    continue;
                }
                BlockState state = level.getBlockState(center.offset(x, 0, z));
                boolean corner = Math.abs(x) == 2 && Math.abs(z) == 2;
                if (corner) {
                    if (!state.is(Blocks.COBBLESTONE)) {
                        return false;
                    }
                } else {
                    if (!state.is(ModBlocks.ALTAR.get()) || state.getValue(AltarBlock.FOOD) == 0) {
                        return false;
                    }
                    kinds.add(state.getValue(AltarBlock.FOOD));
                }
            }
        }
        return kinds.size() == 12;
    }

    private static void start(ServerLevel level, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = center.offset(x, 0, z);
                BlockState state = level.getBlockState(pos);
                if (state.is(ModBlocks.ALTAR.get())) {
                    level.setBlock(pos, state.setValue(AltarBlock.FOOD, 0), Block.UPDATE_ALL);
                }
            }
        }

        Vec3 spot = new Vec3(center.getX() + 0.5, center.getY() + 4.0, center.getZ() + 0.5);
        EntityType<HoesacBossEntity> type = ModEntities.HOESAC_BOSS.get();
        HoesacBossEntity boss = type.create(level, EntitySpawnReason.TRIGGERED);
        if (boss == null) {
            return;
        }
        boss.snapTo(spot.x, spot.y, spot.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(center), EntitySpawnReason.TRIGGERED, null);
        level.addFreshEntity(boss);

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.snapTo(spot.x, center.getY(), spot.z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(spot) < 64.0 * 64.0) {
                player.displayClientMessage(Component.translatable("message.carmod.boss_summoned"), false);
            }
        }
    }

    private SummonRitual() {
    }
}
