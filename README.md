
![quicksell](https://raw.githubusercontent.com/CCMG-Minecraft/QuickSellReloaded/2a940e0ae7258f6ed73dfda7f6a17ef9078602ae/.github/readme/name.png)  
<div style="text-align: center;">  
<img src="https://img.shields.io/github/workflow/status/CCMG-Minecraft/QuickSellReloaded/Java%20CI?style=for-the-badge" alt="Badge">  
<img alt="Badge" src="https://img.shields.io/github/v/release/CCMG-Minecraft/QuickSellReloaded?style=for-the-badge">  
<img alt="Badge" src="https://img.shields.io/github/issues/CCMG-Minecraft/QuickSellReloaded?style=for-the-badge">  
<img alt="Badge" src="https://img.shields.io/github/license/CCMG-Minecraft/QuickSellReloaded?style=for-the-badge">  
<img alt="Badge" src="https://img.shields.io/badge/Minecraft%20Version-1.16-darkgreen?style=for-the-badge">  
<img alt="Badge" src="https://img.shields.io/badge/Recommended_Software-PaperMC-blue?style=for-the-badge">  
</div>  
  
# About this Plugin  
QuickSell Reloaded is a fork of the plugin [QuickSell](https://github.com/TheBusyBiscuit/QuickSell) by MrCookieSlime - the credits for the original plugin go to him!
QuickSell has since been discontinued in terms of active development. This plugin adds modernised features to the plugin, and fixes underlying issues with the plugin and other bugs.  
  
It's perfect for your prison server to be placed beside mines, and works mostly the same as QuickSell!

# Server Requirements
The plugin should work from any version above **1.8**, but we strongly recommend **1.16**.
Additionally, it should work on CraftBukkit, Spigot or PaperMC, but we __strongly__ recommend that you use PaperMC, for better performance.

You currently need **CS-CoreLib** for this plugin to function, however, in a future update this dependency will be removed.
## Commands
Arguments marked with triangular brackets (e.g. `<argument>`) are **required** for the command.
Arguments marked with square brackets (e.g. `[argument]`) are **optional** for the command.

### /quicksell
**Description**

The main help command - this gives you a list of commands that can be used with the plugin

**Example Usage**

`/quicksell`

**Permission**

`quicksell.help` and/or `quicksell.admin`

### /quicksell reload

**Description**

Reload all configurations, shops and messages. While this command *should* reload everything, we cannot guarantee it.
**Example Usage**

`/quicksell reload`

**Permission**

`quicksell.reload` and/or `quicksell.admin`
### /quicksell editor
**Description**

Open the QuickSell Editor GUI, which allows you to configure shops easily and dynamically with a rich, visual interface.
**Example Usage**

`/quicksell editor`

**Permission**

`quicksell.editor` and/or `quicksell.admin`
### /quicksell stopboosters [player]
**Description**

Stop any boosters either:
- Active on the server
- Active for a specific player

Not providing a player will stop any running booster on the server.

**Example Usage**

`/quicksell stopboosters` *would stop all boosters on the server*
`/quicksell stopboosters Notch` *would stop any booster that affects Notch - including global boosters*

**Permission**

`quicksell.stopboosters` and/or `quicksell.admin`
### /quicksell linknpc \<shop> <sell/sellall>
**Plugin Requirement**

This plugin requires you to have Citizens 2 installed on your server. You can purchase it [here](https://www.spigotmc.org/resources/citizens.13811/), or, you can download it from the Citizens Jenkins server [here](https://ci.citizensnpcs.co/job/Citizens2/).

**Description**

Link an NPC to a shop. This command uses the NPC you currently have selected via Citizens, so to select an NPC for this command, run `/npc select <id>`, or stand beside the NPC and run `/npc sel`.
You should provide a shop name which already exists on the server, and `sell/sellall` determines the *right-click* behaviour of the NPC.
- When you provide `sell` as an argument, right-clicking the NPC will open a GUI where players can deposit items. When the player closes the GUI, all the items in the GUI will be sold to the shop provided. 
- When you provide `sellall` as an argument, right-clicking the NPC will automatically sell all eligible items to the shop provided. (If there's items in the player's inventory which isn't in the shop, it gets ignored).

When you left-click (punch) the NPC after linking it, you will see the prices for the shop the NPC is linked to (how much everything sells for).
**Example Usage**

- `/quicksell linknpc A sellall`
- `/quicksell linknpc B sell`

**Permission**

`quicksell.linknpc` and/or `quicksell.admin`
### /quicksell unlinknpc
**Plugin Requirement**

This plugin requires you to have Citizens 2 installed on your server. You can purchase it [here](https://www.spigotmc.org/resources/citizens.13811/), or, you can download it from the Citizens Jenkins server [here](https://ci.citizensnpcs.co/job/Citizens2/).

**Description**

Remove a linked shop from the NPC you have selected. This command uses the NPC you currently have selected via Citizens, so to select an NPC for this command, run `/npc select <id>`, or stand beside the NPC and run `/npc sel`.

**Example Usage**

`/quicksell unlinknpc`

**Permission**
`quicksell.unlinknpc` and/or `quicksell.admin`

### /sell [shop]
**Description**

Open a GUI for either
- The shop provided in the `[shop]` argument
- The highest shop you have access to, if no shop argument is given.

**Example Usage**
- `/sell`
- `/sell A`

**Permission**

`quicksell.sell`
Additionally, for each shop, you need the permission to access the shop (which is configurable in `config.yml`, but by default is `QuickSell.shop.<shop>`, e.g. `QuickSell.shop.A`)

### /sellall \<shop>
**Description**

Sells all your items to the shop provided.
 
**Example Usage**

`/sellall A`

**Permission**

`quicksell.sellall`
Additionally, for each shop, you need the permission to access the shop (which is configurable in `config.yml`, but by default is `QuickSell.shop.<shop>`, e.g. `QuickSell.shop.A`)
### /prices \<shop>
**Description**

Open a GUI showing how much items in a certain shop is worth.

**Example Usage**

`/prices A`+

**Permission**
`quicksell.prices`
Additionally, for each shop, you need the permission to access the shop (which is configurable in `config.yml`, but by default is `QuickSell.shop.<shop>`, e.g. `QuickSell.shop.A`) **OR** you can give a player the permission `quicksell.prices.anyshop`.

### /booster \<type> \<author> \<multiplier> \<duration>
**Description**

Starts a global booster on the server. The command parameters are as follows:
- `type` defines the type of booster. It can be either:
    - `monetary`
    This grants a certain boost to players when they sell if the multiplier is above 1.0 - for instance, a 1.5x monetary booster would give you an extra 50% whenever you sell to the server.
    - `exp`
    This grants you a boost with experience, and works similarly to monetary in terms of boosters.
    - `all`
    This grants you both EXP and Monetary boosters.
- `author` is the user who 'started' the booster. Whenever a player benefits from a booster, the author is shown in chat.
- `multiplier` is how much of a boost to give (as a multipler). Anything above `1.0` will add positive amounts to the total, and anything less than `1.0` will remove amounts from the total. For example, if you have a monetary booster of 1.5x, and sell items worth `$5,000`, the booster will give you an additional `$2,500` for a total of `$7,500`.
- `duration` is the duration of the booster, expressed in **minutes**.
	- General units here are 60 Minutes in an Hour, 1440 Minutes in a Day and 10080 Minutes in a week.
	- We're working on a way to express more fluid durations for this command.

**Example Usage**

`/booster monetary Notch 1.5 60`
`/booster all Dinnerbone 1.25 1440`

**Permission**

`quicksell.booster` and/or `quicksell.admin`

### /pbooster \<type> \<player> \<multiplier> \<duration>
**Description**

Starts a private booster for a specific player. The command parameters are as follows:
- `type` defines the type of booster. It can be either:
    - `monetary`
    This grants a certain boost to players when they sell if the multiplier is above 1.0 - for instance, a 1.5x monetary booster would give you an extra 50% whenever you sell to the server.
    - `exp`
    This grants you a boost with experience, and works similarly to monetary in terms of boosters.
    - `all`
    This grants you both EXP and Monetary boosters.
- `player` is the player of whom the booster should apply to.
- `multiplier` is how much of a boost to give (as a multipler). Anything above `1.0` will add positive amounts to the total, and anything less than `1.0` will remove amounts from the total. For example, if you have a monetary booster of 1.5x, and sell items worth `$5,000`, the booster will give you an additional `$2,500` for a total of `$7,500`.
- `duration` is the duration of the booster, expressed in **minutes**.
	- General units here are 60 Minutes in an Hour, 1440 Minutes in a Day and 10080 Minutes in a week.
	- We're working on a way to express more fluid durations for this command.

**Example Usage**

`/pbooster monetary Notch 1.5 60`
`/pbooster all Dinnerbone 1.25 1440`

**Permission**

`quicksell.privatebooster` and/or `quicksell.admin`

## YouTube Tutorial
Koz4Christ originally made a video for QuickSell 1.10.10 - it's slightly outdated but gives you a great general idea of how to use the plugin. Click the image below to see it.

[!["Tutorial Video"](http://img.youtube.com/vi/EIZ2wwzhgbM/0.jpg)](http://www.youtube.com/watch?v=EIZ2wwzhgbM)
