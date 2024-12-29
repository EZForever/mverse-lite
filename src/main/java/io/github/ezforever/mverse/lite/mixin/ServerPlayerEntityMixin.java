package io.github.ezforever.mverse.lite.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ezforever.mverse.lite.*;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PendingTargetSetter {
    @Shadow public abstract void sendMessage(Text message);

    @Shadow public abstract ServerWorld getServerWorld();

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Unique
    private GlobalPos pendingTarget;

    @Inject(method = "tick()V", at = @At("HEAD"))
    public void onHeadTick(CallbackInfo ci) {
        if(this.getWorld().isClient)
            return; // XXX: Is this necessary?

        if(pendingTarget == null || this.getItemCooldownManager().isCoolingDown(Items.COMPASS))
            return;

        MinecraftServer server = Objects.requireNonNull(this.getServer());
        ServerWorld targetWorld = Objects.requireNonNull(server.getWorld(pendingTarget.dimension()));

        ServerWorld beforeWorld = this.getServerWorld();
        Avfx.onCommenceTeleport((ServerPlayerEntity)(Object)this, beforeWorld == targetWorld);

        Vec3d targetPos = null;
        BlockPos targetBlockPos = pendingTarget.pos();
        WorldBorder worldBorder = targetWorld.getWorldBorder();
        if(!targetWorld.isOutOfHeightLimit(targetBlockPos) && worldBorder.contains(targetBlockPos)) {
            // Target pos is likely sane; assume lodestone, and teleport near it with respawn anchor logic
            targetPos = RespawnAnchorBlock.findRespawnPosition(this.getType(), targetWorld, targetBlockPos).orElse(null);

            // Target pos is less likely sane; assume command-generated compass, and test the immediate position
            if(targetPos == null && Mod.isBlockPosValid(targetWorld, targetBlockPos.down()))
                targetPos = Vec3d.ofBottomCenter(targetBlockPos);

            // Target pos is not-so-sane; namely the near vicinity of the lodestone is fully obstructed
            // Send the player to world spawn instead
            if(targetPos == null)
                this.sendMessage(Translations.TELEPORT_TARGET_OBSTRUCTED.get());
        }

        if(targetPos == null) {
            // Target pos is out of this world, or at least out of play field (e.g. command-generated compasses with invalid Y value)
            // Teleport to world spawn (or "equivalent" position) instead
            targetPos = Vec3d.ofBottomCenter(Mod.getWorldSpawn(targetWorld, true));
        }

        // Teleport stuffs
        this.teleport(targetWorld, targetPos.getX(), targetPos.getY(), targetPos.getZ(), Set.of(), this.getHeadYaw(), this.getPitch());
        if(this.isFallFlying()) {
            this.setVelocity(this.getVelocity().multiply(1.0, 0.0, 1.0));
            this.setOnGround(true);
        }

        // NOTE: Do this after teleport since switching world resets cooldown
        this.getItemCooldownManager().set(Items.COMPASS, Config.TELEPORT_WIND_DOWN);
        pendingTarget = null;

        Avfx.onAfterTeleport((ServerPlayerEntity)(Object)this, beforeWorld == targetWorld);
    }

    // --- Implements PendingTargetSetter

    @Override
    public void mverse_lite$setPendingTarget(GlobalPos value) {
        pendingTarget = value;
    }
}
