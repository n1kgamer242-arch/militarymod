package com.example.militarymod.entity;

import com.example.militarymod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RangedAttackMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Soldato alleato. Senza padrone: aspetta di essere reclutato (3 smeraldi, tasto destro).
 * Con padrone: tasto destro a mano vuota = alterna SEGUI / GUARDIA. Si comanda anche con la radio.
 */
public class AllySoldier extends PathfinderMob implements RangedAttackMob {
    public static final int MODE_FOLLOW = 0;
    public static final int MODE_GUARD = 1;
    public static final int RECRUIT_COST = 3;

    @Nullable private UUID owner;
    private int mode = MODE_GUARD;
    @Nullable private BlockPos rally;

    public AllySoldier(EntityType<? extends AllySoldier> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RIFLE.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    public boolean isOwnedBy(Player p) { return owner != null && owner.equals(p.getUUID()); }
    @Nullable public Player getOwnerPlayer() { return owner == null ? null : level().getPlayerByUUID(owner); }
    public void setMode(int m) { this.mode = m; }
    public int getMode() { return mode; }
    public void setRally(@Nullable BlockPos p) { this.rally = p; }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 16, 28.0F));
        goalSelector.addGoal(2, new FollowOwnerGoal(this));
        goalSelector.addGoal(3, new GoToRallyGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new DefendOwnerGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this, Player.class, AllySoldier.class));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 5, true, false,
                e -> e instanceof Enemy && !(e instanceof AllySoldier)));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        Gun.fire(this, target, 1.6D, 4.0F);
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (owner != null && src.getEntity() != null && owner.equals(src.getEntity().getUUID())) return false;
        return super.hurt(src, amount);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (owner == null) {
            if (!level().isClientSide) {
                boolean creative = player.getAbilities().instabuild;
                if (held.is(Items.EMERALD) && (creative || held.getCount() >= RECRUIT_COST)) {
                    if (!creative) held.shrink(RECRUIT_COST);
                    owner = player.getUUID();
                    mode = MODE_FOLLOW;
                    player.displayClientMessage(Component.translatable("msg.militarymod.recruited"), true);
                } else {
                    player.displayClientMessage(Component.translatable("msg.militarymod.need_emeralds", RECRUIT_COST), true);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isOwnedBy(player)) {
            if (!level().isClientSide) {
                mode = mode == MODE_FOLLOW ? MODE_GUARD : MODE_FOLLOW;
                rally = null;
                player.displayClientMessage(Component.translatable(
                        mode == MODE_FOLLOW ? "msg.militarymod.mode_follow" : "msg.militarymod.mode_guard"), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.PLAYER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PLAYER_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putInt("Mode", mode);
        if (rally != null) {
            tag.putInt("RallyX", rally.getX());
            tag.putInt("RallyY", rally.getY());
            tag.putInt("RallyZ", rally.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        if (tag.contains("Mode")) mode = tag.getInt("Mode");
        rally = tag.contains("RallyX") ? new BlockPos(tag.getInt("RallyX"), tag.getInt("RallyY"), tag.getInt("RallyZ")) : null;
    }

    // ------------------------------------------------------------------ goals

    static class FollowOwnerGoal extends Goal {
        private final AllySoldier s;
        private int recalc;

        FollowOwnerGoal(AllySoldier s) { this.s = s; setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            Player o = s.getOwnerPlayer();
            return s.mode == MODE_FOLLOW && o != null && s.getTarget() == null && s.distanceToSqr(o) > 36.0D;
        }
        @Override public boolean canContinueToUse() {
            Player o = s.getOwnerPlayer();
            return s.mode == MODE_FOLLOW && o != null && s.getTarget() == null && s.distanceToSqr(o) > 12.0D;
        }
        @Override public void tick() {
            Player o = s.getOwnerPlayer();
            if (o == null) return;
            if (s.distanceToSqr(o) > 1600.0D) { s.teleportTo(o.getX(), o.getY(), o.getZ()); return; }
            if (--recalc <= 0) { recalc = 10; s.getNavigation().moveTo(o, 1.15D); }
        }
    }

    static class GoToRallyGoal extends Goal {
        private final AllySoldier s;

        GoToRallyGoal(AllySoldier s) { this.s = s; setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            return s.mode == MODE_GUARD && s.rally != null && s.getTarget() == null
                    && s.blockPosition().distSqr(s.rally) > 9.0D;
        }
        @Override public boolean canContinueToUse() { return canUse() && !s.getNavigation().isDone(); }
        @Override public void start() {
            s.getNavigation().moveTo(s.rally.getX() + 0.5D, s.rally.getY(), s.rally.getZ() + 0.5D, 1.1D);
        }
    }

    static class DefendOwnerGoal extends TargetGoal {
        private final AllySoldier s;
        private LivingEntity victim;

        DefendOwnerGoal(AllySoldier s) { super(s, false); this.s = s; setFlags(EnumSet.of(Flag.TARGET)); }

        private boolean valid(LivingEntity t) {
            return t != null && t.isAlive() && t != s && !(t instanceof AllySoldier) && !(t instanceof Player)
                    && !(t instanceof AbstractVillager) && !(t instanceof IronGolem);
        }

        @Override public boolean canUse() {
            Player o = s.getOwnerPlayer();
            if (o == null) return false;
            LivingEntity a = o.getLastHurtByMob();
            if (valid(a) && o.tickCount - o.getLastHurtByMobTimestamp() < 200) { victim = a; return true; }
            LivingEntity b = o.getLastHurtMob();
            if (valid(b) && o.tickCount - o.getLastHurtMobTimestamp() < 200) { victim = b; return true; }
            return false;
        }
        @Override public void start() { s.setTarget(victim); super.start(); }
    }
}
