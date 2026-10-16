package com.example.militarymod.worldgen;

import com.example.militarymod.MilitaryMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MilitaryMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MilitaryMod.MODID);

    public static final RegistryObject<StructureType<MilitaryStructure>> TYPE =
            TYPES.register("military_template", () -> typeOf(MilitaryStructure.CODEC));

    public static final RegistryObject<StructurePieceType> PIECE =
            PIECES.register("military_template", () -> (StructurePieceType) MilitaryPiece::new);

    private static <S extends Structure> StructureType<S> typeOf(Codec<S> codec) {
        return () -> codec;
    }
}
