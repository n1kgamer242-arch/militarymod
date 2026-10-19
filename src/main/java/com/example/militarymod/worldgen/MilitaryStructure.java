package com.example.militarymod.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

/**
 * Struttura che si genera SOLO su terra asciutta e abbastanza piana, a livello del terreno.
 * Se il punto scelto e' in acqua o troppo in pendenza, in quella zona non genera nulla.
 */
public class MilitaryStructure extends Structure {
    public static final Codec<MilitaryStructure> CODEC = RecordCodecBuilder.<MilitaryStructure>mapCodec(inst -> inst.group(
            settingsCodec(inst),
            ResourceLocation.CODEC.fieldOf("template").forGetter(s -> s.template),
            Codec.INT.fieldOf("y_offset").forGetter(s -> s.yOffset),
            Codec.INT.optionalFieldOf("max_slope", 3).forGetter(s -> s.maxSlope),
            Codec.BOOL.optionalFieldOf("underground", false).forGetter(s -> s.underground),
            Codec.INT.optionalFieldOf("check_radius", 8).forGetter(s -> s.checkRadius)
    ).apply(inst, MilitaryStructure::new)).codec();

    private final ResourceLocation template;
    private final int yOffset;
    private final int maxSlope;
    private final boolean underground;
    private final int checkRadius;

    public MilitaryStructure(StructureSettings settings, ResourceLocation template, int yOffset, int maxSlope,
                             boolean underground, int checkRadius) {
        super(settings);
        this.template = template;
        this.yOffset = yOffset;
        this.maxSlope = maxSlope;
        this.underground = underground;
        this.checkRadius = checkRadius;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        StructureTemplate tpl = ctx.structureTemplateManager().getOrCreate(template);
        Vec3i size = tpl.getSize();
        Rotation rotation = Rotation.getRandom(ctx.random());
        StructurePlaceSettings settings = MilitaryPiece.settings(rotation, size);

        int cx = ctx.chunkPos().getMiddleBlockX();
        int cz = ctx.chunkPos().getMiddleBlockZ();
        BlockPos origin = new BlockPos(cx - size.getX() / 2, 0, cz - size.getZ() / 2);
        BoundingBox box = tpl.getBoundingBox(settings, origin);

        int x0 = box.minX(), x1 = box.maxX(), z0 = box.minZ(), z1 = box.maxZ();
        if (underground) { x0 = cx - checkRadius; x1 = cx + checkRadius; z0 = cz - checkRadius; z1 = cz + checkRadius; }

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, sum = 0, n = 0;
        for (int x = x0; x <= x1 + 3; x += 4) {
            for (int z = z0; z <= z1 + 3; z += 4) {
                int sx = Math.min(x, x1), sz = Math.min(z, z1);
                int floor = ctx.chunkGenerator().getFirstFreeHeight(sx, sz, Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState());
                int surface = ctx.chunkGenerator().getFirstFreeHeight(sx, sz, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
                if (surface > floor) return Optional.empty();                       // acqua
                if (floor <= ctx.chunkGenerator().getSeaLevel()) return Optional.empty(); // troppo vicino al mare
                min = Math.min(min, floor);
                max = Math.max(max, floor);
                sum += floor;
                n++;
            }
        }
        if (n == 0 || max - min > maxSlope) return Optional.empty();

        int ground = Math.round((float) sum / n);
        BlockPos pos = new BlockPos(origin.getX(), ground + yOffset, origin.getZ());
        BlockPos startPos = new BlockPos(cx, ground, cz);
        return Optional.of(new GenerationStub(startPos,
                builder -> builder.addPiece(new MilitaryPiece(ctx.structureTemplateManager(), template, pos, rotation, !underground))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.TYPE.get();
    }
}
