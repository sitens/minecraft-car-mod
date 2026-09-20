package com.kiancars.carmod.block;

import com.kiancars.carmod.menu.CraftingTable4x4Menu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The special crafting table: same idea as vanilla's, but opens a 4x4
 * (16-slot) grid instead of 3x3. Stateless like vanilla's crafting table —
 * no block entity, contents are held transiently by the menu and dropped if
 * the player closes the screen mid-craft (matches vanilla behavior).
 */
public class CraftingTable4x4Block extends Block {

    public CraftingTable4x4Block(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, net.minecraft.core.BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            MenuProvider menuProvider = new SimpleMenuProvider(
                    (windowId, inventory, p) -> new CraftingTable4x4Menu(windowId, inventory, level, pos),
                    Component.translatable("container.carmod.crafting_table_4x4")
            );
            player.openMenu(menuProvider);
            player.awardStat(net.minecraft.stats.Stats.INTERACT_WITH_CRAFTING_TABLE);
        }
        return InteractionResult.SUCCESS;
    }
}
