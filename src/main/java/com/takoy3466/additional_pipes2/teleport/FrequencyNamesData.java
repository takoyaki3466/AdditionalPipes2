package com.takoy3466.additional_pipes2.teleport;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 周波数の名前をワールドに保存する。元実装: {@code SaveManager} が {@code ap/ap<種類>freqmap.dat} へ
 * Java のシリアライズで書いていたもの。ワールド全体で 1 つ (オーバーワールドのデータとして保存)。
 * <p>
 * 旧版は GUI を閉じたときに保存していたが、ここでは変更時に dirty にしてワールド保存に任せる。
 */
public final class FrequencyNamesData extends SavedData {
    private static final String DATA_NAME = AdditionalPipes2.MODID + "_frequency_names";
    private static final String TAG_NAMES = "names";
    private static final Factory<FrequencyNamesData> FACTORY = new Factory<>(FrequencyNamesData::new, FrequencyNamesData::load, null);

    private final FrequencyNameBook book = new FrequencyNameBook();

    public static FrequencyNamesData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private static FrequencyNamesData load(CompoundTag tag, HolderLookup.Provider registries) {
        FrequencyNamesData data = new FrequencyNamesData();
        int skipped = data.book.load(tag.getList(TAG_NAMES, Tag.TAG_COMPOUND));
        if (skipped > 0) {
            AdditionalPipes2.LOGGER.warn("Skipped {} invalid teleport frequency name entries while loading {}", skipped, DATA_NAME);
        }
        return data;
    }

    /** 読み取り専用の参照。変更は {@link #edit} を通す。 */
    public FrequencyNameBook book() {
        return book;
    }

    /** 帳簿を変更し、変更があれば保存対象にする。 */
    public boolean edit(java.util.function.Predicate<FrequencyNameBook> change) {
        boolean changed = change.test(book);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(TAG_NAMES, book.save());
        return tag;
    }
}
