package com.example.militarymod.wave;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WaveData extends SavedData {
    public long lastWaveDay = 0;

    public static WaveData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(WaveData::load, WaveData::new, "militarymod_waves");
    }

    public static WaveData load(CompoundTag tag) {
        WaveData d = new WaveData();
        d.lastWaveDay = tag.getLong("last");
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("last", lastWaveDay);
        return tag;
    }
}
