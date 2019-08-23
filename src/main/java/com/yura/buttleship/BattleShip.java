package com.yura.buttleship;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by Юра on 19.11.2015.
 */
public class BattleShip {
    public final static int LAYOUT_COLUMNS = 12;
    private final static boolean RIGHT_DIRECTION = false;
    private final static boolean DOWN_DIRECTION = true;
    private final static Boolean DETECT_DIRECTION = null;
    public final static String WIN_MESSAGE = "Победа!";
    public final static String LOSE_MESSAGE = "Вы проиграли!";
    public final static String WAIT_MESSAGE = "Ожидайте, ход противника";
    public final static String READY_MESSAGE = "Готовы? Ваш ход.";
    public final static String HIT_NONE_RESULT_VALUE = "0";
    public final static String HIT_RESULT_VALUE = "H";
    public final static String SHIP_DISPLAY_VALUE = "X";
    private final int localPositionAreaScaleValue = 3;
    private BattleshipActivity activity;
    public static int buttonSize = 50;
    public static int buttonTextSize = 12;
    final static int WIN_POINTS = 50;
    Button[][] viewPositions = new Button[10][10];
    private Button[][] enemyViewPositions = new Button[10][10];
    private boolean clickableEnemyArea;
    int points = 0;

    BattleShip(final ObjectOutputStream oos, final int[][] positions, final int[][] enemyPositions, final BattleshipActivity activity) {
        this.activity = activity;
        buttonSize = BattleShipHelper.getFullscreenGameButtonSize(activity);
        BattleShipHelper.createFirstColumns(activity, true, buttonSize, buttonTextSize);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Создание миниатюры своего поля в активности, внизу экрана
                Button myShip = BattleShipHelper.addButton(
                        activity,
                        "",
                        (j + 1) * buttonSize / localPositionAreaScaleValue + buttonSize,
                        (i + 1) * buttonSize / localPositionAreaScaleValue + buttonSize * LAYOUT_COLUMNS,
                        buttonSize / localPositionAreaScaleValue,
                        buttonSize / localPositionAreaScaleValue,
                        buttonTextSize / localPositionAreaScaleValue
                );
                myShip.setBackgroundResource(R.drawable.button_border);
                viewPositions[i][j] = myShip;
                if (positions[i][j] != 0) {
                    BattleShipHelper.changeButtonStyle(myShip, SHIP_DISPLAY_VALUE, Color.DKGRAY, buttonTextSize / 3);
                }
                // Создание поля противника с полученными позициями кораблей
                final Button enemyShip = BattleShipHelper.addButton(activity, "", (j + 1) * buttonSize, (i + 1) * buttonSize, buttonSize, buttonSize, buttonTextSize);
                enemyShip.setTag(enemyPositions[i][j]);
                enemyShip.setBackgroundResource(R.drawable.button_border);
                final int x = i;
                final int y = j;
                enemyShip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (clickableEnemyArea && enemyShip.getText().toString().equals("")) {
                            String hitResult = (enemyPositions[x][y] == 0) ? HIT_NONE_RESULT_VALUE : HIT_RESULT_VALUE;
                            BattleShipHelper.changeButtonStyle(enemyShip, hitResult, Color.LTGRAY, buttonTextSize);
                            Runnable sender = new Runnable(){
                                @Override
                                public void run() {
                                    try {
                                        oos.writeObject(new Target(x,y));
                                    } catch (IOException ignored) { }
                                }
                            };
                            new Thread(sender).start();
                            enemyShip.setClickable(false);
                            invalidateEnemyArea();
                            if (points == WIN_POINTS){
                                Message message = new Message();
                                message.what = BattleshipActivity.Client.TOAST_END_MESSAGE;
                                message.obj = WIN_MESSAGE;
                                activity.clientHandler.sendMessage(message);
                                new Thread(sender).start();
                            }
                        } else if(!clickableEnemyArea) {
                            Toast.makeText(activity, WAIT_MESSAGE, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                this.enemyViewPositions[i][j] = enemyShip;
            }
        }
    }
    //Меняем ограничение на взможность открытия ячейки противника
    public void enableActions(boolean flag) {
        this.clickableEnemyArea = flag;
    }
    //Перерисовываем поле противника, и для ячеек содержащих информацию о попадании в корабль, проверяем состояние корабля
    public void invalidateEnemyArea() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (enemyViewPositions[i][j].getText().toString().equals(HIT_RESULT_VALUE)) {
                    hasKiled(i, j, (Integer) enemyViewPositions[i][j].getTag(), DETECT_DIRECTION, new ArrayList<Button>());
                }
            }
        }
    }
    // Проверяем сосотояние коробля, и выделяем корабля и его окружение в случае, если он был убит
    public void hasKiled(int i, int j, int shipValue, Boolean direction, ArrayList<Button> shipParts) {
        //Проверяем равно ли количество открытых рядом ячеек размеру корабля
        if (shipValue != shipParts.size()) {
            try {
                shipParts.add(enemyViewPositions[i][j]);
            } catch (ArrayIndexOutOfBoundsException  e) {
                return;
            }
            if (direction == DETECT_DIRECTION) {
                boolean right = j < 9 && enemyViewPositions[i][j + 1].getText().toString().equals(HIT_RESULT_VALUE);
                boolean down = i < 9 && enemyViewPositions[i + 1][j].getText().toString().equals(HIT_RESULT_VALUE);
                if (right & !down) {
                    hasKiled(i, j + 1, shipValue, RIGHT_DIRECTION, shipParts);
                } else {
                    if (!right & down) {
                            hasKiled(i + 1, j, shipValue, DOWN_DIRECTION, shipParts);
                    }else{
                        try{
                            hasKiled(-1,-1,shipValue, DETECT_DIRECTION, shipParts);
                        }catch (ArrayIndexOutOfBoundsException ignored){}
                    }
                }
                return;
            }
            if (direction == RIGHT_DIRECTION){
                hasKiled(i, j + 1, shipValue, RIGHT_DIRECTION, shipParts);
            }
            if (direction == DOWN_DIRECTION){
                hasKiled(i + 1, j, shipValue, DOWN_DIRECTION, shipParts);
            }
        } else {
            //Исключаем случай проверки пустой ячейки
            boolean viewIsShip = shipValue != 0;
            for (Button button : shipParts) {
                viewIsShip &= button.getText().toString().equals(HIT_RESULT_VALUE);
            }
            if (viewIsShip) {
                for (Button shipPart : shipParts) {
                    int x = (int) ((shipPart.getY()) / buttonSize - 1);
                    int y = (int) ((shipPart.getX() - buttonSize) / buttonSize);
                    for (int k = -1 ; k < 2; k++){
                        if (x + k > -1 && x + k < 10){
                            for (int l = -1; l < 2; l ++){
                                if (y + l > -1 && y + l < 10){
                                    Button neighbor = enemyViewPositions[x + k][y + l];
                                    String neighborText = neighbor.getText().toString();
                                    if (neighborText.equals("") || neighborText.equals(HIT_NONE_RESULT_VALUE)) {
                                        neighbor.setClickable(false);
                                        BattleShipHelper.changeButtonStyle(neighbor,neighbor.getTag() + "", Color.YELLOW, buttonTextSize);
                                    }
                                }
                            }
                        }
                    }
                    BattleShipHelper.changeButtonStyle(shipPart, SHIP_DISPLAY_VALUE, Color.DKGRAY);
                    points += shipValue;
                }
            }
        }
    }
}

