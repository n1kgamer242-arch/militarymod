package com.example.militarymod.entity;

import com.example.militarymod.compat.ModCompat;
import com.example.militarymod.compat.TaczCompat;
import com.example.militarymod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.EnumSet;

/** Soldato nemico: spara a giocatore, villager, golem e ai tuoi soldati. */
public class HostileSoldier extends Monster implements RangedAttackMob {
    private boolean wave = false;
    private boolean gunReady = false;
    private int lastShot = -100;

    public HostileSoldier(EntityType<? extends HostileSoldier> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RIFLE.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    public void setWave(boolean wave) { this.wave = wave; }
    public boolean isWave() { return wave; }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new SoldierAttackGoal<>(this, 1.0D, 26.0F));
        goalSelector.addGoal(3, new MarchGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this, HostileSoldier.class).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, 10, true, false, null));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AllySoldier.class, 10, true, false, null));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (ModCompat.tacz() && TaczCompat.isGun(getMainHandItem())) {
            TaczCompat.shoot(this, target);
        } else if (tickCount - lastShot >= 20) {
            lastShot = tickCount;
            Gun.fire(this, target, 1.2D, 8.0F);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && !gunReady && tickCount >= 2) {
            gunReady = true;
            if (ModCompat.tacz()) TaczCompat.equip(this, false);
        }
        // le ondate si ripuliscono da sole se il giocatore e' lontano
        if (!level().isClientSide && wave && tickCount > 12000 && level().getNearestPlayer(this, 64.0D) == null) {
            discard();
        }
    }

    @Override protected boolean shouldDespawnInPeaceful() { return true; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.PLAYER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PLAYER_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Wave", wave);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        wave = tag.getBoolean("Wave");
    }

    /** Durante le ondate marcia verso il villager piu' vicino, altrimenti verso il giocatore. */
    static class MarchGoal extends Goal {
        private final HostileSoldier m;
        private int recalc;

        MarchGoal(HostileSoldier m) {
            this.m = m;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override public boolean canUse() { return m.wave && m.getTarget() == null; }
        @Override public boolean canContinueToUse() { return canUse(); }

        @Override
        public void tick() {
            if (--recalc > 0) return;
            recalc = 20 + m.random.nextInt(10);
            LivingEntity dest = m.level().getEntitiesOfClass(AbstractVillager.class, m.getBoundingBox().inflate(48.0D))
                    .stream().min(Comparator.comparingDouble(m::distanceToSqr)).orElse(null);
            if (dest == null) dest = m.level().getNearestPlayer(m, 128.0D);
            if (dest != null) m.getNavigation().moveTo(dest, 1.0D);
        }
    }
}
