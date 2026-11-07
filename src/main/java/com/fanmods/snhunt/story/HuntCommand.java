package com.fanmods.snhunt.story;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * /hunt              open the journal
 * /hunt start        begin the current chapter
 * /hunt hint         where to go, what to do (targets glow)
 * /hunt restart      respawn the current hunt
 * /hunt abandon      give up the current hunt
 * /hunt chapters     list Season 1
 * /hunt lore         monster weaknesses
 * /hunt goto <n>     jump to a chapter (cheats / op only)
 */
public final class HuntCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hunt")
                .executes(ctx -> {
                    StoryManager.showJournal(ctx.getSource().getPlayerOrThrow());
                    return 1;
                })
                .then(CommandManager.literal("start").executes(ctx -> {
                    StoryManager.startChapter(ctx.getSource().getPlayerOrThrow());
                    return 1;
                }))
                .then(CommandManager.literal("hint").executes(ctx -> {
                    StoryManager.hint(ctx.getSource().getPlayerOrThrow());
                    return 1;
                }))
                .then(CommandManager.literal("restart").executes(ctx -> {
                    StoryManager.restart(ctx.getSource().getPlayerOrThrow());
                    return 1;
                }))
                .then(CommandManager.literal("abandon").executes(ctx -> {
                    StoryManager.abandon(ctx.getSource().getPlayerOrThrow());
                    return 1;
                }))
                .then(CommandManager.literal("chapters").executes(ctx -> {
                    StoryManager.showChapters(ctx.getSource().getPlayerOrThrow());
                    return 1;
                }))
                .then(CommandManager.literal("lore").executes(ctx -> {
                    StoryManager.showLore(ctx.getSource().getPlayerOrThrow());
                    return 1;
                }))
                .then(CommandManager.literal("goto")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("chapter", IntegerArgumentType.integer(1, 99)).executes(ctx -> {
                            StoryManager.gotoChapter(ctx.getSource().getPlayerOrThrow(), IntegerArgumentType.getInteger(ctx, "chapter"));
                            return 1;
                        }))));
    }

    private HuntCommand() {
    }
}
