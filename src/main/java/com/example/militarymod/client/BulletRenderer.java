package com.example.militarymod.client;

import com.example.militarymod.entity.BulletEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BulletRenderer extends ArrowRenderer<BulletEntity> {
    private static final ResourceLocation TEX = new ResourceLocation("textures/entity/projectiles/arrow.png");

    public BulletRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(BulletEntity e) {
        return TEX;
    }
}
