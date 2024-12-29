# as: the player who triggered the advancement
# at: world spawn

tag @s add mverse_silent_hill_wither
schedule function mverse:silent_hill/event_wither_tick 1t replace

advancement revoke @s only mverse:silent_hill/event_wither

