package com.draw.free.ar.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import androidx.annotation.Keep;

import com.draw.free.Global;
import com.draw.free.interfaceaction.ICallback;

import java.io.IOException;

import timber.log.Timber;

public class JniInterfaceForPlace {
    static {
        System.loadLibrary("place_everywhere_native");
    }

    @Keep
    public static native long createNativeApplication(AssetManager assetManager);

    @Keep
    public static native void destroyNativeApplication(long nativeApplication);

    @Keep
    public static native void loadObj(long nativeApplication, String fileName);

    @Keep
    public static native void onPause(long nativeApplication);

    @Keep
    public static native void onResume(long nativeApplication, Context context, Activity activity);

    @Keep
    public static native void onContextCreated(long nativeApplication);

    @Keep
    public static native void onDrawFrame(long nativeApplication);

    @Keep
    public static native void onPreDrawFrame(long nativeApplication);

    @Keep
    public static native void onDisplayGeometryChanged(long nativeApplication, int displayRotation, int width, int height);

    @Keep
    public static native void onTouchScreen(long nativeApplication, float x, float y, int options);

    @Keep
    public static native void resetRotation(long nativeApplication);

    @Keep
    public static native void setMode(long nativeApplication, int value);


    @Keep
    public static native void setScale(long nativeApplication, int zoom);


    @Keep
    public static native void setSaveMode(long nativeApplicatoin);


    @Keep
    public static AssetManager mAssetManager = Global.getAssetManager();

    @Keep
    public static PlaceModeListener mPlaceModeListener = null;

    @Keep
    public static void makeToastMessage(String rtaert) {
        Global.makeToast(rtaert);
    }

    @Keep
    public static ICallback NNDady = null;

    @Keep
    public static void makeDisplay(String message) {
        if (NNDady == null) {
            return;
        }
        NNDady.callback(message);
    }

    @Keep
    public static void modeCallBack(String string) {
        if (mPlaceModeListener == null) {
            return;
        }
        //  INIT, SEARCH, DRAW, SAVE
        switch (string) {
            case "INIT":
                mPlaceModeListener.InitModeCallBack();
                break;
            case "SEARCH":
                mPlaceModeListener.SearchModeCallBack();
                break;
            case "PREVIEW":
                mPlaceModeListener.PreViewModeCallBack();
                break;
            case "PLACE":
                mPlaceModeListener.PlaceModeCallBack();
                break;
            case "SAVE":
                mPlaceModeListener.SaveModeCallBack();
                break;
        }
    }

    @Keep
    public static String getCacheDir() {
        return Global.getCacheDir();
    }

    @Keep
    public static UndoRedoValidCheck mRedoUndoValidChecker = null;

    @Keep
    public static void undoRedoCallBack(boolean isUndo, boolean value) {
        if (mRedoUndoValidChecker == null) {
            return;
        }

        if (isUndo) {
            mRedoUndoValidChecker.undoCheck(value);
        } else {
            mRedoUndoValidChecker.redoCheck(value);
        }
    }

    @Keep
    public static void loadTexture(int target, Bitmap bitmap) {
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Timber.d(": glError %s", error);
            throw new RuntimeException("LoadTexture Error : glError " + error);
        }
        Timber.d("지나감");
    }

    @Keep
    public static Bitmap loadImage(String imageName) {
        try {
            Timber.e("image : $imageName");

            return BitmapFactory.decodeStream(mAssetManager.open(imageName));
        } catch (IOException e) {
            Timber.e("Cannot open image %s", imageName);
            return null;
        }
    }

}
