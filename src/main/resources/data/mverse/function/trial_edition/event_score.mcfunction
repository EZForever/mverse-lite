# as: the player who triggered the advancement
# at: world spawn

scoreboard players add @s mverse_trial_edition_score 1

execute if score @s mverse_trial_edition_score matches 10.. run advancement grant @s only mverse:trial_edition/score_1
execute if score @s mverse_trial_edition_score matches 30.. run advancement grant @s only mverse:trial_edition/score_2
execute if score @s mverse_trial_edition_score matches 50.. run advancement grant @s only mverse:trial_edition/score_3

advancement revoke @s only mverse:trial_edition/event_score

