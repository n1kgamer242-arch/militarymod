package com.example.militarymod.client;

import com.example.militarymod.item.RifleItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;

public class SoldierModel<T extends Mob> extends HumanoidModel<T> {
    public SoldierModel(ModelPart root) {
        super(root);
    }

    @Override
    public void prepareMobModel(T mob, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(mob, limbSwing, limbSwingAmount, partialTick);
        this.leftArmPose = ArmPose.EMPTY;
        this.rightArmPose = mob.getMainHandItem().getItem() instanceof RifleItem
                ? ArmPose.CROSSBOW_HOLD : ArmPose.EMPTY;
    }
}
