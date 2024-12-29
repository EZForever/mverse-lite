# as: the player who triggered the advancement
# at: world spawn

scoreboard players add @s mverse_badminton_score 1

execute if score @s mverse_badminton_score matches 10.. run advancement grant @s only mverse:badminton/score_1
execute if score @s mverse_badminton_score matches 30.. run advancement grant @s only mverse:badminton/score_2
execute if score @s mverse_badminton_score matches 50.. run advancement grant @s only mverse:badminton/score_3

advancement revoke @s only mverse:badminton/event_score

