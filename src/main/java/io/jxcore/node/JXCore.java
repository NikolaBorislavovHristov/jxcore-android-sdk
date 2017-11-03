package io.jxcore.node;

import android.content.Context;

public final class JXCore {

    static {
        System.loadLibrary("jxcore");
    }

    private final Context mContext;
    private final String mMainFileName;

    public JXCore(final Context context, final String mainFileName) {
        mContext = context;
        mMainFileName = mainFileName;
        setNativeContext(mContext);
    }

    private native void setNativeContext(Context context);

}
