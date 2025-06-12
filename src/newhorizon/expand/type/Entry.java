package newhorizon.expand.type;

import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;

public class Entry extends UnlockableContent {

    public Entry(String name) {
        super(name);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.typeid_UNUSED;
    }
}
