package com.example.militarymod.item;

import com.example.militarymod.entity.BulletEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RifleItem extends Item {
    public RifleItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);
        boolean creative = player.getAbilities().instabuild;
        ItemStack ammo = ItemStack.EMPTY;
        if (!creative) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.is(ModItems.AMMO.get())) { ammo = s; break; }
            }
            if (ammo.isEmpty()) return InteractionResultHolder.fail(gun);
        }
        if (!level.isClientSide) {
            BulletEntity b = new BulletEntity(level, player);
            b.setBaseDamage(1.8D);
            b.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.5F, 0.8F);
            level.addFreshEntity(b);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.8F);
            if (!creative) {
                ammo.shrink(1);
                gun.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
        }
        player.getCooldowns().addCooldown(this, 8);
        return InteractionResultHolder.sidedSuccess(gun, level.isClientSide());
    }
}
