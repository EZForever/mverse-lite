package io.github.ezforever.mverse.lite;

import com.mojang.brigadier.CommandDispatcher;
import io.github.ezforever.mverse.lite.command.AnchorCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SideShapeType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.feature.EndPlatformFeature;

import java.util.HashSet;
import java.util.Set;

public class Mod implements ModInitializer {
    private void onCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        AnchorCommand.register(dispatcher);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::onCommandRegistration);
    }

    // ---

    private static boolean isBlockStateNonSolid(World world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isReplaceable() && blockState.getFluidState().isEmpty();
    }

    private static void makeBlockPosValid(ServerWorld world, BlockPos blockPos) {
        Set<BlockPos> toUpdate = new HashSet<>();

        world.breakBlock(blockPos, true);
        toUpdate.add(blockPos);
        world.breakBlock(blockPos.up(), true);
        toUpdate.add(blockPos.up());

        for(BlockPos pos : BlockPos.iterateInSquare(blockPos.down(), Config.TELEPORT_PLATFORM_RADIUS, Direction.EAST, Direction.SOUTH)) {
            if(!isBlockStateNonSolid(world, pos))
                continue;

            world.breakBlock(pos, true);
            world.setBlockState(pos, Blocks.STONE.getDefaultState(), Block.NOTIFY_LISTENERS);
            toUpdate.add(pos);
        }

        for(BlockPos pos : toUpdate)
            world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
    }

    // Get if the space *immediately above* "blockPos" is valid for player to teleport into
    public static boolean isBlockPosValid(World world, BlockPos blockPos) {
        if(world.isOutOfHeightLimit(blockPos.getY() + 2))
            return false;

        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolid(world, blockPos, Direction.UP, SideShapeType.FULL)
                && !(world.getDimension().hasCeiling() && blockState.isOf(Blocks.BEDROCK)) // HACK: Do not teleport onto bedrock ceiling
                && isBlockStateNonSolid(world, blockPos.up())
                && isBlockStateNonSolid(world, blockPos.up(2));
    }

    // Get world spawn or equivalent position for given "world", create or break blocks if necessary
    public static BlockPos getWorldSpawn(ServerWorld world, boolean allowAlteringWorld) {
        if(world.getRegistryKey().equals(World.END)) {
            // Spawn point in The End is fixed (see Entity.getTeleportTarget), and we don't like the high chance of falling into the void
            if(allowAlteringWorld)
                EndPlatformFeature.generate(world, ServerWorld.END_SPAWN_POS.down(), true);
            return ServerWorld.END_SPAWN_POS;
        }

        // For other dimensions we use simplified nether portal logic (see Entity.getTeleportTarget and PortalForcer.createPortal)
        BlockPos blockPos;
        if(world.getRegistryKey().equals(World.OVERWORLD)) {
            // "The world spawn" is a single value for Overworld only
            blockPos = world.getSpawnPos();
        } else {
            // Other dimensions have to make do with the center of world border
            WorldBorder worldBorder = world.getWorldBorder();
            int spawnHeight = world.getChunkManager().getChunkGenerator().getSpawnHeight(world);
            blockPos = BlockPos.ofFloored(worldBorder.getCenterX(), spawnHeight, worldBorder.getCenterZ());
        }

        double distance = -1.0;
        int logicalHeight = world.getBottomY() + world.getLogicalHeight() - 1;
        for(BlockPos.Mutable pos : BlockPos.iterateInSquare(blockPos, 16, Direction.EAST, Direction.SOUTH)) {
            int posHeight = Math.min(world.getTopY(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()), logicalHeight);
            for(int y = posHeight; y >= world.getBottomY(); y--) {
                pos.setY(y);
                if(!isBlockPosValid(world, pos))
                    continue;

                double newDistance = blockPos.getSquaredDistance(pos);
                if(distance == -1.0 || newDistance < distance) {
                    blockPos = pos.up();
                    distance = newDistance;
                }
            }
        }

        if(distance == -1.0) {
            // No suitable place was ever found; force position according to nether portal rules, break blocks if necessary
            int minY = Math.max(world.getBottomY() + 1, 70);
            int maxY = logicalHeight - 10;
            if(minY > maxY) {
                // Original impl errors out in this scenario, but we cannot do that
                int temp = minY;
                minY = maxY;
                maxY = temp;
            }
            blockPos = new BlockPos(blockPos.getX(), Math.clamp(blockPos.getY(), minY, maxY), blockPos.getZ());

            // "Yo so fat yo telefrag blocks"
            if(allowAlteringWorld)
                makeBlockPosValid(world, blockPos);
        }

        return blockPos;
    }
}
