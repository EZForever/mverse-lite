# as: server console
# at: world spawn

execute as @a[gamemode=!spectator] at @s if dimension mverse:sands_of_gold run xp add @s -1 points

schedule function mverse:sands_of_gold/sched_leak 1.5s replace

