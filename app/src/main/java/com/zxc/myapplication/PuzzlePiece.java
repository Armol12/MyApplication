package com.zxc.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class PuzzlePiece extends androidx.appcompat.widget.AppCompatImageView {

    private Float currentRotation = 0f;
    private int targetRow;
    private int targetCol;
    private int currentRow;
    private int currentCol;
    private boolean moved; // Track if the piece has been moved or rotated

    public PuzzlePiece(Context context, Bitmap image, int targetRow, int targetCol, int size) {
        super(context);
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.setImageBitmap(image);
        this.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        init();
    }

    public PuzzlePiece(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PuzzlePiece(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        moved = false; // Initially, the piece hasn't been moved
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;  // Ensure proper handling in the parent view for dragging
            }
        });
    }

    public Float getCurrentRotation() {
        return currentRotation;
    }

    public void setCurrentRotation(Float rotation) {
        this.currentRotation = rotation;
        setRotation(rotation);
        moved = true; // Mark as moved when rotated
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
        moved = true; // Mark as moved when row changes
    }

    public int getCurrentCol() {
        return currentCol;
    }

    public void setCurrentCol(int currentCol) {
        this.currentCol = currentCol;
        moved = true; // Mark as moved when column changes
    }

    public boolean isInCorrectPosition() {
        return this.currentRow == this.targetRow && this.currentCol == this.targetCol;
    }

    public boolean hasMoved() {
        return moved;
    }
}
