package com.example.militarymod.loot;

import com.example.militarymod.compat.ModCompat;
import com.example.militarymod.compat.SbwCompat;
import com.example.militarymod.compat.TaczCompat;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/** Aggiunge armi TACZ / SBW alle casse militari e munizioni TACZ ai soldati nemici. */
public class MilitaryLootModifier extends LootModifier {
    public static final Supplier<Codec<MilitaryLootModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, MilitaryLootModifier::new)));

    public MilitaryLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        ResourceLocation table = ctx.getQueriedLootTableId();
        RandomSource r = ctx.getRandom();
        boolean chest = table.getPath().startsWith("chests/");
        if (table.getPath().contains("depot")) {
            if (ModCompat.tacz()) TaczCompat.addLootMedium(loot, r);
            return loot;
        }
        if (table.getPath().contains("friendly")) {
            // strutture dei buoni: quasi niente (solo ogni tanto un caricatore)
            if (ModCompat.tacz()) TaczCompat.addLootLow(loot, r);
            return loot;
        }
        if (ModCompat.tacz()) TaczCompat.addLoot(loot, r, chest);
        if (chest && ModCompat.sbw()) SbwCompat.addLoot(loot, r);
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
