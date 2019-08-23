package com.yura.buttleship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;


public class BattleshipActivity extends AppCompatActivity {
    Server server;
    Handler serverHandler;
    Handler clientHandler;
    Button serverStart;
    boolean serverWork = true;
    String ip;
    Button connect;
    TextView serverResults;
    TextView username;
    int[][] myPositions = new int[10][10];
    ArrayList<Connection> connections;
    BattleShip battleship;
    String jsonPositions = null;
    DBHelper dbHelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_position);
        setTitle("Подготовка к бою");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //получение переданных из активности расстановки кораблей данных об их позициях
        if (jsonPositions == null) {
            jsonPositions = getIntent().getStringExtra(BattleShipCreatingActivity.EXTRA_POSITIONS);
        }
        //Инициализация основных кнопок для запуска игры
        if (jsonPositions != null && !jsonPositions.equals("")) {
            myPositions = BattleShipHelper.positionsFromJson(jsonPositions);
            serverStart = findViewById(R.id.serverStart);
            serverStart.setOnClickListener(serverStartListener());
            connect = findViewById(R.id.connect);
            connect.setOnClickListener(connectListener());
            serverResults = findViewById(R.id.serverResults);
            username = findViewById(R.id.username);
            jsonPositions = "";
        }
    }
    // Слушатель для остановки и запуска сервера
    private View.OnClickListener serverStartListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connections = new ArrayList<>();
                serverHandler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        ip = (String) msg.obj;
                        serverResults.setText("IP запущенного сервера: " + ip);
                        serverStart.setText("Остановить сервер");
                        serverStart.setOnClickListener(serverStopListener());
                    }
                };
                serverWork = true;
                server = new Server();
                server.start();
            }
        };
    }
    // Слушатель для остановки запущенного сервера
    private View.OnClickListener serverStopListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverStart.setText("Запустить сервер");
                serverStart.setOnClickListener(serverStartListener());
                serverWork = false;
                serverResults.setText("");
            }
        };
    }
    // Слушатель для подключения к указанному серверу
    private View.OnClickListener connectListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ip == null) {
                    ip = ((EditText) findViewById(R.id.serverIp)).getText().toString();
                }
                setContentView(R.layout.activity_buttle_ship_creating);
                BattleshipActivity.this.setTitle("Ожидание второго игрока");
                Client client = new Client();
                new Thread(client).start();
            }
        };
    }
    // Класс для запуска сервера в отдельном потоке
    public class Server extends Thread {
        final static int PORT = 1234;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiMan != null) {
                    ip = getWifiApIpAddress().split("/")[0];;
                } else {
                    ip = (InetAddress.getLocalHost() + "").split("/")[1];
                    throw  new IOException();
                }
                serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName(ip));
                Message message = new Message();
                message.obj = ip;
                serverHandler.sendMessage(message);
                Socket client = serverSocket.accept();
                Socket client2 = serverSocket.accept();
                connections.add(new Connection(client, client2));
                while (serverWork) {
                    Thread.sleep(500);
                }
                serverSocket.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(BattleshipActivity.this, "Необходимо подключение к wi-fi", Toast.LENGTH_SHORT).show();
            }
        }
        // получения ip адреса wifi-адаптера
        private String getWifiApIpAddress() {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                        .hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    if (intf.getName().contains("wlan")) {
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                                .hasMoreElements();) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress()
                                    && (inetAddress.getAddress().length == 4)) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            } catch (SocketException ignored) { }
            return null;
        }

    }
    // Класс для подключения к серверу начала игры
    public class Client implements Runnable {
        BattleshipActivity activity;
        final static int BATTLESHIP_DRAW_MESSAGE = 1;
        final static int CHANGE_BUTTON_STYLE_MESSAGE = 2;
        final static int TOAST_MESSAGE = 3;
        final static int TOAST_END_MESSAGE = 4;
        long startTime;
        int[][] enemyPositions;
        Socket socket;
        ObjectOutputStream oos;
        ObjectInputStream ois;
        public Client() {
            activity = BattleshipActivity.this;
            Toast.makeText(BattleshipActivity.this, "Вы готовы к игре.", Toast.LENGTH_SHORT).show();
            clientHandler = new Handler(getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    switch (msg.what) {
                        //Начать игру
                        case BATTLESHIP_DRAW_MESSAGE:
                            startTime = System.currentTimeMillis();
                            battleship = new BattleShip(oos, myPositions, enemyPositions, activity);
                            break;
                        //Сменить стиль кнопки
                        case CHANGE_BUTTON_STYLE_MESSAGE:
                            Target t = (Target) msg.obj;
                            BattleShipHelper.changeButtonStyle(battleship.viewPositions[t.getI()][t.getJ()], t.getText(), t.getColor());
                            break;
                        //Завершение игры
                        case TOAST_END_MESSAGE:
                            startActivity(new Intent(BattleshipActivity.this, MainActivity.class));
                            try {
                                if (battleship.points == BattleShip.WIN_POINTS) {
                                    saveRecord();
                                }
                                Thread.sleep(1000);
                                socket.close();
                                if (server != null) {
                                    server.serverSocket.close();
                                }
                                serverWork = false;
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        //Отображение сообщения
                        case TOAST_MESSAGE:
                            Toast.makeText(activity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };
        }

        @Override
        public void run() {
            ///open socket streams
            try {
                socket = new Socket(ip, Server.PORT);
                clientHandler.sendMessage(getHandlerMessage(TOAST_MESSAGE, "Соединение успешно установлено"));
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                oos.writeObject(username.getText().toString());
                oos.writeObject(myPositions);
                boolean first = (boolean) ois.readObject();
                enemyPositions = (int[][]) ois.readObject();
                final String enemyName = (String) ois.readObject();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BattleshipActivity.this.setTitle("Вы играете против " + enemyName);
                    }
                });
                clientHandler.sendMessage(getHandlerMessage(BATTLESHIP_DRAW_MESSAGE));
                for (boolean endGame = false; !endGame;) {
                    //Параметр определяющий передачу хода сопернику
                    final boolean flag = first;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            battleship.enableActions(flag);
                        }
                    });
                    if (!flag) {
                        clientHandler.sendMessage(getHandlerMessage(TOAST_MESSAGE, BattleShip.WAIT_MESSAGE));
                        // Был ли удар решающим
                        endGame = (boolean) ois.readObject();
                        // Информация об ударе
                        Target tFM = (Target) ois.readObject();
                        while (myPositions[tFM.getI()][tFM.getJ()] != 0 && !endGame) {
                            tFM.setColor(Color.RED);
                            tFM.setText(BattleShip.SHIP_DISPLAY_VALUE);
                            clientHandler.sendMessage(getHandlerMessage(CHANGE_BUTTON_STYLE_MESSAGE, tFM));
                            // Был ли удар решающим
                            endGame = (boolean) ois.readObject();
                            // Информация об ударе
                            tFM = (Target) ois.readObject();
                        }
                        tFM.setColor(Color.YELLOW);
                        tFM.setText("");
                        clientHandler.sendMessage(getHandlerMessage(CHANGE_BUTTON_STYLE_MESSAGE, tFM));
                        // Проверяем количество очков противника
                        if (checkEnemyWin(endGame)) {
                            break;
                        }
                    } else {
                        // Сообщение о передаче хода
                        clientHandler.sendMessage(getHandlerMessage(TOAST_MESSAGE, BattleShip.READY_MESSAGE));
                    }
                    // Информации о передаче хода
                    first = (boolean) ois.readObject();
                    // Был ли удар решающим
                    endGame = (boolean) ois.readObject();
                }
            } catch (IOException | ClassNotFoundException e) {
                clientHandler.sendMessage(getHandlerMessage(TOAST_MESSAGE, "Cоединение закрыто"));
                e.printStackTrace();
            }

        }
        // Проверяем количество очков противника
        private boolean checkEnemyWin(boolean serverEndGame){
            if (serverEndGame) {
                clientHandler.sendMessage(getHandlerMessage(TOAST_END_MESSAGE, BattleShip.LOSE_MESSAGE));
                return true;
            }
            return false;
        }
        // Генерация объекта сообщения
        private Message getHandlerMessage(int clientMessageType, Object object){
            Message message = new Message();
            message.what = clientMessageType;
            message.obj = object;
            return message;
        }
        // Генерация объекта сообщения
        private Message getHandlerMessage(int clientMessageType) {
            return  getHandlerMessage(clientMessageType, null);
        }
        // Сохранение рекорда
        private void saveRecord() {
            int workTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
            startTime = 0;
            if (username.getText().toString().equals("")) {
                return;
            }
            dbHelper = new DBHelper(BattleshipActivity.this);
            db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("username", username.getText().toString());
            cv.put("time", workTime);
            db.insert("records", null, cv);
            db.close();
            dbHelper.close();
        }
    }

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(BattleshipActivity.Server.PORT);
            Socket client = serverSocket.accept();
            System.out.println(1);
            Socket client2 = serverSocket.accept();
            System.out.println(2);
            new Connection(client, client2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
