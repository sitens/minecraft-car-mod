package com.kiancars.carmod.item;

import com.kiancars.carmod.entity.CarEntity;
import com.kiancars.carmod.entity.CarType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;

/**
 * Placement item for a car, modeled directly on vanilla {@code BoatItem}:
 * right-clicking a block (or looking at water) spawns a {@link CarEntity} of
 * this item's {@link CarType} in front of the player.
 */
public class CarItem extends Item {

    private final CarType carType;

    public CarItem(CarType carType, Properties properties) {
        super(properties);
        this.carType = carType;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        HitResult hit = getPlayerPOVHitResult(level, player, net.minecraft.world.level.ClipContext.Fluid.ANY);
        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            CarEntity car = new CarEntity(level, carType);
            car.setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
            car.setYRot(player.getYRot());
            level.addFreshEntity(car);
            level.playSound(null, car.blockPosition(), SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.getItemInHand(hand).shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
