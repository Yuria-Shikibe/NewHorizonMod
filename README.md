# NEWHORIZON MOD

![Logo](github-pictures/ui/logo.png)

****A Java Mindustry mod that works on Android and PC.****

**Powered by *Yuria*.**

## Server
`mindustry.xyz:10704`

######For foreigners (foreign countries relative to China)
Most of the players in the server may Chinese when you log in it.

Well, you would feel it is impossible to communicate with them because as I know many of the player in the Chinese game community of Mindustry (especially in my place) do not use English often or not fluent in it ~~just likes me~~. However, I believe there must exist players who are good at English and willing to translate what you say to others then replied you.

But if that doesn't happen, making everything function all right and don't demolish buildings at will are enough. I believe every player can get on well with each other ~~in the case of someone wouldn't blow up a reactor or break other's blueprint~~.

Don't remember to invite your friends to try this mod, because you guys can use the amount of non-Chinese speaking players to turn the situation. ~~Also if Chinese players don't quit, this may be a fantastic chance for them to learn English~~(~~Maaaaaaaybeeeeeee~~). 

Have a nice day.

## Cautions
**MOD DOES NOT SUPPORT _IOS_**

**If you are using a PC**, mod require install `jdk 14` or maybe jdk in other versions(**_at least 8_**).

If you download from `Action`, unzip the Mod.zip file first, then install the mod in game;

Mod is ***unstable***(mainly for phones, which are some UI and load problems) and is ***WORK IN PROGRESS***.

All codes are here, complete open source, ~~and so no code that has *safety* problems could be hid~~.

## In Game Settings
This mod adds a new setting dialog when start the game.
By choose the available setting, you can activate in-game debug panel and advance load, which creates outline icons and unit full icons automatically.
However, the advance load now causes stuck problem when the game is loaded on a phone. So for your gaming experience, I made it defaults false. If you are confident with your device, active it.
If you find your device cannot afford it, open the mod file folder and find the "new-horizon" folder then open the properties file in it, and then rewrite the "@active.advance-load*" to false, then open the game again.

**However, when the mod is deleted, the setting file wouldn't be deleted automatically, so before I solve this problem, you may have to delete it by yourself.**

## MOD Guide

### Block Guide

---
#### Scalable Turret & Upgrade Block

---

###### Example:

![end-of-era](assets/sprites/blocks/turrets/end-of-era.png)  
- **_Turret:_** End of Era

![eoe-upgrader](assets/sprites/blocks/special/EOE/end-of-era-upgrader.png)  
- **_Upgrader:_** End of Era Upgrader

###### Use Steps:
1. Click the `Upgrader`.
2. Click target turret, which must be `Scalable`.
   
   if all things go right, you can see the link sign between two buildings.
   ![guide-link](github-pictures/guide/link-upgrade.png)
3. Click the `Upgrader`.
4. Click button `Upgrade`.
   if all things go right, you can see the table of all the upgrade options.
5. Select the option which you want to upgrade.
   ![guide-upgrade](github-pictures/guide/ui-upgrade.png)
   
- Upgrade needs resource, and the upgrader can take the resource directly from the `Core`. ~~Nice and simple, right?~~
6. Make the turret function correctly just likes other turrets.
---
#### Mass Deliver

---
![mass-deliver](assets/sprites/blocks/special/mass-deliver.png)
- **_Deliver:_** Mass Deliver

###### Use Steps:
1. Click the building.
2. Select the mod, `input` or `output`.
   If `input`, choose the item you want to take in the table.
   
   You can choose multiple item at the same time.
   
   ![guide-mass-deliver](github-pictures/guide/ui-deliver.png)
3. Select the target just like doing it on the `Mass Driver`.

---
#### Jump Gate

---
![jump-gate](assets/sprites/blocks/special/jump-gate.png)
![jump-gate-junior](assets/sprites/blocks/special/jump-gate-junior.png)
- **_JumpGate:_** Senior Jump Gate; Junior Jump Gate

###### Use Steps:
1. Click the building.
2. Click the button `Spawn`.
   Then you will see the spawn dialog.
   ![guide-jump-gate](github-pictures/guide/ui-jump-gate.png)
3. Select the spawn gangplank just like doing it on the `Mass Driver`.
   
- Spawn units needs resource, and the jump gate can take the resource directly from the `Core`.
- While placing the `Senior Jump Gate`, the `Junior Jump Gate` is required as a base.

---
#### Player Jump Gate

---
![player-jump-gate](assets/sprites/blocks/special/player-jump-gate.png)
- **_JumpGate:_** Senior Jump Gate; Junior Jump Gate

###### Use Steps:
1. Click the building.
   
2. Make sure the building isn't locked(You can get and switch the mode through the left button), then tap another `Player Jump Gate` to link.
2. Make sure you are using a flyable unit and get close enough to the building, then click the button `Teleport` to teleport to the link building.

- Has reloaded time.
- Requires power to function.
- Cannot be placed in the server, ~~as you can see that text while placing~~.

