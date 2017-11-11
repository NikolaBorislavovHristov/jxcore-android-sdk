package io.jxcore.node;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public final class JXCore {

    private static final String TAG = JXCore.class.getSimpleName();
    private static final int EVENT_LOOP_TIMEOUT = 5;
    private static final String ASSETS_PATH = "www/jxcore";
    private static final String MAIN_FILE_FORMAT = "var HOME_PATH = '%s', ASSETS_PATH = '%s';\n%s";

    static {
        System.loadLibrary("jxcore");
    }

    private final Handler mEventLoopHandler;
    private boolean mIsRunning = false;
    private final Runnable mEventLoop = new Runnable() {
        @Override
        public void run() {
            JXCore.this.loopOnce();
            mEventLoopHandler.postDelayed(mEventLoop, EVENT_LOOP_TIMEOUT);
        }
    };

    public JXCore(final Context context, final String mainFileName) throws IOException {
        final HandlerThread eventLoopThread = new HandlerThread(TAG);
        eventLoopThread.start();
        mEventLoopHandler = new Handler(eventLoopThread.getLooper());
        final AssetManager assetManager = context.getAssets();
        final HashMap assetsFilesTree = getAssetsFilesTree(assetManager, ASSETS_PATH);
        final String assetsAsString = new JSONObject(assetsFilesTree).toString();
        final String homePath = context.getFilesDir().getAbsolutePath();
        final String mainFileAsset = readAsset(assetManager, ASSETS_PATH + "/" + mainFileName);
        final String mainFileContent = String.format(MAIN_FILE_FORMAT, homePath, ASSETS_PATH, mainFileAsset);
        mEventLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                final String assetsAbsolutePath = homePath + '/' + ASSETS_PATH;
                JXCore.this.initializeEngine(assetManager, assetsAbsolutePath, assetsAsString, mainFileContent);
            }
        });
    }

    public void start() {
        mEventLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                JXCore.this.startEngine();
                JXCore.this.resume();
            }
        });
    }

    public void stop() {
        mEventLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                JXCore.this.pause();
                JXCore.this.stopEngine();
            }
        });
    }

    public void resume() {
        synchronized (this) {
            if (!mIsRunning) {
                mIsRunning = true;
                mEventLoopHandler.post(mEventLoop);
            }
        }
    }

    public void pause() {
        synchronized (this) {
            if (mIsRunning) {
                mIsRunning = false;
                mEventLoopHandler.removeCallbacks(mEventLoop);
            }
        }
    }

    private HashMap<String, Integer> getAssetsFilesTree(final AssetManager assetManager, final String assetsPath) throws IOException {
        final HashMap<String, Integer> filesTree = new HashMap<>();
        final String[] assetFilePaths = assetManager.list(assetsPath);
        for (final String filePath : assetFilePaths) {
            final InputStream assetInputStream = assetManager.open(assetsPath + "/" + filePath, AssetManager.ACCESS_UNKNOWN);
            final int size = assetInputStream.available();
            filesTree.put(filePath, size);
            assetInputStream.close();
        }

        return filesTree;
    }

    private String readAsset(final AssetManager assetManager, final String filePath) throws IOException {
        final StringBuilder content = new StringBuilder();
        final BufferedReader assetReader = new BufferedReader(
                new InputStreamReader(assetManager.open(filePath), "UTF-8")
        );

        String line;
        while ((line = assetReader.readLine()) != null) {
            content.append(line).append('\n');
        }

        assetReader.close();
        return content.toString();
    }

    private native void initializeEngine(final AssetManager assetManager, final String assetsPath, final String assetsFilesTree, final String mainFileContent);

    private native void stopEngine();

    private native void startEngine();

    private native void loopOnce();
}
