package it.polimi.ingsw.client.GUI.controller;

import it.polimi.ingsw.client.GUI.Controller;
import it.polimi.ingsw.client.GUI.Database;
import it.polimi.ingsw.client.GUI.View;
import it.polimi.ingsw.utilities.MessageEvent;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author: hamzahaddaoui
 *
 * Control/View class. Based on the state where the main player (who has created the match) has to decide the number of players.
 * */


public class GettingPlayersNum extends State {

    boolean selected = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        this.addObserver(Database.getNetworkHandler());
        Database.getNetworkHandler().addObserver(this);
    }



    @Override
    public void showPane(){
        Platform.runLater(() -> Controller.replaceSceneContent("fxml_files/selectNum.fxml"));
    }

    @Override
    public void showError(){

    }

    @Override
    public void sendData(){
        MessageEvent message = new MessageEvent();
        message.setPlayersNum(Database.getPlayersNum());
        notify(message);
    }

    @Override
    public void update(MessageEvent message){

        if (message.getError()){
            System.out.println(message);
            showError();

            return;
        }
        else {
            Database.updateStandardData(message);
            View.updateView();
            Database.getCurrentState().showPane();
            new Thread(()->{
                Database.getNetworkHandler().removeObserver(this);
                this.removeObserver(Database.getNetworkHandler());
            }).start();
        }
    }


    public void selectedTwoPlayers(MouseEvent mouseEvent){
        if (selected)
            return;

        selected = true;
        Database.setPlayersNum(2);
        System.out.println("Match players: 2");
        selected = true;
        sendData();
    }

    public void selectedThreePlayers(MouseEvent mouseEvent){
        if (selected)
            return;

        selected = true;
        Database.setPlayersNum(3);
        System.out.println("Match players: 3");
        sendData();
    }


}
