package View;

import ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.net.URL;
import java.util.*;

import static javafx.application.Platform.exit;

public class MyViewController implements IView, Initializable, Observer {
    public MyViewModel viewModel;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }

    public TextField textField_mazeRows;
    public TextField textField_mazeColumns;
    public MazeDisplayer mazeDisplayer;
    public Label playerRow;
    public Label playerCol;

    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();

    public String getUpdatePlayerRow() {
        return updatePlayerRow.get();
    }

    public void setUpdatePlayerRow(int updatePlayerRow) {
        this.updatePlayerRow.set(updatePlayerRow + "");
    }

    public String getUpdatePlayerCol() {
        return updatePlayerCol.get();
    }

    public void setUpdatePlayerCol(int updatePlayerCol) {
        this.updatePlayerCol.set(updatePlayerCol + "");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerRow.textProperty().bind(updatePlayerRow);
        playerCol.textProperty().bind(updatePlayerCol);
    }

    public void generateMaze(ActionEvent actionEvent) {
        int rows = Integer.parseInt(textField_mazeRows.getText());
        int cols = Integer.parseInt(textField_mazeColumns.getText());

        viewModel.generateMaze(rows, cols);
    }

    public void solveMaze(ActionEvent actionEvent) {

        try {
            viewModel.solveMaze();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("Maze was not generated \n" + "Hint: Insert numbers and press 'Generate Maze'");
            error.show();
        }

        //Solution sol = viewModel.getSolution();
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open maze");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Maze files (*.maze)", "*.maze"));
        fc.setInitialDirectory(new File("./resources"));
        File chosen = fc.showOpenDialog(null);
        //...
    }

    public void keyPressed(KeyEvent keyEvent) {
        viewModel.movePlayer(keyEvent);
        keyEvent.consume();
    }

    public void setPlayerPosition(int row, int col){
        mazeDisplayer.setPlayerPosition(row, col);
        setUpdatePlayerRow(row);
        setUpdatePlayerCol(col);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        mazeDisplayer.requestFocus();
    }

    @Override
    public void update(Observable o, Object arg) {
        String change = (String) arg;
        switch (change){
            case "maze generated" -> mazeGenerated();
            case "player moved" -> playerMoved();
            case "maze solved" -> mazeSolved();
            case "solving maze" -> solvingMaze();
            case "goal reached" -> reachingGoal();
            default -> System.out.println("Not implemented change: " + change);
        }
    }

    private void reachingGoal() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Congratulations");
        alert.setHeaderText("You have reached the flag!");
        alert.setContentText("Would you like to solve another maze?");
        Optional<ButtonType> answer = alert.showAndWait();
        if ((answer.isPresent()) && (answer.get() == ButtonType.YES)) {

            Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION,"", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Check");
            alert.setHeaderText("Would you like to change difficulty?");
            alert.setContentText(null);
            Optional<ButtonType> answer2 = alert.showAndWait();
            if ((answer2.isPresent()) && (answer2.get() == ButtonType.YES))
                {
                    boxOfChoices();
                }
            else if (answer2.get() == ButtonType.NO)
                {
                    viewModel.generateMaze(Integer.parseInt(textField_mazeRows.getText()), Integer.parseInt(textField_mazeColumns.getText()));
                }
        }
        else if (answer.get() == ButtonType.NO)
        {
            ButtonType ExitButtonType = new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE);
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Exit");
            dialog.setContentText("Thank you for playing! Hope you had fun.");
            dialog.getDialogPane().getButtonTypes().add(ExitButtonType);
            boolean disabled = false; // computed based on content of text fields, for example
            dialog.getDialogPane().lookupButton(ExitButtonType).setDisable(false);
            dialog.showAndWait();
            exit();
        }
    }

    private void solvingMaze() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Solving maze...");
        alert.setContentText("Please wait.");
        alert.show();
    }

    private void mazeSolved() {

        mazeDisplayer.setSolution(viewModel.getSolution());
    }

    private void playerMoved() {
        setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());
    }

    public void boxOfChoices()
        {
            {
                List<String> choices = new ArrayList<>();
                choices.add("Easy");
                choices.add("Medium");
                choices.add("Hard");
                choices.add("Custom");

                ChoiceDialog<String> dialog = new ChoiceDialog<>("Easy", choices);
                dialog.setTitle("Difficulty Choice");
                dialog.setHeaderText("Please Choose Difficulty");
                dialog.setContentText(null);

                Optional<String> choice = dialog.showAndWait();
                if(choice.isPresent())
                    {
                        if (choice.get().equals("Easy"))
                        {
                            viewModel.generateMaze(10,10);
                        }
                        else if (choice.get().equals("Medium"))
                            {
                                viewModel.generateMaze(25,25);
                            }
                        else if(choice.get().equals("Hard"))
                            {
                                viewModel.generateMaze(50,50);
                            }
                        else if(choice.get().equals("Custom"))
                            {
                                Dialog<Pair<String, String>> dialog1 = new Dialog<>();
                                dialog1.setTitle("Resize");
                                dialog1.setHeaderText("Please enter the preferred size");


                                // Set the button types.
                                ButtonType setButtonType = new ButtonType("Set", ButtonBar.ButtonData.OK_DONE);
                                dialog1.getDialogPane().getButtonTypes().addAll(setButtonType, ButtonType.CANCEL);

                                // Create the Rows and Columns labels and fields.
                                GridPane grid = new GridPane();
                                grid.setHgap(10);
                                grid.setVgap(10);
                                grid.setPadding(new Insets(20, 150, 10, 10));

                                TextField Rows = new TextField();
                                Rows.setPromptText("Rows");
                                TextField Columns = new TextField();
                                Columns.setPromptText("Columns");

                                grid.add(new Label("Rows:"), 0, 0);
                                grid.add(Rows, 1, 0);
                                grid.add(new Label("Columns:"), 0, 1);
                                grid.add(Columns, 1, 1);
                                dialog1.getDialogPane().setContent(grid);

                                // Request focus on the username field by default.
                                Platform.runLater(Rows::requestFocus);

                                // Convert the result to a Rows-Columns-pair when the set button is clicked.
                                dialog1.setResultConverter(dialogButton -> {
                                    if (dialogButton == setButtonType) {
                                        return new Pair<>(Rows.getText(), Columns.getText());
                                    }
                                    return null;
                                });

                                Optional<Pair<String, String>> result = dialog1.showAndWait();

                                viewModel.generateMaze(Integer.parseInt(String.valueOf(Rows.getText())), Integer.parseInt (String.valueOf(Columns.getText())));
//                             if (answer2.get() == ButtonType.NO)
//                                {
//                                    viewModel.generateMaze(Integer.parseInt(textField_mazeRows.getText()), Integer.parseInt(textField_mazeColumns.getText()));
//                                }
                            }
                    }
            }
        }

    public void greet()
        {
                    Alert info = new Alert(Alert.AlertType.CONFIRMATION, "",  new ButtonType("Accept!", ButtonBar.ButtonData.YES), new ButtonType("Decline",ButtonBar.ButtonData.CANCEL_CLOSE));
        info.setTitle("Welcome");
        info.setHeaderText("Welcome to Super Mazes!");
        info.setContentText("Hello and welcome to our maze!\n" + "Mario needs to get to the flag and can really use your help.\n" + "Can you help him?");
        ButtonType buttonTypeAccept = new ButtonType("Accept!");
        ButtonType buttonTypeDecline = new ButtonType("Decline");
        info.getButtonTypes().setAll(buttonTypeAccept, buttonTypeDecline);
        Optional<ButtonType> answer = info.showAndWait();
        if ((answer.isPresent()) && (answer.get() == buttonTypeAccept))
            {
                boxOfChoices();
            }
        else if(answer.get() == buttonTypeDecline)
            {
                ButtonType ExitButtonType = new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE);
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Exit");
                dialog.setContentText("Thank you anyway, and I hope to see you soon.");
                dialog.getDialogPane().getButtonTypes().add(ExitButtonType);
                boolean disabled = false; // computed based on content of text fields, for example
                dialog.getDialogPane().lookupButton(ExitButtonType).setDisable(false);
                dialog.showAndWait();
                exit();
            }
        }
    private void mazeGenerated() {
        mazeDisplayer.drawMaze(viewModel.getMaze(), viewModel.getGoal());
    }

    public void close()
    {
        exit();
    }

    public void Zoom(ZoomEvent zoomEvent)
    {
        mazeDisplayer.setWidth(mazeDisplayer.getWidth() * zoomEvent.getZoomFactor());
        mazeDisplayer.setHeight(mazeDisplayer.getHeight() * zoomEvent.getZoomFactor());
        zoomEvent.consume();
    }
//
//        //inc(mazeDisplayer);
//        mazeDisplayer.setOnZoomStarted(Event::consume);
//
//        //dec(mazeDisplayer);
//        //log("Rectangle: Zoom event finished");
//        mazeDisplayer.setOnZoomFinished(Event::consume);

    public void mouseMoved(MouseEvent mouseEvent) {
        viewModel.movePlayerWithMouse(mouseEvent);
    }

    public void getInstructions(ActionEvent actionEvent) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Instructions");
        info.setHeaderText("Instructions:");
        info.setContentText("Your goal is to reach the flag icon\n" + "Legal steps: Up, Down, Left, Right and also diagonally\n" + "You can use the keyboard and the mouse to move");
        info.show();
    }

    public void getInfo(ActionEvent actionEvent) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("About Us");
        info.setHeaderText("Behind The Maze!");
        info.setContentText("This maze has been created by Lidor Ashtamker And Chen Kolely.\n");
        info.show();
    }
}
