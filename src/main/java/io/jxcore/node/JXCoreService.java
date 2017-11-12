package io.jxcore.node;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public final class JXCoreService extends Service {

    private final String TAG = JXCoreService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            final JXCore jxCoreClient = new JXCore(this);
            jxCoreClient.start();
        } catch (final IOException e) {
            Log.e(TAG, "JXCore service not started", e);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

}
