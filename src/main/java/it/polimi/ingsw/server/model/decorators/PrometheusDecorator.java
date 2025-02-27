package it.polimi.ingsw.server.model.decorators;

import it.polimi.ingsw.server.model.*;
import it.polimi.ingsw.utilities.Position;
import javafx.scene.shape.MoveTo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static it.polimi.ingsw.utilities.TurnState.*;

/**
 * @author Vasio1298
 *
 * Prometheus Commands Decorator
 * Description: "If your Worker does not move up, it may build both before and after moving"
 * Differente methods from Basic Commands: nextState, build, computeAvailableMovements, notifySpecialFunction
 */

public class PrometheusDecorator extends CommandsDecorator {

    static final private GodCards card = GodCards.Prometheus;

    public PrometheusDecorator(Commands commands){
        this.commands=commands;
    }

    private static boolean hasBuiltBeforeMoving;

    /**
     * Method that sets the next state of the player.
     * <p>
     * If the player sets the special function, from WAIT the turn shifts to BUILD
     * then if the player has done just his first building move, the turn shifts to MOVE
     * otherwise, the player has finished his turn, sets the boolean and the turn shifts to WAIT.
     * Else, the turn follows his standard shifting.
     *
     * @param player  the player who makes the turn, not null
     */
    @Override
    public void nextState(Player player) {
        switch(player.getTurnState()){
            case IDLE:
                hasBuiltBeforeMoving = false;
                player.setUnsetSpecialFunctionAvailable(canBuildBeforeMove(player));
                player.setTurnState(MOVE);
                break;
            case MOVE:
                player.setTurnState(BUILD);
                player.setUnsetSpecialFunctionAvailable(null);
                break;
            case BUILD:
                if (hasBuiltBeforeMoving) {
                    player.setTurnState(MOVE);
                    player.setUnsetSpecialFunctionAvailable(null);
                }
                else{
                    player.setHasFinished();
                }

                break;
        }
    }


    /**
     * Method that allows the specific building block action of Prometheus.
     * <p>
     * If the player sets the special function and it's his first building move, after the standard move,
     * the method sets the boolean at true.
     * In all the other case, the player does the standard building move
     * and the method sets the boolean at false.
     *
     * @param player     the player who makes the building move, not null
     * @param position   the position that player have inserted, not null
     */
    @Override
    public void build (Position position, Player player) {
        if (player.hasSpecialFunction() && !hasBuiltBeforeMoving) {
            super.build(position,player);
            hasBuiltBeforeMoving = true;
        }
        else {
            super.build(position, player);
            hasBuiltBeforeMoving = false;
        }
    }

    /**
     * Returns the spaces that are available after a check in the billboard.
     * <p>
     * If the player set the boolean special function, ha can't move up this turn so
     * this method checks if the space is free, if has height less than or equal to the current space
     * and if there isn't a dome in it.
     * Else, it's not a special function and the method returns the basic cells available for movements.
     *
     * @param player  the player who makes the move, not null
     * @param worker  the current worker of the player, not null
     * @return        the spaces which are available
     */
    @Override
    public Set<Position> computeAvailableMovements(Player player, Worker worker) {

        if (!player.hasSpecialFunction())
            return super.computeAvailableMovements(player, worker);
        else {
            Billboard billboard = player.getMatch().getBillboard();
            Position currentPosition = player.getCurrentWorker().getPosition();

            return currentPosition
                    .neighbourPositions()
                    .stream()
                    .filter(position -> billboard.getPlayer(position) == 0)
                    .filter(position -> (billboard.getTowerHeight(position) <= billboard.getTowerHeight(currentPosition)))
                    .filter(position -> !billboard.getDome(position))
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Method that modifies the standard turn after the special function activation.
     * <p>
     * If the special function has been activate, the method checks for both worker if the check cells method returns true.
     * If it's true, the method remove the specific cell from the available building cells of the specific worker.
     * After that the method does the check for both workers, this sets the turn state at BUILD.
     *
     * @param player the current player of the match
     */
    @Override
    public void notifySpecialFunction(Player player){
        Billboard billboard = player.getMatch().getBillboard();
        Set<Position> avoidPositions = new HashSet<>();
        ArrayList<Worker> workers = new ArrayList<>(player.getWorkers());
        Position avoidPosition;
        Worker worker;

        if (player.hasSpecialFunction()){
            for (int i=0;i<2;i++) {
                worker = workers.get(i);
                if (checkCells(player, worker)) {
                    Position workerPosition = worker.getPosition();

                    avoidPositions = worker.getAvailableCells(MOVE)
                            .stream()
                            .filter(position -> billboard.getTowerHeight(position) <= billboard.getTowerHeight(workerPosition))
                            .collect(Collectors.toSet());


                    if(avoidPositions.size() == 1 ){
                        avoidPosition = avoidPositions.stream().findFirst().get();
                        if(billboard.getTowerHeight(avoidPosition) == billboard.getTowerHeight(workerPosition))
                            worker.getAvailableCells(BUILD).remove(avoidPosition);
                    }
                }
            }
            player.setTurnState(BUILD);
        }
        else player.setTurnState(MOVE);
    }

    /**
     * Method that analyze how many cells are available to move into, after the first building turn.
     * <p>
     * The method confronts every available cells of the worker with the starting cell
     * and counts every cell that has an height tower of same or lower level then the starting one.
     *
     * @param player the current player of the match
     * @param worker the worker of the player
     * @return true if there's only one cell available, false otherwise
     */
    private boolean checkCells(Player player, Worker worker) {
        Billboard billboard = player.getMatch().getBillboard();

        long num1 = worker
                .getAvailableCells(MOVE)
                .stream()
                .filter(pos -> billboard.getTowerHeight(pos) <= billboard.getTowerHeight(worker.getPosition()))
                .count();

        long num2 = worker
                .getAvailableCells(MOVE)
                .stream()
                .filter(pos -> billboard.getTowerHeight(pos) < billboard.getTowerHeight(worker.getPosition()))
                .count();

        return (num1>0 && worker.getAvailableCells(BUILD).size() >1) || (num2>0);
    }

    /**
     * Method that checks if the boolean special function available can be true.
     * <p>
     * The method controls for both workers if they have at least one building available cells.
     *
     *
     * @param player  the current player of the match
     * @return        true if you can build before move, false otherwise
     */
    private Map<Position, Boolean> canBuildBeforeMove(Player player){
        player.setAvailableCells();

        return player
                .getWorkers()
                .stream()
                .collect(Collectors
                        .toMap(Worker::getPosition, worker -> checkCells(player,worker)));
    }
}
