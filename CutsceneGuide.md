# Important
* WU: world unit, 8 WU = 1 Tile. in float.
* Text: format: `<text here>`. replace all `\n` with `[n]`.
* Team: team name(derelict/sharded/crux/malis) or team id.
* UnitType: unit's inner name(dagger, new-horizon-branch for example) or UnitType id.

***
# Available Action
### special - Special Actions
* `wait`: Wait a specific time.

  available args:
    * [1] (second)time: how long the wait lasts.
### camera - Camera Action
* `camera-control`: Move camera to a specific position.
 
  available args: 
    * [1] (second)time: how long it takes to move the camera.
    * [2] (WU)x: camara's target X coordinate.
    * [3] (WU)y: camara's target y coordinate.
  

* `camera-reset`: Reset camera to player.

  available args:
    * [1] (second)time: how long it takes to move the camera.

* `camera-set`: Move camera to a specific position immediately.

  available args:
    * [1] (WU)x: camara's target X coordinate.
    * [2] (WU)y: camara's target y coordinate.
### curtain - Curtain & Background Action
* `curtain-draw`: Draw the curtain. Hide the ui. hard-coded with 1.5s duration.

* `curtain-raise`: Raise the curtain. Show the ui. hard-coded with 1.5s duration.

* `curtain-fade-in`: Turn the screen to dark. Hide the ui. hard-coded with 2s duration.

* `curtain-fade-out`: Reveal the screen. Show the ui. hard-coded with 2s duration.
### info - COD style Info Text
* `info-fade-in`: Info text fade in. hard-coded with 0.25s duration.

* `info-fade-out`: Info text fade out. hard-coded with 0.25s duration. Remove the info text.

* `info-text`: the text of the info.

  available args:
  * [1] (Text)text: The info text. COD style.
### signal - Signal Dialog
* `signal-cut-in`: Signal dialog fade in. hard-coded with 0.5s duration.

* `signal-cut-out`: Signal dialog fade out. hard-coded with 0.5s duration. Remove the signal text.

* `signal-text`: the text of the signal.

  available args:
  * [1] (Text)text: The signal text.
### input - Input Lock/Unlock
* `input-lock`: Lock the input. Hide the ui. Camera can only move with cutscene control.

* `input-unlock`: Unlock the input. resume player's control.
### event - Special World Event
* `jump-in`: Summon a unit to jump in.

  available args:
  * [1] (UnitType)unit: the unit that jump in.
  * [2] (Team)team: unit's team.
  * [3] (WU)x: the X coordinate where unit jump in.
  * [4] (WU)y: the Y coordinate where unit jump in.
  * [5] (Float)angle: the angle the unit jump in.
  * [6] (Second)delay: the jump in delay of the unit.
  * [7] (WU)inaccuracy: the inaccuracy range of the jump in.


* `mark-world`: Create a marker on the map.

  available args:
  * [1] (WU)x: the X coordinate of the marker.
  * [2] (WU)y: the Y coordinate of the marker.
  * [3] (WU)radius: the radius of the marker.
  * [4] (Second)duration: how long the marker last.
  * [5] (Integer)style: the style of the marker.
    * default: default style
    * 1: default style without lines
    * 2: default style but fixed
    * 3: shaking signal

* `raid`: Create a bullet from a specific position.

  available args:
  * [1] (Team)team: raid's team.
  * [2] (Integer)bullet: the bullet type of the raid.
    * default: basic air raid (splash damage 500, splash radius 60)
  * [3] (WU)sourceX: the X coordinate where the bullet created.
  * [4] (WU)sourceY: the Y coordinate where the bullet created.
  * [3] (WU)targetX: the X coordinate where the bullet targets.
  * [4] (WU)targetY: the Y coordinate where the bullet targets.
  * [7] (WU)inaccuracy: the inaccuracy range of the bullet.

### warning: Warning Control.
* `warning-icon`: Create a warning hud.

  available args:
  * [1] (Integer)icon: the icon of the warning.
    * default: objective
    * 1: raid
    * 2: fleet
    * 3: capture
  * [2] (Team)team: the warning hud's team. used for color.
  * [3] (Text)text: The warning text.

* `warning-sound`: Broadcast a warning sound.

  available args:
  * [1] (Integer)allySound: warning sound for friendly.
  * [2] (Integer)enemySound: warning sound for enemy.
  * [3] (Team)team: the warning sound's team.