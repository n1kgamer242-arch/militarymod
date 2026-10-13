package com.example.militarymod.item;

import com.example.militarymod.MilitaryMod;
import com.example.militarymod.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MilitaryMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MilitaryMod.MODID);

    public static final RegistryObject<Item> RIFLE = ITEMS.register("rifle",
            () -> new RifleItem(new Item.Properties().durability(500)));
    public static final RegistryObject<Item> AMMO = ITEMS.register("ammo",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COMMAND_RADIO = ITEMS.register("command_radio",
            () -> new CommandRadioItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ALLY_SPAWN_EGG = ITEMS.register("ally_soldier_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ALLY_SOLDIER, 0x4B5320, 0x2B2B2B, new Item.Properties()));
    public static final RegistryObject<Item> HOSTILE_SPAWN_EGG = ITEMS.register("hostile_soldier_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HOSTILE_SOLDIER, 0x32323A, 0xAA2222, new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.militarymod"))
                    .icon(() -> new ItemStack(RIFLE.get()))
                    .displayItems((params, out) -> {
                        out.accept(RIFLE.get());
                        out.accept(AMMO.get());
                        out.accept(COMMAND_RADIO.get());
                        out.accept(ALLY_SPAWN_EGG.get());
                        out.accept(HOSTILE_SPAWN_EGG.get());
                    }).build());
}
