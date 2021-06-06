package Model;

import Client.*;
import IO.MyDecompressorInputStream;
import Server.*;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.search.AState;
import algorithms.search.Solution;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;


public class MyModel extends Observable implements IModel {
    private Maze maze;

    private int playerRow;
    private int playerCol;
    private Solution solution;


    public MyModel() {

    }

    @Override
    public void generateMaze(int rows, int cols) {
        Server mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        mazeGeneratingServer.start();

        final Maze[] tempMaze = new Maze[1];
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{rows, cols};
                        toServer.writeObject(mazeDimensions); //send maze dimensions to server
                        toServer.flush();

                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);

                        byte[] compressedMaze = (byte[]) fromServer.readObject(); //read generated maze (compressed with MyCompressor) from server
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[1000000 /*CHANGE SIZE ACCORDING TO YOU MAZE SIZE*/]; //allocating byte[] for the decompressed maze -
                        is.read(decompressedMaze); //Fill decompressedMaze with bytes

                        tempMaze[0] = new Maze(decompressedMaze);


                    } catch (EOFException e){
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        mazeGeneratingServer.stop();

        this.maze = tempMaze[0];
        setChanged();
        notifyObservers("maze generated");
        // start position:
        movePlayer(maze.getStartPosition().getRowIndex(), maze.getStartPosition().getColumnIndex());
    }

    @Override
    public Maze getMaze() {
        return maze;
    }

    @Override
    public void updatePlayerLocation(MovementDirection direction) {
        switch (direction) {
            case UP -> {
                if (playerRow > 0 && maze.getMap()[playerRow - 1][playerCol] != 1)
                    movePlayer(playerRow - 1, playerCol);
            }
            case DOWN -> {
                if (playerRow < maze.getYMazeLength() - 1 && maze.getMap()[playerRow + 1][playerCol] != 1)
                    movePlayer(playerRow + 1, playerCol);
            }
            case LEFT -> {
                if (playerCol > 0 && maze.getMap()[playerRow][playerCol - 1] != 1)
                    movePlayer(playerRow, playerCol - 1);
            }
            case RIGHT -> {
                if (playerCol < maze.getXMazeLength() - 1 && maze.getMap()[playerRow][playerCol + 1] != 1)
                    movePlayer(playerRow, playerCol + 1);
            }
        }
    }

    private void movePlayer(int row, int col){
        this.playerRow = row;
        this.playerCol = col;
        setChanged();
        notifyObservers("player moved");
    }

    @Override
    public int getPlayerRow() {
        return playerRow;
    }

    @Override
    public int getPlayerCol() {
        return playerCol;
    }

    @Override
    public void assignObserver(Observer o) {
        this.addObserver(o);
    }

    @Override
    public void solveMaze() {
        //solve the maze
        Server solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        solveSearchProblemServer.start();

        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();

                        toServer.writeObject(maze); //send maze to server
                        toServer.flush();
                        solution = (Solution) fromServer.readObject(); //read generated maze (compressed with MyCompressor) from server

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        solveSearchProblemServer.stop();

        setChanged();
        notifyObservers("maze solved");
    }

    @Override
    public Solution getSolution() {
//        System.out.println(String.format("Solution steps: %s", mazeSolution));
//        ArrayList<AState> mazeSolutionSteps = mazeSolution.getSolutionPath();
//        for (int i = 0; i < mazeSolutionSteps.size(); i++) {
//            System.out.println(String.format("%s. %s", i, mazeSolutionSteps.get(i).toString()));
//        }
        return solution;
    }
}
