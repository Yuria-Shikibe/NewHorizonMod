package newhorizon.expand.net;

import arc.audio.Sound;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.ability.active.ActiveAbility;
import newhorizon.expand.logic.DefaultRaid;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import newhorizon.expand.net.packet.AlertToastPacket;
import newhorizon.expand.net.packet.WarnHUDPacket;
import newhorizon.util.ui.NHUIFunc;

import static mindustry.Vars.headless;

public class NHCall {
    public static void triggerActiveAbility(Unit unit, int abilityId) {
        if (Vars.net.server() || !Vars.net.active()) {
            if (unit != null && unit.abilities.length > abilityId) {
                Ability ability = unit.abilities[abilityId];
                if (ability instanceof ActiveAbility) {
                    ((ActiveAbility) ability).trigger(unit);
                }
            }
        }
        if (Vars.net.server() || Vars.net.client()) {
            ActiveAbilityTriggerPacket packet = new ActiveAbilityTriggerPacket();
            packet.unit = unit;
            packet.abilityId = abilityId;
            Vars.net.send(packet, true);
        }
    }

    public static void warnHudPacket(String timerName, float time, float range, float sx, float sy, float tx, float ty) {
        if (Vars.net.server() || !Vars.net.active()) {
            DefaultRaid.clientAlertHud(timerName, time, range, sx, sy, tx, ty);
        }

        if (Vars.net.server()) {
            WarnHUDPacket packet = new WarnHUDPacket();
            packet.name = timerName;
            packet.time = time;
            packet.range = range;
            packet.sourceX = sx;
            packet.sourceY = sy;
            packet.targetX = tx;
            packet.targetY = ty;
            Vars.net.send(packet, false);
        }
    }

    public static void alertToastTable(int iconID, int soundID, String text) {
        if (Vars.net.server() || !Vars.net.active()) {
            if (headless) return;
            NHUIFunc.showToast(getDrawable(iconID), text, getSound(soundID));
        }

        if (Vars.net.server()) {
            AlertToastPacket packet = new AlertToastPacket();
            packet.text = text;
            packet.iconID = iconID;
            packet.soundID = soundID;
            Vars.net.send(packet, false);
        }
    }

    public static Drawable getDrawable(int id) {
        return switch (id){
            case 1 -> new TextureRegionDrawable(NHContent.raid);
            case 2 -> new TextureRegionDrawable(NHContent.fleet);
            case 3 -> new TextureRegionDrawable(NHContent.icon);
            case 4 -> new TextureRegionDrawable(NHContent.capture);
            default -> new TextureRegionDrawable(NHContent.objective);
        };
    }

    public static Sound getSound(int id) {
        if (id == 0) return Sounds.uiUnlock;
        if (id == 1) return NHSounds.alert2;
        return NHSounds.alarm;
    }
}
