package com.kiancars.carmod.block;

import com.kiancars.carmod.menu.BigCraftingMenu;
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

/**
 * A crafting table with a bigger grid (4x4 or 5x5). Stateless like vanilla's
 * crafting table: the grid contents live only in the open menu and get
 * handed back to the player when it closes.
 */
public class BigCraftingTableBlock extends Block {

    private final int size;

    public BigCraftingTableBlock(BlockBehaviour.Properties properties, int size) {
        super(properties);
        this.size = size;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
            player.openMenu(new SimpleMenuProvider(
                    (windowId, inventory, p) -> size == 5
                            ? BigCraftingMenu.fiveByFive(windowId, inventory, access)
                            : BigCraftingMenu.fourByFour(windowId, inventory, access),
                    Component.translatable("container.carmod.crafting_table_" + size + "x" + size)
            ));
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }
        return InteractionResult.SUCCESS;
    }
}
