package newhorizon.expand.rules;

import arc.Core;
import arc.math.Mathf;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.state;

public class AscensionRuleDialog extends BaseDialog {
    private final Table all = new Table();

    public float enemyHealthModifier = 1f;
    public float enemyDamageModifier = 1f;

    public AscensionRuleDialog() {
        super("Ascension Rules");

        shouldPause = true;
        addCloseButton();

        shown(this::rebuild);
        onResize(this::rebuild);

        all.margin(20);

        //cont.top();
        //cont.label(() -> "Ascension Rules").style(Styles.techLabel).width(Core.graphics.getWidth() / 2f).fillX().padBottom(4).row();
        cont.pane(all).scrollX(false);
    }

    void rebuild(){
        all.table(t -> {
            t.label(() -> "Enemy Health Modifier: " + Strings.fixed(enemyHealthModifier, 2)).growX().padTop(10f).padBottom(10f).row();
            t.table(s -> {
                s.button(Icon.left, Styles.grayi, () -> enemyHealthModifier = Mathf.clamp(enemyHealthModifier - 0.1f, 1f, 3f)).size(40f);
                s.slider(1f, 3f, 0.01f, 1f, f -> enemyHealthModifier = f).size(0, 40f).padLeft(20f).padRight(20f).update(slider -> {
                    slider.setValue(enemyHealthModifier);
                }).growX();
                s.button(Icon.right, Styles.grayi, () -> enemyHealthModifier = Mathf.clamp(enemyHealthModifier + 0.1f, 1f, 3f)).size(40f);
            }).growX().row();

            t.label(() -> "Enemy Damage Modifier: " + Strings.fixed(enemyDamageModifier, 2)).growX().padTop(10f).padBottom(10f).row();
            t.table(s -> {
                s.button(Icon.left, Styles.grayi, () -> enemyDamageModifier = Mathf.clamp(enemyDamageModifier - 0.1f, 1f, 3f)).size(40f);
                s.slider(1f, 5f, 0.01f, 1f, f -> enemyDamageModifier = f).size(0, 40f).padLeft(20f).padRight(20f).update(slider -> {
                    slider.setValue(enemyDamageModifier);
                }).growX();
                s.button(Icon.right, Styles.grayi, () -> enemyDamageModifier = Mathf.clamp(enemyDamageModifier + 0.1f, 1f, 3f)).size(40f);
            }).growX().row();

        }).width(Math.min(Core.graphics.getWidth() / 1.5f, 600f)).expandX();
    }
}
