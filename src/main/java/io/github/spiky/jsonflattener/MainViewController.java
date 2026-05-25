package io.github.spiky.jsonflattener;

import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class MainViewController {
    private final Button menuReadMe;
    private final Button menuFolderSettings;
    private final Button menuSummary;
    private final Button menuResults;
    private final Button contentBack;
    private final Button contentNext;
    private final Label contentReadMe;
    private final Label contentSummary;
    private final Label contentResults;
    private final VBox contentDirectoryVBox;
    private final TextField contentSourceDirectoryPath;
    private final TextField contentDestinationDirectoryPath;

    private final JsonFlattenerModel model;

    public MainViewController (Parent root, JsonFlattenerModel model){
        this.model = model;

        this.menuReadMe = (Button) root.lookup("#menuReadMe");
        this.menuFolderSettings = (Button) root.lookup("#menuFolderSettings");
        this.menuSummary = (Button) root.lookup("#menuSummary");
        this.menuResults = (Button) root.lookup("#menuResults");
        this.contentBack = (Button) root.lookup("#contentBack");
        this.contentNext = (Button) root.lookup("#contentNext");

        Button contentSourceDirectoryBrowse = (Button) root.lookup("#contentSourceDirectoryBrowse");
        Button contentDestinationDirectoryBrowse = (Button) root.lookup("#contentDestinationDirectoryBrowse");
        Button menuAutoRun = (Button) root.lookup("#menuAutoRun");
        Button menuCancel = (Button) root.lookup("#menuCancel");

        this.contentReadMe = (Label) root.lookup("#contentReadMe");
        this.contentSummary = (Label) root.lookup("#contentSummary");
        this.contentResults = (Label) root.lookup("#contentResults");

        this.contentDirectoryVBox = (VBox) root.lookup("#contentDirectoryVBox");

        this.contentSourceDirectoryPath = (TextField) root.lookup("#contentSourceDirectoryPath");
        this.contentDestinationDirectoryPath = (TextField) root.lookup("#contentDestinationDirectoryPath");

        this.menuReadMe.setOnAction(_ -> onMenuReadMeClick());
        this.menuFolderSettings.setOnAction(_ -> onMenuFolderSettingsClick());
        this.contentBack.setOnAction(_ -> onContentBackClick());
        this.contentNext.setOnAction(_ -> {
            try {
                onContentNextClick();
            } catch (IOException ex) {
                showError(String.valueOf(ex.getCause()), ex.getMessage());
            }
        });
        contentSourceDirectoryBrowse.setOnAction(_ -> onContentSourceDirectoryBrowseClick());
        contentDestinationDirectoryBrowse.setOnAction(_ -> onContentDestinationDirectoryBrowseClick());
        menuAutoRun.setOnAction(_ -> {
            try {
                onMenuAutoRunClick();
            } catch (IOException ex) {
                showError(String.valueOf(ex.getCause()), ex.getMessage());
            }
        });
        menuCancel.setOnAction(_ -> onMenuCancelClick());

        contentSourceDirectoryPath.setText(model.sourceDirectory);
        contentDestinationDirectoryPath.setText(model.destinationDirectory);

        this.initialiseFXML();
    }

    public void initialiseFXML(){
        menuReadMe.setDisable(false);
        menuFolderSettings.setDisable(true);
        menuSummary.setDisable(true);
        menuResults.setDisable(true);
        contentSummary.setWrapText(true);
        contentResults.setWrapText(true);
        showPage(Page.READ_ME);
    }

    private void showError(String title, String msg){
        Alert error = new Alert(AlertType.ERROR);
        error.setTitle(title);
        error.setContentText(msg);
        error.showAndWait();
    }

    public enum Page {
        READ_ME, DIRECTORY, SUMMARY, RESULTS
    }

    private Page currentPage;
    private boolean directoryFlag = false;

    private void showPage(@NotNull Page page){
        contentReadMe.setVisible(false);
        contentReadMe.setManaged(false);

        contentDirectoryVBox.setVisible(false);
        contentDirectoryVBox.setManaged(false);

        contentSummary.setVisible(false);
        contentSummary.setManaged(false);

        contentResults.setVisible(false);
        contentResults.setManaged(false);
        contentResults.setTextFill(Paint.valueOf("#000000"));

        contentBack.setVisible(true);
        contentBack.setManaged(true);

        contentNext.setText("Next");

        currentPage = page;
        switch (page){
            case READ_ME -> {
                contentReadMe.setVisible(true);
                contentReadMe.setManaged(true);

                contentBack.setVisible(false);
                contentBack.setManaged(false);
            }
            case DIRECTORY -> {
                contentDirectoryVBox.setVisible(true);
                contentDirectoryVBox.setManaged(true);

                menuFolderSettings.setDisable(false);

                directoryFlag = false;

            }
            case SUMMARY -> {
                contentSummary.setVisible(true);
                contentSummary.setManaged(true);
                StringBuilder sb = new StringBuilder();
                sb.append("The utility found ").append(model.jsonFilesFound).append(" json files.\n").append(model.otherFilesFound)
                        .append(" other files have also been found and will be ignored.\n\n").append("If these numbers are within expected ranges click \"Run flattener\" to proceed");
                if (directoryFlag){
                    sb.append("\n\nThe selected destination folder is either on the same path as the source, or left empty.\nAn output folder will be created at:\n").append(model.destinationDirectory);
                }
                contentSummary.setText(sb.toString());
                menuSummary.setDisable(false);

                contentNext.setText("Run flattener");

            }
            case RESULTS -> {
                contentResults.setVisible(true);
                contentResults.setManaged(true);

                StringBuilder sb = new StringBuilder();
                String info = "\n\nIf any unsuccessful files did not generate an error report this means that the error handling itself threw an error.";
                if (model.jsonFilesSuccess==0&&model.jsonFilesFail==0&&model.jsonFilesFound==0){
                    sb.append("Successfully failed.\nOf all the 0 json files you attempted to flatten, all 0 files have <succeeded|failed>");
                }
                else if (model.jsonFilesSuccess == model.jsonFilesFound) {
                    sb.append("All json files have successfully been flattened.");
                }
                else if (model.jsonFilesSuccess != 0 && model.jsonFilesFail != 0) {
                    sb.append(model.jsonFilesSuccess).append(" json files have successfully been flattened.\nAs well as ").append(model.jsonFilesFail).append(" files having thrown an error.\nSee \"")
                            .append(Paths.get(model.destinationDirectory).resolve("0ERROR")).append("\" for more information.").append(info);
                }
                else if (model.jsonFilesFail==model.jsonFilesFound) {
                    sb.append("All files have thrown an error.\nSee \"").append(Paths.get(model.destinationDirectory).resolve("0ERROR")).append("\" for more information.").append(info);
                }
                else {
                    sb.append("No files were processed, or all files threw unhandled errors.\nYou should not be seeing this under normal circumstances").append(info);
                }
                if (model.ioExceptionFlag){
                    sb.append("\n\nAn IO Exception was thrown, please see IOException.log in the output folder.")
                            .append("\nIf the file, or folder, does not exist, the program has run into critical issues such as drive space limitations. ")
                            .append("Please verify your hardware is capable of running this program as intended.");
                    contentResults.setTextFill(Paint.valueOf("#ff2020"));
                }
                contentResults.setText(sb.toString());
                menuResults.setDisable(false);

                contentBack.setVisible(false);
                contentBack.setManaged(false);

                contentNext.setText("Start again?");


            }
            default -> showError("showPage","Error during showPage switching.\nIf you're seeing this something went VERY wrong.");
        }
    }

    protected void onMenuReadMeClick(){
        showPage(Page.READ_ME);
    }

    protected void onMenuFolderSettingsClick(){
        showPage(Page.DIRECTORY);
    }

    protected void onContentBackClick(){
        switch (currentPage){
            case DIRECTORY -> showPage(Page.READ_ME);
            case SUMMARY -> showPage(Page.DIRECTORY);
            default -> showError("BackClick","Error during onContentBackClick switching.\nIf you're seeing this something went VERY wrong.");
        }
    }

    protected void onContentNextClick() throws IOException {
        switch (currentPage) {
            case READ_ME -> showPage(Page.DIRECTORY);
            case DIRECTORY -> {
                model.sourceDirectory = contentSourceDirectoryPath.getText();
                if (model.sourceDirectory.isBlank()) {
                    showError("Path error", "Please provide a source folder!");
                    break;
                }
                try{
                    if(!Files.isDirectory(Paths.get(model.sourceDirectory))){
                        showError("Path error","The selected source folder does not exist");
                        break;
                    }
                    Paths.get(contentDestinationDirectoryPath.getText());
                } catch (InvalidPathException e) {
                    showError("Invalid paths", "One or both of your filepaths contain illegal characters");
                    break;
                }

                if (Paths.get(contentDestinationDirectoryPath.getText()).equals(Paths.get(model.sourceDirectory))){
                    model.destinationDirectory = model.sourceDirectory+"\\JsonFlattenerOutput";
                    directoryFlag = true;
                } else if (contentDestinationDirectoryPath.getText().isBlank()) {
                    model.destinationDirectory = model.sourceDirectory+"\\JsonFlattenerOutput";
                    directoryFlag = true;
                } else {
                    model.destinationDirectory = contentDestinationDirectoryPath.getText();
                }
                model.fileScan();
                showPage(Page.SUMMARY);
            }
            case SUMMARY -> {
                model.fileFlatten();
                showPage(Page.RESULTS);
            }
            case RESULTS -> {
                showPage(Page.DIRECTORY);
                menuFolderSettings.setDisable(true);
                menuSummary.setDisable(true);
                menuResults.setDisable(true);
                //Reset directory paths for fresh start.
            }
            default -> showError("NextClick","Error during onContentNextClick switching.\nIf you're seeing this something went VERY wrong.");
        }
    }

    protected void onContentSourceDirectoryBrowseClick(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(model.sourceDirectory));
        chooser.setTitle("Please select the source folder.");
        Stage stage = (Stage) contentSourceDirectoryPath.getScene().getWindow();
        File selectedDirectory = chooser.showDialog(stage);
        if (selectedDirectory != null){
            contentSourceDirectoryPath.setText(selectedDirectory.getAbsolutePath());
            contentDestinationDirectoryPath.setText("");
        }
    }

    protected void onContentDestinationDirectoryBrowseClick(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(model.sourceDirectory));
        chooser.setTitle("Please select the destination folder.");
        Stage stage = (Stage) contentDestinationDirectoryPath.getScene().getWindow();
        File selectedDirectory = chooser.showDialog(stage);
        if (selectedDirectory != null){
            contentDestinationDirectoryPath.setText(selectedDirectory.getAbsolutePath());
        }
    }

    protected void onMenuAutoRunClick() throws IOException{
        model.fileScan();
        model.fileFlatten();
        showPage(Page.RESULTS);
    }

    protected void onMenuCancelClick(){
        System.exit(0);
    }
}
