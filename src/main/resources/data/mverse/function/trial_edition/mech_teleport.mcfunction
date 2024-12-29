# as: server console
# at: world spawn

# XXX: /spreadplayers only the init player to save a predicate
execute as @r[tag=!mverse_trial_edition_init] at @s if dimension mverse:trial_edition run tag @s add mverse_trial_edition_init
execute in mverse:trial_edition run spreadplayers 0 0 0 200 under 60 true @a[tag=mverse_trial_edition_init]

execute as @a[tag=!mverse_trial_edition_challenger] at @s if dimension mverse:trial_edition run tp @s @a[tag=mverse_trial_edition_init,limit=1]
execute as @a[tag=!mverse_trial_edition_challenger] at @s if dimension mverse:trial_edition run tag @s add mverse_trial_edition_challenger

execute as @a[tag=mverse_trial_edition_init] run tag @s remove mverse_trial_edition_init

