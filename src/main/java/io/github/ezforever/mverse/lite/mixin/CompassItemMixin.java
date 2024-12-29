package io.github.ezforever.mverse.lite.mixin;

import io.github.ezforever.mverse.lite.Avfx;
import io.github.ezforever.mverse.lite.Config;
import io.github.ezforever.mverse.lite.PendingTargetSetter;
import io.github.ezforever.mverse.lite.Translations;
import io.github.ezforever.mverse.lite.command.AnchorCommand;
import net.minecraft.component.Component;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Set;

@Mixin(CompassItem.class)
public abstract class CompassItemMixin extends Item {
    public CompassItemMixin(Settings settings) {
        super(settings);
    }

    // Revoke all modified anchors on sight
    // NOTE: Ideally non-tracking Lodestone Compasses should not work on Lodestones
    // But without control to client code that does not work
    @Inject(method = "inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V", at = @At("RETURN"))
    public void onReturnInventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if(world.isClient)
            return;

        LodestoneTrackerComponent tracker = stack.get(DataComponentTypes.LODESTONE_TRACKER);
        if(tracker == null || !tracker.tracked())
            return;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(nbt == null || !nbt.contains(AnchorCommand.ANCHOR_MARKER))
            return;

        if(entity instanceof ServerPlayerEntity player) {
            Avfx.onItemBreak(player, stack);
        }
        stack.setCount(0);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if(itemStack.contains(DataComponentTypes.FOOD))
            return super.use(world, user, hand);

        LodestoneTrackerComponent tracker = itemStack.get(DataComponentTypes.LODESTONE_TRACKER);
        if(tracker == null)
            return super.use(world, user, hand);

        NbtComponent nbt = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if(!Config.TELEPORT_ALLOW_COMPASSES && (nbt == null || !nbt.contains(AnchorCommand.ANCHOR_MARKER)))
            return super.use(world, user, hand);

        // Don't do any of the following logic on client
        if(world.isClient)
            return TypedActionResult.success(itemStack);

        // Empty target == lodestone is broken (See CompassItem.inventoryTick)
        if(tracker.target().isEmpty()) {
            user.sendMessage(Translations.TELEPORT_TARGET_NOT_VALID.get());
            return TypedActionResult.success(itemStack);
        }

        GlobalPos target = tracker.target().get();
        MinecraftServer server = Objects.requireNonNull(world.getServer());
        ServerWorld targetWorld = server.getWorld(target.dimension());
        if(targetWorld == null) {
            // Rare but could happen (e.g. mistyped command, data pack change, etc.)
            user.sendMessage(Translations.TELEPORT_TARGET_NOT_VALID.get());
            return TypedActionResult.success(itemStack);
        }

        // ---

        user.getItemCooldownManager().set(this, Config.TELEPORT_WIND_UP);
        ((PendingTargetSetter)user).mverse_lite$setPendingTarget(target);

        // Curse of Vanishing == one time use, also for commands
        if(EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
            itemStack.decrementUnlessCreative(1, user);
        }

        ServerPlayerEntity player = (ServerPlayerEntity)user;
        Avfx.onBeforeTeleport(player, player.getServerWorld() == targetWorld);

        return TypedActionResult.success(itemStack);
    }

    // ---

    @Unique
    private static final ThreadLocal<Set<Component<?>>> workingComponents = new ThreadLocal<>();
}