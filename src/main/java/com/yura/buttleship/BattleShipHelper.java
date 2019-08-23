package com.yura.buttleship;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;


public class BattleShipHelper {
    // Изменение стиля кнопки
    public static void changeButtonStyle(Button button, String value, int backgroundColor, int textSize){
        button.setFocusable(false);
        button.setOnClickListener(null);
        button.setPadding(0, 0, 0 ,0);
        button.setText(value);
        button.setTextSize(textSize);
        button.setBackgroundColor(backgroundColor);
        button.setTextColor(Color.WHITE);
        button.invalidate();
    }
    // Изменение стиля кнопки
    public static void changeButtonStyle(Button button, String value, int backgroundColor) {
        changeButtonStyle(button, value, backgroundColor, 25);
    }
    //Отрисовываем кнопки нумерация ячеек
    public static void createFirstColumns(AppCompatActivity activity, boolean createOrGame, int buttonSize, int textSize){
        int k = createOrGame ? 118 : 107;
        k = 107;
        for (int i = 97; i < k; i++) {
            if (i != 107) {
                addButton(activity, (i < k ? (char) i : (char) (i - 11)) + "", (i + 1 - 97) * buttonSize, 0, buttonSize, buttonSize, textSize).setClickable(false);
            }
        }
        for (int i = 0; i < 10; i++) {
            addButton(activity, "" + (1 + i), 0, (1 + i) * buttonSize, buttonSize, buttonSize, textSize).setClickable(false);
        }
        for (int i = 0; i < 10; i++) {
            addButton(activity, "" + (1 + i), buttonSize * 11, (1 + i) * buttonSize, buttonSize, buttonSize, textSize).setClickable(false);
        }
    }
    // Добавляем кнопку в активность
    public static Button addButton(AppCompatActivity activity, String text, float x, float y, int width, int height, int textSize) {
        Button button = new Button(activity);
        button.setText(text);
        button.setBackgroundColor(Color.LTGRAY);
        button.setX(x);
        button.setY(y);
        button.setTextSize(textSize);
        button.setPadding(0, 0, 0 ,0);
        button.setTextColor(Color.BLACK);
        activity.addContentView(button, new LinearLayout.LayoutParams(width, height));
        return button;
    }
    // Получаем массив с позициями кораблей из json-строки
    public static int[][] positionsFromJson(String json) {
        int[][] res = new int[10][10];
        try {
            JSONArray jsonPositions = new JSONArray(json);
            for (int i = 0; i < jsonPositions.length(); i++) {
                JSONArray raw = new JSONArray(jsonPositions.getJSONArray(i).toString());
                for (int j = 0; j < raw.length(); j++) {
                    res[i][j] = Integer.parseInt(raw.getString(j));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }
    // Очищаем поле от ячеек нарисованных в процессе определения позиции корабля
    public static void clearDragShadowEffect(int[][] positions, Button[][] viewPositions, int background) {
        for(int i = 0; i < positions.length; i++) {
            for(int j = 0; j < positions[i].length; j++) {
                if (positions[i][j] == 0) {
                    viewPositions[i][j].setBackgroundResource(background);
                    viewPositions[i][j].invalidate();
                    viewPositions[i][j].setTag(0);
                }
            }
        }
    }

    // Получаем размер ячейки поля для полноэкранной отрисовки
    public static int getFullscreenGameButtonSize(AppCompatActivity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return (int) size.x / BattleShip.LAYOUT_COLUMNS;
    }
}
