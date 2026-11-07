package com.fanmods.snhunt.story;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** One story chapter (roughly one episode). Built with a small fluent API in {@link Season1}. */
public final class Chapter {

    public enum Objective {
        /** Salt and burn the grave that spawns nearby. */
        SALT_BURN,
        /** Kill every target. */
        KILL,
        /** Resolve every demon target (exorcise it or burn it out). */
        EXORCISE,
        /** Break the cursed object that spawns nearby. */
        BREAK_CURSED,
        /** Stay alive while waves spawn. */
        SURVIVE,
        /** Drive off the boss. */
        BOSS
    }

    public enum Speaker {
        NARRATOR(null, Formatting.GRAY),
        DEAN("Dean", Formatting.GOLD),
        SAM("Sam", Formatting.AQUA),
        JOHN("John", Formatting.DARK_GREEN),
        BOBBY("Bobby", Formatting.YELLOW),
        MISSOURI("Missouri", Formatting.LIGHT_PURPLE),
        MEG("Meg", Formatting.DARK_RED),
        JOURNAL("Journal", Formatting.DARK_AQUA);

        @Nullable
        public final String displayName;
        public final Formatting color;

        Speaker(@Nullable String displayName, Formatting color) {
            this.displayName = displayName;
            this.color = color;
        }
    }

    public record Line(Speaker speaker, String text) {
    }

    public record SpawnSpec(EntityType<?> type, int count, String name, boolean bound) {
    }

    public record Reward(Item item, int count) {
        public ItemStack stack() {
            return new ItemStack(item, count);
        }
    }

    public final int number;
    public final String title;
    public final String location;
    public final Objective objective;
    final List<Line> intro = new ArrayList<>();
    final List<Line> outro = new ArrayList<>();
    final List<SpawnSpec> spawns = new ArrayList<>();
    final List<Reward> startItems = new ArrayList<>();
    final List<Reward> rewards = new ArrayList<>();
    String goal = "";
    @Nullable
    Block anchor;
    String anchorName = "";
    int surviveSeconds;
    int waveSeconds = 20;

    Chapter(int number, String title, String location, Objective objective) {
        this.number = number;
        this.title = title;
        this.location = location;
        this.objective = objective;
    }

    // ----- builder -----

    Chapter narrate(String text) {
        intro.add(new Line(Speaker.NARRATOR, text));
        return this;
    }

    Chapter say(Speaker who, String text) {
        intro.add(new Line(who, text));
        return this;
    }

    Chapter goal(String text) {
        this.goal = text;
        return this;
    }

    Chapter spawn(EntityType<?> type, int count, String name) {
        spawns.add(new SpawnSpec(type, count, name, false));
        return this;
    }

    Chapter spawnBound(EntityType<?> type, String name) {
        spawns.add(new SpawnSpec(type, 1, name, true));
        return this;
    }

    Chapter anchor(Block block, String name) {
        this.anchor = block;
        this.anchorName = name;
        return this;
    }

    Chapter survive(int seconds, int waveEvery) {
        this.surviveSeconds = seconds;
        this.waveSeconds = waveEvery;
        return this;
    }

    Chapter startWith(Item item, int count) {
        startItems.add(new Reward(item, count));
        return this;
    }

    Chapter reward(Item item, int count) {
        rewards.add(new Reward(item, count));
        return this;
    }

    Chapter outroNarrate(String text) {
        outro.add(new Line(Speaker.NARRATOR, text));
        return this;
    }

    Chapter outroSay(Speaker who, String text) {
        outro.add(new Line(who, text));
        return this;
    }

    // ----- queries -----

    /** How many targets must be resolved for KILL / EXORCISE / BOSS chapters. */
    public int targetCount() {
        int total = 0;
        for (SpawnSpec s : spawns) {
            total += s.count();
        }
        return Math.max(1, total);
    }

    public boolean countsTargets() {
        return objective == Objective.KILL || objective == Objective.EXORCISE || objective == Objective.BOSS;
    }

    public List<Line> intro() {
        return intro;
    }

    public List<Line> outro() {
        return outro;
    }

    public List<SpawnSpec> spawns() {
        return spawns;
    }

    public List<Reward> startItems() {
        return startItems;
    }

    public List<Reward> rewards() {
        return rewards;
    }

    public String goal() {
        return goal;
    }

    @Nullable
    public Block anchor() {
        return anchor;
    }

    public String anchorName() {
        return anchorName;
    }

    public int surviveSeconds() {
        return surviveSeconds;
    }

    public int waveSeconds() {
        return waveSeconds;
    }
}
