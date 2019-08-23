package com.yura.buttleship;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;

public class BattleShipCreatingActivity extends AppCompatActivity {
    public static final String EXTRA_POSITIONS = "viewPositions";
    int buttonSize;
    Button[][] viewPositions = new Button[10][10];
    int positionStyle = R.drawable.button_border;
    int[][] positions = new int[10][10];
    public static final boolean RIGHT_DIRECTION = false;
    public static final boolean DOWN_DIRECTION = true;
    boolean vectorDirection = RIGHT_DIRECTION;
    public static final String vectorHorizontalText = "Слева-направо";
    public static final String vectorVerticalText = "Сверху-вниз";
    int shipValue = 0;
    int x1Ships = 4;
    int x2Ships = 3;
    int x3Ships = 2;
    int x4Ships = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buttle_ship_creating);
        buttonSize = BattleShipHelper.getFullscreenGameButtonSize(this);
        creating();
    }

    public void creating() {
        setTitle("Расстановка кораблей");
        BattleShipHelper.createFirstColumns(this, false, buttonSize, BattleShip.buttonTextSize);
        //Отрисовываем поле для перетаскивания кораблей
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = BattleShipHelper.addButton(this, "", (j + 1) * buttonSize, (i + 1) * buttonSize, buttonSize, buttonSize, BattleShip.buttonTextSize);
                button.setBackgroundResource(positionStyle);
                button.setTag(0);
                button.setOnDragListener(dragListener());
                viewPositions[i][j] = button;
            }
        }
        // Кнопка для изменения ориентации устанавливаемого корабля
        final Button vectorButton = BattleShipHelper.addButton(
                this,
                vectorHorizontalText,
                (int) (buttonSize * 2.5),
                (int) (buttonSize * 13.5),
                buttonSize * 7, buttonSize,
                BattleShip.buttonTextSize
        );
        vectorButton.setBackgroundColor(Color.DKGRAY);
        vectorButton.setTextColor(Color.WHITE);
        vectorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPossibility(false);
                vectorDirection = !vectorDirection;
                viewPossibility(true);
                if (vectorButton.getText().toString().equals(vectorHorizontalText)) {
                    vectorButton.setText(vectorVerticalText);
                } else {
                    vectorButton.setText(vectorHorizontalText);
                }
            }
        });
        //Кнопка 4-х палубного корабля
        Button button = BattleShipHelper.addButton(this, "XXXX", buttonSize, buttonSize * BattleShip.LAYOUT_COLUMNS, buttonSize * 2, buttonSize, BattleShip.buttonTextSize);
        button.setBackgroundColor(Color.DKGRAY);
        button.setTextColor(Color.WHITE);
        button.setOnLongClickListener(shipListener(4));
        //Кнопка 3-х палубного корабля
        button = BattleShipHelper.addButton(this, "XXX", buttonSize * 4, buttonSize * BattleShip.LAYOUT_COLUMNS, buttonSize * 2, buttonSize, BattleShip.buttonTextSize);
        button.setBackgroundColor(Color.DKGRAY);
        button.setTextColor(Color.WHITE);
        button.setOnLongClickListener(shipListener(3));
        //Кнопка 2-х палубного корабля
        button = BattleShipHelper.addButton(this, "XX", buttonSize * 7, buttonSize * BattleShip.LAYOUT_COLUMNS, buttonSize * 2, buttonSize, BattleShip.buttonTextSize);
        button.setBackgroundColor(Color.DKGRAY);
        button.setTextColor(Color.WHITE);
        button.setOnLongClickListener(shipListener(2));
        //Кнопка 1-х палубного корабля
        button = BattleShipHelper.addButton(this, "X", buttonSize * 10, buttonSize * BattleShip.LAYOUT_COLUMNS, buttonSize, buttonSize, BattleShip.buttonTextSize);
        button.setBackgroundColor(Color.DKGRAY);
        button.setTextColor(Color.WHITE);
        button.setOnLongClickListener(shipListener(1));
    }
    //Слушатель переноса корабля на основное поля
    public View.OnDragListener dragListener() {
        return new View.OnDragListener() {
            int x = 0;
            int y = 0;
            @Override
            public boolean onDrag(View v, DragEvent event) {
                try {
                    switch (event.getAction()) {
                        case DragEvent.ACTION_DRAG_ENTERED:
                            x = (int) (event.getY() / buttonSize);
                            y = (int) (event.getX() / buttonSize);
                            aroundView(x, y, false);
                            viewPossibility(true);
                            break;
                        case DragEvent.ACTION_DRAG_EXITED:
                            BattleShipHelper.clearDragShadowEffect(positions, viewPositions, positionStyle);
                            v.invalidate();
                            break;
                        case DragEvent.ACTION_DROP:
                            Button button = (Button) event.getLocalState();
                            if (getCurrentShipCount() != 0) {
                                viewPossibility(false);
                                if (!aroundView(x, y, true)) {
                                    Toast.makeText(v.getContext(), "Невозможно установить корабль на указанную позицию", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                checkReady();
                            }
                            if (getCurrentShipCount() == 0) {
                                button.setClickable(false);
                                button.setOnLongClickListener(null);
                                button.setText("");
                                button.setBackgroundColor(Color.LTGRAY);
                                button.invalidate();
                            }
                            shipValue = 0;
                            break;
                    }
                //Игнорируем перенос на кнопки нумераций ячеек
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
                return true;
            }
        };
    }
    //Слушатель захвата кнопки корабля для переноса
    public View.OnLongClickListener shipListener(final int value) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewPossibility(false);
                shipValue = value;
                viewPossibility(true);
                v.startDrag(null, new View.DragShadowBuilder(v), v, 0);
                return true;
            }
        };
    }
    // Получение информации о захваченном корабле
    private int getCurrentShipCount() {
        int shipCount = 0;
        switch (shipValue) {
            case (1):
                shipCount = x1Ships;
                break;
            case (2):
                shipCount = x2Ships;
                break;
            case (3):
                shipCount = x3Ships;
                break;
            case (4):
                shipCount = x4Ships;
                break;
        }
        return shipCount;
    }
    //Скрытие позиций, куда невозможно установить корабль
    public void viewPossibility(boolean notClickable) {
        boolean shipIsAddable = shipValue <= 2 ? x2Ships != 0
                : shipValue == 3 ? x3Ships != 0
                : x4Ships != 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (shipIsAddable && positions[i][j] == 0 && shipValue != 0)
                    if (vectorDirection == DOWN_DIRECTION && (i > 10 - shipValue)
                            || vectorDirection == RIGHT_DIRECTION && (j > 10 - shipValue)) {
                        Button current = viewPositions[i][j];
                        current.setClickable(!notClickable);
                        if(((int) current.getTag()) != 1 ) {
                            if (!notClickable) {
                                current.setBackgroundResource(positionStyle);
                            } else {
                                current.setBackgroundResource(R.drawable.disabled_button_border);
                            }
                            current.setText(positions[i][j] == 0 ? "" : BattleShip.SHIP_DISPLAY_VALUE);
                            current.invalidate();
                        }
                    }
            }
        }
    }
    //Проверка возможности установить корабль на указаннную ячейку с последующей установкой корабля
    public boolean aroundView(int x, int y, boolean select) {
        x--;
        y--;
        int needFreeCount = shipValue;
        ArrayList<ShipCoordination> list = new ArrayList<>();
        try {
            while (needFreeCount > 0) {
                if (positions[x][y] == 0) {
                    ShipCoordination coordination = new ShipCoordination(x, y);
                    list.add(coordination);
                    if (vectorDirection == DOWN_DIRECTION) {
                        x++;
                    } else {
                        y++;
                    }
                    needFreeCount--;
                } else {
                    viewPossibility(false);
                    return false;
                }
                if ((x == 10 || y > 10) & needFreeCount != 0) {
                    viewPossibility(false);
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            viewPossibility(true);
            return false;
        }
        for (ShipCoordination sc : list) {
            x = sc.getX();
            y = sc.getY();
            for (int i = -1; i < 2; i++) {
                if (x + i > -1 && x + i < 10) {
                    for (int j = -1; j < 2; j++) {
                        if (y + j > -1 && y + j < 10) {
                            if (!(i == 0 && j == 0)) {
                                int q = x + i;
                                int w = y + j;
                                if (!list.contains(new ShipCoordination(q, w)) && positions[q][w] != 0) {
                                    viewPossibility(false);
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        for (ShipCoordination sc : list) {
            if (select) {
                positions[sc.getX()][sc.getY()] = shipValue;
            }
            viewPositions[sc.getX()][sc.getY()].setTag(1);
            BattleShipHelper.changeButtonStyle(viewPositions[sc.getX()][sc.getY()], BattleShip.SHIP_DISPLAY_VALUE, Color.DKGRAY);
        }
        if (select) {
            if (shipValue == 1) {
                x1Ships--;
            }
            if (shipValue == 2) {
                x2Ships--;
            }
            if (shipValue == 3) {
                x3Ships--;
            }
            if (shipValue == 4) {
                x4Ships--;
            }
        }
        viewPossibility(false);
        return true;
    }
    // Проверка на завершенность расстановки
    public void checkReady() {
        if (x1Ships + x2Ships + x3Ships + x4Ships == 0) {
            toBattle();
        }
        ;
    }
    // Возвращение json строки с позициями
    public String getShipPositions() {
        JSONArray array = new JSONArray(Arrays.asList(positions));
        return array.toString();
    }
    // Переход в активность начала игры с передачей указанных позиций
    public void toBattle() {
        Intent intent = new Intent(this, BattleshipActivity.class);
        intent.putExtra(BattleShipCreatingActivity.EXTRA_POSITIONS, getShipPositions());
        startActivity(intent);
    }


}
