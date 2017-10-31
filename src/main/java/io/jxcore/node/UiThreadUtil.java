package io.jxcore.node;

import android.os.Handler;
import android.os.Looper;

final class UiThreadUtil {

    private static Handler sMainHandler;

    static boolean isOnUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    static void runOnUiThread(final Runnable runnable) {
        synchronized (UiThreadUtil.class) {
            if (sMainHandler == null) {
                sMainHandler = new Handler(Looper.getMainLooper());
            }
        }

        sMainHandler.post(runnable);
    }

}
