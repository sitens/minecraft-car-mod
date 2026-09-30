package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.block.AltarBlock;
import com.kiancars.carmod.block.BigCraftingTableBlock;
import com.kiancars.carmod.block.TackBlock;
import com.kiancars.carmod.block.VerticalSlabBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CarMod.MOD_ID);

    public static final DeferredBlock<BigCraftingTableBlock> CRAFTING_TABLE_4X4 =
            BLOCKS.registerBlock("crafting_table_4x4", props -> new BigCraftingTableBlock(props, 4),
                    props -> props.mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<BigCraftingTableBlock> CRAFTING_TABLE_5X5 =
            BLOCKS.registerBlock("crafting_table_5x5", props -> new BigCraftingTableBlock(props, 5),
                    props -> props.mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).ignitedByLava());

    /** Blast-proof so a ritual TNT blast leaves the ring standing. */
    public static final DeferredBlock<AltarBlock> ALTAR =
            BLOCKS.registerBlock("altar", AltarBlock::new,
                    props -> props.mapColor(MapColor.COLOR_BLACK).strength(5.0F, 1200.0F).sound(SoundType.STONE)
                            .requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<TackBlock> TACK =
            BLOCKS.registerBlock("tack", TackBlock::new,
                    props -> props.mapColor(MapColor.METAL).strength(0.5F).sound(SoundType.METAL)
                            .noCollision().noOcclusion().pushReaction(PushReaction.DESTROY));

    /**
     * Every vanilla slab gets a standing-up version, named "<slab>_vertical_slab"
     * without the "_slab" (e.g. oak_slab -> oak_vertical_slab). Petrified oak
     * is skipped because players can't normally get it. The block models,
     * recipes and loot tables for these come from tools/gen_vertical_slabs.sh.
     */
    public static final List<String> VERTICAL_SLAB_BASES = List.of(
            "acacia", "andesite", "bamboo_mosaic", "bamboo", "birch", "blackstone", "brick", "cherry",
            "cobbled_deepslate", "cobblestone", "crimson", "cut_copper", "cut_red_sandstone", "cut_sandstone",
            "dark_oak", "dark_prismarine", "deepslate_brick", "deepslate_tile", "diorite", "end_stone_brick",
            "exposed_cut_copper", "granite", "jungle", "mangrove", "mossy_cobblestone", "mossy_stone_brick",
            "mud_brick", "nether_brick", "oak", "oxidized_cut_copper", "pale_oak", "polished_andesite",
            "polished_blackstone_brick", "polished_blackstone", "polished_deepslate", "polished_diorite",
            "polished_granite", "polished_tuff", "prismarine_brick", "prismarine", "purpur", "quartz",
            "red_nether_brick", "red_sandstone", "resin_brick", "sandstone", "smooth_quartz",
            "smooth_red_sandstone", "smooth_sandstone", "smooth_stone", "spruce", "stone_brick", "stone",
            "tuff_brick", "tuff", "warped", "waxed_cut_copper", "waxed_exposed_cut_copper",
            "waxed_oxidized_cut_copper", "waxed_weathered_cut_copper", "weathered_cut_copper");

    private static final Map<String, DeferredBlock<VerticalSlabBlock>> VERTICAL_SLABS = new LinkedHashMap<>();

    static {
        for (String base : VERTICAL_SLAB_BASES) {
            VERTICAL_SLABS.put(base, BLOCKS.registerBlock(base + "_vertical_slab", VerticalSlabBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(vanillaSlab(base))));
        }
    }

    private static Block vanillaSlab(String base) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(base + "_slab"));
    }

    public static Map<String, DeferredBlock<VerticalSlabBlock>> verticalSlabs() {
        return Collections.unmodifiableMap(VERTICAL_SLABS);
    }

    private ModBlocks() {
    }
}
