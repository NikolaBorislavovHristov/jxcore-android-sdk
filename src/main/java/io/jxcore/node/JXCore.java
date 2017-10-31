package io.jxcore.node;

import android.content.Context;

public final class JXCore {

    static {
        System.loadLibrary("jxcore");
    }

    private final Context mContext;

    public JXCore(final Context context) {
        mContext = context;
        setNativeContext(mContext);
    }

    private native void setNativeContext(Context context);

}
