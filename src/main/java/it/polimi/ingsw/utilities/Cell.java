package it.polimi.ingsw.utilities;

/**
 * @author: hamzahaddaoui
 *
 * Cells definition. Each cell has a tower height (0 to 3), a dome (T/F) and a player (0, or !=0)
 *
 */

public class Cell implements Comparable{
    private int towerHeight;
    private boolean dome;
    private int playerID;

    public Cell() {
        towerHeight = 0;
        dome = false;
        playerID = 0;
    }

    public int getTowerHeight(){
        return towerHeight;
    }

    public boolean isDome(){
        return dome;
    }

    public int getPlayerID(){
        return playerID;
    }

    public void setTowerHeight(int towerHeight){
        this.towerHeight = towerHeight;
    }

    public void setDome(boolean dome){
        this.dome = dome;
    }

    public void setPlayerID(int playerID){
        this.playerID = playerID;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Cell){
            Cell cell = (Cell) obj;
            if (this.towerHeight == cell.getTowerHeight() && dome == cell.isDome() && playerID == cell.getPlayerID())
                return true;
            else
                return false;
        }
        return false;
    }

    @Override
    public int compareTo(Object o){
        if(o instanceof Cell){
            Cell cell = (Cell) o;
            if (this.towerHeight == cell.getTowerHeight() && dome == cell.isDome() && playerID == cell.getPlayerID())
                return 0;
            else
                return -1;
        }
        return -1;
    }

    @Override
    public String toString(){
        return towerHeight + (dome ? "D" : "")+(playerID==0?"":"-ID_"+playerID);
    }
}
