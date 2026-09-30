package com.kiancars.carmod.event;

import com.kiancars.carmod.entity.HoesacBossEntity;
import com.kiancars.carmod.entity.HoesacEntity;
import com.kiancars.carmod.registry.ModEntities;
import com.kiancars.carmod.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

public final class ModEvents {

    /** How often (in ticks) each player gets a chance to attract a hOesaC. */
    private static final int SPAWN_CHECK_INTERVAL = 200;
    private static final float SPAWN_CHANCE = 0.35F;
    private static final int MAX_NEARBY = 2;
    private static final int MIN_DISTANCE = 12;
    private static final int MAX_DISTANCE = 24;

    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.HOESAC.get(), HoesacEntity.createAttributes().build());
        event.put(ModEntities.HOESAC_BOSS.get(), HoesacBossEntity.createAttributes().build());
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level) || player.tickCount % SPAWN_CHECK_INTERVAL != 0) {
            return;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!ModTags.isCarryingJunkFood(player) || level.getRandom().nextFloat() > SPAWN_CHANCE) {
            return;
        }
        int nearby = level.getEntitiesOfClass(HoesacEntity.class, player.getBoundingBox().inflate(48)).size();
        if (nearby >= MAX_NEARBY) {
            return;
        }

        BlockPos spot = findSpawnSpot(level, player);
        if (spot == null) {
            return;
        }
        HoesacEntity hoesac = ModEntities.HOESAC.get().create(level, EntitySpawnReason.EVENT);
        if (hoesac == null) {
            return;
        }
        hoesac.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
        hoesac.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), EntitySpawnReason.EVENT, null);
        level.addFreshEntity(hoesac);
    }

    /** A spot 12-24 blocks away with solid ground and room for a 2-block-tall mob. */
    private static @Nullable BlockPos findSpawnSpot(ServerLevel level, Player player) {
        RandomSource random = level.getRandom();
        BlockPos center = player.blockPosition();
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int distance = MIN_DISTANCE + random.nextInt(MAX_DISTANCE - MIN_DISTANCE + 1);
            int x = center.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * distance);
            for (int dy = 6; dy >= -6; dy--) {
                BlockPos feet = new BlockPos(x, center.getY() + dy, z);
                BlockPos below = feet.below();
                if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                        && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                        && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                        && level.getFluidState(feet).isEmpty()) {
                    return feet;
                }
            }
        }
        return null;
    }

    /** If hOesaC killed a player, he keeps their junk food; everything else drops as normal. */
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getSource().getEntity() instanceof HoesacEntity hoesac)) {
            return;
        }
        Iterator<ItemEntity> drops = event.getDrops().iterator();
        while (drops.hasNext()) {
            ItemEntity drop = drops.next();
            if (drop.getItem().is(ModTags.JUNK_FOOD)) {
                hoesac.takeJunkFood(drop.getItem());
                drops.remove();
            }
        }
    }

    private ModEvents() {
    }
}
