package com.fanmods.snhunt.story;

import com.fanmods.snhunt.entity.HuntMonsterEntity;
import com.fanmods.snhunt.registry.ModBlocks;
import com.fanmods.snhunt.registry.ModItems;
import com.fanmods.snhunt.story.Chapter.Objective;
import com.fanmods.snhunt.story.StoryState.Progress;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/** Runs story mode: starting chapters, spawning hunts, tracking objectives, and handing out rewards. */
public final class StoryManager {
    public static final String TAG_TARGET = "snhunt_target";
    public static final String TAG_BOUND = "snhunt_bound";
    public static final String TAG_RESOLVED = "snhunt_resolved";
    public static final String TAG_OWNER = "snhunt_owner_";
    public static final String TAG_CHAPTER = "snhunt_ch_";

    private static final int LINE_GAP = 60; // ticks between story lines

    private record Task(long due, Consumer<MinecraftServer> action) {
    }

    private static final List<Task> TASKS = new ArrayList<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(StoryManager::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> onResolved(entity));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.getPlayer()));
        ServerEntityEvents.ENTITY_LOAD.register(StoryManager::onEntityLoad);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TASKS.clear());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> HuntCommand.register(dispatcher));
    }

    // ------------------------------------------------------------------ helpers

    public static List<Chapter> chapters() {
        return Season1.chapters();
    }

    static Chapter chapter(Progress pr) {
        List<Chapter> all = chapters();
        return all.get(Math.min(pr.chapter, all.size() - 1));
    }

    static StoryState state(MinecraftServer server) {
        return StoryState.get(server);
    }

    static Progress progress(ServerPlayerEntity player) {
        return state(player.getServer()).get(player.getUuid());
    }

    private static void schedule(MinecraftServer server, int delay, Consumer<MinecraftServer> action) {
        TASKS.add(new Task(server.getTicks() + delay, action));
    }

    /** Schedule something for a player that is skipped if they left or the chapter was restarted. */
    private static void scheduleFor(ServerPlayerEntity player, int delay, Consumer<ServerPlayerEntity> action) {
        UUID id = player.getUuid();
        int session = progress(player).session;
        schedule(player.getServer(), delay, server -> {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
            if (p != null && state(server).get(id).session == session) {
                action.accept(p);
            }
        });
    }

    private static void tick(MinecraftServer server) {
        if (!TASKS.isEmpty()) {
            List<Task> due = new ArrayList<>();
            for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
                Task t = it.next();
                if (t.due() <= server.getTicks()) {
                    due.add(t);
                    it.remove();
                }
            }
            due.forEach(t -> t.action().accept(server));
        }

        StoryState st = state(server);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            Progress pr = st.get(p.getUuid());
            if (!pr.active || !pr.encounterLive) {
                continue;
            }
            Chapter c = chapter(pr);
            if (c.objective != Objective.SURVIVE) {
                continue;
            }
            pr.ticksLeft--;
            if (--pr.waveTimer <= 0) {
                spawnTargets(p, c, null);
                pr.waveTimer = c.waveSeconds() * 20;
            }
            if (pr.ticksLeft % 20 == 0) {
                p.sendMessage(Text.literal("Survive: " + Math.max(0, pr.ticksLeft / 20) + "s").formatted(Formatting.RED, Formatting.BOLD), true);
                st.markDirty();
            }
            if (pr.ticksLeft <= 0) {
                complete(p, pr);
            }
        }
    }

    static void say(ServerPlayerEntity p, Chapter.Line line) {
        MutableText msg;
        if (line.speaker().displayName == null) {
            msg = Text.literal(line.text()).formatted(Formatting.GRAY, Formatting.ITALIC);
        } else {
            msg = Text.literal(line.speaker().displayName + ": ").formatted(line.speaker().color, Formatting.BOLD)
                    .append(Text.literal(line.text()).formatted(Formatting.WHITE));
        }
        p.sendMessage(msg, false);
    }

    static void title(ServerPlayerEntity p, Text title, Text subtitle) {
        p.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 20));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
        p.networkHandler.sendPacket(new TitleS2CPacket(title));
    }

    static MutableText button(String label, String command, Formatting color) {
        return Text.literal("[" + label + "]").styled(s -> s.withColor(color).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(command))));
    }

    static void give(ServerPlayerEntity p, ItemStack stack) {
        if (!p.giveItemStack(stack)) {
            p.dropItem(stack, false);
        }
    }

    @Nullable
    static UUID ownerOf(Entity e) {
        for (String tag : e.getCommandTags()) {
            if (tag.startsWith(TAG_OWNER)) {
                try {
                    return UUID.fromString(tag.substring(TAG_OWNER.length()));
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    static String direction(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double angle = (Math.toDegrees(Math.atan2(dx, -dz)) + 360.0) % 360.0;
        String[] names = {"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};
        return names[(int) Math.round(angle / 45.0) % 8];
    }

    static BlockPos findSurface(ServerWorld world, BlockPos center, int min, int max) {
        Random r = world.getRandom();
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < 24; i++) {
                double a = r.nextDouble() * Math.PI * 2;
                int d = min + r.nextInt(max - min + 1);
                int x = center.getX() + (int) Math.round(Math.cos(a) * d);
                int z = center.getZ() + (int) Math.round(Math.sin(a) * d);
                int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos pos = new BlockPos(x, y, z);
                if (pass == 0 && Math.abs(y - center.getY()) > 10) {
                    continue;
                }
                if (!world.getFluidState(pos.down()).isEmpty() || !world.getBlockState(pos).isAir()) {
                    continue;
                }
                return pos;
            }
        }
        return center.north(3);
    }

    /** The player's current story grave / cursed object, if it's in their world. Used by the EMF meter. */
    @Nullable
    public static BlockPos anchorFor(ServerPlayerEntity player) {
        Progress pr = progress(player);
        if (pr.active && pr.anchor != null && pr.anchorDim.equals(player.getWorld().getRegistryKey().getValue().toString())) {
            return pr.anchor;
        }
        return null;
    }

    @Nullable
    private static ServerWorld anchorWorld(MinecraftServer server, Progress pr) {
        if (pr.anchorDim.isEmpty()) {
            return null;
        }
        Identifier id = Identifier.tryParse(pr.anchorDim);
        return id == null ? null : server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }

    // ------------------------------------------------------------------ chapter flow

    public static void startChapter(ServerPlayerEntity p) {
        StoryState st = state(p.getServer());
        Progress pr = st.get(p.getUuid());
        if (pr.seasonDone) {
            p.sendMessage(Text.literal("You've finished Season 1. Use /hunt goto <chapter> (cheats on) to replay a chapter.").formatted(Formatting.GOLD), false);
            return;
        }
        if (pr.active) {
            p.sendMessage(Text.literal("You're already on a hunt. ").formatted(Formatting.YELLOW)
                    .append(button("Hint", "/hunt hint", Formatting.AQUA)).append(" ")
                    .append(button("Restart", "/hunt restart", Formatting.GOLD)), false);
            return;
        }
        Chapter c = chapter(pr);
        if (!pr.kitGiven) {
            giveStarterKit(p);
            pr.kitGiven = true;
        }
        if (pr.startItemsFor != pr.chapter && !c.startItems().isEmpty()) {
            for (Chapter.Reward r : c.startItems()) {
                give(p, r.stack());
            }
            p.sendMessage(Text.literal("Bobby slipped some gear into your bag.").formatted(Formatting.DARK_GREEN), false);
        }
        pr.startItemsFor = pr.chapter;
        pr.active = true;
        pr.encounterLive = false;
        pr.count = 0;
        pr.anchor = null;
        pr.session++;
        st.markDirty();

        title(p, Text.literal("Chapter " + c.number + ": " + c.title).formatted(Formatting.GOLD, Formatting.BOLD),
                Text.literal(c.location).formatted(Formatting.GRAY, Formatting.ITALIC));
        p.sendMessage(Text.literal("━━ Chapter " + c.number + ": " + c.title + " ━━ ").formatted(Formatting.GOLD, Formatting.BOLD)
                .append(Text.literal(c.location).formatted(Formatting.GRAY, Formatting.ITALIC)), false);

        int delay = 50;
        for (Chapter.Line line : c.intro()) {
            scheduleFor(p, delay, pl -> say(pl, line));
            delay += LINE_GAP;
        }
        scheduleFor(p, delay, pl -> {
            Progress p2 = progress(pl);
            if (p2.active && !p2.encounterLive) {
                beginEncounter(pl, p2);
            }
        });
    }

    static void beginEncounter(ServerPlayerEntity p, Progress pr) {
        Chapter c = chapter(pr);
        ServerWorld world = p.getServerWorld();
        pr.encounterLive = true;

        if (c.anchor() != null) {
            BlockPos ap = findSurface(world, p.getBlockPos(), 18, 28);
            world.setBlockState(ap, c.anchor().getDefaultState());
            pr.anchor = ap;
            pr.anchorDim = world.getRegistryKey().getValue().toString();
            int dist = (int) Math.sqrt(p.getBlockPos().getSquaredDistance(ap));
            p.sendMessage(Text.literal("Your EMF meter spikes. " + capitalize(c.anchorName()) + " is about " + dist
                    + " blocks " + direction(p.getBlockPos(), ap) + ".").formatted(Formatting.DARK_AQUA), false);
        }
        if (c.objective == Objective.SURVIVE) {
            pr.ticksLeft = c.surviveSeconds() * 20;
            pr.waveTimer = 0; // first wave next tick
        } else {
            spawnTargets(p, c, pr.anchor);
        }

        p.sendMessage(Text.literal("▶ OBJECTIVE: ").formatted(Formatting.RED, Formatting.BOLD)
                .append(Text.literal(c.goal()).formatted(Formatting.WHITE)), false);
        p.sendMessage(button("Hint", "/hunt hint", Formatting.AQUA).append(" ")
                .append(button("Monster lore", "/hunt lore", Formatting.DARK_AQUA)).append(" ")
                .append(button("Restart", "/hunt restart", Formatting.GOLD)), false);
        world.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 0.5f, 1.0f);
        state(p.getServer()).markDirty();
    }

    static void spawnTargets(ServerPlayerEntity p, Chapter c, @Nullable BlockPos anchor) {
        ServerWorld world = p.getServerWorld();
        Progress pr = progress(p);
        for (Chapter.SpawnSpec spec : c.spawns()) {
            for (int i = 0; i < spec.count(); i++) {
                BlockPos pos = spec.bound() && anchor != null
                        ? findSurface(world, anchor, 3, 8)
                        : findSurface(world, p.getBlockPos(), 12, 20);
                Entity e = spec.type().create(world);
                if (e == null) {
                    continue;
                }
                e.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.getRandom().nextFloat() * 360f, 0);
                if (e instanceof MobEntity mob) {
                    mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
                    mob.setPersistent();
                }
                e.setCustomName(Text.literal(spec.name()));
                e.setCustomNameVisible(true);
                e.addCommandTag(TAG_TARGET);
                e.addCommandTag(TAG_OWNER + p.getUuid());
                e.addCommandTag(TAG_CHAPTER + pr.chapter);
                if (spec.bound()) {
                    e.addCommandTag(TAG_BOUND);
                }
                world.spawnEntity(e);
                world.spawnParticles(ParticleTypes.LARGE_SMOKE, e.getX(), e.getY() + 1, e.getZ(), 15, 0.3, 0.6, 0.3, 0.02);
            }
        }
    }

    /** A story target died, was exorcised, banished, or fled. */
    public static void onResolved(Entity e) {
        if (e.getWorld().isClient) {
            return;
        }
        if (!e.getCommandTags().contains(TAG_TARGET) || e.getCommandTags().contains(TAG_RESOLVED)) {
            return;
        }
        e.addCommandTag(TAG_RESOLVED);
        UUID owner = ownerOf(e);
        MinecraftServer server = e.getServer();
        if (owner == null || server == null) {
            return;
        }
        StoryState st = state(server);
        Progress pr = st.get(owner);
        if (!pr.active || !e.getCommandTags().contains(TAG_CHAPTER + pr.chapter)) {
            return;
        }
        Chapter c = chapter(pr);
        if (!c.countsTargets()) {
            return;
        }
        pr.count++;
        st.markDirty();
        int need = c.targetCount();
        ServerPlayerEntity p = server.getPlayerManager().getPlayer(owner);
        if (p != null) {
            p.sendMessage(Text.literal("Target down: " + Math.min(pr.count, need) + "/" + need).formatted(Formatting.GREEN), true);
        }
        if (pr.count >= need) {
            if (p != null) {
                complete(p, pr);
            } else {
                pr.pendingComplete = true;
            }
        }
    }

    /** A grave was burned or a cursed object broken. */
    public static void onAnchorDestroyed(ServerWorld world, BlockPos pos, Objective type) {
        List<HuntMonsterEntity> bound = world.getEntitiesByClass(HuntMonsterEntity.class, new Box(pos).expand(96), HuntMonsterEntity::isBound);
        for (HuntMonsterEntity m : bound) {
            m.banish();
        }
        MinecraftServer server = world.getServer();
        StoryState st = state(server);
        String dim = world.getRegistryKey().getValue().toString();
        for (Map.Entry<UUID, Progress> entry : st.players.entrySet()) {
            Progress pr = entry.getValue();
            if (pr.active && pos.equals(pr.anchor) && dim.equals(pr.anchorDim) && chapter(pr).objective == type) {
                pr.anchor = null;
                st.markDirty();
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(entry.getKey());
                if (p != null) {
                    complete(p, pr);
                } else {
                    pr.pendingComplete = true;
                }
                return;
            }
        }
    }

    static void complete(ServerPlayerEntity p, Progress pr) {
        MinecraftServer server = p.getServer();
        Chapter c = chapter(pr);
        pr.active = false;
        pr.encounterLive = false;
        pr.pendingComplete = false;
        despawnTargets(server, p.getUuid());
        removeAnchor(server, pr);
        pr.session++;

        title(p, Text.literal("Hunt complete").formatted(Formatting.GOLD, Formatting.BOLD),
                Text.literal(c.title).formatted(Formatting.GRAY));
        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.8f, 1.0f);

        int delay = 40;
        for (Chapter.Line line : c.outro()) {
            scheduleFor(p, delay, pl -> say(pl, line));
            delay += LINE_GAP;
        }
        // Rewards are given now so nothing is lost if the player logs off during the outro.
        if (!c.rewards().isEmpty()) {
            MutableText list = Text.literal("Rewards: ").formatted(Formatting.GREEN, Formatting.BOLD);
            boolean first = true;
            for (Chapter.Reward r : c.rewards()) {
                ItemStack stack = r.stack();
                if (!first) {
                    list.append(Text.literal(", ").formatted(Formatting.GREEN));
                }
                list.append(Text.literal(r.count() + "× ").formatted(Formatting.WHITE)).append(stack.getName().copy().formatted(Formatting.WHITE));
                give(p, stack);
                first = false;
            }
            Text rewardText = list;
            scheduleFor(p, delay, pl -> pl.sendMessage(rewardText, false));
        }

        pr.chapter++;
        boolean finished = pr.chapter >= chapters().size();
        if (finished) {
            pr.seasonDone = true;
            pr.chapter = chapters().size() - 1;
        }
        state(server).markDirty();

        scheduleFor(p, delay + 30, pl -> {
            if (finished) {
                title(pl, Text.literal("Season 1 complete").formatted(Formatting.DARK_RED, Formatting.BOLD),
                        Text.literal("To be continued...").formatted(Formatting.GRAY, Formatting.ITALIC));
                pl.sendMessage(Text.literal("You finished Season 1! More seasons can be added in later versions of the mod.").formatted(Formatting.GOLD), false);
            } else {
                Chapter next = chapter(progress(pl));
                pl.sendMessage(Text.literal("Next: Chapter " + next.number + ", " + next.title + " (" + next.location + ") ").formatted(Formatting.YELLOW)
                        .append(button("Start next hunt", "/hunt start", Formatting.GREEN)), false);
            }
        });
    }

    static void despawnTargets(MinecraftServer server, UUID owner) {
        String tag = TAG_OWNER + owner;
        for (ServerWorld world : server.getWorlds()) {
            List<Entity> found = new ArrayList<>();
            for (Entity e : world.iterateEntities()) {
                if (e.getCommandTags().contains(tag)) {
                    found.add(e);
                }
            }
            for (Entity e : found) {
                world.spawnParticles(ParticleTypes.POOF, e.getX(), e.getBodyY(0.5), e.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
                e.discard();
            }
        }
    }

    static void removeAnchor(MinecraftServer server, Progress pr) {
        if (pr.anchor == null) {
            return;
        }
        ServerWorld world = anchorWorld(server, pr);
        if (world != null) {
            BlockState state = world.getBlockState(pr.anchor);
            if (state.isOf(ModBlocks.GRAVE) || state.isOf(ModBlocks.CURSED_OBJECT)) {
                world.removeBlock(pr.anchor, false);
            }
        }
        pr.anchor = null;
    }

    public static void abandon(ServerPlayerEntity p) {
        Progress pr = progress(p);
        if (!pr.active) {
            p.sendMessage(Text.literal("You're not on a hunt right now.").formatted(Formatting.GRAY), false);
            return;
        }
        despawnTargets(p.getServer(), p.getUuid());
        removeAnchor(p.getServer(), pr);
        pr.active = false;
        pr.encounterLive = false;
        pr.session++;
        state(p.getServer()).markDirty();
        p.sendMessage(Text.literal("Hunt abandoned. ").formatted(Formatting.GRAY).append(button("Start again", "/hunt start", Formatting.GREEN)), false);
    }

    public static void restart(ServerPlayerEntity p) {
        Progress pr = progress(p);
        if (!pr.active) {
            startChapter(p);
            return;
        }
        despawnTargets(p.getServer(), p.getUuid());
        removeAnchor(p.getServer(), pr);
        pr.count = 0;
        pr.session++;
        p.sendMessage(Text.literal("The trail picks up again...").formatted(Formatting.GRAY, Formatting.ITALIC), false);
        beginEncounter(p, pr);
    }

    public static void gotoChapter(ServerPlayerEntity p, int number) {
        Progress pr = progress(p);
        if (pr.active) {
            despawnTargets(p.getServer(), p.getUuid());
            removeAnchor(p.getServer(), pr);
            pr.active = false;
            pr.encounterLive = false;
        }
        pr.chapter = Math.max(0, Math.min(number - 1, chapters().size() - 1));
        pr.seasonDone = false;
        pr.startItemsFor = -1;
        pr.session++;
        state(p.getServer()).markDirty();
        Chapter c = chapter(pr);
        p.sendMessage(Text.literal("Jumped to Chapter " + c.number + ": " + c.title + ". ").formatted(Formatting.YELLOW)
                .append(button("Start", "/hunt start", Formatting.GREEN)), false);
    }

    public static void hint(ServerPlayerEntity p) {
        Progress pr = progress(p);
        if (!pr.active) {
            p.sendMessage(Text.literal("No hunt in progress. ").formatted(Formatting.GRAY).append(button("Start", "/hunt start", Formatting.GREEN)), false);
            return;
        }
        Chapter c = chapter(pr);
        p.sendMessage(Text.literal("Objective: ").formatted(Formatting.RED).append(Text.literal(c.goal()).formatted(Formatting.WHITE)), false);
        BlockPos anchor = anchorFor(p);
        if (anchor != null) {
            int dist = (int) Math.sqrt(p.getBlockPos().getSquaredDistance(anchor));
            p.sendMessage(Text.literal(capitalize(c.anchorName()) + ": " + dist + " blocks " + direction(p.getBlockPos(), anchor)
                    + " (" + anchor.getX() + ", " + anchor.getY() + ", " + anchor.getZ() + ")").formatted(Formatting.DARK_AQUA), false);
        }
        String tag = TAG_OWNER + p.getUuid();
        List<LivingEntity> targets = p.getServerWorld().getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(160),
                e -> e.getCommandTags().contains(tag) && e.isAlive());
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : targets) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0, false, false));
            double d = e.squaredDistanceTo(p);
            if (d < best) {
                best = d;
                nearest = e;
            }
        }
        if (nearest != null) {
            p.sendMessage(Text.literal("Nearest target: ").formatted(Formatting.GRAY)
                    .append(nearest.getDisplayName().copy().formatted(Formatting.WHITE))
                    .append(Text.literal(", " + (int) Math.sqrt(best) + " blocks " + direction(p.getBlockPos(), nearest.getBlockPos())
                            + ". Targets are glowing for 10 seconds.").formatted(Formatting.GRAY)), false);
        } else if (c.countsTargets() && pr.encounterLive) {
            p.sendMessage(Text.literal("No targets nearby. If they were lost, use ").formatted(Formatting.GRAY)
                    .append(button("Restart", "/hunt restart", Formatting.GOLD)), false);
        }
        if (c.countsTargets()) {
            p.sendMessage(Text.literal("Progress: " + pr.count + "/" + c.targetCount()).formatted(Formatting.GREEN), false);
        } else if (c.objective == Objective.SURVIVE && pr.encounterLive) {
            p.sendMessage(Text.literal("Hold on for " + Math.max(0, pr.ticksLeft / 20) + " more seconds.").formatted(Formatting.GREEN), false);
        }
    }

    // ------------------------------------------------------------------ journal & lore

    public static void showJournal(ServerPlayerEntity p) {
        Progress pr = progress(p);
        List<Chapter> all = chapters();
        p.sendMessage(Text.literal("━━━ Hunter's Journal · Season 1 ━━━").formatted(Formatting.GOLD, Formatting.BOLD), false);
        if (pr.seasonDone) {
            p.sendMessage(Text.literal("Every Season 1 hunt is done. The yellow-eyed demon is still out there...").formatted(Formatting.GRAY, Formatting.ITALIC), false);
            p.sendMessage(button("Chapters", "/hunt chapters", Formatting.YELLOW).append(" ").append(button("Monster lore", "/hunt lore", Formatting.DARK_AQUA)), false);
            return;
        }
        Chapter c = chapter(pr);
        p.sendMessage(Text.literal("Chapter " + c.number + "/" + all.size() + ": ").formatted(Formatting.GRAY)
                .append(Text.literal(c.title).formatted(Formatting.YELLOW, Formatting.BOLD))
                .append(Text.literal("  " + c.location).formatted(Formatting.DARK_GRAY, Formatting.ITALIC)), false);
        if (pr.active) {
            p.sendMessage(Text.literal("Objective: ").formatted(Formatting.RED).append(Text.literal(c.goal()).formatted(Formatting.WHITE)), false);
            if (c.countsTargets()) {
                p.sendMessage(Text.literal("Progress: " + pr.count + "/" + c.targetCount()).formatted(Formatting.GREEN), false);
            }
            p.sendMessage(button("Hint", "/hunt hint", Formatting.AQUA).append(" ")
                    .append(button("Restart", "/hunt restart", Formatting.GOLD)).append(" ")
                    .append(button("Abandon", "/hunt abandon", Formatting.RED)), false);
        } else {
            if (!c.intro().isEmpty()) {
                p.sendMessage(Text.literal(c.intro().get(0).text()).formatted(Formatting.GRAY, Formatting.ITALIC), false);
            }
            p.sendMessage(Text.literal("Tip: start hunts outdoors in the Overworld. Night makes it scarier.").formatted(Formatting.DARK_GRAY), false);
            p.sendMessage(button("▶ Start this hunt", "/hunt start", Formatting.GREEN).append(" ")
                    .append(button("Chapters", "/hunt chapters", Formatting.YELLOW)).append(" ")
                    .append(button("Monster lore", "/hunt lore", Formatting.DARK_AQUA)), false);
        }
    }

    public static void showChapters(ServerPlayerEntity p) {
        Progress pr = progress(p);
        p.sendMessage(Text.literal("━━━ Season 1 ━━━").formatted(Formatting.GOLD, Formatting.BOLD), false);
        for (Chapter c : chapters()) {
            int idx = c.number - 1;
            boolean done = pr.seasonDone || idx < pr.chapter;
            boolean current = !pr.seasonDone && idx == pr.chapter;
            MutableText line = Text.literal(done ? " ✔ " : current ? " ▶ " : " · ")
                    .formatted(done ? Formatting.GREEN : current ? Formatting.YELLOW : Formatting.DARK_GRAY);
            line.append(Text.literal(c.number + ". " + c.title).formatted(done ? Formatting.GRAY : current ? Formatting.YELLOW : Formatting.DARK_GRAY));
            p.sendMessage(line, false);
        }
    }

    public static void showLore(ServerPlayerEntity p) {
        p.sendMessage(Text.literal("━━━ Know your monsters ━━━").formatted(Formatting.DARK_AQUA, Formatting.BOLD), false);
        lore(p, "Vengeful Spirit", "Weapons pass through it. Iron and rock salt disperse it for a few seconds; it can't cross a salt line. "
                + "Story spirits only die when you salt and burn their remains (or break their cursed object). The EMF meter finds them.");
        lore(p, "Wendigo", "Nearly immune to normal damage. Set it on fire (Flare Gun, flint and steel, fire charge) and hit it while it burns.");
        lore(p, "Shapeshifter", "Silver kills it. Use the Silver Knife; other weapons barely scratch it.");
        lore(p, "Demon", "Its host can't be killed with weapons. Lure it onto a Devil's Trap and read the Rite of Exorcism, or burn it out with Holy Water. Salt lines stop it.");
        lore(p, "Vampire", "Heals constantly. Dead Man's Blood stops the healing. Only a beheading (Machete or any axe) can finish it.");
        lore(p, "Shtriga", "Only iron hurts it: iron weapons and salt rounds.");
        lore(p, "Daeva", "Invisible and tough in darkness, weak in the light. Light the area up.");
        lore(p, "Pagan God (Scarecrow)", "Can't be harmed while its sacred tree stands. Freezes in daylight. Wild ones burn well.");
        lore(p, "Yellow-Eyed Demon", "Throws you around with its mind. Only the Colt can wound it badly enough to make it flee.");
    }

    private static void lore(ServerPlayerEntity p, String name, String text) {
        p.sendMessage(Text.literal(name + ": ").formatted(Formatting.YELLOW, Formatting.BOLD).append(Text.literal(text).formatted(Formatting.GRAY)), false);
    }

    // ------------------------------------------------------------------ events

    private static void giveStarterKit(ServerPlayerEntity p) {
        give(p, new ItemStack(ModItems.EMF_METER));
        give(p, new ItemStack(ModItems.SALT_SHOTGUN));
        give(p, new ItemStack(ModItems.SALT, 32));
        give(p, new ItemStack(Items.FLINT_AND_STEEL));
        give(p, new ItemStack(Items.IRON_SWORD));
        give(p, new ItemStack(Items.BREAD, 8));
        p.sendMessage(Text.literal("Dean tosses you a duffel bag: EMF meter, salt shotgun, rock salt, a lighter, an iron blade and some food.")
                .formatted(Formatting.DARK_GREEN), false);
    }

    private static void onJoin(ServerPlayerEntity p) {
        StoryState st = state(p.getServer());
        Progress pr = st.get(p.getUuid());
        if (!pr.journalGiven) {
            pr.journalGiven = true;
            st.markDirty();
            give(p, new ItemStack(ModItems.HUNTERS_JOURNAL));
            p.sendMessage(Text.literal("You found a battered Hunter's Journal. Right-click it to begin Season 1 story mode. ")
                    .formatted(Formatting.GOLD).append(button("Open journal", "/hunt", Formatting.GREEN)), false);
            return;
        }
        if (pr.pendingComplete && pr.active) {
            complete(p, pr);
            return;
        }
        if (pr.active && !pr.encounterLive) {
            // The server stopped during the intro; let them start again cleanly.
            pr.active = false;
            st.markDirty();
        }
        if (pr.active) {
            p.sendMessage(Text.literal("Your hunt is still on: " + chapter(pr).title + ". ").formatted(Formatting.YELLOW)
                    .append(button("Hint", "/hunt hint", Formatting.AQUA)), false);
        }
    }

    /** Clean up stale story monsters that load from disk after their chapter ended. */
    private static void onEntityLoad(Entity entity, ServerWorld world) {
        if (!entity.getCommandTags().contains(TAG_TARGET)) {
            return;
        }
        UUID owner = ownerOf(entity);
        if (owner == null) {
            return;
        }
        Progress pr = state(world.getServer()).get(owner);
        if (!pr.active || !entity.getCommandTags().contains(TAG_CHAPTER + pr.chapter)) {
            schedule(world.getServer(), 1, s -> {
                if (!entity.isRemoved()) {
                    entity.discard();
                }
            });
        }
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private StoryManager() {
    }
}
