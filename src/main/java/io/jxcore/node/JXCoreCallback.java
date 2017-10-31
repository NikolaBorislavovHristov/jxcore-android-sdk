package io.jxcore.node;

import java.util.ArrayList;

public interface JXCoreCallback {
    void call(ArrayList<Object> params, String eventName);
}
