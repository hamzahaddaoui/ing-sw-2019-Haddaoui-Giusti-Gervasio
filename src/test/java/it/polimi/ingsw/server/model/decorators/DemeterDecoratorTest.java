package it.polimi.ingsw.server.model.decorators;


import it.polimi.ingsw.server.model.Commands;
import it.polimi.ingsw.server.model.GodCards;
import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.Player;
import it.polimi.ingsw.utilities.PlayerState;
import it.polimi.ingsw.utilities.Position;
import it.polimi.ingsw.utilities.TurnState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static it.polimi.ingsw.utilities.TurnState.IDLE;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author giusti-leo
 *
 * Demeter Commands Decorator Tests
 *
 */

public class DemeterDecoratorTest {

    Commands commands1,commands2;
    Player player1 = new Player(1,"leo");
    Match match = new Match(2, player1);
    Player player2 = new Player(2,"dario");
    Set<GodCards> godCards = new HashSet<GodCards>();

    Position position00=new Position(0,0);
    Position position01=new Position(0,1);
    Position position02=new Position(0,2);
    Position position03=new Position(0,3);
    Position position04=new Position(0,4);
    Position position10=new Position(1,0);
    Position position11=new Position(1,1);
    Position position12=new Position(1,2);
    Position position13=new Position(1,3);
    Position position14=new Position(1,4);
    Position position20=new Position(2,0);
    Position position21=new Position(2,1);
    Position position22=new Position(2,2);
    Position position23=new Position(2,3);
    Position position24=new Position(2,4);
    Position position30=new Position(3,0);
    Position position31=new Position(3,1);
    Position position32=new Position(3,2);
    Position position33=new Position(3,3);
    Position position34=new Position(3,4);
    Position position40=new Position(4,0);
    Position position41=new Position(4,1);
    Position position42=new Position(4,2);
    Position position43=new Position(4,3);
    Position position44=new Position(4,4);

    @BeforeEach
    void setUp() {
        match.setPlayersNum(2);
        match.addPlayer(player2);
        godCards.add(GodCards.Demeter);
        godCards.add(GodCards.Apollo);
        match.setCards(godCards);
        player1.setCommands(GodCards.Demeter);
        player2.setCommands(GodCards.Apollo);
        commands1=player1.getCommands();
        commands2=player2.getCommands();
    }

    /**
     * nextStateJustOneBuild tests the exchange of the position between 2 differtent workers of different players
     */
    @Test
    public void nextStateJustOneBuild() {

        match.nextState();
        match.nextState();
        match.nextState();
        match.nextState();
        match.nextState();
        player1.setWorker(position12);
        player1.setWorker(position44);
        player1.setWorker(position43);
        player1.setWorker(position42);
        player1.setPlayerState();
        player1.setCurrentWorker(position12);
        player1.setTurnState(IDLE);

        commands1.nextState(player1);

        assertTrue(player1.getTurnState() == TurnState.MOVE);

        commands1.nextState(player1);

        assertTrue( player1.getTurnState() == TurnState.BUILD);

        commands1.build(position11,player1);

        commands1.build(position10,player1);

        commands1.nextState(player1);

        assertTrue(player1.hasFinished() == true);
        assertTrue( player1.getTurnState() == TurnState.IDLE);
    }

    /**
     * testMoveWorkerNonChange tests the standard move
     */
    @Test
    public void nextStateSecondBuild() {
        player1.setWorker(position12);
        player1.setTurnState(TurnState.IDLE);
        commands1.nextState(player1);

        assertTrue( player1.getTurnState() == TurnState.MOVE);

        commands1.nextState(player1);

        assertTrue(player1.getTurnState() == TurnState.BUILD);

        commands1.build(position11,player1);
        player1.setUnsetSpecialFunction(true);

        player1.setAvailableCells();
        assertTrue( player1.getTurnState() == TurnState.BUILD);

        commands1.build(position01,player1);
        commands1.nextState(player1);

        assertTrue( player1.getTurnState() == TurnState.IDLE);
        assertTrue(player1.getPlayerState() == PlayerState.IDLE);
    }

    /**
     * testComputeAvailableMovements tests that the current worker can move in the cell where there is a enemy
     */
    @Test
    public void computeAvailableBuildings() {
        player1.setWorker(position12);
        player1.setWorker(position00);
        player2.setWorker(position13);
        player2.setWorker(position41);
        player1.setTurnState(TurnState.IDLE);
        commands1.nextState(player1);

        assertTrue(player1.getTurnState() == TurnState.MOVE);

        commands1.nextState(player1);

        assertTrue(player1.getTurnState() == TurnState.BUILD);

        Set<Position> Set= new HashSet<>();
        Set.add(position03);
        Set.add(position02);
        Set.add(position01);
        Set.add(position11);
        Set.add(position23);
        Set.add(position22);
        Set.add(position21);
        player1.setCurrentWorker(position12);
        Set<Position> SetCheck=commands1.computeAvailableBuildings(player1,player1.getCurrentWorker());

        assertTrue(SetCheck.containsAll(Set));
        assertTrue(Set.containsAll(SetCheck));

        Set.clear();

        commands1.build(position11,player1);
        player1.setUnsetSpecialFunction(true);

        player1.setAvailableCells();

        assertTrue( player1.getTurnState() == TurnState.BUILD);

        Set.clear();
        Set.add(position03);
        Set.add(position02);
        Set.add(position01);
        Set.add(position23);
        Set.add(position22);
        Set.add(position21);
        SetCheck=commands1.computeAvailableBuildings(player1,player1.getCurrentWorker());

        assertTrue(SetCheck.containsAll(Set));
        assertTrue(Set.containsAll(SetCheck));
        commands1.build(position01,player1);
        commands1.nextState(player1);

        assertTrue( player1.getTurnState() == TurnState.IDLE);
        assertTrue(player1.hasFinished()==true);
    }

}