package com.example.militarymod.item;

import com.example.militarymod.entity.AllySoldier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/** Click su un blocco: i tuoi soldati vanno li' e fanno la guardia. Shift+click: ti seguono. */
public class CommandRadioItem extends Item {
    public CommandRadioItem(Properties props) {
        super(props);
    }

    private static List<AllySoldier> mySoldiers(Level level, Player p) {
        return level.getEntitiesOfClass(AllySoldier.class, p.getBoundingBox().inflate(64), s -> s.isOwnedBy(p));
    }

    private static void follow(Level level, Player p) {
        List<AllySoldier> list = mySoldiers(level, p);
        list.forEach(s -> { s.setMode(AllySoldier.MODE_FOLLOW); s.setRally(null); });
        p.displayClientMessage(Component.translatable("msg.militarymod.radio_follow", list.size()), true);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player p = ctx.getPlayer();
        if (p == null) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (p.isShiftKeyDown()) {
                follow(level, p);
            } else {
                BlockPos pos = ctx.getClickedPos().above();
                List<AllySoldier> list = mySoldiers(level, p);
                list.forEach(s -> { s.setMode(AllySoldier.MODE_GUARD); s.setRally(pos); });
                p.displayClientMessage(Component.translatable("msg.militarymod.radio_rally", list.size()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        if (p.isShiftKeyDown()) {
            if (!level.isClientSide) follow(level, p);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }
}
