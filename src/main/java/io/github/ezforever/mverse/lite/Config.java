package io.github.ezforever.mverse.lite;

public class Config {
    private Config() {
        // Empty
    }

    // ---

    // Allow non-OP players to use "/mv anchor" command, with limitations applied
    public static final boolean COMMAND_AVAILABLE_FOR_NON_OP = true;

    // Allow non-OP players to create anchors to The Nether and The End
    public static final boolean COMMAND_ALLOW_VANILLA_DIMENSIONS = false;

    // Allow non-OP players to use "/mv back" command, with limitations applied
    public static final boolean COMMAND_BACK_AVAILABLE_FOR_NON_OP = true;

    // "/mv back" costs experience like "/mv anchor"
    public static final boolean COMMAND_BACK_COST_EXP = false;

    // "/mv back" clears last death pos on success, making the command single-use only
    // For preventing players hoarding anchors when COMMAND_BACK_COST_EXP is not set
    public static final boolean COMMAND_BACK_CLEAR_POS = true;

    // Experience level cost for each anchor for non-Creative players, 0 == disable
    public static final int COMMAND_EXP_LEVEL_PER_ANCHOR = 3; // 5?

    // Chance of a single-use anchor being "food-grade" (i.e. edible), <= 0.0f == disable
    // XXX: Due to MC mechanics a food item cannot serve other function, rendering "food-grade" anchors unusable
    public static final float COMMAND_FOOD_GRADE_CHANCE = 0.0f; // 0.02f;

    // ---

    // Allow normal Lodestone Compasses (in addition to anchors) to be used for teleportation
    public static final boolean TELEPORT_ALLOW_COMPASSES = true;

    // Delay between using the compass and commencing teleportation, in game ticks
    public static final int TELEPORT_WIND_UP = 50; //20 * 3;

    // Cooldown time after teleportation, in game ticks
    public static final int TELEPORT_WIND_DOWN = 20 * 30;

    // Radius of generated platform if no valid destination could be found
    public static final int TELEPORT_PLATFORM_RADIUS = 2;

    // ---

    // Initial radius of particle cloud during teleport wind up
    public static final float AVFX_CLOUD_RADIUS_INITIAL = 1.5f;

    // Final radius of particle cloud during teleport wind up
    public static final float AVFX_CLOUD_RADIUS_FINAL = 0.5f;
}
