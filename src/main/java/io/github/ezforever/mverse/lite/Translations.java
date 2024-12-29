package io.github.ezforever.mverse.lite;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public enum Translations {
    ANCHOR_NAME("mverse.anchor.name"),
    ANCHOR_DIMENSION_AND_POS("mverse.anchor.dimension_and_pos"),
    ANCHOR_SINGLE_USE("mverse.anchor.single_use"),
    ANCHOR_FOOD_GRADE("mverse.anchor.food_grade"),

    COMMAND_DISALLOWED_DIMENSION("mverse.command.disallowed_dimension"),
    COMMAND_INSUFFICIENT_EXP_LEVEL("mverse.command.insufficient_exp_level"),
    DEATH_POS_NOT_AVAILABLE("mverse.command.death_pos_not_available"),

    TELEPORT_TARGET_NOT_VALID("mverse.teleport.target_not_valid"),
    TELEPORT_TARGET_OBSTRUCTED("mverse.teleport.target_obstructed");

    // ---

    public final String key;

    Translations(String key) {
        this.key = key;
    }

    public MutableText get(Object... args) {
        return Translations.get(this.key, args);
    }

    // ---

    public static MutableText get(String key, Object... args) {
        // NOTE: This may seem redundant but is for clients without the mod or resource pack
        String fallback = Text.translatable(key, args).getString();
        return Text.translatableWithFallback(key, fallback, args);
    }
}
