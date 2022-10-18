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

public class JniInterface {
    static {
        // draw_everywhere_nbat
        System.loadLibrary("draw_everywhere_native");
    }

    enum TYPE {
        INIT, SEARCH, DRAW, SAVE
    }

    public static AssetManager assetManager;

    @Keep
    public static native long createNativeApplication(AssetManager x);

    @Keep
    public static native void destroyNativeApplication(long n);

    @Keep
    public static native void onPause(long z);

    @Keep
    public static native void onResume(long a, Context s, Activity d);

    @Keep
    public static native void onContextCreated(long s);

    @Keep
    public static native void onDrawFrame(long nativeApplication);

    @Keep
    public static native void onPreDrawFrame(long nativeApplication);

    @Keep
    public static native void onDisplayGeometryChanged(long qw, int displayRotation, int width, int height);

    @Keep
    public static native void onTouchScreen(long nativeApplication, float x, float y, int options);


    public static native void setLineColor(long nativeApplication, int r, int g, int b);

    @Keep
    public static native void setLineWidth(long nativeApplication, int zeroTo100);

    @Keep
    public static native void setLineType(long yer, int aery);

    @Keep
    public static native void clearDrawing(long nativeApplicatoin);

    public static native void setSaveMode(long nativeApplicatoin);


    @Keep
    public static native void setDrawMode(long nativeApplicatoin);


    public static native void saveTempDraw(long nativeApplicatoin);


    public static native void loadTempDraw(long nativeApplicatoin);

    @Keep
    public static ICallback mCallback = null;

    @Keep
    public static void makeDisplay(String message) {
        if (mCallback == null) {
            return;
        }

        mCallback.callback(message);
    }


    @Keep
    public static Bitmap loadImage(String imageName) {
        try {
            return BitmapFactory.decodeStream(assetManager.open(imageName));
        } catch (IOException e) {
            return null;
        }
    }

    @Keep
    public static ModeListener mModeListener = null;

    @Keep
    public static void makeToastMessage(String asdf) {
        Global.makeToast(asdf);
    }

    @Keep
    public static void modeCallBack(String asdf) {
        if (mModeListener == null) {
            Timber.d("mode not set : %s", asdf);
            return;
        }
        //  INIT, SEARCH, DRAW, SAVE
        switch (asdf) {
            case "INIT":
                mModeListener.InitModeCallBack();
                break;
            case "SEARCH":
                mModeListener.SearchModeCallBack();
                break;
            case "DRAW":
                mModeListener.DrawModeCallBack();
                break;
            case "SAVE":
                mModeListener.SaveModeCallBack();
                break;
        }
    }

    @Keep
    public static String getCacheDir() {
        return Global.getCacheDir();
    }

    public static UndoRedoValidCheck mRedoUndoValidCheck = null;

    @Keep
    public static void undoRedoCallBack(boolean asdf, boolean asdf2) {
        if (mRedoUndoValidCheck == null) {
            return;
        }

        if (asdf) {
            mRedoUndoValidCheck.undoCheck(asdf2);
        } else {
            mRedoUndoValidCheck.redoCheck(asdf2);
        }
    }

    @Keep
    public static void loadTexture(int wetq, Bitmap ngfsgn) {
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, ngfsgn, 0);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw new RuntimeException("Error " + error);
        }
    }

    @Keep
    public static native void undo(long nativeApplication);

    @Keep
    public static native void redo(long nativeApplication);


}
