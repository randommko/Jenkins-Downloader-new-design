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
import view.MainController;

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

import static core.AppSettings.findTagInConfigFile;
import static core.AppSettings.findTimeInConfigFile;


public class Job extends Pane {

    public enum JobStatusListing {built,  Успешно, Провалилось, Прервано, Приостановлено, Впроцессе, Неизвестно, Ошибка}

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
    private Pane overlayPane;

    private final String bgColor = "#E3F2FD";
    private final String mainColor = "#0D47A1";
    private final String secondColor = "#E3F2FD";
    private final String errorColor = "#D63908";
    private final String inProcessColor = "#10A3E2";
    private final String darkTextColor = "#000000";
    private final String lightTextColor = "#CFD8DC";
    private final String font = "-fx-font-family: Roboto;";
    private final String rad = "5";
    private final String rounding = "-fx-border-radius: " + rad +", " + rad + " , " + rad + ", " +  rad + ";" +
            "-fx-background-radius:" + rad +", " + rad + " , " + rad + ", " +  rad + ";";

    private double CARD_WIDTH = 250;    //ШИРИНА
    private double CARD_HEIGHT = 165;   //ВЫСОТА

    private String visibleName; //отображемое имя (необязательно)
    private int jobID;          //номер последней сборки
    private String jobName ;    //имя джобы
    private URL jobURL;         // serverAddress + /view/actual/job/ + jobName + /lastSuccessfulBuild/artifact/*zip*/archive.zip
    private JobStatusListing jobStatus; //статус последней сборки
    private boolean isFile;
    private String lastChange;
    private double size;    //размер джобы

    private boolean favorite = false;

    private static final String favoriteIconLoc
            = "image/favorite_icon.png";
    private static final String unfavoriteIconLoc
            = "image/unfavorite_icon.png";


//TODO: переработать этот класс. Сделать одну функцию на скачивание которая вызывается при нажатии на кнопку

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

        if ( !(findTimeInConfigFile(jobName)).equals(""))
            this.lastChange = (findTimeInConfigFile(jobName));
        else
            this.lastChange     = "-";

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
        this.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        this.setStyle("-fx-background-color: " + secondColor + ";" +
                "-fx-border-color: " + inProcessColor + ";" +
                rounding);
        //TODO: при наведении мышки на карточку нужно подсвечивать её границы

        rootVBox = new VBox();
        jobNameLabel = new Label(jobName + " #" + jobID);   //надпись с именем джобы и номеров
        stateHBox = new HBox(); //кружок состояния, тест состояния, избранное
        dateLabel = new Label(lastChange); //время последнего изменения статуса джобы
        descriptionText = new Label();   //надпись с описанием
        separator_1 = new Separator();      //разделитель 1
        separator_2 = new Separator();      //разделитель 2
        downloadPane = new StackPane();
        downloadButton = new Button("Download"); //кнопка скачивания
        progressBar = new ProgressBar();
        errorText = new Label();

        //TODO: сделать overlayPane прозрачной
        overlayPane = new Pane();

        rootVBox.setSpacing(2); //отступы между элментами карточки
        rootVBox.setAlignment(Pos.CENTER);

        configJobNameInfo();
        configJobStateInfo();
        configJobDescription();
        configSeparators();
        configDownloadPane();


        rootVBox.getChildren().addAll(jobNameLabel, stateHBox, dateLabel, separator_1, descriptionText, separator_2, downloadPane, overlayPane);
        this.getChildren().addAll(rootVBox);
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

    private void configJobStateInfo() {
        stateHBox.setPrefSize(250, 18);
        stateHBox.setMaxHeight(18);
        stateHBox.setSpacing(5);
        stateHBox.setPadding(new Insets(0, 0, 0, 5));
        stateHBox.setAlignment(Pos.CENTER_LEFT);

        dateLabel.setText(lastChange);
        dateLabel.setPrefSize(250, 18);
        dateLabel.setAlignment(Pos.CENTER_LEFT);
        dateLabel.setPadding(new Insets(0, 0, 0, 5));
        dateLabel.setStyle(font +
                "-fx-font-size: 14px;" +
                "-f-text-fill:" + darkTextColor + ";");

        jobStatusLabel = new Label(jobStatus.toString()); //статус дждобы текстом
        jobStatusLabel.setStyle(font + "-fx-font-size: 14px;" +
                "-f-text-fill:" + darkTextColor + ";");

        iconJobStatus = new Label();  //статус джобы цветом
        iconJobStatus.setPrefSize(16, 16);


        roundingForStatusIcon = "-fx-border-radius: 8, 8, 8, 8;" +
                "-fx-background-radius: 8, 8, 8, 8;";

        switch (jobStatus) {
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

    private void configJobDescription() {
        descriptionText.setPrefSize(250, 36);
        descriptionText.setPadding(new Insets(0, 0, 0, 5));
        descriptionText.setStyle(font + "-fx-font-size: 14px;" +
                "-f-text-fill:" + darkTextColor + ";");
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
            startJobDownload();
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

        if (isFile)
            downloadButton.setVisible(true);
        else
            downloadButton.setVisible(false);

        //TODO: не отображаются прогресс бар и текст
        progressBar.setVisible(false);
        errorText.setVisible(false);
        progressBar.setStyle("-fx-padding: 14px;" +
                "-fx-background-color: " + darkTextColor + ";" +
                "-fx-accent: " + lightTextColor + ";");
        progressBar.getStyleClass().add("-fx-padding: 14px;" +
                "-fx-background-color:"  + darkTextColor + ";");
        progressBar.setProgress(0.5);

        downloadPane.setVisible(true);
//        downloadPane.setAlignment(Pos.CENTER);
        downloadPane.getChildren().addAll(downloadButton, progressBar, errorText);
    }




    private boolean startJobDownload() {
        String path = AppSettings.getSavePath();
        File folder = new File(path + "\\" + jobName + "\\");

        if (!folder.exists())
        {
            if (!folder.mkdirs())
            {
                writeToLog("Can't create folder for download. Check file path in settings");
                //TODO: при начале скачивания должем менятся текст статуса приложения? подумать нужно ли это т.к.
                //при скачивании будет появляется прогресс бар в карточке  джобы
                //MainController.setStatus(MainController.ClientStatus._lastStatus);
                return false;
            }
        }
        //double size = job.getSize();

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

    public String getLastChange() {
        return lastChange;
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
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Date date = new Date();
        this.lastChange     = formatForDateNow.format(date);
        AppSettings.changeSettingInConfig(jobName + "_time", this.lastChange);  //запись в конфиг времени последнего изменения
    }

    public void setJobStatus(JobStatusListing jobStatus) {
        this.jobStatus = jobStatus;
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Date date = new Date();
        this.lastChange = formatForDateNow.format(date);
        AppSettings.changeSettingInConfig(jobName + "_time", this.lastChange);  //запись в конфиг времени последнего изменения
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
        };

        return download;
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
                        System.out.println("(Job) Can't display tray message: " + e);
                    }
                }
        );
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
}
