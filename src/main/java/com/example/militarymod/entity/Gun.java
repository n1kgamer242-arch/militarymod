package com.example.militarymod.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class Gun {
    private Gun() {}

    public static void fire(LivingEntity shooter, LivingEntity target, double damage, float inaccuracy) {
        Level level = shooter.level();
        BulletEntity b = new BulletEntity(level, shooter);
        b.setBaseDamage(damage);
        double dx = target.getX() - shooter.getX();
        double dy = target.getY(0.33D) - b.getY();
        double dz = target.getZ() - shooter.getZ();
        b.shoot(dx, dy, dz, 3.5F, inaccuracy);
        level.addFreshEntity(b);
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.HOSTILE, 1.0F, 1.8F);
    }
}
