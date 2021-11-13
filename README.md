# NEWHORIZON MOD

![Logo](github-pictures/logo.png)

****A Java Mindustry<V7> mod for Android and PC.****

**Powered by *Yuria*.**

## Community
[![Discord](https://img.shields.io/discord/825801593466388520.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=7289DA&label=Discord&color=17cf48)](https://discord.gg/yNmbMcuwyW)

## Server
`n4.mcyxsj.top:20177`

###### For foreigners (foreign countries relative to China)

First, you may need a __VPN Service__ to have a better play experience, ~~since that big fucking gaint net wall stands there, around China.~~

Most of the players in the server may speak Chinese when you log in.

Well, you may feel it is impossible to communicate with them because as I know, many of the players in the Chinese Mindustry community (especially in my place) do not speak English fluently. However, I believe there must be players who are good at English that are willing to translate what you say to others, then translate what is replied to you.

But if that doesn't happen, making everything function right and not demolishing buildings at will are enough. I believe every player can get along well with one another ~~as long as no one blows up a reactor or breaks someone else's blueprint~~.

Don't forget to invite your friends to try this mod, because you guys can use the amount of non-Chinese speaking players to turn the situation. ~~Also if Chinese players don't quit, this may be a fantastic chance for them to learn English~~ (~~Maaaaaaaybeeeeeee~~). 

Have a nice day.

## Caution
**MOD DOES NOT SUPPORT _IOS_**

**If you are using a PC**, mod requires `JDK/JVM` installed, or maybe other versions (**_at least 8_**).

If you downloaded from `Action`, unzip the Mod.zip file first, then install the mod in-game;

Here are the `Requirements` override code, which I believe it wouldn't cause conflict between multiple mods. Still, I give a setting to disable the override.

```java
public class NHOverride{
    /*Override detail code...*/
    private static void addReq(Block target, ItemStack... items){
       ItemStack[] newReq = new ItemStack[items.length + target.requirements.length];
       
       System.arraycopy(target.requirements, 0, newReq, 0, target.requirements.length);
       System.arraycopy(items, 0, newReq, target.requirements.length, items.length);

       target.requirements = newReq;
       Arrays.sort(target.requirements, Structs.comparingInt((j) -> j.item.id));
    }

   private static void removeReq(Block target, Item... items){
      Seq<ItemStack> req = new Seq<>(ItemStack.class);
      req.addAll(target.requirements);

      for(Item item : items)req.each(itemReq -> itemReq.item == item, req::remove);

      target.requirements = req.shrink();
   }
}
```

## Custom Cutscene Script
[Get the information](https://github.com/Yuria-Shikibe/NewHorizonMod/wiki/Cutscene-Script-Custom-Guide)

## MOD Guide

### Block Guide

---
#### Scalable Turret & Upgrade Block

---

###### Example:

![end-of-era](assets/sprites/blocks/turret/end-of-era.png)  
- **_Turret:_** End of Era

![eoe-upgrader](assets/sprites/blocks/upgrade/end-of-era-upgrader.png)  
- **_Upgrader:_** End of Era Upgrader

###### Use Steps:
1. Click the `Upgrader`.
2. Click target turret, which must be `Scalable`.
   
   if everything goes correctly, you will see the link sign between the two buildings.
   ![guide-link](github-pictures/guide/link-upgrade.png)
3. Click the `Upgrader`.
4. Click button `Upgrade`.
   if everything goes correctly, you will see the table of all the upgrade options.
5. Select the option you want to upgrade.
   ![guide-upgrade](github-pictures/guide/ui-upgrade.png)
   
- Upgrade needs resources. The upgrader can take resources directly from the `Core`. ~~Nice and simple, right?~~
6. Make the turret function correctly just like other turrets.
---

#### Jump Gate

---
![jump-gate](assets/sprites/blocks/special/jump-gate.png)
![jump-gate-junior](assets/sprites/blocks/special/jump-gate-junior.png)
![jump-gate-primary](assets/sprites/blocks/special/jump-gate-primary.png)

- **_JumpGate:_** Senior Jump Gate; Junior Jump Gate

###### Use Steps:
1. Click the building.
2. Click the `Spawn` button.
   Then you will see the spawn dialog.
   ![guide-jump-gate](github-pictures/guide/ui-jump-gate.png)
3. Select the plus icon to summon the unit.
   
- Spawning units requires resource, which the jump gate can take the resource directly from the `Core`.
- To place the `Senior Jump Gate`, the `Junior Jump Gate` is required as a base.
- A new auto-spawn system has been added since 1.7.8, which allows you to spawn specific unit when wave passed by. This also works for enemies, so you guys can use it to make some interesting maps. 

---
#### Player Jump Gate

---
![player-jump-gate](assets/sprites/blocks/special/player-jump-gate.png)
- **_PlayerJumpGate:_** Quickly transports a player from one position to another.

###### Use Steps:
1. Click the building.
2. Make sure the building isn't locked (You can get and switch the mode through the left button), then tap another `Player Jump Gate` to link.
3. Make sure you are using a flying unit. Get close to the building, then click the button `Teleport` to teleport to the linked building.

- Has cooldown time.
- Requires power to function. 
- Available in the server.

---
#### Hyperspace Folding Gate & Gravity Trap

---
![hyper-space-warper](assets/sprites/blocks/defence/hyper-space-warper.png)
![gravity-gully](assets/sprites/blocks/defence/gravity-gully.png)

- **_Hyperspace Folding Gate:_** Transports a group of units from one side to another.

###### Use Steps:
1. Click the Hyperspace Folding Gate.
2. Click the `Select Destination` button, then click the screen. A cross will appear on the position you clicked. Click the cross again to set the destination.
3. Click the `Select Units` button, then click the screen. Drag the mouse or click the other diagonal point to select all friendly units within a rectangle. Click the button with the arrow icon below the select rectangle to confirm.
4. Click the `Transport Units` button.


- The jump could be intercepted by a `Gravity Trap Field` on it's **WAY TO THE DESTINATION(Not only the destination position will be affected)**, and the intercepted unit will receeive percentage damage. 
- Has cooldown time.
- Requires power and other items to function.
- Available in the server.

---
#### Commandable Block

---
![air-raider](assets/sprites/blocks/defence/air-raider.png)

- **_Commandable Block:_** Active Defence Blocks.

![select-pos](github-pictures/guide/select-pos.gif)
![attack](github-pictures/guide/attack.gif)

###### Use Steps:
1. Click the Commandable Block.
2. Click the `Select Destination` button, then click the screen. A cross will appear on the position you clicked. Click the cross again to set the destination.
3. Click the `Up Open` button, Corresponding blocks of the same kind will make feedback actions.

- Has cooldown time.
- Requires power and other items to function.
- Available in the server.

