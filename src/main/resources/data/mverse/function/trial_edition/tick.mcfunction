# as: server console
# at: world spawn

execute as @a[gamemode=survival] at @s if dimension mverse:trial_edition run tag @s add mverse_trial_edition_visitor
execute as @a[gamemode=survival] at @s if dimension mverse:trial_edition run gamemode adventure @s

execute as @a[tag=mverse_trial_edition_visitor] at @s unless dimension mverse:trial_edition run gamemode survival @s
execute as @a[tag=mverse_trial_edition_visitor] at @s unless dimension mverse:trial_edition run tag @s remove mverse_trial_edition_visitor

# ---

execute as @a[tag=!mverse_trial_edition_challenger] at @s if dimension mverse:trial_edition unless entity @e[type=minecraft:evoker_fangs,tag=mverse_trial_edition_init] run function mverse:trial_edition/mech_init

execute as @a[tag=mverse_trial_edition_challenger] at @s unless dimension mverse:trial_edition run tag @s remove mverse_trial_edition_challenger
execute in mverse:trial_edition as @a[tag=mverse_trial_edition_challenger,x=-16,y=128,z=-16,dx=32,dy=128,dz=32] run tag @s remove mverse_trial_edition_challenger

scoreboard players reset @a[tag=!mverse_trial_edition_challenger] mverse_trial_edition_score

