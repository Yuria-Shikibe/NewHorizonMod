package newhorizon.content;

import arc.Core;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;

import static mindustry.ctype.ContentType.loadout_UNUSED;

public class DatabaseEntry extends UnlockableContent {
    public DatabaseEntry(String name) {
        super(name);

        localizedName = Core.bundle.get("database." + name + ".name");
        description = Core.bundle.getOrNull("database." + name + ".description");
        details = Core.bundle.getOrNull("database." + name + ".details");

        alwaysUnlocked = true;
        hideDetails = false;
        allDatabaseTabs = true;
        databaseCategory = "mechanic";
    }

    @Override
    public ContentType getContentType() {
        return loadout_UNUSED;
    }

    @Override
    public void load() {
        super.load();
        if (NHContent.icon != null) {
            fullIcon = uiIcon = NHContent.icon;
        }
    }
}
