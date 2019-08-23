package com.yura.buttleship;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Runnable {
    Socket socket;
    Socket socket2;
    Thread thread;
    int[][] firstPositions = new int[10][10];
    boolean firstUserStep = true;
    int[][] secondPositions = new int[10][10];
    boolean secondUserStep = false;
    String name1;
    String name2;
    int firstPoints;
    int secondPoints;

    public Connection(Socket socket, Socket socket2) {
        firstPoints = 0;
        secondPoints = 0;
        this.socket = socket;
        this.socket2 = socket2;
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois2 = new ObjectInputStream(socket2.getInputStream());
            ObjectOutputStream oos2 = new ObjectOutputStream(socket2.getOutputStream());

            readFirstData(ois,1);
            readFirstData(ois2, 2);
            writeFirstData(oos, firstUserStep, secondPositions, name2);
            writeFirstData(oos2, secondUserStep, firstPositions, name1);

            boolean endGame = false;
            while (!endGame) {
                if (firstUserStep) {
                    endGame = step(ois, oos, oos2, 1);
                } else {
                    endGame = step(ois2, oos2, oos, 2);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public boolean readFirstData(ObjectInputStream ois, int num){
        try {
            String name = (String) ois.readObject();
            int[][] field = (int[][]) ois.readObject();
            if (num == 1) {
                name1 = name;
                firstPositions = field;
            }else{
                name2 = name;
                secondPositions = field;
            }
            return true;
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }
    public boolean writeFirstData(ObjectOutputStream oos,boolean flag, int[][] enemyField, String enemyName){
        try {
            oos.writeObject(flag);
            oos.writeObject(enemyField);
            oos.writeObject(enemyName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public boolean step(ObjectInputStream ois, ObjectOutputStream oos, ObjectOutputStream enemyOOS,
                        int num) throws IOException, ClassNotFoundException {
        Target target = (Target) ois.readObject();
        boolean endGame = false;
        int[][] positions;
        if (num == 1){
            positions = secondPositions;
        }else {
            positions = firstPositions;
        }
        while (positions[target.getI()][target.getJ()] != 0 && !endGame) {
            int c = positions[target.getI()][target.getJ()];
            if (num == 1){
                firstPoints += c;
                if (firstPoints == BattleShip.WIN_POINTS) {
                    endGame = true;
                }
            }else{
                secondPoints += c;
                if (secondPoints == BattleShip.WIN_POINTS) {
                    endGame = true;
                }
            }
            enemyOOS.writeObject(endGame);
            enemyOOS.writeObject(target);
            System.out.println(firstPoints + " - " + secondPoints);
            target = (Target) ois.readObject();
        }
        enemyOOS.writeObject(endGame);
        enemyOOS.writeObject(target);
        firstUserStep = !firstUserStep;
        secondUserStep = !secondUserStep;
        if (num == 1){
            oos.writeObject(firstUserStep);
            enemyOOS.writeObject(secondUserStep);
        }else{
            oos.writeObject(secondUserStep);
            enemyOOS.writeObject(firstUserStep);
        }
        oos.writeObject(endGame);
        enemyOOS.writeObject(endGame);
        return endGame;
    }
}
