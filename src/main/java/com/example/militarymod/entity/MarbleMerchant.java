package com.example.militarymod.entity;

import com.example.militarymod.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

/**
 * Mercante di biglie: resta fermo nel magazzino e cambia materiali in biglie,
 * la valuta che serve per reclutare i soldati.
 *   64 ferro -> 2   | 64 rame -> 1   | 32 oro -> 2
 *   16 smeraldi -> 3 | 8 diamanti -> 6 | 4 netherite -> 18
 */
public class MarbleMerchant extends AbstractVillager {

    public MarbleMerchant(EntityType<? extends MarbleMerchant> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setCustomName(Component.translatable("entity.militarymod.marble_merchant"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(2, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (!this.level().isClientSide) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private static void trade(MerchantOffers offers, Item what, int amount, int marbles) {
        offers.add(new MerchantOffer(new ItemStack(what, amount), new ItemStack(ModItems.MARBLE.get(), marbles),
                9999999, 0, 0.0F));
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        trade(offers, Items.IRON_INGOT, 64, 2);
        trade(offers, Items.COPPER_INGOT, 64, 1);
        trade(offers, Items.GOLD_INGOT, 32, 2);
        trade(offers, Items.EMERALD, 16, 3);
        trade(offers, Items.DIAMOND, 8, 6);
        trade(offers, Items.NETHERITE_INGOT, 4, 18);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        // nessuna esperienza
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
