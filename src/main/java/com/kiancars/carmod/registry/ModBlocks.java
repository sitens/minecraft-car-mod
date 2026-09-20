package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.block.CraftingTable4x4Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CarMod.MOD_ID);

    public static final DeferredBlock<CraftingTable4x4Block> CRAFTING_TABLE_4X4 =
            BLOCKS.register("crafting_table_4x4", () -> new CraftingTable4x4Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.5F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOD)
            ));

    private ModBlocks() {
    }
}
