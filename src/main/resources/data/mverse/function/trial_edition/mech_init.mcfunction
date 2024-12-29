# as: the first player to start a teleport
# at: @s

summon minecraft:evoker_fangs 0 128 0 {Tags:["mverse_trial_edition_init"],Warmup:300}
title @s actionbar {"type": "translatable", "translate": "mverse.ui.waiting_for_teammates", "fallback": "🗡 Waiting for your teammates 🗡", "color": "yellow"}

schedule function mverse:trial_edition/mech_hint 12s replace
schedule function mverse:trial_edition/mech_teleport 15s replace

