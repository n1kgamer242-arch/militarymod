package com.example.militarymod.client;

import com.example.militarymod.MilitaryMod;
import com.example.militarymod.ModEntities;
import com.example.militarymod.entity.AllySoldier;
import com.example.militarymod.entity.HostileSoldier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MilitaryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.ALLY_SOLDIER.get(),
                ctx -> new SoldierRenderer<AllySoldier>(ctx, new ResourceLocation(MilitaryMod.MODID, "textures/entity/ally_soldier.png")));
        e.registerEntityRenderer(ModEntities.HOSTILE_SOLDIER.get(),
                ctx -> new SoldierRenderer<HostileSoldier>(ctx, new ResourceLocation(MilitaryMod.MODID, "textures/entity/hostile_soldier.png")));
        e.registerEntityRenderer(ModEntities.BULLET.get(), BulletRenderer::new);
    }
}
