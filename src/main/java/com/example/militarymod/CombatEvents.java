package com.example.militarymod;

import com.example.militarymod.entity.AllySoldier;
import com.example.militarymod.entity.HostileSoldier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Niente fuoco amico (vale anche per i proiettili di TACZ). */
@Mod.EventBusSubscriber(modid = MilitaryMod.MODID)
public class CombatEvents {
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent e) {
        Entity src = e.getSource().getEntity();
        LivingEntity victim = e.getEntity();
        if (src instanceof AllySoldier && (victim instanceof Player || victim instanceof AllySoldier
                || victim instanceof AbstractVillager || victim instanceof IronGolem)) {
            e.setCanceled(true);
        } else if (src instanceof HostileSoldier && victim instanceof HostileSoldier) {
            e.setCanceled(true);
        } else if (src instanceof Player p && victim instanceof AllySoldier a && a.isOwnedBy(p)) {
            e.setCanceled(true);
        }
    }
}
