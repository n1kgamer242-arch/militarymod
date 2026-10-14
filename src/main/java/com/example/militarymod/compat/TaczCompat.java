package com.example.militarymod.compat;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Tutto il codice che tocca le classi di TACZ sta qui: viene caricato solo se TACZ e' presente. */
public final class TaczCompat {
    private TaczCompat() {}

    /** id arma, id munizione, caricatore, modalita' di fuoco, peso */
    private record Def(String gun, String ammo, int mag, FireMode mode, int weight) {}

    private static final List<Def> HOSTILE = List.of(
            new Def("ak47", "762x39", 30, FireMode.AUTO, 5),
            new Def("m4a1", "556x45", 30, FireMode.AUTO, 4),
            new Def("hk416d", "556x45", 30, FireMode.AUTO, 3),
            new Def("qbz95", "58x42", 30, FireMode.AUTO, 2),
            new Def("glock_17", "9mm", 17, FireMode.SEMI, 2),
            new Def("m870", "12g", 8, FireMode.SEMI, 1),
            new Def("m700", "308", 5, FireMode.SEMI, 1));

    private static final List<Def> ALLY = List.of(
            new Def("m4a1", "556x45", 30, FireMode.AUTO, 5),
            new Def("hk416d", "556x45", 30, FireMode.AUTO, 4),
            new Def("ak47", "762x39", 30, FireMode.AUTO, 3),
            new Def("qbz95", "58x42", 30, FireMode.AUTO, 2),
            new Def("m870", "12g", 8, FireMode.SEMI, 1),
            new Def("m700", "308", 5, FireMode.SEMI, 1));

    private static Def pick(List<Def> list, RandomSource r) {
        int total = 0;
        for (Def d : list) total += d.weight;
        int n = r.nextInt(total);
        for (Def d : list) { n -= d.weight; if (n < 0) return d; }
        return list.get(0);
    }

    private static ItemStack build(Def d) {
        return GunItemBuilder.create()
                .setId(new ResourceLocation("tacz", d.gun))
                .setFireMode(d.mode)
                .setAmmoCount(d.mag)
                .setAmmoInBarrel(true)
                .build();
    }

    private static ItemStack ammo(Def d, int count) {
        return AmmoItemBuilder.create().setId(new ResourceLocation("tacz", d.ammo)).setCount(count).build();
    }

    public static boolean isGun(ItemStack stack) {
        return IGun.getIGunOrNull(stack) != null;
    }

    /** Mette in mano al soldato un'arma TACZ (se non ce l'ha gia') e la prepara. */
    public static void equip(Mob mob, boolean ally) {
        if (!isGun(mob.getMainHandItem())) {
            List<Def> list = ally ? ALLY : HOSTILE;
            ItemStack gun = ItemStack.EMPTY;
            for (int i = 0; i < 6 && gun.isEmpty(); i++) gun = build(pick(list, mob.getRandom()));
            if (gun.isEmpty()) return; // nessuna arma installata nei gun pack
            mob.setItemSlot(EquipmentSlot.MAINHAND, gun);
            mob.setDropChance(EquipmentSlot.MAINHAND, ally ? 0.0F : 0.05F);
        }
        IGunOperator op = IGunOperator.fromLivingEntity(mob);
        op.initialData();
        op.draw(mob::getMainHandItem);
    }

    /** Spara con la logica vera di TACZ (suoni, bossoli, rinculo, balistica). Munizioni infinite. */
    public static void shoot(LivingEntity shooter, LivingEntity target) {
        ItemStack gun = shooter.getMainHandItem();
        IGun ig = IGun.getIGunOrNull(gun);
        if (ig == null) return;
        IGunOperator op = IGunOperator.fromLivingEntity(shooter);

        double dx = target.getX() - shooter.getX();
        double dy = target.getY(0.5D) - shooter.getEyeY();
        double dz = target.getZ() - shooter.getZ();
        double h = Math.sqrt(dx * dx + dz * dz);
        final float yaw = (float) (Mth.atan2(dz, dx) * 57.29577951308232D) - 90.0F;
        final float pitch = (float) (-(Mth.atan2(dy, h) * 57.29577951308232D));

        ShootResult result = op.shoot(() -> pitch, () -> yaw);
        if (result == ShootResult.NO_AMMO) {
            ig.setCurrentAmmoCount(gun, 30);
            ig.setBulletInBarrel(gun, true);
        } else if (result == ShootResult.NEED_BOLT) {
            op.bolt();
        }
    }

    /** Aggiunge armi/munizioni TACZ al loot. */
    public static void addLoot(ObjectArrayList<ItemStack> loot, RandomSource r, boolean chest) {
        Def d = pick(HOSTILE, r);
        if (chest) {
            ItemStack gun = build(d);
            if (!gun.isEmpty()) {
                loot.add(gun);
                loot.add(ammo(d, 16 + r.nextInt(48)));
                if (r.nextFloat() < 0.5F) loot.add(ammo(pick(HOSTILE, r), 16 + r.nextInt(32)));
            }
        } else if (r.nextFloat() < 0.4F) {
            loot.add(ammo(d, 8 + r.nextInt(24)));
        }
    }
}
