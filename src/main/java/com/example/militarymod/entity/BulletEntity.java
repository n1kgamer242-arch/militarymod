package com.example.militarymod.entity;

import com.example.militarymod.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class BulletEntity extends net.minecraft.world.entity.projectile.AbstractArrow {
    public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
        init();
    }

    public BulletEntity(Level level, LivingEntity shooter) {
        super(ModEntities.BULLET.get(), shooter, level);
        init();
    }

    private void init() {
        this.setNoGravity(true);
        this.pickup = Pickup.DISALLOWED;
        this.setBaseDamage(1.5D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 60) discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) discard();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        Entity owner = getOwner();
        if (owner instanceof HostileSoldier && target instanceof HostileSoldier) return false;
        if (owner instanceof AllySoldier ally) {
            if (target instanceof AllySoldier || target instanceof Player
                    || target instanceof AbstractVillager || target instanceof IronGolem) return false;
        }
        if (owner instanceof Player p && target instanceof AllySoldier a && a.isOwnedBy(p)) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}
