package com.example.militarymod;

import com.example.militarymod.entity.AllySoldier;
import com.example.militarymod.entity.HostileSoldier;
import com.example.militarymod.entity.MarbleMerchant;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MilitaryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.ALLY_SOLDIER.get(), AllySoldier.createAttributes().build());
        e.put(ModEntities.HOSTILE_SOLDIER.get(), HostileSoldier.createAttributes().build());
        e.put(ModEntities.MARBLE_MERCHANT.get(), MarbleMerchant.createAttributes().build());
    }
}
