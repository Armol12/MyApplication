package com.zxc.myapplication;

import static com.zxc.myapplication.BitmapUtils.scaleBitmapToSquare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GridLayout puzzleBoard;
    private Button rotateButton;
    private ImageView selectedShape;
    private ImageView previousSelectedShape;
    private Float[][] solutionMatrix;
    private Bitmap[][] imageParts;
    private ArrayList<ImageView> imageViewList;
    private int rows;
    private int cols;
    private boolean isTimed;
    private int timerDuration;
    private int puzzleCount = 1; // Номер текущего пазла
    private CountDownTimer countDownTimer;
    private TextView timerTextView;

    private int[] imageResources = {
            R.drawable.sample_image,
            R.drawable.puzzle,
            R.drawable.puzzle1,
            R.drawable.puzzle2,
            R.drawable.puzzle3,
            R.drawable.puzzle4,
            R.drawable.puzzle5,
            R.drawable.puzzle6,
            R.drawable.puzzle7
    };

    private boolean useRandomImage;
    private boolean isImageSelectionActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        puzzleBoard = findViewById(R.id.gameBoard);
        rotateButton = findViewById(R.id.button_rotate);
        timerTextView = findViewById(R.id.timerTextView);
        ImageButton surrenderButton = findViewById(R.id.surrender_button);

        // Загрузка настроек
        loadPreferences();

        // Показ диалогового окна для выбора изображения, если не выбран режим случайных изображений
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        useRandomImage = sharedPref.getBoolean("is_random_image", false); // исправление параметра
        if (useRandomImage) {
            initializeGame(getRandomImage());
        } else {
            showImageSelectionDialog();
        }

        surrenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSurrenderDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isImageSelectionActive) {
            super.onBackPressed();
        } else {
            // Иначе не делаем ничего
        }
    }

    private void showImageSelectionDialog() {
        isImageSelectionActive = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_selection, null);
        builder.setView(dialogView);

        ViewPager viewPager = dialogView.findViewById(R.id.viewPager);
        List<Integer> imageList = new ArrayList<>();
        for (int image : imageResources) {
            imageList.add(image);
        }
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageList);
        viewPager.setAdapter(adapter);

        Button backButton = dialogView.findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        AlertDialog dialog = builder.create();

        Button playButton = dialogView.findViewById(R.id.button_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedImage = adapter.getImageAt(viewPager.getCurrentItem());
                dialog.dismiss();
                isImageSelectionActive = false;
                initializeGame(selectedImage);
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void showSurrenderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сдаться")
                .setMessage("Вы действительно хотите сдаться?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        showGameOverDialog();
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Игра окончена")
                .setMessage("Вы проиграли")
                .setPositiveButton("На главный экран", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void initializeGame(int selectedImage) {
        Log.d(TAG, "Selected Image: " + selectedImage);
        loadSelectedImage(selectedImage);

        // Установка параметров GridLayout
        puzzleBoard.setRowCount(rows);
        puzzleBoard.setColumnCount(cols);

        // Инициализация игрового поля с фигурами
        initializeGameBoard(selectedImage); // Pass the selected image to the game board initialization

        // Установка onClickListener для кнопки поворота
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateShape();
                checkSolution();
            }
        });

        if (isTimed) {
            startTimer(timerDuration);
        }
    }

    private void loadPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String puzzleSize = sharedPref.getString("puzzle_size", "4x4");
        isTimed = sharedPref.getBoolean("is_timed", false);
        timerDuration = sharedPref.getInt("timer_duration", 60);

        switch (puzzleSize) {
            case "3x3":
                rows = 3;
                cols = 3;
                break;
            case "4x4":
                rows = 4;
                cols = 4;
                break;
            case "5x5":
                rows = 5;
                cols = 5;
                break;
            case "6x6":
                rows = 6;
                cols = 6;
                break;
            default:
                rows = 4;
                cols = 4;
                break;
        }
    }

    private void loadSelectedImage(int selectedImage) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), selectedImage);
        if (bitmap == null) {
            Log.e(TAG, "Failed to load selected image");
        } else {
            Log.d(TAG, "Selected image loaded successfully");
        }
        // Further processing to set up the puzzle game with this bitmap...
    }

    private void initializeGameBoard(int puzzleImage) {
        Log.d(TAG, "Initializing game board with image: " + puzzleImage);
        // Инициализация матрицы решения
        solutionMatrix = new Float[rows][cols];

        // Разделение изображения на части
        splitImage(puzzleImage, rows, cols);

        imageViewList = new ArrayList<>();

        int padding = 16; // отступы от краев экрана
        puzzleBoard.setPadding(padding, padding, padding, padding);

        // Получаем размеры puzzleBoard после layout pass
        puzzleBoard.post(new Runnable() {
            @Override
            public void run() {
                int puzzleBoardWidth = puzzleBoard.getWidth();
                int puzzleBoardHeight = puzzleBoard.getHeight();
                int pieceSize = Math.min(puzzleBoardWidth / cols, puzzleBoardHeight / rows) - 10; // размер части с учетом отступов

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        ImageView shape = new ImageView(MainActivity.this);
                        shape.setImageBitmap(imageParts[i][j]); // Устанавливаем часть изображения
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = pieceSize;  // Устанавливаем ширину с отступами
                        params.height = pieceSize; // Устанавливаем высоту с отступами
                        params.setMargins(5, 5, 5, 5); // Устанавливаем отступы
                        shape.setLayoutParams(params);
                        shape.setTag(new int[]{i, j}); // Изначально сохраняем позицию фигуры в теге

                        shape.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (previousSelectedShape != null) {
                                    previousSelectedShape.setBackgroundResource(0); // Возвращаем фон к исходному состоянию
                                }
                                selectedShape = (ImageView) v;
                                selectedShape.setBackgroundResource(R.drawable.selected_puzzle_piece); // Подсвечиваем выбранный пазл зеленым
                                previousSelectedShape = selectedShape;
                            }
                        });

                        shape.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                                v.startDragAndDrop(null, shadowBuilder, v, 0);
                                return true;
                            }
                        });

                        shape.setOnDragListener(new View.OnDragListener() {
                            @Override
                            public boolean onDrag(View v, DragEvent event) {
                                switch (event.getAction()) {
                                    case DragEvent.ACTION_DRAG_STARTED:
                                        return true;
                                    case DragEvent.ACTION_DROP:
                                        ImageView dropped = (ImageView) event.getLocalState();
                                        BitmapDrawable droppedDrawable = (BitmapDrawable) dropped.getDrawable();
                                        Bitmap droppedBitmap = droppedDrawable.getBitmap();
                                        int[] droppedTag = (int[]) dropped.getTag();
                                        float droppedRotation = dropped.getRotation(); // Сохраняем поворот

                                        ImageView target = (ImageView) v;
                                        BitmapDrawable targetDrawable = (BitmapDrawable) target.getDrawable();
                                        Bitmap targetBitmap = targetDrawable.getBitmap();
                                        int[] targetTag = (int[]) target.getTag();
                                        float targetRotation = target.getRotation(); // Сохраняем поворот

                                        dropped.setImageBitmap(targetBitmap);
                                        dropped.setTag(targetTag);
                                        dropped.setRotation(targetRotation); // Устанавливаем поворот

                                        target.setImageBitmap(droppedBitmap);
                                        target.setTag(droppedTag);
                                        target.setRotation(droppedRotation); // Устанавливаем поворот

                                        checkSolution(); // Call checkSolution after dropping a piece

                                        return true;
                                    default:
                                        return true;
                                }
                            }
                        });

                        puzzleBoard.addView(shape);
                        imageViewList.add(shape);
                        // Устанавливаем ожидаемое значение в матрицу решения
                        solutionMatrix[i][j] = 0f; // Все фигуры изначально должны быть с поворотом 0
                    }
                }

                // Перемешивание пазла
                shufflePuzzle();
            }
        });
    }

    private void rotateShape() {
        if (selectedShape != null) {
            float currentRotation = (float) selectedShape.getRotation();
            float newRotation = (currentRotation + 90) % 360;
            selectedShape.setRotation(newRotation);
            selectedShape.setTag(selectedShape.getTag()); // Обновляем тег фигуры после поворота
            Log.d(TAG, "Shape rotated to: " + newRotation);
            checkSolution(); // Call checkSolution after rotating a piece
        } else {
            Log.d(TAG, "No shape selected");
        }
    }

    private boolean isPuzzleSolved() {
        for (int i = 0; i < imageViewList.size(); i++) {
            ImageView shape = imageViewList.get(i);
            int[] pos = (int[]) shape.getTag();
            float expectedRotation = solutionMatrix[pos[0]][pos[1]];
            if (shape.getRotation() != expectedRotation || (shape.getLeft() / shape.getWidth() != pos[1]) || (shape.getTop() / shape.getHeight() != pos[0])) {
                return false;
            }
        }
        return true;
    }

    private void checkSolution() {
        if (isPuzzleSolved()) {
            if (isTimed && countDownTimer != null) {
                countDownTimer.cancel();
            }
            Toast.makeText(this, "Пазл собран!", Toast.LENGTH_SHORT).show();
            showCompletionDialog();
        }
    }

    public void showCompletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Игра окончена")
                .setMessage("Вы прошли пазл номер " + puzzleCount)
                .setPositiveButton("Продолжить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        puzzleCount++;
                        resetGameBoard(); // Очищаем игровое поле перед новым пазлом
                        if (isTimed) {
                            startTimer(timerDuration); // Перезапускаем таймер
                        }
                        if (useRandomImage) {
                            initializeGame(getRandomImage()); // Запускаем новый пазл с случайным изображением
                        } else {
                            showImageSelectionDialog(); // Показать слайдер выбора изображения
                        }
                        dialog.dismiss(); // Закрываем диалог
                    }
                })
                .setNegativeButton("На главный экран", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private int getRandomImage() {
        Random random = new Random();
        int randomIndex = random.nextInt(imageResources.length);
        return imageResources[randomIndex];
    }

    public void setSelectedPiece(ImageView piece) {
        if (previousSelectedShape != null) {
            previousSelectedShape.setBackgroundResource(0); // Возвращаем фон к исходному состоянию
        }
        selectedShape = piece;
        selectedShape.setBackgroundResource(R.drawable.selected_puzzle_piece); // Подсвечиваем выбранный пазл зеленым
        previousSelectedShape = selectedShape;
    }

    private void shufflePuzzle() {
        Collections.shuffle(imageViewList);
        Random random = new Random();

        for (int i = 0; i < imageViewList.size(); i++) {
            ImageView shape = imageViewList.get(i);
            float randomRotation = random.nextInt(4) * 90; // Случайный угол поворота: 0, 90, 180, 270
            shape.setRotation(randomRotation);
            shape.setTag(shape.getTag());

            if (shape.getParent() != null) {
                ((ViewGroup) shape.getParent()).removeView(shape);
            }

            puzzleBoard.addView(shape, i);
        }
    }

    private void startTimer(int seconds) {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Отменяем предыдущий таймер, если он существует
        }
        timerTextView.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Осталось времени: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                showGameOverDialog();
            }
        }.start();
    }

    private void splitImage(int resourceId, int rows, int cols) {
        // Загружаем исходное изображение
        Bitmap original = BitmapFactory.decodeResource(getResources(), resourceId);
        if (original == null) {
            Log.e(TAG, "Failed to decode resource image");
            return;
        }
        Bitmap scaledBitmap = scaleBitmapToSquare(original);
        int width = scaledBitmap.getWidth() / cols;
        int height = scaledBitmap.getHeight() / rows;
        imageParts = new Bitmap[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                imageParts[i][j] = Bitmap.createBitmap(scaledBitmap, j * width, i * height, width, height);
            }
        }
        Log.d(TAG, "Image split into parts successfully");
    }

    private void resetGameBoard() {
        puzzleBoard.removeAllViews();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Останавливаем таймер
            countDownTimer = null; // Сбрасываем таймер
        }
        timerTextView.setVisibility(View.GONE); // Скрываем текст таймера
    }
}
