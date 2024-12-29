# mverse-lite

*M'verse Teleportation Tickets, dimensions and more*

> [!NOTE]
> This repository is part of my *Year-end Things Clearance 2024* code dump, which implies the following things:
> 
> 1. The code in this repo is from one of my personal hobby projects (mainly) developed in 2024
> 2. Since it's a hobby project, expect PoC / alpha-quality code, and an *astonishing* lack of documentation
> 3. The project is archived by the time this repo goes public; there will (most likely) be no future updates
> 
> In short, if you wish to use the code here for anything good, think twice.

This is a Fabric mod for Minecraft 1.21, made in June 2024, originally meant for the (short-lived) [r00team](https://github.com/r00t-security-lab) Minecraft server. Also my first attempt to create a content-focused Minecraft mod and datapack.

This mod adds *Teleportation Anchors* into the game. They look and behave like your good-old Lodestone Compasses, but they point to a specific location in the world, instead of a Lodestone. By using it (right click), you consume it, and gets teleported to the pointed location.

You can get a Teleportation Anchor to a specific dimension by issuing command `/mv anchor <dimension>`, or `/mv back` if you just died and want to go back to your death location. Note that using these commands cost your XPs. Or if you're OP on a server, you can generate infinite-use Teleportation Anchors for any player to any dimension without any XP cost.

For fun, this mod has a multi-dimension datapack bundled in, along with a corresponding advancement system. Be careful when you travel, though; while some dimensions are wonderful places for a sightseeing, other dimensions could take an unprepared player out *in seconds*. There are also dimensions that love players too much to let them leave, so best of luck.

For any server mod that wish to host a server with this mod, this mod is especially designed to be server-side only, which means you could just put it on any Fabric-enabled server and vanilla clients could also enjoy the game. Many aspects affecting gameplay is configurable, e.g. teleportation cooldown, command permissions, etc., but there are no config interfaces and/or files (remember when I say "hobby project" and "alpha-quality code"?); you will have to edit `Config.java` and recompile.

The mod jar also serves as a resource pack for translations (only English and Simplified Chinese are currently available); you can put a link to it as `resource-pack` in `server.properties`, and clients (after allowing it to load) would show proper translations. The mod will work perfectly fine without it though; just that everything added by this mod will be in English.

Lastly, about the name of this mod: "M'verse" is the common part of words "multiverse" and "metaverse" (that word is still in trend, right?), and also a satire to the name of a hot but (subjectively) vulgar movie which I shall not mention here.

