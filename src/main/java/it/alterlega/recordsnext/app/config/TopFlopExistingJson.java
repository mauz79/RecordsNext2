package it.alterlega.recordsnext.app.config;

/** Reuse the JSON parser already shipped with RecordsNext 3.2. */
public final class TopFlopExistingJson {
    private TopFlopExistingJson() {}
    public static Object parse(String original) {
        return MiniJson.parse(original);
    }
}
