package io.github.ezforever.mverse.lite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.ezforever.mverse.lite.Config;
import io.github.ezforever.mverse.lite.Mod;
import io.github.ezforever.mverse.lite.Translations;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.*;

// 1. Generate Teleportation Anchors to "dimension" at "pos" (or world spawn if not specified) for "targets"
// Non-OP (vanilla dims other than Overworld cannot be selected):
// - /mv anchor <target>
// OP/Console:
// - /mv anchor <dimension> [singleUse=true] [targets=@s]
// - /mv anchor <dimension> <pos> [singleUse=true] [targets=@s]

// 2. Generate a single-use Teleportation Anchor to the last death position for each player in "targets"
// Non-OP (vanilla dims other than Overworld cannot be selected):
// - /mv back
// OP/Console:
// - /mv back [targets=@s]

public class AnchorCommand {
    private AnchorCommand() {
        // Empty
    }

    // ---

    private static final SimpleCommandExceptionType DISALLOWED_DIMENSION = new SimpleCommandExceptionType(Translations.COMMAND_DISALLOWED_DIMENSION.get());
    private static final DynamicCommandExceptionType INSUFFICIENT_EXP_LEVEL = new DynamicCommandExceptionType(Translations.COMMAND_INSUFFICIENT_EXP_LEVEL::get);
    private static final DynamicCommandExceptionType DEATH_POS_NOT_AVAILABLE = new DynamicCommandExceptionType(Translations.DEATH_POS_NOT_AVAILABLE::get);
    private static final Style LORE_STYLE = Style.EMPTY.withColor(Formatting.BLUE).withItalic(false);

    public static final String ANCHOR_MARKER = "mverse_lite_anchor";
    public static final int ANCHOR_VERSION = 1;

    private static int execute(ServerCommandSource source, ServerWorld dimension, BlockPos pos, boolean singleUse, Collection<ServerPlayerEntity> targets, boolean isBack) throws CommandSyntaxException {
        // Disallow non-OP players from getting anchors to The Nether or The End
        if(!source.hasPermissionLevel(2) && !isBack && !Config.COMMAND_ALLOW_VANILLA_DIMENSIONS) {
            RegistryKey<World> registryKey = dimension.getRegistryKey();
            if(registryKey.getValue().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && !registryKey.equals(ServerWorld.OVERWORLD))
                throw DISALLOWED_DIMENSION.create();
        }

        if(source.isExecutedByPlayer() && Config.COMMAND_EXP_LEVEL_PER_ANCHOR != 0) {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if(!player.isCreative() && !(isBack && !Config.COMMAND_BACK_COST_EXP)) {
                int requiredLevels = Config.COMMAND_EXP_LEVEL_PER_ANCHOR * targets.size();
                if(player.experienceLevel < requiredLevels)
                    throw INSUFFICIENT_EXP_LEVEL.create(Config.COMMAND_EXP_LEVEL_PER_ANCHOR);

                player.addExperienceLevels(-requiredLevels);
            }
        }

        ItemStack itemStack = new ItemStack(Items.COMPASS);

        // Set target position
        GlobalPos globalPos;
        if(pos == null) {
            // NOTE: Getting world spawn here is only for visuals
            BlockPos spawnPos = Mod.getWorldSpawn(dimension, false);
            globalPos = GlobalPos.create(dimension.getRegistryKey(), new BlockPos(spawnPos.getX(), dimension.getBottomY() - 1, spawnPos.getZ()));
        } else {
            globalPos = GlobalPos.create(dimension.getRegistryKey(), pos);
        }
        itemStack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(globalPos), false));

        // Anchor markers
        NbtCompound nbt = new NbtCompound();
        nbt.putInt(ANCHOR_MARKER, ANCHOR_VERSION);
        itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        if(singleUse) {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            builder.add(source.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.VANISHING_CURSE).orElseThrow(), 1);
            itemStack.set(DataComponentTypes.ENCHANTMENTS, builder.build().withShowInTooltip(false));
        }

        // Visuals
        // NOTE: Enchantments used for single-use anchors up the rarity for one level
        itemStack.set(DataComponentTypes.ITEM_NAME, Translations.ANCHOR_NAME.get());
        itemStack.set(DataComponentTypes.RARITY, singleUse ? Rarity.UNCOMMON : Rarity.RARE);

        // Fine print on anchors
        List<Text> lore = new ArrayList<>();
        String dimensionNameKey = dimension.getRegistryKey().getValue().toTranslationKey("mverse.dimension");
        MutableText dimensionName = Translations.get(dimensionNameKey);
        if(pos == null) {
            lore.add(dimensionName.fillStyle(LORE_STYLE));
        } else {
            ChunkPos chunkPos = new ChunkPos(pos);
            lore.add(Translations.ANCHOR_DIMENSION_AND_POS.get(dimensionName, chunkPos.x, chunkPos.z).fillStyle(LORE_STYLE));
        }
        if(singleUse)
            lore.add(Translations.ANCHOR_SINGLE_USE.get().fillStyle(LORE_STYLE));
        itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        // Distribute copies of the anchor, dropping them if necessary
        for(ServerPlayerEntity target : targets) {
            ItemStack itemStackCopy = itemStack.copy();
            if(target.getRandom().nextFloat() < Config.COMMAND_FOOD_GRADE_CHANCE) {
                LoreComponent newLore = Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE));
                newLore = newLore.with(Translations.ANCHOR_FOOD_GRADE.get().fillStyle(LORE_STYLE));

                itemStackCopy.set(DataComponentTypes.FOOD, new FoodComponent.Builder().alwaysEdible().build());
                itemStackCopy.set(DataComponentTypes.LORE, newLore);
            }

            if(!target.getInventory().insertStack(itemStackCopy)) {
                ItemEntity itemEntity = target.dropItem(itemStack, false);
                if(itemEntity == null)
                    continue;

                itemEntity.resetPickupDelay();
                itemEntity.setOwner(target.getUuid());
            }
            target.currentScreenHandler.sendContentUpdates();
        }

        // XXX: Use custom text instead?
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.give.success.single", 1, itemStack.toHoverableText(), targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.give.success.multiple", 1, itemStack.toHoverableText(), targets.size()), true);
        }
        return targets.size();
    }

    private static int execute(ServerCommandSource source, ServerWorld dimension, BlockPos pos, boolean singleUse, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        return execute(source, dimension, pos, singleUse, targets, false);
    }

    private static int execute(ServerCommandSource source, ServerWorld dimension, BlockPos pos, boolean singleUse) throws CommandSyntaxException {
        return execute(source, dimension, pos, singleUse, Collections.singleton(source.getPlayerOrThrow()));
    }

    private static int execute(ServerCommandSource source, ServerWorld dimension, BlockPos pos) throws CommandSyntaxException {
        return execute(source, dimension, pos, true);
    }

    private static int execute(ServerCommandSource source, ServerWorld dimension) throws CommandSyntaxException {
        return execute(source, dimension, null);
    }

    private static int executeBack(ServerCommandSource source, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        int result = 0;
        CommandSyntaxException pendingException = null;
        for(ServerPlayerEntity target : targets) {
            int targetResult = 0;
            try {
                GlobalPos deathPos = target.getLastDeathPos().orElseThrow(() -> DEATH_POS_NOT_AVAILABLE.create(target.getDisplayName()));
                targetResult = execute(source, source.getServer().getWorld(deathPos.dimension()), deathPos.pos(), true, Collections.singleton(target), true);
            } catch (CommandSyntaxException exc) {
                if(pendingException == null)
                    pendingException = exc;
            }

            if(targetResult > 0 && Config.COMMAND_BACK_CLEAR_POS) {
                target.setLastDeathPos(Optional.empty());
            }

            result += targetResult;
        }

        if(pendingException != null)
            throw pendingException;
        return result;
    }

    private static int executeBack(ServerCommandSource source) throws CommandSyntaxException {
        return executeBack(source, Collections.singleton(source.getPlayerOrThrow()));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("mv")
                        .then(
                                CommandManager.literal("anchor")
                                        .then(
                                                CommandManager.argument("target", DimensionArgumentType.dimension())
                                                        .requires(source -> Config.COMMAND_AVAILABLE_FOR_NON_OP && !source.hasPermissionLevel(2) && !(source.isExecutedByPlayer() && Objects.requireNonNull(source.getPlayer()).isSpectator()))
                                                        .executes(context -> AnchorCommand.execute(
                                                                context.getSource(),
                                                                DimensionArgumentType.getDimensionArgument(context, "target")
                                                        ))
                                        )
                                        .then(
                                                CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                                        .requires(source -> source.hasPermissionLevel(2))
                                                        .executes(context -> AnchorCommand.execute(
                                                                context.getSource(),
                                                                DimensionArgumentType.getDimensionArgument(context, "dimension")
                                                        ))
                                                        .then(
                                                                CommandManager.argument("singleUse", BoolArgumentType.bool())
                                                                        .executes(context -> AnchorCommand.execute(
                                                                                context.getSource(),
                                                                                DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                                                null,
                                                                                BoolArgumentType.getBool(context, "singleUse")
                                                                        ))
                                                                        .then(
                                                                                CommandManager.argument("targets", EntityArgumentType.players())
                                                                                        .executes(context -> AnchorCommand.execute(
                                                                                                context.getSource(),
                                                                                                DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                                                                null,
                                                                                                BoolArgumentType.getBool(context, "singleUse"),
                                                                                                EntityArgumentType.getPlayers(context, "targets")
                                                                                        ))
                                                                        )
                                                        )
                                                        .then(
                                                                CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                                        .executes(context -> AnchorCommand.execute(
                                                                                context.getSource(),
                                                                                DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                                                BlockPosArgumentType.getBlockPos(context, "pos")
                                                                        ))
                                                                        .then(
                                                                                CommandManager.argument("singleUse", BoolArgumentType.bool())
                                                                                        .executes(context -> AnchorCommand.execute(
                                                                                                context.getSource(),
                                                                                                DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                                                                BlockPosArgumentType.getBlockPos(context, "pos"),
                                                                                                BoolArgumentType.getBool(context, "singleUse")
                                                                                        ))
                                                                                        .then(
                                                                                                CommandManager.argument("targets", EntityArgumentType.players())
                                                                                                        .executes(context -> AnchorCommand.execute(
                                                                                                                context.getSource(),
                                                                                                                DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                                                                                BlockPosArgumentType.getBlockPos(context, "pos"),
                                                                                                                BoolArgumentType.getBool(context, "singleUse"),
                                                                                                                EntityArgumentType.getPlayers(context, "targets")
                                                                                                        ))
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );

        // ---

        dispatcher.register(
                CommandManager.literal("mv")
                        .then(
                                CommandManager.literal("back")
                                        .requires(source -> source.hasPermissionLevel(2) || (Config.COMMAND_BACK_AVAILABLE_FOR_NON_OP && !(source.isExecutedByPlayer() && Objects.requireNonNull(source.getPlayer()).isSpectator())))
                                        .executes(context -> AnchorCommand.executeBack(
                                                context.getSource()
                                        ))
                                        .then(
                                                CommandManager.argument("targets", EntityArgumentType.players())
                                                        .requires(source -> source.hasPermissionLevel(2))
                                                        .executes(context -> AnchorCommand.executeBack(
                                                                context.getSource(),
                                                                EntityArgumentType.getPlayers(context, "targets")
                                                        ))
                                        )
                        )
        );
    }
}
