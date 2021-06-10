package ViewModel;

import Model.IModel;
import Model.MovementDirection;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {
    private IModel model;

    public MyViewModel(IModel model) {
        this.model = model;
        this.model.assignObserver(this); //Observe the Model for it's changes
    }

    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(arg);
    }

    public int[][] getMaze(){
        return model.getMaze().getMap();
    }

    public Position getGoal() {
        return model.getMaze().getGoalPosition();
    }

    public int getPlayerRow(){
        return model.getPlayerRow();
    }

    public int getPlayerCol(){
        return model.getPlayerCol();
    }

    public Solution getSolution(){
        return model.getSolution();
    }

    public void generateMaze(int rows, int cols){
        model.generateMaze(rows, cols);
    }

    public void movePlayer(KeyEvent keyEvent){
        MovementDirection direction;
        switch (keyEvent.getCode()){
            case UP, NUMPAD8 -> direction = MovementDirection.UP;
            case DOWN, NUMPAD2 -> direction = MovementDirection.DOWN;
            case LEFT, NUMPAD4 -> direction = MovementDirection.LEFT;
            case RIGHT, NUMPAD6 -> direction = MovementDirection.RIGHT;
            case NUMPAD7 -> direction = MovementDirection.UPLeft;
            case NUMPAD9 -> direction = MovementDirection.UPRight;
            case NUMPAD1 -> direction = MovementDirection.DownLeft;
            case NUMPAD3 -> direction = MovementDirection.DownRight;

            default -> {
                // no need to move the player...
                return;
            }
        }
        model.updatePlayerLocation(direction);
    }

    public void solveMaze() throws Exception {
        model.solveMaze();
    }

    public void movePlayerWithMouse(MouseEvent mouseEvent) {
        model.movePlayer(mouseEvent.getSceneX(), mouseEvent.getSceneY());
    }
}
