package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.util.*;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.net.ServerGroup;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.WarningBar;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.*;
import newhorizon.content.register.RecipeRegister;
import newhorizon.content.register.UnitRecipeRegister;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.util.DebugFunc;
import newhorizon.util.ui.FeatureLog;
import newhorizon.util.ui.TableFunc;
import newhorizon.util.ui.dialog.NewFeatureDialog;

import static mindustry.Vars.*;
import static newhorizon.NHInputListener.registerModBinding;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;


public class NewHorizon extends Mod {
    public static final boolean DEBUGGING_SPRITE = false;
    public static final String MOD_RELEASES = "https://github.com/Yuria-Shikibe/NewHorizonMod/releases";
    public static final String MOD_REPO = "Yuria-Shikibe/NewHorizonMod";
    public static final String MOD_GITHUB_URL = "https://github.com/Yuria-Shikibe/NewHorizonMod.git";
    public static final String MOD_NAME = "new-horizon";
    public static final String SERVER = "203.135.99.51:10094";
    public static boolean DEBUGGING = false;
    public static Mods.LoadedMod MOD;
    public static Links.LinkEntry[] links;
    private static boolean showed = false;

    public NewHorizon() {
        DEBUGGING = NHSetting.getBool(NHSetting.DEBUGGING);

        registerModBinding();
        Events.on(EventType.ContentInitEvent.class, e -> NHPostProcess.postProcessOverride());
        Events.on(EventType.PlayerConnect.class, e -> Call.clientPacketReliable("override_check", NHSetting.overrideStatus()));
        Events.on(ClientLoadEvent.class, e -> {
            Core.app.post(NHUI::init);
            updateServer();
            fetchNewRelease();
            showNewDialog();
            showStartLog();
            Time.run(10f, () -> {
                if (OS.username.equals("LaoHuaJi")) {
                    DebugFunc.updateBlockList();
                    DebugFunc.generateBlankBundle();
                    DebugFunc.writeVanillaBlockList();
                    DebugFunc.writeTeamList();
                    //showNew();
                }

                DebugFunc.unlockModContent();
                //DebugFunc.outputIcon();
                //DebugFunc.outlineIcon();

                //DebugFunc.replaceAllSpriteColor("E:/project/MindustryModDevLib/Exoprosopa-main/sprites/blocks", DebugFunc.EXOPROSOPA_SPRITE_PALETTE);
                //DebugFunc.replaceAllSpriteColor("E:/project/MindustryModDevLib/Asthosus-main/sprites/blocks", DebugFunc.ASTHOSUS_SPRITE_PALETTE);
                //DebugFunc.outputSettings();
                //DebugFunc.outputAtlas();
            });
        });
        if (!headless){
            netClient.addPacketHandler("override_check", string -> {
                try {
                    String[] status = string.split("\\|");
                    boolean canConnect = true;
                    for (String s : status) {
                        String[] setting = s.split(":");
                        if (NHSetting.getBool(setting[0]) != Boolean.parseBoolean(setting[1])) {
                            canConnect = false;
                            break;
                        }
                    }
                    if (!canConnect) {
                        StringBuilder str = new StringBuilder();
                        for (String s : status) {
                            String[] setting = s.split(":");
                            boolean enabled = Boolean.parseBoolean(setting[1]);
                            if (NHSetting.getBool(setting[0]) != enabled) {
                                str.append(Core.bundle.get("nh.setting." + setting[0] + ".name")).append("\n");
                            }
                        }
                        ui.showConfirm(Core.bundle.format("mod.ui.require.need-override", str.toString()), NHSetting::showDialog);
                        net.disconnect();
                    }
                } catch (Exception e) {
                    Log.err(e);
                    net.disconnect();
                }
            });
        }
    }

    public static void debugLog(Object obj) {
        if (DEBUGGING) Log.info(obj);
    }

    /**
     * return "new-horizon-name" for sprite.
     */
    public static String name(String name) {
        return MOD_NAME + "-" + name;
    }

    private static void showAbout() {
        if (links == null) links = new Links.LinkEntry[]{
                new Links.LinkEntry("mod.discord", "https://discord.gg/yNmbMcuwyW", Icon.discord, Color.valueOf("7289da")),
                new Links.LinkEntry("mod.github", MOD_GITHUB_URL, Icon.github, Color.valueOf("24292e")),
                new Links.LinkEntry("yuria.plugin", "https://github.com/Yuria-Shikibe/RangeImager", Icon.export, NHColor.thurmixRed)
        };

        BaseDialog dialog = new BaseDialog("@links");
        dialog.cont.pane(table -> {
            for (Links.LinkEntry entry : links) {
                TableFunc.link(table, entry);
            }
        }).grow().row();
        dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 4, LEN);
        dialog.addCloseListener();
        dialog.show();
    }

    public static void showNew() {
        NewFeatureDialog newFeatureDialog = new NewFeatureDialog();
        newFeatureDialog.show();
    }

    public static void startLog() {
        if (showed) return;
        showed = true;


        BaseDialog dialog = new BaseDialog("") {
            @Override
            public void hide() {
                super.hide();
            }
        };

        dialog.cont.pane(inner -> {
            inner.pane(table -> {
                table.table(t -> t.image(NHContent.icon2).fill()).center().growX().fillY().row();
                table.image().fillX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
                table.pane(p -> {
                    p.add("[white]<< Powered by New Horizon Mod >>", Styles.techLabel).row();
                }).fillY().growX().row();
                table.image().fillX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
                table.add("").row();
            }).growX().center().row();

            inner.table(table -> {
                if (!Vars.mobile) table.table(t -> {

                }).grow();
                table.table(t -> {
                    t.button("@back", Icon.left, Styles.cleart, dialog::hide).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
                    t.button("@links", Icon.link, Styles.cleart, NewHorizon::showAbout).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
                    t.button("@hide-setting", Icon.settings, Styles.cleart, () -> Core.settings.put("nh_hide_starting_log", true)).disabled(b -> Core.settings.getBool("nh_hide_starting_log", false)).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
                    t.button("@log", Icon.book, Styles.cleart, NewHorizon::showNew).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
                    t.button(Core.bundle.get("servers.remote") + "\n(" + Core.bundle.get("waves.copy") + ")", Icon.host, Styles.cleart, () -> Core.app.setClipboardText(SERVER)).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
                }).grow();
                if (!Vars.mobile) table.table(t -> {

                }).grow();
            }).fill();
        }).grow();
        dialog.show();
    }

    @Override
    public void init() {
        Events.on(ClientLoadEvent.class, e -> {
            if (Vars.netServer != null) {
                Vars.netServer.admins.addChatFilter((player, text) -> text.replace("jvav", "java"));
            }

            NHVars.init();

            Timer.schedule(() -> {
                if (Vars.state == null || Vars.state.isPaused() || Vars.state.teams == null) return;

                Vars.state.teams.getActive().each(teamData -> {
                    if (teamData == null || !teamData.hasCore()) return;

                    int coreCount = 0;
                    try {
                        coreCount = Vars.state.teams.cores(teamData.team).size;
                    } catch (Exception ex) {
                        coreCount = teamData.core() == null ? 0 : 1;
                    }

                    if (coreCount > 0 && teamData.core() != null) {
                        int totalToAdd = coreCount * 2;
                        teamData.core().items.add(NHItems.hardLight, totalToAdd);
                    }
                });
            }, 1f, 1f);
        });
    }


    @Override
    public void registerClientCommands(CommandHandler handler) {
        super.registerClientCommands(handler);

        //from JSEval
        handler.<Player>register("js", "<code...>", "Execute JavaScript code.", (args, player) -> {
            if (player.admin) {
                String output = mods.getScripts().runConsole(args[0]);
                player.sendMessage("> " + (isError(output) ? "[#ff341c]" + output : output));
            } else {
                player.sendMessage("[scarlet]You must be admin to use this command.");
            }
        });
    }

    private boolean isError(String output) {
        try {
            String errorName = output.substring(0, output.indexOf(' ') - 1);
            Class.forName("org.mozilla.javascript." + errorName);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void loadContent() {
        Time.mark();

        MOD = mods.getMod(getClass());
        MOD.meta.displayName = Core.bundle.get("mod.new-horizon.name");
        MOD.meta.author = Core.bundle.get("mod.new-horizon.author");
        MOD.meta.description = Core.bundle.get("mod.new-horizon.description");
        MOD.meta.subtitle = Core.bundle.get("mod.new-horizon.subtitle");

        EntityRegister.load();
        NHRegister.load();
        NHContent.loadPriority();
        NHSounds.load();

        if (!Vars.headless) NHShaders.init();

        NHContent.loadBeforeContentLoad();

        NHStatusEffects.load();
        NHItems.load();
        NHLiquids.load();
        NHBullets.load();
        NHUnitTypes.load();
        NHBlocks.load();
        NHWeathers.load();
        NHPlanets.load();
        NHSectorPresents.load();
        NHTechTree.load();
        //NHDevices.load();

        NHSetting.load();
        NHPostProcess.load();
        NHPostProcess.loadOptional();
        NHContent.loadLast();
        NHLogic.load();

        RecipeRegister.load();
        UnitRecipeRegister.load();

        Log.info("New Horizon Loaded Complete: " + MOD.meta.version + " | Cost Time: " + (Time.elapsed() / Time.toSeconds) + " sec.");
    }

    private void updateServer() {
        Vars.defaultServers.add(new ServerGroup() {{
            name = "[sky]New Horizon [white]Mod [lightgray]Servers";
            addresses = new String[]{SERVER};
        }});
    }

    private void fetchNewRelease() {
        if (!DEBUGGING) Time.runTask(15f, () -> Threads.daemon(() -> {
            Http.get(Vars.ghApi + "/repos/" + MOD_REPO + "/releases/latest", res -> {
                Jval json = Jval.read(res.getResultAsString());

                String tag = json.get("tag_name").asString();
                String body = json.get("body").asString();

                if (tag != null && body != null && !tag.equals(Core.settings.get(MOD_NAME + "-last-gh-release-tag", "0")) && !tag.equals('v' + MOD.meta.version)) {
                    Core.app.post(() -> {
                        new BaseDialog(Core.bundle.get("mod.ui.has-new-update") + ": " + tag) {{
                            cont.table(t -> {
                                t.add(new WarningBar()).growX().height(LEN / 2).padLeft(-LEN).padRight(-LEN).padTop(LEN).expandX().row();
                                t.image(NHContent.icon2).center().pad(OFFSET).color(Pal.accent).scaling(Scaling.bounded).row();
                                t.add(new WarningBar()).growX().height(LEN / 2).padLeft(-LEN).padRight(-LEN).padBottom(LEN).expandX().row();
                                t.add("\t[lightgray]Version: [accent]" + tag).left().row();
                                t.image().growX().height(OFFSET / 3).pad(OFFSET / 3).row();
                                t.pane(c -> {
                                    c.align(Align.topLeft).margin(OFFSET);
                                    c.add("[accent]Description: \n[]" + body).left();
                                }).grow();
                            }).grow().padBottom(OFFSET).row();


                            cont.table(table -> {
                                table.button("@back", Icon.left, Styles.cleart, this::hide).growX().height(LEN);
                                table.button("@mods.github.open", Icon.github, Styles.cleart, () -> Core.app.openURI(MOD_RELEASES)).growX().height(LEN);
                            }).bottom().growX().height(LEN).padTop(OFFSET);

                            addCloseListener();
                        }}.show();
                    });
                }

                if (tag != null) Core.settings.put(MOD_NAME + "-last-gh-release-tag", tag);
            }, ex -> Log.err(ex.toString()));
        }));
    }

    private void showNewDialog() {
        Time.runTask(10f, () -> {
            if (!Core.settings.get("nh-lastver", -1).equals(MOD.meta.version)) {
                showNew();
            }
            Core.settings.put("nh-lastver", MOD.meta.version);
        });
    }

    private void showStartLog() {
        if (!Core.settings.getBool(NHSetting.START_LOG)) Core.app.post(Time.runTask(10f, NewHorizon::startLog));
    }
}
