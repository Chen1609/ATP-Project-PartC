package View;

import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.ZoomEvent;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MazeDisplayer extends Canvas {
    private int[][] maze;
    private Solution solution;
    private Position goal;
    // player position:
    private int playerRow = 0;
    private int playerCol = 0;
    // wall and player images:
    StringProperty imageFileNameWall = new SimpleStringProperty();
    StringProperty imageFileNamePlayer = new SimpleStringProperty();
    StringProperty imageFileNameEndPosition = new SimpleStringProperty();



    public String getImageFileNameEndPosition() {
        return imageFileNameEndPosition.get();
    }

    public StringProperty imageFileNameEndPositionProperty() {
        return imageFileNameEndPosition;
    }

    public void setImageFileNameEndPosition(String imageFileNameEndPosition) {
        this.imageFileNameEndPosition.set(imageFileNameEndPosition);
    }

    public int getPlayerRow() {
        return playerRow;
    }

    public int getPlayerCol() {
        return playerCol;
    }

    public void setPlayerPosition(int row, int col) {
        this.playerRow = row;
        this.playerCol = col;
        draw();
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
        draw();
    }

    public String getImageFileNameWall() {
        return imageFileNameWall.get();
    }

    public String imageFileNameWallProperty() {
        return imageFileNameWall.get();
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.imageFileNameWall.set(imageFileNameWall);
    }

    public String getImageFileNamePlayer() {
        return imageFileNamePlayer.get();
    }

    public String imageFileNamePlayerProperty() {
        return imageFileNamePlayer.get();
    }

    public void setImageFileNamePlayer(String imageFileNamePlayer) {
        this.imageFileNamePlayer.set(imageFileNamePlayer);
    }

    public void drawMaze(int[][] maze, Position goal) {
        this.maze = maze;
        this.solution = null;
        this.goal = goal;
        draw();
    }

    public void zoom(ZoomEvent zoomEvent)
    {
        setHeight(getHeight()*zoomEvent.getZoomFactor());
        setWidth(getWidth()*zoomEvent.getZoomFactor());
        System.out.println("zooming");
    }

    private void draw() {
        if(maze != null){
            double canvasHeight = getHeight();
            double canvasWidth = getWidth();
            int rows = maze.length;
            int cols = maze[0].length;

            double cellHeight = canvasHeight / rows;
            double cellWidth = canvasWidth / cols;

            GraphicsContext graphicsContext = getGraphicsContext2D();
            //clear the canvas:
            graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);

            drawMazeWalls(graphicsContext, cellHeight, cellWidth, rows, cols);
            if(solution != null)
                drawSolution(graphicsContext, cellHeight, cellWidth);
            drawPlayer(graphicsContext, cellHeight, cellWidth);
            drawGoalPosition(graphicsContext, cellHeight, cellWidth, this.goal.getRowIndex(), this.goal.getColumnIndex());
        }
    }

    private void drawGoalPosition(GraphicsContext graphicsContext, double cellHeight, double cellWidth, int rowIndex, int columnIndex) {
        double x = columnIndex * cellWidth;
        double y = rowIndex * cellHeight;
        graphicsContext.setFill(Color.GREEN);

        Image GoalImage = null;
        try {
            GoalImage = new Image(new FileInputStream(getImageFileNameEndPosition()));
        } catch (FileNotFoundException e) {
            System.out.println("There is no goal position image file");
        }
        if(GoalImage == null)
            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
        else
            graphicsContext.drawImage(GoalImage, x, y, cellWidth, cellHeight);
    }

    private void drawSolution(GraphicsContext graphicsContext, double cellHeight, double cellWidth) {
        // need to be implemented

        graphicsContext.setFill(Color.GREEN);

        ArrayList<AState> mazeSolutionSteps = this.solution.getSolutionPath();
        int lastY = 1, lastX = 0, currY, currX;
        for (int i = 1; i < mazeSolutionSteps.size(); i++) {
            String temp = mazeSolutionSteps.get(i).toString();
            int loc = temp.indexOf(",");
            currY = Integer.parseInt(temp.substring(1,loc));
            currX = Integer.parseInt(temp.substring(loc + 2, temp.length() - 1));

//            System.out.println(temp); ///////////


            int deltaX = currX - lastX;
            int deltaY = currY - lastY;

            //System.out.println(currY);

            if(deltaY != 0 && deltaX != 0)
            {
                double x,y;
                if ((deltaY == 1 && deltaX == 1)) {
                    if (this.maze[currY - 1][currX] != 1) {
                        y = (currY - 1) * cellHeight;
                        x = currX * cellWidth;
                    }
                    else {
                        y = currY * cellHeight;
                        x = (currX - 1) * cellWidth;
                    }
                }

                else if (deltaY == -1 && deltaX == -1) {
                    if (this.maze[currY + 1][currX] != 1) {
                        y = (currY + 1) * cellHeight;
                        x = currX * cellWidth;
                    }
                    else {
                        y = currY * cellHeight;
                        x = (currX + 1) * cellWidth;
                    }
                }

                else if (deltaY == 1 && deltaX == -1) {
                    if (this.maze[currY - 1][currX] != 1) {
                        y = (currY - 1) * cellHeight;
                        x = currX * cellWidth;
                    }
                    else {
                        y = currY * cellHeight;
                        x = (currX + 1) * cellWidth;
                    }
                }

                else {
                    if (this.maze[currY + 1][currX] != 1) {
                        y = (currY + 1) * cellHeight;
                        x = currX * cellWidth;
                    }
                    else {
                        y = currY * cellHeight;
                        x = (currX - 1) * cellWidth;
                    }
                }
                graphicsContext.fillRect(x, y, cellWidth, cellHeight);

            }

            lastY = currY;
            lastX = currX;



            double y = currY * cellHeight;
            double x = currX * cellWidth;

            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
        }

//        for (int i = 0; i < rows; i++) {
//            for (int j = 0; j < cols; j++) {
//                if(maze[i][j] == 1){
//                    //if it is a wall:
//                    double x = j * cellWidth;
//                    double y = i * cellHeight;
//                    if(wallImage == null)
//                        graphicsContext.fillRect(x, y, cellWidth, cellHeight);
//                    else
//                        graphicsContext.drawImage(wallImage, x, y, cellWidth, cellHeight);
//                }
//            }
//        }


    }

    private void drawMazeWalls(GraphicsContext graphicsContext, double cellHeight, double cellWidth, int rows, int cols) {
        graphicsContext.setFill(Color.RED);

        Image wallImage = null;
        try{
            wallImage = new Image(new FileInputStream(getImageFileNameWall()));
        } catch (FileNotFoundException e) {
            System.out.println("There is no wall image file");
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(maze[i][j] == 1){
                    //if it is a wall:
                    double x = j * cellWidth;
                    double y = i * cellHeight;
                    if(wallImage == null)
                        graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                    else
                        graphicsContext.drawImage(wallImage, x, y, cellWidth, cellHeight);
                }

                if (i == this.goal.getRowIndex() && j == this.goal.getColumnIndex()) {
                    graphicsContext.setFill(Color.YELLOW);
                    double x = j * cellWidth;
                    double y = i * cellHeight;
                    if(wallImage == null)
                        graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                    else
                        graphicsContext.drawImage(wallImage, x, y, cellWidth, cellHeight);
                }
            }
        }
    }

    private void drawPlayer(GraphicsContext graphicsContext, double cellHeight, double cellWidth) {
        double x = getPlayerCol() * cellWidth;
        double y = getPlayerRow() * cellHeight;
        graphicsContext.setFill(Color.GREEN);

        Image playerImage = null;
        try {
            playerImage = new Image(new FileInputStream(getImageFileNamePlayer()));
        } catch (FileNotFoundException e) {
            System.out.println("There is no player image file");
        }
        if(playerImage == null)
            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
        else
            graphicsContext.drawImage(playerImage, x, y, cellWidth, cellHeight);
    }
}
