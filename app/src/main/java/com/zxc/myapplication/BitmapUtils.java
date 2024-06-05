package com.zxc.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BitmapUtils {

    public static Bitmap scaleBitmapToSquare(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap scaledBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        float left = (size - bitmap.getWidth()) / 2.0f;
        float top = (size - bitmap.getHeight()) / 2.0f;
        canvas.drawBitmap(bitmap, left, top, new Paint(Paint.ANTI_ALIAS_FLAG));
        return scaledBitmap;
    }


    public static Bitmap[][] splitImage(Context context, int resourceId, int rows, int cols) {
        Bitmap original = BitmapFactory.decodeResource(context.getResources(), resourceId);
        Bitmap scaledBitmap = scaleBitmapToSquare(original);
        int width = scaledBitmap.getWidth() / cols;
        int height = scaledBitmap.getHeight() / rows;
        Bitmap[][] imageParts = new Bitmap[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                imageParts[i][j] = Bitmap.createBitmap(scaledBitmap, j * width, i * height, width, height);
            }
        }

        return imageParts;
    }
}
