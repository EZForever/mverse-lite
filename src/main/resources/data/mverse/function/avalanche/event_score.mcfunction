# as: the player who triggered the advancement
# at: world spawn

scoreboard players add @s mverse_avalanche_score 1

execute if score @s mverse_avalanche_score matches 10.. run advancement grant @s only mverse:avalanche/score_1
execute if score @s mverse_avalanche_score matches 30.. run advancement grant @s only mverse:avalanche/score_2
execute if score @s mverse_avalanche_score matches 50.. run advancement grant @s only mverse:avalanche/score_3

advancement revoke @s only mverse:avalanche/event_score

