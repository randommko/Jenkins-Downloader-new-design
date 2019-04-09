package core;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class JobCard extends Pane
{
    private VBox rootVBox;
    private Label jobNameLabel;
    private HBox stateHBox;
    private Label dateLabel;
    private Label descriptionText;
    private Separator separator_1, separator_2;
    private Pane downloadPane;
    private Label errorText;
    private Button downloadButton;
    private ProgressBar progressBar;
    private Label iconJobStatus;
    private Label jobStatusLabel;
    private String roundingForStatusIcon;

    private final String bgColor = "#E3F2FD";
    private final String mainColor = "#0D47A1";
    private final String secondColor = "#E3F2FD";
    private final String errorColor = "#D63908";
    private final String inProcessColor = "#10A3E2";
    private final String darkTextColor = "#000000";
    private final String lightTextColor = "#CFD8DC";
    private final String font = "-fx-font-family: Roboto;";
    private final String rounding = "-fx-border-radius: 10, 10, 10, 10;" +
                                    "-fx-background-radius: 10, 10, 10, 10;";

    private Job job;
    private boolean favorite = false;

    private static final String favoriteIconLoc
            = "image/favorite_icon.png";
    private static final String unfavoriteIconLoc
            = "image/unfavorite_icon.png";



    public JobCard (Job job)
    {
        super();

        this.job = job;

        double CARD_WIDTH = 250;    //ШИРИНА
        double CARD_HEIGHT = 170;   //ВЫСОТА

        this.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        this.setStyle("-fx-background-color: " + secondColor + ";" +
                "-fx-border-color: " + inProcessColor + ";" +
                        rounding);
        //TODO: при наведении мышки на карточку нужно подсвечивать её границы
//        this.setOnMouseDragOver(event -> {
//            this.setStyle("-fx-border-width: 5px;" +
//                    "-fx-background-color: " + secondColor + ";" +
//                    "-fx-border-color: " + mainColor + ";" +
//                    rounding);
//        });
//
//        this.setOnMouseExited(event -> {
//            this.setStyle("-fx-border-width: 1px;" +
//                    "-fx-background-color: " + secondColor + ";" +
//                    "-fx-border-color: " + inProcessColor + ";" +
//                    rounding);
//        });

        rootVBox = new VBox();
        jobNameLabel = new Label(job.getJobName() + " #" + job.getJobID());   //надпись с именем джобы и номеров
        stateHBox = new HBox(); //кружок состояния, тест состояния, избранное
        dateLabel = new Label(job.getLastChange()); //время последнего изменения статуса джобы
        descriptionText = new Label();   //надпись с описанием
        separator_1 = new Separator();      //разделитель 1
        separator_2 = new Separator();      //разделитель 2
        downloadPane = new StackPane();
        downloadButton = new Button("Download"); //кнопка скачивания
        progressBar = new ProgressBar();
        errorText = new Label();

        rootVBox.setSpacing(2); //отступы между элментами карточки
        rootVBox.setAlignment(Pos.CENTER);

        configJobNameInfo();
        configJobStateInfo(job);
        configJobDescription(job);
        configSeparators();
        configDownloadPane(job);


        rootVBox.getChildren().addAll(jobNameLabel, stateHBox, dateLabel, separator_1, descriptionText, separator_2, downloadButton);
        this.getChildren().addAll(rootVBox);
    }

    public JobCard (JobCard card)
    {
        new JobCard(card.getJob());
    }

    private void configJobNameInfo(){
        jobNameLabel.setPrefSize(250, 36);
        jobNameLabel.setAlignment(Pos.CENTER);
        jobNameLabel.setStyle("-fx-text-fill: " + lightTextColor + ";" +
                "-fx-background-color: " + mainColor + ";" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                rounding);
    }

    private void configJobStateInfo(Job job) {
        stateHBox.setPrefSize(250, 18);
        stateHBox.setMaxHeight(18);
        stateHBox.setSpacing(5);
        stateHBox.setPadding(new Insets(0, 0, 0, 5));
        stateHBox.setAlignment(Pos.CENTER_LEFT);

        dateLabel.setText(job.getLastChange());
        dateLabel.setPrefSize(250, 18);
        dateLabel.setAlignment(Pos.CENTER_LEFT);
        dateLabel.setPadding(new Insets(0, 0, 0, 5));
        dateLabel.setStyle(font +
                "-fx-font-size: 14px;" +
                "-f-text-fill:" + darkTextColor + ";");

        jobStatusLabel = new Label(job.getJobStatus().toString()); //статус дждобы текстом
        jobStatusLabel.setStyle(font + "-fx-font-size: 14px;" +
                "-f-text-fill:" + darkTextColor + ";");

        iconJobStatus = new Label();  //статус джобы цветом
        iconJobStatus.setPrefSize(16, 16);


        roundingForStatusIcon = "-fx-border-radius: 8, 8, 8, 8;" +
                "-fx-background-radius: 8, 8, 8, 8;";

        switch (job.getJobStatus()) {
            case Впроцессе:
                iconJobStatus.setStyle("-fx-background-color: " + inProcessColor + ";" + roundingForStatusIcon);
                break;
            case Приостановлено:
                iconJobStatus.setStyle("-fx-background-color: " + darkTextColor + ";"  + roundingForStatusIcon);
                break;
            case Провалилось:
                iconJobStatus.setStyle("-fx-background-color: " + errorColor + ";"  + roundingForStatusIcon);
                break;
            case Неизвестно:
                iconJobStatus.setStyle("-fx-background-color: " + darkTextColor + ";"  + roundingForStatusIcon);
                break;
            case Прервано:
                iconJobStatus.setStyle("-fx-background-color: " + darkTextColor + ";"  + roundingForStatusIcon);
                break;
            case Успешно:
                iconJobStatus.setStyle("-fx-background-color: " + mainColor + ";"  + roundingForStatusIcon);
                break;
            case built:
                iconJobStatus.setStyle("-fx-background-color: " + darkTextColor + ";"  + roundingForStatusIcon);
                break;
            case Ошибка:
                iconJobStatus.setStyle("-fx-background-color: " + errorColor + ";"  + roundingForStatusIcon);
                break;
            default:
                iconJobStatus.setStyle("-fx-background-color: " + lightTextColor + ";"  + roundingForStatusIcon);
                break;
        }

        Button favoriteIconButton = new Button();   //икнока звездочки (любимая джоба)
        favoriteIconButton.setStyle("-fx-border-color: transparent;" +
                "                    -fx-background-color: transparent;" +
                "                    -fx-text-fill:" + darkTextColor +";" +
                "                    -fx-effect: dropshadow( gaussian , rgba(0, 0, 0, 0), 0, 0 , 0 , 0 ) " +
                "                ;");

        Image favoriteIconImage = new Image(favoriteIconLoc);
        Image unfavoriteIconImage = new Image(unfavoriteIconLoc);

        if (favorite)
        {
            favoriteIconButton.setGraphic(new ImageView(favoriteIconImage));
        }
        else
        {
            favoriteIconButton.setGraphic(new ImageView(unfavoriteIconImage));
        }

        favoriteIconButton.setOnMouseClicked(event -> {
            favorite = !favorite;
            if (favorite)
            {
                favoriteIconButton.setGraphic(new ImageView(favoriteIconImage));
            }
            else
            {
                favoriteIconButton.setGraphic(new ImageView(unfavoriteIconImage));
            }
        });

        //TODO: добавить использование "любимых" работ
        stateHBox.getChildren().addAll(iconJobStatus, jobStatusLabel, favoriteIconButton);
    }

    private void configJobDescription(Job job) {
        descriptionText.setPrefSize(250, 36);
        descriptionText.setPadding(new Insets(0, 0, 0, 5));
        descriptionText.setText(job.getVisibleName());
        descriptionText.setStyle(font + "-fx-font-size: 14px;" +
                "-f-text-fill:" + darkTextColor + ";");
        descriptionText.setText(job.getVisibleName());
    }

    private void configSeparators () {
        separator_1.setMaxSize(240, 1);
        //separator_1.setStyle("-fx-border-color: " + mainColor + ";" +
        //        "");

        separator_2.setMaxSize(240, 1);
        //separator_2.setStyle("-fx-border-color: " + mainColor + ";" +
        //        "");
    }

    private void configDownloadPane(Job job) {
        errorText.setStyle(font + "-fx-border-color: transparent;" +
                "    -fx-background-color: transparent;" +
                "    -fx-text-fill: " + darkTextColor + ";" +
                ";");

        downloadButton.setPrefSize(250, 16);
        downloadButton.setAlignment(Pos.CENTER);
        downloadButton.setStyle(font + "-fx-border-color: transparent;" +
                "    -fx-background-color: transparent;" +
                "    -fx-text-fill: " + darkTextColor + ";" +
                "    -fx-effect: dropshadow( gaussian , rgba(0, 0, 0, 0), 0, 0 , 0 , 0 )" +
                ";");

        downloadButton.setOnMouseClicked((event)->{
            startJobDownload(job);
        });
        downloadButton.setOnMousePressed((event -> {
            downloadButton.setStyle(font + "-fx-border-color: transparent;" +
                    "    -fx-background-color: " + mainColor + ";" +
                    "    -fx-text-fill: " + lightTextColor + ";" +
                    "    -fx-effect: dropshadow( gaussian , rgba(0, 0, 0, 0), 0, 0 , 0 , 0 )" +
                    ";");
        }));
        downloadButton.setOnMouseReleased((event -> {
            downloadButton.setStyle(font + "-fx-border-color: " + inProcessColor + ";" +
                    "    -fx-background-color: " + inProcessColor + ";" +
                    "    -fx-text-fill: " + darkTextColor + ";" +
                    "    -fx-effect: dropshadow( gaussian , rgba(0, 0, 0, 0), 0, 0 , 0 , 0 )" +
                    ";");
        }));
        downloadButton.setOnMouseEntered((event -> {
            downloadButton.setStyle(font + "-fx-border-color: " + inProcessColor + ";" +
                    "    -fx-background-color: " + inProcessColor + ";" +
                    "    -fx-text-fill: " + darkTextColor + ";" +
                    "    -fx-effect: dropshadow( gaussian , rgba(0, 0, 0, 0), 0, 0 , 0 , 0 )" +
                    ";");
        }));
        downloadButton.setOnMouseExited((event -> {
            downloadButton.setStyle(font + "-fx-border-color: transparent;" +
                    "    -fx-background-color: transparent;" +
                    "    -fx-text-fill: " + darkTextColor + ";" +
                    "    -fx-effect: dropshadow( gaussian , rgba(0, 0, 0, 0), 0, 0 , 0 , 0 )" +
                    ";");
        }));

        if (job.isFile())
            downloadButton.setVisible(true);
        else
            downloadButton.setVisible(false);

        //TODO: не отображаются прогресс бар и текст
        progressBar.setVisible(false);
        errorText.setVisible(false);
//        progressBar.setStyle("-fx-padding: 14px;" +
//                "-fx-background-color: " + darkTextColor + ";" +
//                "-fx-accent: " + lightTextColor + ";");
//        progressBar.getStyleClass().add("-fx-padding: 14px;" +
//                "-fx-background-color:"  + darkTextColor + ";");
//        progressBar.setProgress(0.5);

        downloadPane.setVisible(true);
        //downloadPane.setAlignment(Pos.CENTER);


        downloadPane.getChildren().addAll(downloadButton, progressBar, errorText);
    }

    private boolean startJobDownload(Job job) {
        String path = AppSettings.getSavePath();
        File folder = new File(path + "\\" + job.getJobName() + "\\");

        if (!folder.exists())
        {
            if (!folder.mkdirs())
            {
                writeToLog("Can't create folder for download. Check file path in settings");
                //TODO: при начале скачивания должем менятся текст статуса приложения? подумать нужно ли это т.к.
                //при скачивании будет появляется прогресс бар в карточке  джобы
                //NewMainStyleController.setStatus(NewMainStyleController.ClientStatus._lastStatus);
                return false;
            }
        }
        double size = job.getSize();

        progressBar.setProgress(0);
        File file = new File(folder, job.getJobID() + ".zip");

        if (!file.exists())
        {
            Thread downloadThread = new Thread(download(job, size, file));
            downloadThread.start();

            if(size != -1.0)
            {
                Runnable setProgress = () ->
                {
                    while (downloadThread.isAlive()) {
                        progressBar.setProgress(file.length() / size);
                        //writeToLog("progress: " + file.length() / size);
                    }
                    showDownloadButton();
                };
                showProgressBar();
                Thread setProgressThread = new Thread(setProgress);
                setProgressThread.start();
            }
            else {
                writeToLog("Size = " + size);

                while (downloadThread.isAlive())
                    showErrorText("Unknown size");
            }
        }
        else {
            writeToLog(job.getJobName() + " (#" + job.getJobID() + ") already exists");
        }
        return true;
    }

    private Runnable download (Job job, double sizeOfLastBuild, File file)
    {
        Runnable download = () -> {
            if (sizeOfLastBuild == -1.0)
                writeToLog("Start downloading: " + job.getJobName() + " (#" + job.getJobID() + ")");
            else {
                String formattedSize = new DecimalFormat("#0.00").format((sizeOfLastBuild / 1024) / 1024);
                writeToLog("Start downloading: " + job.getJobName() + " (#" + job.getJobID() + "), " + formattedSize + "Mb");
            }

            //setStatus(NewMainStyleController.ClientStatus.Downloading, job);

            job.download(file);

            writeToLog("Download complete: " + job.getJobName() + " (#" + job.getJobID() + ")");
            trayMessage("Download complete: " + job.getJobName() + " (#" + job.getJobID() + ")");
            //setStatus(NewMainStyleController.ClientStatus.Connected);
        };

        return download;
    }

    private void writeToLog(String text)     //вывод в лог сообщения
    {
        Platform.runLater(
                () ->
                {
                    System.out.println("(JobCard) (writeToLog) Log msg:  " + text);
                    Date date = new Date();
                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

                    try {
                        //logTextArea.setText(formatForDateNow.format(date) + ": " + text + "\n" + logTextArea.getText());
                    } catch (Exception e) {
                        System.out.println("(JobCard) Error on write to log: " + e);
                    }
                }
        );
    }

    private void trayMessage (String text)
    {
        Main main = new Main();
        Platform.runLater(
                () -> {
                    try {
                        String caption = "Jenkins Downloader";  //заголовок сообщения
                        if (AppSettings.isShowNotifications())
                            main.getTrayIcon().displayMessage(caption, text, TrayIcon.MessageType.INFO); //метод отображения сообщения в трее
                    }
                    catch (Exception e)
                    {
                        System.out.println("(JobCard) Can't display tray message: " + e);
                    }
                }
        );
    }

    private void showDownloadButton()
    {
        downloadButton.setVisible(true);
        progressBar.setVisible(false);
        errorText.setVisible(false);
    }

    private void showProgressBar()
    {
        downloadButton.setVisible(false);
        progressBar.setVisible(true);
        errorText.setVisible(false);
    }

    private void showErrorText(String text)
    {
        downloadButton.setVisible(false);
        progressBar.setVisible(false);
        errorText.setVisible(true);
        errorText.setText(text);
    }

    public Job getJob()
    {
        return job;
    }

    //TODO: добавить функции для изменения состояния джобы, иконки состояния джобы, описания джобы, времени изменения состояния джобы, номера сбокри джобы
    public void changeJobStatusInCard(Job.JobStatusListing status, String time, int ID)
    {
        Platform.runLater(() -> {
            jobNameLabel.setText(job.getJobName() + " #" + ID);
            dateLabel.setText(time);
            switch (status) {
                case Впроцессе:
                    iconJobStatus.setStyle("-fx-background-color: " + inProcessColor + ";" + roundingForStatusIcon);
                    break;
                case Приостановлено:
                    iconJobStatus.setStyle("-fx-background-color: " + darkTextColor + ";"  + roundingForStatusIcon);
                    break;
                case Провалилось:
                    iconJobStatus.setStyle("-fx-background-color: " + errorColor + ";"  + roundingForStatusIcon);
                    break;
                case Неизвестно:
                    iconJobStatus.setStyle("-fx-background-color: " + darkTextColor + ";"  + roundingForStatusIcon);
                    break;
                case Прервано:
                    iconJobStatus.setStyle("-fx-background-color: " + darkTextColor + ";"  + roundingForStatusIcon);
                    break;
                case Успешно:
                    iconJobStatus.setStyle("-fx-background-color: " + mainColor + ";"  + roundingForStatusIcon);
                    break;
                case built:
                    iconJobStatus.setStyle("-fx-background-color: " + darkTextColor + ";"  + roundingForStatusIcon);
                    break;
                case Ошибка:
                    iconJobStatus.setStyle("-fx-background-color: " + errorColor + ";"  + roundingForStatusIcon);
                    break;
                default:
                    iconJobStatus.setStyle("-fx-background-color: " + lightTextColor + ";"  + roundingForStatusIcon);
                    break;
            }
        });

    }

}
