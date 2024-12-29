# as: server console
# at: world spawn

execute as @a[gamemode=survival] at @s if dimension mverse:crystal_palace run tag @s add mverse_crystal_palace_visitor
execute as @a[gamemode=survival] at @s if dimension mverse:crystal_palace run gamemode adventure @s

execute as @a[tag=mverse_crystal_palace_visitor] at @s unless dimension mverse:crystal_palace run gamemode survival @s
execute as @a[tag=mverse_crystal_palace_visitor] at @s unless dimension mverse:crystal_palace run tag @s remove mverse_crystal_palace_visitor

