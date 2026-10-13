package com.example.militarymod;

import com.example.militarymod.entity.AllySoldier;
import com.example.militarymod.entity.BulletEntity;
import com.example.militarymod.entity.HostileSoldier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MilitaryMod.MODID);

    public static final RegistryObject<EntityType<AllySoldier>> ALLY_SOLDIER = ENTITIES.register("ally_soldier",
            () -> EntityType.Builder.of(AllySoldier::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build("ally_soldier"));

    public static final RegistryObject<EntityType<HostileSoldier>> HOSTILE_SOLDIER = ENTITIES.register("hostile_soldier",
            () -> EntityType.Builder.of(HostileSoldier::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build("hostile_soldier"));

    public static final RegistryObject<EntityType<BulletEntity>> BULLET = ENTITIES.register("bullet",
            () -> EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build("bullet"));
}
