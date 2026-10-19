package com.example.militarymod.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Un singolo file .nbt piazzato nel mondo, ruotato attorno al proprio centro. */
public class MilitaryPiece extends TemplateStructurePiece {
    /** il pavimento delle strutture di superficie sta allo strato 3 del file .nbt */
    private static final int FLOOR_INDEX = 3;

    private final boolean fillFoundation;

    public MilitaryPiece(StructureTemplateManager manager, ResourceLocation id, BlockPos pos, Rotation rotation, boolean fillFoundation) {
        super(ModStructures.PIECE.get(), 0, manager, id, id.toString(), settings(rotation, manager.getOrCreate(id).getSize()), pos);
        this.fillFoundation = fillFoundation;
    }

    public MilitaryPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.PIECE.get(), tag, ctx.structureTemplateManager(),
                id -> settings(Rotation.valueOf(tag.getString("Rot")), ctx.structureTemplateManager().getOrCreate(id).getSize()));
        this.fillFoundation = tag.getBoolean("Fill");
    }

    static StructurePlaceSettings settings(Rotation rotation, Vec3i size) {
        return new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setRotationPivot(new BlockPos(size.getX() / 2, 0, size.getZ() / 2))
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super.addAdditionalSaveData(ctx, tag);
        tag.putString("Rot", this.placeSettings.getRotation().name());
        tag.putBoolean("Fill", this.fillFoundation);
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
    }

    /**
     * Dopo aver piazzato la struttura, sotto il pavimento riempie ogni vuoto fino al terreno
     * con lo stesso materiale del terreno locale: cosi' le fondamenta non si vedono mai.
     */
    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        super.postProcess(level, structures, generator, random, box, chunkPos, pivot);
        if (!fillFoundation) return;

        int floorY = this.templatePosition.getY() + FLOOR_INDEX;
        BoundingBox bb = this.getBoundingBox();
        int minX = Math.max(bb.minX(), box.minX()), maxX = Math.min(bb.maxX(), box.maxX());
        int minZ = Math.max(bb.minZ(), box.minZ()), maxZ = Math.min(bb.maxZ(), box.maxZ());
        int limit = Math.max(level.getMinBuildHeight() + 1, floorY - 40);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockState floor = level.getBlockState(pos.set(x, floorY, z));
                if (floor.isAir() || !floor.getFluidState().isEmpty()) continue;   // qui non c'e' pavimento

                int y = floorY - 1;
                while (y > limit && isGap(level.getBlockState(pos.set(x, y, z)))) y--;
                if (y <= limit || y == floorY - 1) continue;                        // niente da riempire

                BlockState fill = convert(level.getBlockState(pos.set(x, y, z)));
                for (int yy = y + 1; yy <= floorY - 1; yy++) level.setBlock(pos.set(x, yy, z), fill, 2);
            }
        }
    }

    private static boolean isGap(BlockState s) {
        return s.isAir() || !s.getFluidState().isEmpty() || s.canBeReplaced();
    }

    private static BlockState convert(BlockState s) {
        if (s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.PODZOL) || s.is(Blocks.MYCELIUM) || s.is(Blocks.DIRT_PATH)
                || s.is(Blocks.ROOTED_DIRT) || s.is(Blocks.MOSS_BLOCK)) return Blocks.DIRT.defaultBlockState();
        if (s.is(Blocks.SAND)) return Blocks.SANDSTONE.defaultBlockState();
        if (s.is(Blocks.RED_SAND)) return Blocks.RED_SANDSTONE.defaultBlockState();
        return s;
    }
}
