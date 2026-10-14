package com.example.militarymod.loot;

import com.example.militarymod.MilitaryMod;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLoot {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MilitaryMod.MODID);

    public static final RegistryObject<Codec<MilitaryLootModifier>> MILITARY =
            MODIFIERS.register("military_loot", MilitaryLootModifier.CODEC);
}
