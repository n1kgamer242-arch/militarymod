package com.example.militarymod.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Combattimento a raffiche: avanza, si ferma a distanza, si muove di lato e spara a burst. */
public class SoldierAttackGoal<T extends Mob & RangedAttackMob> extends Goal {
    private final T mob;
    private final double speed;
    private final float range;
    private int seeTime, burst, pause, strafeTime;
    private boolean strafeRight;

    public SoldierAttackGoal(T mob, double speed, float range) {
        this.mob = mob;
        this.speed = speed;
        this.range = range;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean canUse() { LivingEntity t = mob.getTarget(); return t != null && t.isAlive(); }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { mob.setAggressive(true); }

    @Override
    public void stop() {
        mob.setAggressive(false);
        mob.getNavigation().stop();
        seeTime = 0;
    }

    @Override
    public void tick() {
        LivingEntity t = mob.getTarget();
        if (t == null) return;
        double d = mob.distanceToSqr(t);
        boolean see = mob.getSensing().hasLineOfSight(t);
        seeTime = see ? seeTime + 1 : 0;
        boolean inRange = d <= (double) (range * range);

        if (inRange && seeTime >= 5) {
            mob.getNavigation().stop();
            if (--strafeTime <= 0) { strafeRight = mob.getRandom().nextBoolean(); strafeTime = 20 + mob.getRandom().nextInt(30); }
            float back = d < (double) (range * range * 0.25F) ? -0.5F : 0.0F;
            mob.getMoveControl().strafe(back, strafeRight ? 0.5F : -0.5F);
        } else {
            mob.getNavigation().moveTo(t, speed);
        }
        mob.lookAt(t, 30.0F, 30.0F);
        mob.getLookControl().setLookAt(t, 30.0F, 30.0F);

        if (see && inRange) {
            if (pause > 0) { pause--; return; }
            if (burst <= 0) burst = 8 + mob.getRandom().nextInt(16);
            mob.performRangedAttack(t, 1.0F);
            if (--burst <= 0) pause = 15 + mob.getRandom().nextInt(25);
        }
    }
}
