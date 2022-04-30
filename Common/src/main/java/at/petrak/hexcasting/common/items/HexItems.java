package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.items.colorizer.ItemDyeColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemUUIDColorizer;
import at.petrak.hexcasting.common.items.magic.*;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/2c4f7fdf9ebf0c0afa1406dfe1322841133d75fa/Common/src/main/java/vazkii/botania/common/item/ModItems.java
public class HexItems {
    public static void registerItems(BiConsumer<Item, ResourceLocation> r) {
        for (var e : ITEMS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, Item> ITEMS = new LinkedHashMap<>(); // preserve insertion order

    public static final CreativeModeTab TAB = new CreativeModeTab(HexAPI.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(SPELLBOOK);
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            super.fillItemList(items);

            var manamounts = new int[]{
                100_000,
                1_000_000,
                10_000_000,
                100_000_000,
                1_000_000_000,
            };
            for (int manamount : manamounts) {
                var stack = new ItemStack(BATTERY);
                items.add(ItemManaHolder.withMana(stack, manamount, manamount));
            }
        }
    };


    public static final Item AMETHYST_DUST = make("amethyst_dust", new Item(props()));
    public static final Item CHARGED_AMETHYST = make("charged_amethyst", new Item(props()));

    public static final ItemWand WAND_OAK = make("wand_oak", new ItemWand(unstackable()));
    public static final ItemWand WAND_SPRUCE = make("wand_spruce", new ItemWand(unstackable()));
    public static final ItemWand WAND_BIRCH = make("wand_birch", new ItemWand(unstackable()));
    public static final ItemWand WAND_JUNGLE = make("wand_jungle", new ItemWand(unstackable()));
    public static final ItemWand WAND_ACACIA = make("wand_acacia", new ItemWand(unstackable()));
    public static final ItemWand WAND_DARK_OAK = make("wand_dark_oak", new ItemWand(unstackable()));
    public static final ItemWand WAND_CRIMSON = make("wand_crimson", new ItemWand(unstackable()));
    public static final ItemWand WAND_WARPED = make("wand_warped", new ItemWand(unstackable()));
    public static final ItemWand WAND_AKASHIC = make("wand_akashic", new ItemWand(unstackable()));

    public static final ItemLens SCRYING_LENS = make("lens", new ItemLens(unstackable()));

    public static final ItemAbacus ABACUS = make("abacus", new ItemAbacus(unstackable()));
    public static final ItemFocus FOCUS = make("focus", new ItemFocus(unstackable()));
    public static final ItemSpellbook SPELLBOOK = make("spellbook", new ItemSpellbook(unstackable()));

    public static final ItemCypher CYPHER = make("cypher", new ItemCypher(unstackable()));
    public static final ItemTrinket TRINKET = make("trinket", new ItemTrinket(unstackable()));
    public static final ItemArtifact ARTIFACT = make("artifact", new ItemArtifact(unstackable()));


    public static final ItemScroll SCROLL = make("scroll", new ItemScroll(props()));

    public static final ItemSlate SLATE = make("slate", new ItemSlate(HexBlocks.SLATE, props()));

    public static final ItemManaBattery BATTERY = make("battery",
        new ItemManaBattery(new Item.Properties().stacksTo(1)));

    public static final EnumMap<DyeColor, ItemDyeColorizer> DYE_COLORIZERS = new EnumMap<>(
        DyeColor.class);
    public static final ItemPrideColorizer[] PRIDE_COLORIZERS = new ItemPrideColorizer[14];

    static {
        for (var dye : DyeColor.values()) {
            DYE_COLORIZERS.put(dye, make("dye_colorizer_" + dye.getName(), new ItemDyeColorizer(dye, unstackable())));
        }
        for (int i = 0; i < PRIDE_COLORIZERS.length; i++) {
            PRIDE_COLORIZERS[i] = make("pride_colorizer_" + i, new ItemPrideColorizer(i, unstackable()));
        }
    }

    public static final Item UUID_COLORIZER = make("uuid_colorizer", new ItemUUIDColorizer(unstackable()));

    // BUFF SANDVICH
    public static final Item SUBMARINE_SANDWICH = make("sub_sandwich",
        new Item(props().food(new FoodProperties.Builder().nutrition(14).saturationMod(1.2f).build())));

    //

    public static Item.Properties props() {
        return new Item.Properties().tab(TAB);
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }

    private static <T extends Item> T make(ResourceLocation id, T item) {
        var old = ITEMS.put(id, item);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return item;
    }

    private static <T extends Item> T make(String id, T item) {
        return make(modLoc(id), item);
    }
}