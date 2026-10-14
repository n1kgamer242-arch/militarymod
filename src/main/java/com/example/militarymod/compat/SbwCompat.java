package com.example.militarymod.compat;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Superb Warfare: niente dipendenza in compilazione. Le armi vengono trovate a runtime
 * scorrendo il registro degli oggetti (cosi' non servono gli ID esatti).
 */
public final class SbwCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static List<Item> guns;
    private static List<Item> ammo;

    private SbwCompat() {}

    private static void scan() {
        guns = new ArrayList<>();
        ammo = new ArrayList<>();
        for (Item it : ForgeRegistries.ITEMS) {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(it);
            if (key == null || !key.getNamespace().equals("superbwarfare")) continue;
            String cls = it.getClass().getName().toLowerCase();
            String path = key.getPath();
            if (cls.contains(".item.gun.") && !path.contains("ammo")) guns.add(it);
            else if (path.endsWith("_ammo")) ammo.add(it);
        }
        LOGGER.info("[militarymod] Superb Warfare: {} armi e {} tipi di munizioni trovati per il loot", guns.size(), ammo.size());
        for (Item g : guns) LOGGER.info("[militarymod]   arma SBW: {}", ForgeRegistries.ITEMS.getKey(g));
    }

    public static void addLoot(ObjectArrayList<ItemStack> loot, RandomSource r) {
        if (guns == null) scan();
        if (!guns.isEmpty() && r.nextFloat() < 0.5F) loot.add(new ItemStack(guns.get(r.nextInt(guns.size()))));
        if (!ammo.isEmpty()) loot.add(new ItemStack(ammo.get(r.nextInt(ammo.size())), 8 + r.nextInt(25)));
    }
}
