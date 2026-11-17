package com.takoy3466.additional_pipes2.pipe.behavior;

import com.takoy3466.re_buildcraft.transport.TravelingItem;

/**
 * パイプに入ったアイテムの速度調整 (旧 {@code IPipeTransportItemsHook.readjustSpeed})。
 * 旧版では速いアイテムはパイプを通るたびに減速し、基準速度より遅くはならなかった。
 */
public final class ItemSpeeds {
    private static final float HALF = 0.5F;

    private ItemSpeeds() {
    }

    /**
     * 石系のパイプの減速 (旧 PipeItemsStone / PipeItemsAdvancedInsertion / PipeItemsRedstone):
     * 基準速度を超えていれば基準速度の半分だけ減らし、基準速度未満なら基準速度に戻す。
     */
    public static void applyStoneFriction(TravelingItem item) {
        applyFriction(item, TravelingItem.NORMAL_SPEED * HALF);
    }

    /**
     * 既定の減速 (旧 PipeTransportItems.defaultReajustSpeed, テレポートパイプが使用):
     * 基準速度を超えていれば基準速度分だけ減らし、基準速度未満なら基準速度に戻す。
     */
    public static void applyDefaultFriction(TravelingItem item) {
        applyFriction(item, TravelingItem.NORMAL_SPEED);
    }

    private static void applyFriction(TravelingItem item, float deceleration) {
        float speed = item.speed();
        if (speed > TravelingItem.NORMAL_SPEED) {
            speed -= deceleration;
        }
        item.setSpeed(Math.max(speed, TravelingItem.NORMAL_SPEED));
    }
}
