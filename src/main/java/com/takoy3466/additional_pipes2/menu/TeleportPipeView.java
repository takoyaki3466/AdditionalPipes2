package com.takoy3466.additional_pipes2.menu;

import com.takoy3466.additional_pipes2.teleport.FrequencyNameBook;
import java.util.List;

/**
 * テレポートパイプの画面に表示する状態 (サーバーからクライアントへ送る内容)。
 *
 * @param frequency       現在の周波数
 * @param canReceive      受信が有効か
 * @param connectedCount  リンクしているパイプの数 (受信設定を問わない, 旧 "Connected: N")
 * @param ownerName       所有者の名前
 * @param names           画面を開いたプレイヤーが付けた周波数の名前
 */
public record TeleportPipeView(int frequency, boolean canReceive, int connectedCount, String ownerName,
                               List<FrequencyNameBook.Entry> names) {
    public static final TeleportPipeView EMPTY = new TeleportPipeView(0, false, 0, "", List.of());

    public TeleportPipeView {
        names = List.copyOf(names);
    }

    /** 現在の周波数に付いている名前。無ければ空文字。 */
    public String currentName() {
        for (FrequencyNameBook.Entry entry : names) {
            if (entry.frequency() == frequency) {
                return entry.name();
            }
        }
        return "";
    }
}
