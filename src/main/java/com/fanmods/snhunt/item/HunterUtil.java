package com.fanmods.snhunt.item;

import com.fanmods.snhunt.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/** Shared helpers for hunter gear. */
public final class HunterUtil {

    public record Shot(@Nullable LivingEntity target, Vec3d start, Vec3d end) {
    }

    /** Hitscan from the player's eyes, stopping at blocks. */
    public static Shot trace(PlayerEntity player, double range) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d end = start.add(look.multiply(range));
        BlockHitResult blockHit = player.getWorld().raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getPos();
        }
        Box area = player.getBoundingBox().stretch(look.multiply(range)).expand(1.0);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        List<LivingEntity> candidates = player.getWorld().getEntitiesByClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !e.isSpectator());
        for (LivingEntity e : candidates) {
            Optional<Vec3d> hit = e.getBoundingBox().expand(0.3).raycast(start, end);
            if (hit.isPresent()) {
                double d = start.squaredDistanceTo(hit.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
        }
        if (best != null) {
            end = start.add(look.multiply(Math.sqrt(bestDist)));
        }
        return new Shot(best, start, end);
    }

    public static void trail(ServerWorld world, Vec3d from, Vec3d to, ParticleEffect particle) {
        Vec3d diff = to.subtract(from);
        int steps = Math.max(1, (int) (diff.length() * 2));
        for (int i = 1; i <= steps; i++) {
            Vec3d p = from.add(diff.multiply(i / (double) steps));
            world.spawnParticles(particle, p.x, p.y - 0.1, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    /** Finds ammo in offhand first, then the rest of the inventory. Empty if none. */
    public static ItemStack findAmmo(PlayerEntity player, Item ammo) {
        if (player.getOffHandStack().isOf(ammo)) {
            return player.getOffHandStack();
        }
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.isOf(ammo)) {
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isIron(ItemStack stack) {
        return stack.isOf(Items.IRON_SWORD) || stack.isOf(Items.IRON_AXE) || stack.isOf(Items.IRON_SHOVEL)
                || stack.isOf(Items.IRON_PICKAXE) || stack.isOf(Items.IRON_HOE) || stack.isOf(ModItems.MACHETE)
                || stack.isOf(Items.IRON_INGOT);
    }

    public static boolean isBeheader(ItemStack stack) {
        return stack.isOf(ModItems.MACHETE) || stack.getItem() instanceof AxeItem;
    }

    /** The item a living attacker used for a direct melee hit, or EMPTY. */
    public static ItemStack meleeWeapon(net.minecraft.entity.damage.DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity living && source.getSource() == living) {
            return living.getMainHandStack();
        }
        return ItemStack.EMPTY;
    }

    public static void tooltip(Item item, List<Text> tooltip) {
        tooltip.add(Text.translatable(item.getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
    }

    private HunterUtil() {
    }
}
