package com.example.militarymod;

import com.example.militarymod.item.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MilitaryMod.MODID)
public class MilitaryMod {
    public static final String MODID = "militarymod";

    public MilitaryMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
    }
}
