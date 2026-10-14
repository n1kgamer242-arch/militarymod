package com.example.militarymod.compat;

import net.minecraftforge.fml.ModList;

public final class ModCompat {
    private ModCompat() {}
    public static boolean tacz() { return ModList.get().isLoaded("tacz"); }
    public static boolean sbw() { return ModList.get().isLoaded("superbwarfare"); }
}
