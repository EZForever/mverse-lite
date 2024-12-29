# as: server console
# at: world spawn

execute as @a[tag=mverse_silent_hill_wither] at @s at @e[type=minecraft:wither,distance=..16] run summon lightning_bolt
execute as @a[tag=mverse_silent_hill_wither] at @s at @e[type=minecraft:wither,distance=..16] if entity @e[type=minecraft:warden,distance=..16] run advancement grant @s only mverse:silent_hill/anime_fight

execute as @a[tag=mverse_silent_hill_wither] run tag @s remove mverse_silent_hill_wither

