package core;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static core.AppSettings.findFavoriteInConfigFile;
import static core.AppSettings.findTagInConfigFile;
import static core.AppSettings.findTimeInConfigFile;


public class Job extends Pane {



    public enum JobStatusListing {built,  Успешно, Провалилось, Прервано, Приостановлено, Впроцессе, Неизвестно, Ошибка}

    private VBox rootVBox;
    private Label jobNameLabel;
    private HBox stateHBox;
    private Label dateLabel;
    private Label descriptionText;
    private Button favoriteIconButton;
    private Separator separator_1, separator_2;
    private StackPane downloadPane;
    private Label errorText;
    private Button downloadButton;
    private ProgressBar progressBar;
    private Label iconJobStatus;
    private Label jobStatusLabel;
    private String roundingForStatusIcon;


    private final String mainColor = "#0D47A1";
    private final String cardBackgroundColor = "#E3F2FD";
    private final String cardBorderColor = "#10A3E2";

    private final String errorColor = "#F30707";
    private final String inProcessColor = "#10A3E2";
    private final String successColor = "#00BD35";
    private final String neutralColor = "#BEC4C7";

    private String currentCardColor;

    private final String darkTextColor = "#000000";
    private final String lightTextColor = "#E3E9EC";

    private final String font = "-fx-font-family: Roboto;";
    private final String rad = "5";
    private final String rounding = "-fx-border-radius: " + rad +", " + rad + " , " + rad + ", " +  rad + ";" +
            "-fx-background-radius:" + rad +", " + rad + " , " + rad + ", " +  rad + ";";


    private double CARD_WIDTH = 250;    //ШИРИНА
    private double CARD_HEIGHT = 175;   //ВЫСОТА

    private String visibleName; //отображемое имя (необязательно)
    private int jobID;          //номер последней сборки
    private String jobName ;    //имя джобы
    private URL jobURL;         // serverAddress + /view/actual/job/ + jobName + /lastSuccessfulBuild/artifact/*zip*/archive.zip
    private JobStatusListing jobStatus; //статус последней сборки
    private boolean isFile;
    private String lastChange;
    private double size;    //размер джобы
    private boolean isFavorite = false;

//    private boolean favorite = false;

    private static final String favoriteIconLoc
            = "image/favorite_icon.png";
    private static final String unfavoriteIconLoc
            = "image/unfavorite_icon.png";



    public Job (String jobName, int jobID, JobStatusListing jobStatus)
    {
        super();
        setCoreParams(jobName, jobID, jobStatus);
        setViewParams();
    }

    private void setCoreParams(String jobName, int jobID, JobStatusListing jobStatus)
    {
        this.jobName        = jobName ;
        this.jobID          = jobID;
        this.jobStatus      = jobStatus;
        this.size           = AppSettings.findSizeInConfigFile(this.jobName);

        if ( !(findTagInConfigFile(jobName)).equals(""))
            this.visibleName = (findTagInConfigFile(jobName));   //ищем в конфиг-файле тэг для найденой работы
        else
            this.visibleName    = "";

        if ( !(findTimeInConfigFile(jobName)).equals(""))       //ищем в конфиг-файле время последнего изменения
            this.lastChange = (findTimeInConfigFile(jobName));
        else
            this.lastChange     = "-";


        this.isFavorite = findFavoriteInConfigFile(jobName);


        try
        {
            this.jobURL = new URL(AppSettings.getServerAddress() + "/view/actual/job/" + jobName + "/lastSuccessfulBuild/artifact/*zip*/archive.zip");  //формируем ВОЗМОЖНУЮ(!) ссылку на скачивание работы
            InputStream inputstream = this.jobURL.openStream(); //пробуем открыть сформированную ссылку, если не получится то мы сразу попадаем в блок catch
            inputstream.close();
            this.isFile = true;
        }
        catch (Exception err)
        {
            this.isFile = false;
        }
    }

    private void setViewParams()
    {
        configMainPane();

        rootVBox = new VBox();
        rootVBox.setPadding(new Insets(1, 0,0, 0)); //что бы было видно рамку на верхней грани карточки

        separator_1 = new Separator();      //разделитель 1
        separator_2 = new Separator();      //разделитель 2

        rootVBox.setSpacing(2); //отступы между элментами карточки
        rootVBox.setAlignment(Pos.CENTER);

        configJobNameInfo();
        configJobStateInfo();
        configJobDescription();
        configSeparators();
        configDownloadPane();

        rootVBox.getChildren().addAll(jobNameLabel, stateHBox, dateLabel, separator_1, descriptionText, separator_2, downloadPane);
        this.getChildren().addAll(rootVBox);
    }

    private void configMainPane()
    {
        this.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        this.setStyle("-fx-background-color: " + cardBackgroundColor + ";" +
                "-fx-border-color: " + currentCardColor + ";" +
                rounding);

        this.setOnMouseEntered((event -> {
            this.setStyle("-fx-background-color: " + cardBackgroundColor + ";" +
                    "-fx-border-color:" + currentCardColor + ";" +
                    "-fx-effect: dropshadow(gaussian, " + currentCardColor + ", 5, 0.1, 1, 1);" +
                    rounding);
        }));
        this.setOnMouseExited((event -> {
            this.setStyle("-fx-background-color: " + cardBackgroundColor + ";" +
                    "-fx-border-color: " + currentCardColor + ";" +
                    rounding);
        }));
    }

    private void configJobNameInfo(){
        jobNameLabel = new Label();   //надпись с именем джобы и номеров
        jobNameLabel.setText(jobName + " #" + jobID);
        jobNameLabel.setPrefSize(CARD_WIDTH - 2, 36);
        jobNameLabel.setPadding(new Insets(0, 0, 0, 0));

        jobNameLabel.setAlignment(Pos.CENTER);
        jobNameLabel.setStyle("-fx-text-fill: " + lightTextColor + ";" +
                "-fx-background-color: " + mainColor + ";" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                rounding);
    }

    private void configJobStateInfo() {
        stateHBox = new HBox(); //кружок состояния, тест состояния, избранное

        stateHBox.setPrefSize(250, 18);
        stateHBox.setMaxHeight(18);
        stateHBox.setSpacing(5);
        stateHBox.setPadding(new Insets(0, 0, 0, 5));
        stateHBox.setAlignment(Pos.CENTER_LEFT);

        dateLabel = new Label(); //время последнего изменения статуса джобы

        dateLabel.setText(lastChange);
        dateLabel.setPrefSize(250, 18);
        dateLabel.setAlignment(Pos.CENTER_LEFT);
        dateLabel.setPadding(new Insets(0, 0, 0, 5));
        dateLabel.setStyle(font +
                "-fx-font-size: 14px;" +
                "-fx-text-fill:" + darkTextColor + ";");

        jobStatusLabel = new Label(jobStatus.toString()); //статус дждобы текстом
        jobStatusLabel.setStyle(font + "-fx-font-size: 14px;" +
                "-fx-text-fill:" + darkTextColor + ";");

        iconJobStatus = new Label();  //статус джобы цветом
        iconJobStatus.setPrefSize(16, 16);


        roundingForStatusIcon = "-fx-border-radius: 8, 8, 8, 8;" +
                "-fx-background-radius: 8, 8, 8, 8;";

        changeJobStatusOnCard(jobStatus);
        configFavoriteButton();


        stateHBox.getChildren().addAll(iconJobStatus, jobStatusLabel, favoriteIconButton);
    }

    private void configFavoriteButton()
    {
        favoriteIconButton = new Button();   //икнока звездочки (любимая джоба)
        favoriteIconButton.setStyle("-fx-border-color: transparent;" +
                "                    -fx-background-color: transparent;" +
                "                ;");

        Image favoriteIconImage = new Image(favoriteIconLoc);
        Image unfavoriteIconImage = new Image(unfavoriteIconLoc);

        if (isFavorite)
        {
            favoriteIconButton.setGraphic(new ImageView(favoriteIconImage));
        }
        else
        {
            favoriteIconButton.setGraphic(new ImageView(unfavoriteIconImage));
        }

        favoriteIconButton.setOnMouseClicked(event -> {
            isFavorite = !isFavorite;
            AppSettings.changeSettingInConfig(jobName + "_favorite", String.valueOf(isFavorite));
            if (isFavorite)
            {
                favoriteIconButton.setGraphic(new ImageView(favoriteIconImage));
            }
            else
            {
                favoriteIconButton.setGraphic(new ImageView(unfavoriteIconImage));
            }
        });

        //TODO: добавить использование "любимых" работ
    }

    private void configJobDescription() {
        descriptionText = new Label();   //надпись с описанием

        descriptionText.setPrefSize(250, 36);
        descriptionText.setPadding(new Insets(0, 0, 0, 5));
        descriptionText.setStyle(font + "-fx-font-size: 14px;" +
                "-fx-text-fill:" + darkTextColor + ";");
        descriptionText.setText(visibleName);
    }

    private void configSeparators () {
        separator_1.setMaxSize(240, 1);
        //separator_1.setStyle("-fx-border-color: " + mainColor + ";" +
        //        "");

        separator_2.setMaxSize(240, 1);
        //separator_2.setStyle("-fx-border-color: " + mainColor + ";" +
        //        "");
    }

    private void configDownloadPane() {
        downloadPane = new StackPane();

        configDownloadButton();
        configProgressBar();
        configErrorText();

        downloadPane.setMaxSize(CARD_WIDTH - 6, 16);
        downloadPane.setPadding(new Insets(0, 0, 0, 0));
        downloadPane.setVisible(true);
        downloadPane.setAlignment(Pos.CENTER);

        downloadPane.getChildren().addAll(downloadButton, progressBar, errorText);
    }

    private void configDownloadButton()
    {
        downloadButton = new Button("Download"); //кнопка скачивания
        downloadButton.setPrefSize(CARD_WIDTH - 6, 14);
        downloadButton.setAlignment(Pos.CENTER);
        int borderWidth = 2;
        downloadButton.setStyle(font + " -fx-border-color: transparent;" +
                "    -fx-border-width: " + borderWidth + ", " + borderWidth + ", " + borderWidth + ", " + borderWidth + ";" +
                "    -fx-border-radius: 5, 5, 5, 5;" +
                "    -fx-background-color: transparent;" +
                "    -fx-background-radius: 5, 5, 5, 5;" +
                "    -fx-text-fill: " + darkTextColor + ";" +
                /*"    -fx-effect: dropshadow( gaussian , #000000, 0, 0 , 0, 0)" +*/
                ";");

        downloadButton.setOnMouseClicked((event)->{     //клик по кнопке
            startJobDownload();
        });
        downloadButton.setOnMousePressed((event -> {    //кнопка нажата
            downloadButton.setStyle(font + " -fx-border-color: " + mainColor + ";" +
                    "    -fx-border-width: " + borderWidth + ", " + borderWidth + ", " + borderWidth + ", " + borderWidth + ";" +
                    "    -fx-border-radius: 5, 5, 5, 5;" +
                    "    -fx-background-radius: 5, 5, 5, 5;" +
                    "    -fx-background-color: #153A74;" +    //сделать цвет кнопки темнее на пару тонов
                    "    -fx-text-fill: " + lightTextColor + ";" +
                    /*"    -fx-effect: dropshadow( gaussian , #000000, 0, 0, 0, 0)" +*/
                    ";");
        }));
        downloadButton.setOnMouseReleased((event -> {   //кнопка отпущена
            downloadButton.setStyle(font + " -fx-border-color: transparent;" +
                    "    -fx-border-width: " + borderWidth + ", " + borderWidth + ", " + borderWidth + ", " + borderWidth + ";" +
                    "    -fx-border-radius: 5, 5, 5, 5;" +
                    "    -fx-background-radius: 5, 5, 5, 5;" +
                    "    -fx-background-color: " + mainColor + ";" +
                    "    -fx-text-fill: " + lightTextColor + ";" +
                    /*"    -fx-effect: dropshadow( gaussian , #000000, 0, 0, 0, 0)" +*/
                    ";");
        }));
        downloadButton.setOnMouseEntered((event -> {        //курсор попал на кнопку
            downloadButton.setStyle(font + " -fx-border-color: " + mainColor + ";" +
                    "    -fx-border-width: " + borderWidth + ", " + borderWidth + ", " + borderWidth + ", " + borderWidth + ";" +
                    "    -fx-border-radius: 5, 5, 5, 5;" +
                    "    -fx-background-radius: 5, 5, 5, 5;" +
                    "    -fx-background-color: " + mainColor + ";" +
                    "    -fx-text-fill: " + lightTextColor + ";" +
                    /*"    -fx-effect: dropshadow( gaussian, #000000, 0, 0, 0, 0)" +*/
                    ";");
        }));
        downloadButton.setOnMouseExited((event -> {     //курсор ушел с кнопки
            downloadButton.setStyle(font + " -fx-border-color: transparent;" +
                    "    -fx-border-width: " + borderWidth + ", " + borderWidth + ", " + borderWidth + ", " + borderWidth + ";" +
                    "    -fx-border-radius: 5, 5, 5, 5;" +
                    "    -fx-background-radius: 5, 5, 5, 5;" +
                    "    -fx-background-color: transparent;" +
                    "    -fx-text-fill: " + darkTextColor + ";" +
                    /*"    -fx-effect: dropshadow( gaussian, #000000, 0, 0, 0, 0)" +*/
                    ";");
        }));

        if (isFile)
            downloadButton.setVisible(true);
        else
            downloadButton.setVisible(false);
    }
    private void configProgressBar()
    {
        progressBar = new ProgressBar();

        progressBar.setVisible(false);
        //TODO: сделать прогресс бар красивым!
        progressBar.getStylesheets().add("css/JobCard.css");
        progressBar.setPrefSize(CARD_WIDTH - 6, 16);
        progressBar.setProgress(0);
    }
    private void configErrorText()
    {
        errorText = new Label();
        errorText.setStyle(font + "-fx-border-color: transparent;" +
                "    -fx-background-color: transparent;" +
                "    -fx-text-fill: " + darkTextColor + ";" +
                ";");
        errorText.setVisible(false);
    }




    private boolean startJobDownload() {
        String path = AppSettings.getSavePath();
        File folder = new File(path + "\\" + jobName + "\\");

        if (!folder.exists())
        {
            if (!folder.mkdirs())
            {
                writeToLog("Can't create folder for download. Check file path in settings");
                return false;
            }
        }

        progressBar.setProgress(0);
        File file = new File(folder, jobID + ".zip");

        if (!file.exists())
        {
            Thread downloadThread = new Thread(download(file));
            downloadThread.start();

            if(size != -1.0)
            {
                Runnable setProgress = () ->
                {
                    while (downloadThread.isAlive()) {
                        progressBar.setProgress(file.length() / size);
                        //writeToLog("progress: " + file.length() / size);
                    }
                };

                showProgressBar();
                Thread setProgressThread = new Thread(setProgress);
                setProgressThread.start();
            }
            else {
                writeToLog("Size = " + size);
                showErrorText("Job downloading...");
            }
        }
        else {
            writeToLog(jobName + " (#" + jobID + ") already exists");
        }
        return true;
    }

    private void writeToLog(String text)     //вывод в лог сообщения
    {
        Platform.runLater(
                () ->
                {
                    //System.out.println("(Job) (writeToLog) Log msg:  " + text);
                    Date date = new Date();
                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

                    try {
                        //logTextArea.setText(formatForDateNow.format(date) + ": " + text + "\n" + logTextArea.getText());
                        System.out.println("(Job) (writeToLog) " + formatForDateNow.format(date) + ":  " + text);
                    } catch (Exception e) {
                        System.out.println("(Job) Error on write to log: " + e);
                    }
                }
        );
    }




    public String getJobName() {
        return jobName;
    }

    public String getVisibleName() {
        return visibleName;
    }

    public int getJobID() {
        return jobID;
    }

    public JobStatusListing getJobStatus()
    {
        return jobStatus;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public Date getLastChangeDate()
    {
        if (!lastChange.equals("-"))
        {
            Date lastChangeDate;

            try {
                SimpleDateFormat format =
                        new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                lastChangeDate = format.parse(lastChange);
            }
            catch(ParseException pe) {
                throw new IllegalArgumentException(pe);
            }
            return lastChangeDate;
        }
        else
            return null;
    }



    public void setVisibleName(String visibleName) {
        this.visibleName = visibleName;
        Platform.runLater(() -> {
            descriptionText.setText(visibleName);
        });

    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Date date = new Date();
        this.lastChange     = formatForDateNow.format(date);

        changeJobIDOnCard(jobID);
        changeJobTimeOnCard();
        AppSettings.changeSettingInConfig(jobName + "_time", this.lastChange);  //запись в конфиг времени последнего изменения
    }

    public void setJobStatus(JobStatusListing jobStatus) {
        this.jobStatus = jobStatus;
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Date date = new Date();
        this.lastChange = formatForDateNow.format(date);

        changeJobStatusOnCard(jobStatus);
        changeJobTimeOnCard();

        AppSettings.changeSettingInConfig(jobName + "_time", this.lastChange);  //запись в конфиг времени последнего изменения
    }



    private void changeJobStatusOnCard(JobStatusListing jobStatus)
    {
        Platform.runLater(() -> {
            switch (jobStatus) {
                case Впроцессе:
                    jobStatusLabel.setText("В процессе");
                    iconJobStatus.setStyle("-fx-background-color: " + inProcessColor + ";" + roundingForStatusIcon);
                    currentCardColor = inProcessColor;
                    this.setStyle(this.getStyle() + "-fx-border-color: " + inProcessColor + ";");
                    break;
                case Приостановлено:
                    jobStatusLabel.setText("Приостановленно");
                    iconJobStatus.setStyle("-fx-background-color: " + neutralColor + ";"  + roundingForStatusIcon);
                    currentCardColor = neutralColor;
                    this.setStyle(this.getStyle() + "-fx-border-color: " + neutralColor + ";");
                    break;
                case Провалилось:
                    jobStatusLabel.setText("Провалилось");
                    iconJobStatus.setStyle("-fx-background-color: " + errorColor + ";"  + roundingForStatusIcon);
                    currentCardColor = errorColor;
                    this.setStyle(this.getStyle() + "-fx-border-color: " + errorColor + ";");
                    break;
                case Неизвестно:
                    jobStatusLabel.setText("неизвестно");
                    iconJobStatus.setStyle("-fx-background-color: " + neutralColor + ";"  + roundingForStatusIcon);
                    currentCardColor = neutralColor;
                    this.setStyle(this.getStyle() + "-fx-border-color: " + neutralColor + ";");
                    break;
                case Прервано:
                    jobStatusLabel.setText("Прервано");
                    iconJobStatus.setStyle("-fx-background-color: " + neutralColor + ";"  + roundingForStatusIcon);
                    currentCardColor = neutralColor;
                    this.setStyle(this.getStyle() + "-fx-border-color: " + neutralColor + ";");
                    break;
                case Успешно:
                    jobStatusLabel.setText("Успешно");
                    iconJobStatus.setStyle("-fx-background-color: " + successColor + ";"  + roundingForStatusIcon);
                    currentCardColor = successColor;
                    this.setStyle(this.getStyle() + "-fx-border-color: " + successColor + ";");
                    break;
                case built:
                    jobStatusLabel.setText("Not build");
                    iconJobStatus.setStyle("-fx-background-color: " + neutralColor + ";"  + roundingForStatusIcon);
                    this.setStyle(this.getStyle() + "-fx-border-color: " + neutralColor + ";");
                    break;
                case Ошибка:
                    jobStatusLabel.setText("Ошибка");
                    iconJobStatus.setStyle("-fx-background-color: " + errorColor + ";"  + roundingForStatusIcon);
                    currentCardColor = errorColor;
                    this.setStyle(this.getStyle() + "-fx-border-color: " + errorColor + ";");
                    break;
                default:
                    jobStatusLabel.setText("Unknown job status!");
                    iconJobStatus.setStyle("-fx-background-color: " + errorColor + ";"  + roundingForStatusIcon);
                    currentCardColor = neutralColor;
                    this.setStyle(this.getStyle() + "-fx-border-color: " + errorColor + ";");
                    break;
            }
        });
    }
    private void changeJobIDOnCard(int id)
    {
        Platform.runLater(() -> {
            jobNameLabel.setText(jobName + " #" + id);
        });
    }
    private void changeJobTimeOnCard()
    {
        Platform.runLater(() -> {
            dateLabel.setText(lastChange);
        });
    }



    private Runnable download (File file)
    {
         Runnable download = () -> {
            if (size == -1.0)
                writeToLog("Start downloading: " + jobName + " (#" + jobID + ")");
            else {
                String formattedSize = new DecimalFormat("#0.00").format((size / 1024) / 1024);
                writeToLog("Start downloading: " + jobName + " (#" + jobID + "), " + formattedSize + "Mb");
            }

             startDownload(file);
             writeToLog("Download complete: " + jobName + " (#" + jobID + ")");
             showMessageInTray("Download complete: " + jobName + " (#" + jobID + ")");
             showDownloadButton();
        };


        return download;
    }

    private void startDownload(File file)
    {
        FileOutputStream fileoutputstream;
        InputStream inputstream;

        try {
            fileoutputstream = new FileOutputStream(file);
            inputstream = jobURL.openStream();

            byte abyte0[] = new byte[4096];
            for (int j = 0; -1 != (j = inputstream.read(abyte0));)
                fileoutputstream.write(abyte0, 0, j);
            inputstream.close();
            fileoutputstream.close();

            this.size = file.length();
            AppSettings.changeSettingInConfig(this.jobName + "_size", String.valueOf(this.size));

            unzip(file);
        }
        catch (IOException e) {
            System.out.println("Ошибка: " + e);
        }
    }

    private void unzip(File file)
    {
        String s = file.getPath();
        String s1 = s.substring(0, s.length() - 4);
        try {
            ZipFile zipfile = new ZipFile(file);
            zipfile.extractAll(s1);
        }
        catch(ZipException zipexception) {
            zipexception.printStackTrace();
        }
    }



    private void showDownloadButton()
    {
        Platform.runLater(() -> {
            downloadButton.setVisible(true);
            progressBar.setVisible(false);
            errorText.setVisible(false);
        });

    }

    private void showProgressBar()
    {
        Platform.runLater(() -> {
            downloadButton.setVisible(false);
            progressBar.setVisible(true);
            errorText.setVisible(false);
        });


    }

    private void showErrorText(String text)
    {
        Platform.runLater(() -> {
            downloadButton.setVisible(false);
            progressBar.setVisible(false);
            errorText.setVisible(true);
            errorText.setText(text);
        });
    }


    private void sleep(int timeout)
    {
        try
        {
            TimeUnit.SECONDS.sleep(timeout);
        }
        catch (Exception err)
        {
            System.out.println("(MainController) (sleep) Can't call sleep method: " + err);
        }
    }

    private void showMessageInTray (String text)
    {
        Main main = new Main();
        TrayIcon trayIcon = main.getTrayIcon();

        Runnable showMsg = () -> {
            try {
                String caption = "Jenkins Downloader";  //заголовок сообщения
                if (AppSettings.isShowNotifications())
                    trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO); //метод отображения сообщения в трее
            }
            catch (Exception e)
            {
                System.out.println("Can't display tray message: " + e);
            }
        };
        Thread trayMessage = new Thread(showMsg);
        trayMessage.start();
    }
}
