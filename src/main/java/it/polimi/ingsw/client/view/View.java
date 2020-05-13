package it.polimi.ingsw.client.view;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.NetworkHandler;
import it.polimi.ingsw.client.controller.Controller;
import it.polimi.ingsw.utilities.Observable;
import it.polimi.ingsw.utilities.Observer;
import it.polimi.ingsw.utilities.PlayerState;
import it.polimi.ingsw.utilities.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    TODO gestire la possibilità di disconnessione (enter or esc button)
    TODO gestire "Wait for other players to join!" exhange

 */

public class View extends Observable<String> implements Observer<MessageEvent> {

    static ExecutorService executorView = Executors.newSingleThreadExecutor();
    static ExecutorService executorData = Executors.newSingleThreadExecutor();

    private static boolean refresh = true;
    private static boolean error;
    private static boolean active;

    private static GameBoard gameBoard;
    private static Player player;

    public View(){
        refresh = true;
        error = false;
        player = new Player();
        gameBoard = new GameBoard();
    }

    //UPDATE FROM NETWORK HANDLER

    @Override //FROM CLIENT HANDLER
    public void update(MessageEvent messageEvent){
        //System.out.println(messageEvent);
        executorData.submit(()->{
            Controller.updateStandardData(messageEvent);
            Controller.updateControllerState();
        });
        if(messageEvent.getError()){
            executorData.submit(()-> player.getControlState().error());
        }
        else {
            executorData.submit(()-> player.getControlState().updateData(messageEvent));
        }

    }

    //VIEW
    public static void doUpdate(){
        executorView.submit(View::visualization);
    }

    public static void visualization(){
        if(player.getMatchState() == MatchState.PLACING_WORKERS ){
            System.out.println(getBillboardStat());
        }
        else if(player.getMatchState() == MatchState.RUNNING && gameBoard.getStartingPosition() != null && player.getPlayerState() == PlayerState.ACTIVE){ // visualizzaione delle 3 tabelle
            gameBoardVisualizationActive();
        }
        else if(player.getMatchState() == MatchState.RUNNING && gameBoard.getStartingPosition() == null && player.getPlayerState() == PlayerState.ACTIVE){
            gameBoardVisualizationChooseCurrentWorker();
        }
        else {
            gameBoardVisualizationNotActive();
        }
    }

    /**
     * Method that organize the the visualization of the tables if the worker is active and have to choose the worker for the turn
     *
     */
    public static void gameBoardVisualizationChooseCurrentWorker(){
        StringBuilder output = new StringBuilder();
        String coloredGameBoard = getBillboardStat();
        String heightStateGameBoard = getBillboardHeightStat();
        String availableMovements = getBillBoardEvidence(gameBoard.getWorkersPositions());
        int q, w;
        int j, k;
        int c, v;
        int i;
        for (i = 0, q = 0, j = 0, c = 0, w = coloredGameBoard.indexOf("\n", 0), k = heightStateGameBoard.indexOf("\n", 0), v = availableMovements.indexOf("\n", 0);
             i < 5;
             i++, w = coloredGameBoard.indexOf("\n", q), k = heightStateGameBoard.indexOf("\n", j), v = availableMovements.indexOf("\n", c)) {

            output.append(coloredGameBoard, q, w);
            output.append("\t\t\t");
            output.append(heightStateGameBoard, j, k);
            output.append("\t\t\t");
            output.append(availableMovements, c, v);
            output.append("\n");
            q = ++ w;
            j = ++ k;
            c = ++ v;
        }
        System.out.println(output.toString());
    }

    /**
     * Method that organize the the visualization of the tables if the worker is active and can do his movement
     *
     */
    public static void gameBoardVisualizationActive(){
        StringBuilder output = new StringBuilder();
        String coloredGameBoard = getBillboardStat();
        String heightStateGameBoard = getBillboardHeightStat();
        String availableMovements = getBillboardStat(gameBoard.getWorkersAvailableCells(gameBoard.getStartingPosition()),gameBoard.getStartingPosition());

        int q, w;
        int j, k;
        int c, v;
        int i;
        for (i = 0, q = 0, j = 0, c = 0, w = coloredGameBoard.indexOf("\n", 0), k = heightStateGameBoard.indexOf("\n", 0), v = availableMovements.indexOf("\n", 0);
             i < 5;
             i++, w = coloredGameBoard.indexOf("\n", q), k = heightStateGameBoard.indexOf("\n", j), v = availableMovements.indexOf("\n", c)) {

            output.append(coloredGameBoard, q, w);
            output.append("\t\t\t");
            output.append(heightStateGameBoard, j, k);
            output.append("\t\t\t");
            output.append(availableMovements, c, v);
            output.append("\n");
            q = ++ w;
            j = ++ k;
            c = ++ v;
        }
        System.out.println(output.toString());
    }

    /**
     * Method that organize the the visualization of the tables if the worker is not active and have to choose his current worker
     */
    public static void gameBoardVisualizationNotActive(){
        StringBuilder output = new StringBuilder();
        String coloredGameBoard = getBillboardStat();
        String heightStateGameBoard = getBillboardHeightStat();

        int q, w;
        int j, k;
        int i;
        for (i = 0, q = 0, j = 0, w = coloredGameBoard.indexOf("\n", 0), k = heightStateGameBoard.indexOf("\n", 0);
             i < 5;
             i++, w = coloredGameBoard.indexOf("\n", q), k = heightStateGameBoard.indexOf("\n", j)) {

            output.append(coloredGameBoard, q, w);
            output.append("\t\t\t");
            output.append(heightStateGameBoard, j, k);
            output.append("\n");
            q = ++ w;
            j = ++ k;
        }
        System.out.println(output.toString());
    }

    /**
     * Print the GameBoard situation with colored blocks
     *
     * @return  the string of the GameBoard's situation
     */
    static String getBillboardStat(){
        StringBuilder outputA = new StringBuilder();

        Map<Position, Cell> billboardCells = gameBoard.getBillboardStatus();
        List<Integer> players = new ArrayList<>(player.getMatchPlayers().keySet());


        billboardCells
                .keySet()
                .stream()
                .sorted()
                .forEach(position -> outputA
                        .append(billboardCells.get(position).getPlayerID() == 0 ? "⬜ " : "")
                        .append(players.indexOf(billboardCells.get(position).getPlayerID()) == 0 ? "🟥 " : "")
                        .append(players.indexOf(billboardCells.get(position).getPlayerID()) == 1 ? "🟩 " : "")
                        .append(players.indexOf(billboardCells.get(position).getPlayerID()) == 2 ? "🟦 " : "")
                        .append((position.getY() == 4) ? "\n" : " "));

        outputA.append("\n");
        return outputA.toString();
    }

    /**
     * Print the height of the GameBoard' cells
     *
     * @return  the string of the GameBoard's situation
     */
    static String getBillboardHeightStat() {
        StringBuilder outputB = new StringBuilder();

        Map<Position, Cell> billboardCells = gameBoard.getBillboardStatus();

        billboardCells
                .keySet()
                .stream()
                .sorted()
                .forEach(position -> outputB
                        .append(billboardCells.get(position).isDome() ? "⏺" : "")
                        .append(!billboardCells.get(position).isDome() && billboardCells.get(position).getTowerHeight() == 0 ? "0️⃣" : "")
                        .append(!billboardCells.get(position).isDome() && billboardCells.get(position).getTowerHeight() == 1 ? "1️⃣" : "")
                        .append(!billboardCells.get(position).isDome() && billboardCells.get(position).getTowerHeight() == 2 ? "2️⃣" : "")
                        .append(!billboardCells.get(position).isDome() && billboardCells.get(position).getTowerHeight() == 3 ? "3️⃣" : "")
                        .append((position.getY() == 4) ? "\n" : " "));

        outputB.append("\n");
        return outputB.toString();
    }

    /**
     * Put in evidence the position where the current worker can move or build
     *
     * @param cells  the set of available cells where worker can move or build
     * @param p  the actual position of the worker
     * @return  the string of the GameBoard's situation
     */
    static String getBillboardStat( Set<Position> cells, Position p){
        StringBuilder outputC = new StringBuilder();

        Map<Position, Cell> billboardCells = gameBoard.getBillboardStatus();

        billboardCells
                .keySet()
                .stream()
                .sorted()
                .forEach(position -> outputC
                        .append(cells.contains(position) ? "\u2B1B" : "")
                        .append(! (p.equals(position)) && ! cells.contains(position) ? "\u2B1C" : "")
                        .append((p.equals(position)) ? "\uD83D\uDC77\uD83C\uDFFB" : "")
                        .append((position.getY() == 4) ? "\n" : " "));

        outputC.append("\n");
        return outputC.toString();
    }

    /**
     * evidence the cells of the user in the billboard table
     *
     * @param cells  are the worker' positions of the user
     * @return  cells of the user in the billboard table
     */
    static String getBillBoardEvidence( Set<Position> cells){
        StringBuilder outputD = new StringBuilder();

        Map<Position, Cell> billboardCells = gameBoard.getBillboardStatus();

        billboardCells
                .keySet()
                .stream()
                .sorted()
                .forEach(position -> outputD
                        .append(cells.contains(position) ? "\u2B1B" : "")
                        .append(! cells.contains(position) ? "\u2B1C" : "")
                        .append((position.getY() == 4) ? "\n" : " "));

        outputD.append("\n");
        return outputD.toString();
    }

    public static void print(){
        if(refresh){
            System.out.println(player.getControlState().computeView());
            refresh = false;
        }
        if(error){
            System.out.println("Wrong output");
            System.out.println(player.getControlState().computeView());
            error = false;
        }
    }

    // GETTER AND SETTER

    public static boolean getRefresh(){
        return refresh;
    }

    public static void setError(boolean value){
        error = value;
    }

    public static boolean getError(){
        return error;
    }

    public static Player getPlayer() {
        return player;
    }

    public static void setGameBoard(GameBoard newGameBoard){
        gameBoard = newGameBoard;
    }

    public static void setPlayer(Player newPlayer){
        player = newPlayer;
    }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean active) {
        View.active = active;
    }

    public static GameBoard getGameBoard() {
        return gameBoard;
    }

    public static void setRefresh(boolean value){
        refresh = value;
    }

    public static void disconnect(){
        Client.close();
    }

}
