# as: server console
# at: world spawn

execute as @a[tag=!mverse_trial_edition_challenger] at @s if dimension mverse:trial_edition run title @s actionbar {"type": "translatable", "translate": "mverse.ui.get_ready", "fallback": "🗡 Get ready... 🗡", "color": "yellow"}

