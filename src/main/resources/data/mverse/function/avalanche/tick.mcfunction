# as: server console
# at: world spawn

execute in mverse:avalanche as @e[type=minecraft:villager] run data merge entity @s {TicksFrozen:0}
execute in mverse:avalanche as @e[type=minecraft:iron_golem] run data merge entity @s {TicksFrozen:0}
execute in mverse:avalanche as @e[type=minecraft:illusioner] run data merge entity @s {TicksFrozen:0}

execute at @a[gamemode=!spectator] if dimension mverse:avalanche run effect give @e[type=minecraft:illusioner,distance=0..10] minecraft:glowing 10 0 true

