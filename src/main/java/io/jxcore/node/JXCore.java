package io.jxcore.node;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public final class JXCore {

    static {
        System.loadLibrary("jxcore");
    }

    private final AssetManager mAssetManager;
    private final String mHomePath;
    private final String mAssetsPath;
    private final String mMainFileName;
    private final Handler mLoopHandler;

    public JXCore(final Context context, final String assetsPath, final String mainFileName) {
        mAssetManager = context.getAssets();
        mHomePath = context.getFilesDir().getAbsolutePath();
        mAssetsPath = assetsPath;
        mMainFileName = mainFileName;
        mLoopHandler = new Handler(context.getMainLooper());
    }

    public void start() throws Exception {
        final HashMap assets = getAssets();
        final String assetsAsString = new JSONObject(assets).toString();
        final String assetsAbsolutePath = mHomePath + '/' + mAssetsPath;
        initializeEngine(mAssetManager, assetsAbsolutePath, assetsAsString);
        final String mainFileContent = "process.setPaths = function(){ process.cwd = function() { return '"
                + assetsAbsolutePath
                + "';};\n"
                + "process.userPath ='"
                + mHomePath
                + "';\n"
                + "};" + readAsset(mMainFileName);
//                    final String mainFileContent = "process.cwd = function(){ return '" + assetsAbsolutePath + "'; };\n"
//                            + "process.userPath = '" + assetsAbsolutePath + "';\n"
//                            + readAsset(mMainFileName);
        defineMainFile(mainFileContent);
        startEngine();
        startEventLoop();
    }

    public void stop() {
        stopEngine();
    }

    private void startEventLoop() {
        final Runnable eventLoop = new Runnable() {
            @Override
            public void run() {
                JXCore.this.loopOnce();
                mLoopHandler.postDelayed(this, 5);
            }
        };

        mLoopHandler.postDelayed(eventLoop, 5);
    }

    private HashMap<String, Integer> getAssets() throws IOException {
        final HashMap<String, Integer> assets = new HashMap<>();
        final String[] assetFilePaths = mAssetManager.list(mAssetsPath);
        for(final String filePath : assetFilePaths) {
            final InputStream assetInputStream = mAssetManager.open(mAssetsPath + '/' + filePath, AssetManager.ACCESS_UNKNOWN);
            final int size = assetInputStream.available();
            assetInputStream.close();
            assets.put(filePath, size);
        }

        return assets;
    }

    private String readAsset(final String fileName) throws IOException {
        final StringBuilder content = new StringBuilder();
        final BufferedReader assetReader = new BufferedReader(
                new InputStreamReader(mAssetManager.open(mAssetsPath + "/" + fileName), "UTF-8")
        );

        String line;
        while ((line = assetReader.readLine()) != null) {
            content.append(line).append('\n');
        }

        assetReader.close();
        return content.toString();
    }

    private native void initializeEngine(final AssetManager assetManager, final String assetsPath, final String assetsFilesTree);

    private native void defineMainFile(final String mainFileContent);

    private native void stopEngine();

    private native void startEngine();

    private native void loopOnce();

}
