package Model;

import Client.Client;
import Client.IClientStrategy;
import IO.MyDecompressorInputStream;
import Server.Configurations;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;


public class MyModel extends Observable implements IModel {
    private Maze maze;

    private int playerRow;
    private int playerCol;
    private Solution solution;

    private volatile double lastSceneX;
    private volatile double lastSceneY;

    private final Configurations conf = Configurations.getInstance();
    private final Logger LOG = LogManager.getLogger(); //Log4j2

    public MyModel() {
        lastSceneX = 0;
        lastSceneY = 0;

    }

    @Override
    public void generateMaze(int rows, int cols) {
        Server mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        mazeGeneratingServer.start();
        LOG.info("Starting generate maze server at port = " + 5400);

        final Maze[] tempMaze = new Maze[1];
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        LOG.info(String.format("Client accepted - maze at size %d * %d was generated", rows, cols));
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
                        //e.printStackTrace();
                        LOG.error("EOFException", e);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        LOG.error("Exception", e);
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            //e.printStackTrace();
            LOG.error("UnknownHostException", e);
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

            case UPLeft-> {
                if (playerRow > 0 && playerCol > 0 && maze.getMap()[playerRow - 1][playerCol - 1] != 1)
                    movePlayer(playerRow - 1, playerCol - 1);
            }
            case UPRight-> {
                if (playerRow > 0 && playerCol < maze.getXMazeLength() - 1 && maze.getMap()[playerRow - 1][playerCol + 1] != 1)
                    movePlayer(playerRow - 1, playerCol + 1);
            }
            case DownLeft -> {
                if (playerRow < maze.getYMazeLength() - 1 && playerCol > 0 &&  maze.getMap()[playerRow + 1][playerCol - 1] != 1)
                    movePlayer(playerRow + 1, playerCol - 1);
            }
            case DownRight -> {
                if (playerRow < maze.getYMazeLength() - 1 && playerCol < maze.getXMazeLength() - 1 &&  maze.getMap()[playerRow + 1][playerCol + 1] != 1)
                    movePlayer(playerRow + 1, playerCol + 1);
            }
        }

        if(playerRow == maze.getGoalPosition().getRowIndex() && playerCol == maze.getGoalPosition().getColumnIndex()) {
            setChanged();
            notifyObservers("goal reached");
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
    public void solveMaze() throws Exception {
        //solve the maze
        if(maze == null)
            throw new Exception();

        else {
            setChanged();
            notifyObservers("solving maze");
        }

        Server solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        solveSearchProblemServer.start();
        LOG.info("Starting solving maze server at port = " + 5401);

        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        LOG.info("Client accepted - maze was solved");
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();

                        toServer.writeObject(maze); //send maze to server
                        toServer.flush();
                        solution = (Solution) fromServer.readObject(); //read generated maze (compressed with MyCompressor) from server

                    } catch (Exception e) {
                        //e.printStackTrace();
                        LOG.error("Exception", e);
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            //e.printStackTrace();
            LOG.error("UnknownHostException", e);
        }

        solveSearchProblemServer.stop();

        setChanged();
        notifyObservers("maze solved");
    }

    @Override
    public Solution getSolution() {
        return solution;
    }

    @Override
    public void movePlayer(double sceneX, double sceneY) {
        if (maze == null)
            return;
        if (sceneX > lastSceneX)
            this.updatePlayerLocation(MovementDirection.RIGHT);


        if (sceneX < lastSceneX) {
            this.updatePlayerLocation(MovementDirection.LEFT);
        }

        if (sceneY > lastSceneY) {
            this.updatePlayerLocation(MovementDirection.DOWN);
        }

        if (sceneY < lastSceneY) {
            this.updatePlayerLocation(MovementDirection.UP);
        }

        lastSceneX = sceneX;
        lastSceneY = sceneY;
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setProp(String p) {
        if (p.equals("DFS")) {
            conf.setMazeSearchingAlgorithm("DFS");
        }
        else if (p.equals("BFS")) {
            conf.setMazeSearchingAlgorithm("BFS");
        }
        else {
            conf.setMazeSearchingAlgorithm("BEST");
        }
    }

    @Override
    public void setPropGenerate(String s) {
        if (s.equals("Empty Maze")) {
            conf.setMazeGeneratingAlgorithm("Empty");
        }
        else if (s.equals("Ugly Maze")) {
            conf.setMazeGeneratingAlgorithm("Simple");
        }
        else {
            conf.setMazeGeneratingAlgorithm("My");
        }
    }
}
