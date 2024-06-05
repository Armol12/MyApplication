package com.zxc.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PuzzleBoard {
    private static final String TAG = "PuzzleBoard";
    private final Context context;
    private final GridLayout gameBoard;
    private final int rows;
    private final int cols;
    private Bitmap[][] imageParts;
    private final List<PuzzlePiece> pieces;
    private final Float[][] solutionMatrix;
    private PuzzlePiece selectedPiece;  // Track the selected piece
    private boolean gameStarted; // Track if the game has started

    public PuzzleBoard(Context context, GridLayout gameBoard, int rows, int cols) {
        this.context = context;
        this.gameBoard = gameBoard;
        this.rows = rows;
        this.cols = cols;
        this.pieces = new ArrayList<>();
        this.solutionMatrix = new Float[rows][cols];
        initializeSolutionMatrix();
        gameStarted = false; // Initially, the game hasn't started
    }

    private void initializeSolutionMatrix() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                solutionMatrix[i][j] = 0f; // All pieces should initially have 0 rotation
            }
        }
    }

    public void initializeBoard(Bitmap[][] imageParts) {
        this.imageParts = imageParts;
        gameBoard.removeAllViews();
        pieces.clear();

        int gameBoardWidth = gameBoard.getWidth();
        int gameBoardHeight = gameBoard.getHeight();
        int marginSize = 16; // Adjust this value for desired spacing

        // Calculate the size of each piece to fit within the game board with margins
        int pieceSize = Math.min((gameBoardWidth - (cols + 1) * marginSize) / cols, (gameBoardHeight - (rows + 1) * marginSize) / rows);

        // Create pieces and add to the game board
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Bitmap pieceImage = Bitmap.createScaledBitmap(imageParts[i][j], pieceSize, pieceSize, true);
                PuzzlePiece piece = new PuzzlePiece(context, pieceImage, i, j, pieceSize);
                piece.setCurrentRow(i);
                piece.setCurrentCol(j);
                pieces.add(piece);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = pieceSize;
                params.height = pieceSize;
                params.setMargins(marginSize, marginSize, marginSize, marginSize);
                piece.setLayoutParams(params);

                piece.setOnLongClickListener(new LongClickListener());
                piece.setOnDragListener(new DragListener());
                piece.setOnClickListener(new PuzzlePieceClickListener()); // Add click listener
                gameBoard.addView(piece);
            }
        }

        // Shuffle pieces to scatter them randomly
        Collections.shuffle(pieces);
        gameBoard.removeAllViews();
        for (PuzzlePiece piece : pieces) {
            gameBoard.addView(piece);
        }

        // Randomly rotate the pieces
        Random random = new Random();
        for (PuzzlePiece piece : pieces) {
            float randomRotation = random.nextInt(4) * 90; // Random rotation 0, 90, 180, 270
            piece.setCurrentRotation(randomRotation);
        }
        gameStarted = true; // Mark the game as started
    }

    public void checkCompletion() {
        if (!gameStarted) return; // Ensure the game has started before checking for completion

        boolean isSolved = true;
        boolean anyPieceMoved = false;

        for (PuzzlePiece piece : pieces) {
            if (!piece.isInCorrectPosition()) {
                isSolved = false;
                break;
            }
            if (piece.hasMoved()) {
                anyPieceMoved = true;
            }
        }

        if (isSolved && anyPieceMoved) {
            ((MainActivity) context).showCompletionDialog();
        }
    }

    private class LongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(null, shadowBuilder, v, 0);
            v.setAlpha(0.5f); // Make the view semi-transparent while dragging
            return true;
        }
    }

    private class DragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            PuzzlePiece dropped = (PuzzlePiece) event.getLocalState();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("PuzzleBoard", "Drag started");
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d("PuzzleBoard", "Drag entered");
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("PuzzleBoard", "Drag exited");
                    return true;
                case DragEvent.ACTION_DROP:
                    if (v instanceof PuzzlePiece) {
                        PuzzlePiece target = (PuzzlePiece) v;
                        swapPieces(dropped, target);
                    } else {
                        // Drop was not on a valid target, make sure piece is visible
                        dropped.setAlpha(1.0f);
                        dropped.setVisibility(View.VISIBLE);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        // Drop was unsuccessful, make sure piece is visible
                        dropped.setAlpha(1.0f);
                        dropped.setVisibility(View.VISIBLE);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private void swapPieces(PuzzlePiece dropped, PuzzlePiece target) {
        ViewGroup parent = (ViewGroup) dropped.getParent();
        int droppedIndex = parent.indexOfChild(dropped);
        int targetIndex = parent.indexOfChild(target);

        // Swap positions in the parent view
        parent.removeView(dropped);
        parent.removeView(target);

        parent.addView(dropped, targetIndex);
        parent.addView(target, droppedIndex);

        // Swap currentRow and currentCol between dropped and target
        int droppedRow = dropped.getCurrentRow();
        int droppedCol = dropped.getCurrentCol();
        dropped.setCurrentRow(target.getCurrentRow());
        dropped.setCurrentCol(target.getCurrentCol());
        target.setCurrentRow(droppedRow);
        target.setCurrentCol(droppedCol);

        Log.d("PuzzleBoard", "Swapped pieces at indices: " + droppedIndex + " and " + targetIndex);

        // Set visibility after re-adding to ensure correct order
        dropped.setVisibility(View.VISIBLE);
        target.setVisibility(View.VISIBLE);

        checkCompletion(); // Check for completion after a piece swap
    }

    private class PuzzlePieceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v instanceof PuzzlePiece) {
                selectedPiece = (PuzzlePiece) v;
                ((MainActivity) context).setSelectedPiece(selectedPiece);
                // Optionally, highlight the selected piece or provide some visual feedback
            }
        }
    }
}
