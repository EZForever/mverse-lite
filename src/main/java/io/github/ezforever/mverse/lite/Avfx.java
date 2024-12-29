package io.github.ezforever.mverse.lite;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class Avfx {
    private Avfx() {
        // Empty
    }

    // ---

    // NOTE: Do not use player.playSound(), since for server side source == except

    // Basically LivingEntity.playEquipmentBreakEffects on server side
    public static void onItemBreak(ServerPlayerEntity player, ItemStack itemStack) {
        if(itemStack.isEmpty())
            return;

        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8f, 0.8f + player.getWorld().getRandom().nextFloat() * 0.4f);

        ItemStack itemStackCopy = itemStack.copyWithCount(1);
        Random random = player.getRandom();
        for(int i = 0; i < 5; i++) {
            Vec3d pos = new Vec3d((random.nextDouble() - 0.5) * 0.3, -random.nextDouble() * 0.6 - 0.3, 0.6);
            pos = pos.rotateX((float)(-player.getPitch() * Math.PI / 180));
            pos = pos.rotateY((float)(-player.getYaw() * Math.PI / 180));
            pos = pos.add(player.getX(), player.getEyeY(), player.getZ());

            Vec3d vel = new Vec3d((random.nextDouble() - 0.5) * 0.1, random.nextDouble() * 0.1 + 0.1, 0.0);
            vel = vel.rotateX((float)(-player.getPitch() * Math.PI / 180));
            vel = vel.rotateY((float)(-player.getYaw() * Math.PI / 180));
            vel = vel.add(0.0, 0.05, 0.0);

            player.getServerWorld().spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, itemStackCopy), pos.getX(), pos.getY(), pos.getZ(), 0, vel.getX(), vel.getY(), vel.getZ(), 1.0);
        }
    }

    // Happens on using a Lodestone Compass
    public static void onBeforeTeleport(ServerPlayerEntity player, boolean isSameWorld) {
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(player.getWorld(), player.getX(), player.getY(), player.getZ());
        cloud.setOwner(player);
        cloud.startRiding(player);
        cloud.setParticleType(ParticleTypes.ENCHANT);

        cloud.setWaitTime(0);
        cloud.setDuration(Config.TELEPORT_WIND_UP);
        cloud.setRadius(Config.AVFX_CLOUD_RADIUS_INITIAL);
        cloud.setRadiusOnUse(0.0f);
        cloud.setRadiusGrowth((Config.AVFX_CLOUD_RADIUS_FINAL - Config.AVFX_CLOUD_RADIUS_INITIAL) / Config.TELEPORT_WIND_UP);

        player.getWorld().spawnEntity(cloud);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Config.TELEPORT_WIND_UP, 0, true, true));
        if(!isSameWorld)
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, Config.TELEPORT_WIND_UP + 60, 0, true, false));

        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS);
    }

    // Happens on finishing wind up
    public static void onCommenceTeleport(ServerPlayerEntity player, boolean isSameWorld) {
        //player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.PLAYERS);
    }

    // Happens on starting wind down
    public static void onAfterTeleport(ServerPlayerEntity player, boolean isSameWorld) {
        player.getServerWorld().spawnParticles(ParticleTypes.CLOUD, player.offsetX(0.5), player.getBodyY(0.5), player.offsetZ(0.5), 16, player.getWidth() / 2, player.getHeight() / 2, player.getWidth() / 2, 0.0);

        if(!isSameWorld) {
            player.removeStatusEffect(StatusEffects.DARKNESS);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 30, 0, true, false));
        }

        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.PLAYERS);
    }
}
