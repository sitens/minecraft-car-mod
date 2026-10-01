package com.kiancars.carmod.block;

import com.kiancars.carmod.menu.HeroWorkshopMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The AI Crafting Table: where the built-in AI designs and builds hero gear. */
public class AiCraftingTableBlock extends Block {

    public AiCraftingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
            player.openMenu(new SimpleMenuProvider(
                    (windowId, inventory, p) -> new HeroWorkshopMenu(windowId, inventory, access),
                    Component.translatable("container.carmod.ai_workshop")));
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }
        return InteractionResult.SUCCESS;
    }
}
