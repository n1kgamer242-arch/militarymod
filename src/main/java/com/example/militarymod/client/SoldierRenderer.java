package com.example.militarymod.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class SoldierRenderer<T extends Mob> extends HumanoidMobRenderer<T, SoldierModel<T>> {
    private final ResourceLocation texture;

    public SoldierRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture) {
        super(ctx, new SoldierModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.texture = texture;
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                ctx.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
