package com.example.militarymod.wave;

import com.example.militarymod.MilitaryMod;
import com.example.militarymod.ModEntities;
import com.example.militarymod.entity.HostileSoldier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Ogni 5 giorni di Minecraft, al tramonto, parte un'ondata di assedio attorno a ogni giocatore. */
@Mod.EventBusSubscriber(modid = MilitaryMod.MODID)
public class WaveManager {
    public static final int DAYS_BETWEEN_WAVES = 5;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.level instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD || level.getGameTime() % 20 != 0) return;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;

        long dayTime = level.getDayTime();
        long day = dayTime / 24000L;
        long t = dayTime % 24000L;
        long waveDay = day - (day % DAYS_BETWEEN_WAVES);
        if (waveDay <= 0) return;

        WaveData data = WaveData.get(level);
        if (data.lastWaveDay < waveDay && (day > waveDay || t >= 13000L)) {
            data.lastWaveDay = waveDay;
            data.setDirty();
            long waveNumber = waveDay / DAYS_BETWEEN_WAVES;
            for (ServerPlayer p : level.players()) {
                if (p.isSpectator() || !p.isAlive()) continue;
                p.sendSystemMessage(Component.translatable("msg.militarymod.wave", waveNumber));
                p.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 2.0F, 0.5F);
                spawnWave(level, p, waveNumber);
            }
        }
    }

    private static void spawnWave(ServerLevel level, ServerPlayer player, long wave) {
        int count = (int) Math.min(4 + wave * 2, 28);
        RandomSource r = level.random;
        int spawned = 0;
        for (int i = 0; i < count * 6 && spawned < count; i++) {
            double ang = r.nextDouble() * Math.PI * 2;
            double dist = 26 + r.nextInt(14);
            int x = Mth.floor(player.getX() + Math.cos(ang) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(ang) * dist);
            if (!level.hasChunkAt(new BlockPos(x, 64, z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getFluidState(pos.below()).isEmpty()) continue;

            HostileSoldier s = ModEntities.HOSTILE_SOLDIER.get().create(level);
            if (s == null) continue;
            s.moveTo(x + 0.5D, y, z + 0.5D, r.nextFloat() * 360.0F, 0.0F);
            s.setWave(true);
            s.setPersistenceRequired();
            s.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.min(24 + wave * 2, 60));
            s.setHealth(s.getMaxHealth());
            s.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
            level.addFreshEntity(s);
            spawned++;
        }
    }
}
