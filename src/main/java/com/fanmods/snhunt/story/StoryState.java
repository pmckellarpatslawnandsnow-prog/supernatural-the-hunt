package com.fanmods.snhunt.story;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Saved per-world story progress for every player. */
public class StoryState extends PersistentState {
    public static final PersistentState.Type<StoryState> TYPE =
            new PersistentState.Type<>(StoryState::new, StoryState::fromNbt, null);

    final Map<UUID, Progress> players = new HashMap<>();

    public static StoryState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "snhunt_story");
    }

    public Progress get(UUID id) {
        return players.computeIfAbsent(id, k -> new Progress());
    }

    public static class Progress {
        public int chapter;           // index into Season1.chapters()
        public boolean active;        // a chapter is running
        public boolean encounterLive; // the monsters have arrived (intro finished)
        public int count;             // targets resolved this chapter
        public int ticksLeft;         // SURVIVE timer
        public int waveTimer;         // SURVIVE next wave
        @Nullable
        public BlockPos anchor;       // grave / cursed object position
        public String anchorDim = "";
        public boolean kitGiven;
        public boolean journalGiven;
        public int startItemsFor = -1;
        public boolean seasonDone;
        public boolean pendingComplete;
        /** Not saved. Bumped whenever a chapter starts or stops so stale scheduled lines are ignored. */
        public int session;

        NbtCompound write() {
            NbtCompound n = new NbtCompound();
            n.putInt("chapter", chapter);
            n.putBoolean("active", active);
            n.putBoolean("live", encounterLive);
            n.putInt("count", count);
            n.putInt("ticksLeft", ticksLeft);
            n.putInt("waveTimer", waveTimer);
            if (anchor != null) {
                n.putLong("anchor", anchor.asLong());
                n.putString("anchorDim", anchorDim);
            }
            n.putBoolean("kit", kitGiven);
            n.putBoolean("journal", journalGiven);
            n.putInt("startItemsFor", startItemsFor);
            n.putBoolean("seasonDone", seasonDone);
            n.putBoolean("pending", pendingComplete);
            return n;
        }

        static Progress read(NbtCompound n) {
            Progress p = new Progress();
            p.chapter = n.getInt("chapter");
            p.active = n.getBoolean("active");
            p.encounterLive = n.getBoolean("live");
            p.count = n.getInt("count");
            p.ticksLeft = n.getInt("ticksLeft");
            p.waveTimer = n.getInt("waveTimer");
            if (n.contains("anchor")) {
                p.anchor = BlockPos.fromLong(n.getLong("anchor"));
                p.anchorDim = n.getString("anchorDim");
            }
            p.kitGiven = n.getBoolean("kit");
            p.journalGiven = n.getBoolean("journal");
            p.startItemsFor = n.contains("startItemsFor") ? n.getInt("startItemsFor") : -1;
            p.seasonDone = n.getBoolean("seasonDone");
            p.pendingComplete = n.getBoolean("pending");
            return p;
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound all = new NbtCompound();
        players.forEach((id, p) -> all.put(id.toString(), p.write()));
        nbt.put("players", all);
        return nbt;
    }

    public static StoryState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        StoryState state = new StoryState();
        NbtCompound all = nbt.getCompound("players");
        for (String key : all.getKeys()) {
            try {
                state.players.put(UUID.fromString(key), Progress.read(all.getCompound(key)));
            } catch (IllegalArgumentException ignored) {
                // skip malformed entries
            }
        }
        return state;
    }
}
