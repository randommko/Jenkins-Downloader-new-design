package view;

import core.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


public class NewMainStyleController
{
    private static Stage settingsStage, helpStage;
    private static final String settingsImageURL = "image/settings(small).png";
    private static final String helpImageURL = "image/help.png";
    private final int WIDTH = 810;
    private final int HEIGHT = 800;
    private static JenkinsJobs jobsForMainForm;
    private static JenkinsJobs allFoundJobs;
    private static ObservableList<JobCard> JobCards = FXCollections.observableArrayList ();
    private Main main;
    public enum ClientStatus {Disconnected, Connected, Downloading, Extracting, Connecting, Updating, _lastStatus}
    private ClientStatus lastStatus, actualStatus;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private FlowPane botFlowPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label statusLabel;



    @FXML
    private void initialize() //метод в котором выполняется код при запуске приложения
    {
        jobsForMainForm = new JenkinsJobs();
        allFoundJobs = new JenkinsJobs();

        AppSettings.loadConfigFile();

        initWindow();

        setStatus(ClientStatus.Disconnected);

        connectToServer();

        Thread threadUpdateStatusOfJobs = new Thread(endlessUpdateStatusOfJobs());
        threadUpdateStatusOfJobs.start();

    }

    @FXML
    private void connectToServer()              //нажатие кнопки "Refresh"
    {
        setStatus(ClientStatus.Connecting);
        connect();
    }

    @FXML
    public void onSettingsClick()   //Открытие настроек
    {
        Window stage = Main.getStage();
        try {
            Parent settingsRoot = FXMLLoader.load(getClass().getClassLoader().getResource("view/newStyleSettings.fxml"));
            Scene settingsScene = new Scene(settingsRoot, 390, 250);

            settingsStage = new Stage();
            settingsStage.setTitle("TagSettingsController");
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.initOwner(stage);

            settingsStage.getIcons().add(new Image(settingsImageURL));

            settingsStage.setScene(settingsScene);

            settingsStage.setResizable(false);

            settingsStage.show();
        }
        catch (Exception e)
        {
            System.out.println("(Main) Can't open settings: " + e);
        }
    }

    @FXML
    private void helpButtonClick()
    {
        openHelp();
    }

    private void openHelp()  //открытие help'a
    {
        Window stage = Main.getStage();
        try {
            Parent helpRoot = FXMLLoader.load(getClass().getClassLoader().getResource("view/help.fxml"));
            Scene helpScene = new Scene(helpRoot, 800, 400);

            helpStage = new Stage();
            helpStage.setTitle("Help");
            helpStage.initModality(Modality.NONE);
            helpStage.initOwner(stage);

            helpStage.getIcons().add(new Image(helpImageURL));

            helpStage.setScene(helpScene);

            helpStage.setResizable(false);

            helpStage.show();
        }
        catch (Exception e)
        {
            System.out.println("(Main) Can't open settings: " + e);
        }
    }

    private void initWindow()
    {
        //WIDTH - ширина
        //HEIGHT - высота
        rootPane.setMaxSize(WIDTH, HEIGHT);
        rootPane.setMinSize(WIDTH, HEIGHT);

        scrollPane.setMinSize(WIDTH - 20, HEIGHT - 84);
        botFlowPane.setPrefSize(WIDTH - 35, 10); //было (WIDTH - 35, HEIGHT - 84);
        //TODO: настроить цвета scrollPane, botFlowPane

        botFlowPane.setOrientation(Orientation.HORIZONTAL);
        botFlowPane.setHgap(10);
        botFlowPane.setVgap(10);


        rootPane.getStylesheets().add(this.getClass().getResource("../css/myStyle.css").toExternalForm());

        main = new Main();
    }

    private Runnable endlessUpdateStatusOfJobs()
    {
        Runnable updateStatus;
        updateStatus  = () -> {
            String out;
            do {
                if (AppSettings.isAutoUpdate())
                {
//                    Iterator iterator = JobCards.iterator();
//
//                    while (iterator.hasNext())
//                    {
//                        JobCard card = (JobCard) iterator.next();
//                        JenkinsJobs.updateJobCardStatus(card);
//                    }
                    try {
                        out = allFoundJobs.refreshStatusOfAllJobs(AppSettings.getServerAddress());   //обновляем статусы всех работ. Возврашает строку с навзаниями работ у которых статус изменился

                        if (!out.equals("No jobs has been updated")) {
                            writeToLog(out);
                            trayMessage(out);
                            //TODO: здесь нужно изменять JobCards у которых произошли изменения в статусе
                        }
                    }
                    catch (Exception e) {
                        System.out.println("(NewMainStyleController) (endlessUpdateStatusOfJobs) Error on job status updating: " + e);
                        if (actualStatus == ClientStatus.Connected)
                            setStatus(ClientStatus.Disconnected);
                    }
                }
                sleep(1);

            } while (true);
        };
        return updateStatus;
    }

    private void connect()
    {
        jobsForMainForm.clear();
        allFoundJobs.clear();

        botFlowPane.getChildren().clear();
        JobCards.clear();

        String serverAddress = AppSettings.getServerAddress();

        Runnable runnableGetJobList = () -> {

            if ( isJenkins(serverAddress) ) {
                setStatus(ClientStatus.Updating);
                trayMessage("Updating job list");

                refreshingAllJobsStatus(serverAddress);

                Iterator iterator = jobsForMainForm.getListOfJobs().iterator();

                while (iterator.hasNext())
                {
                    Job job = (Job) iterator.next();
                    JobCards.add(new JobCard(job));
                }
                sortJobCardsByTime();
                showAllJobCard();
            }
            else
                setStatus(ClientStatus.Disconnected);
        };

        Thread threadGetJobList = new Thread(runnableGetJobList);
        threadGetJobList.start();
    }

    private void showAllJobCard()
    {
        Iterator iterator = JobCards.iterator();
        Platform.runLater(
                () -> {
                    while (iterator.hasNext())
                    {
                        botFlowPane.getChildren().addAll((JobCard)iterator.next());
                    }
                }
        );
    }

    private void sortJobCardsByTime()
    {
        JobDateComparator comparator = new JobDateComparator();
        FXCollections.sort(JobCards, comparator);
    }

    private void refreshingAllJobsStatus(String serverAddress)
    {
        try {
            jobsForMainForm.getJobListFromServer(serverAddress);    //Получение спика работ

            allFoundJobs = new JenkinsJobs(jobsForMainForm);    //копируем список

            //System.out.println("(NewMainStyleController) (refreshingAllJobsStatus) AppSettings.isShowAllJobs():" + AppSettings.isShowAllJobs());
            if (!AppSettings.isShowAllJobs())   //если нужно отображать только скачиваемые джобы то удаляем ненужные элементы
            {
                Iterator iterator = jobsForMainForm.getListOfJobs().iterator();
                while (iterator.hasNext())
                {
                    Job job = (Job) iterator.next();
                    if ( !job.isFile() )
                        iterator.remove();
                }
            }
            writeToLog("Job list has been updated.");
            setStatus(ClientStatus.Connected);
        }
        catch (IllegalArgumentException err)
        {
            writeToLog("Incorrect server address");
            System.out.println("(NewMainStyleController) (refreshingAllJobsStatus) Incorrect server address: " + err);
            setStatus(ClientStatus.Disconnected);
        }
        catch (Exception error)
        {
            System.out.println("(NewMainStyleController) (refreshingAllJobsStatus) Unknown error: " + error);
            setStatus(ClientStatus.Disconnected);
        }
    }

    private boolean isJenkins(String address)
    {
        try
        {
            Document document = Jsoup.connect(address).get(); //получаем копию страницы в виде документа

            Elements elements = document.select("title");
            Iterator iterator = elements.iterator();
            String serverName = "";

            while ( iterator.hasNext() )
            {
                Element element = (Element) iterator.next();
                serverName = element.attr("Jenkins");
            }

            return serverName.equals("");
        }
        catch (IOException e)
        {
            writeToLog("Enter valid jenkins server address: \"http://[address]:[port]\"");
            setStatus(ClientStatus.Disconnected);
            trayMessage("Server not found");
            return false;
        }
        catch (IllegalArgumentException err)
        {
            writeToLog("Enter correct server address");
            setStatus(ClientStatus.Disconnected);
            return false;
        }
    }

    private void writeToLog(String text)     //вывод в лог сообщения
    {
        Platform.runLater(
                () ->
                {
                    System.out.println("(NewMainStyleController) (writeToLog) Log msg:  " + text);
                    Date date = new Date();
                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

//                    try {
//                        logTextArea.setText(formatForDateNow.format(date) + ": " + text + "\n" + logTextArea.getText());
//                    } catch (Exception e) {
//                        System.out.println("(NewMainStyleController) Error on write to log: " + e);
//                    }
                }
        );
    }

    public void setStatus (ClientStatus status, Job job)    //установка статуса клиента (приложения)
    {

        try
        {
            switch (status)
            {
                case _lastStatus:
                    actualStatus = lastStatus;
                    break;
                case Connecting:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Finding Jenkins on: \"" + AppSettings.getServerAddress() + "\"", Color.GREEN);
                    actualStatus = ClientStatus.Connecting;
                    break;
                case Connected:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(false);
                    setStatusText("Connected to \"" + AppSettings.getServerAddress()+ "\"", Color.GREEN);
                    actualStatus = ClientStatus.Connected;
                    break;
                case Updating:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Getting job list from \"" + AppSettings.getServerAddress() + "\"", Color.GREEN);
                    actualStatus = ClientStatus.Updating;
                    break;
                case Extracting:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Extracting " + job.getJobName() + " (#" + job.getJobID() + ") in \"" + AppSettings.getSavePath() + "\\" + job.getJobName() + "\"", Color.GREEN);
                    actualStatus = ClientStatus.Extracting;
                    break;
                case Downloading:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Downloading \"" + job.getJobName() + " (#" + job.getJobID() + ")\" in \"" + AppSettings.getSavePath() + "\\" + job.getJobName() + "\\" + job.getJobID() + ".zip\"", Color.GREEN);
                    actualStatus = ClientStatus.Downloading;
                    break;
                case Disconnected:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Can't find Jenkins server on \"" + AppSettings.getServerAddress() + "\"", Color.RED);
                    actualStatus = ClientStatus.Disconnected;
                    break;
                default:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(false);
                    actualStatus = ClientStatus.Disconnected;
                    break;
            }
        }
        catch (Exception e)
        {
            System.out.println("(NewMainStyleController) Error on change status: " + e);
        }

    }

    private void setStatus (ClientStatus status)    //установка статуса клиента (приложения)
    {

        try
        {
            switch (status)
            {
                case _lastStatus:
                    actualStatus = lastStatus;
                    break;
                case Connecting:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Finding Jenkins on: \"" + AppSettings.getServerAddress() + "\"", Color.GREEN);
                    actualStatus = ClientStatus.Connecting;
                    break;
                case Connected:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(false);
                    setStatusText("Connected to \"" + AppSettings.getServerAddress()+ "\"", Color.GREEN);
                    actualStatus = ClientStatus.Connected;
                    break;
                case Updating:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Getting job list from \"" + AppSettings.getServerAddress() + "\"", Color.GREEN);
                    actualStatus = ClientStatus.Updating;
                    break;
                case Extracting:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Extracting...", Color.GREEN);
                    actualStatus = ClientStatus.Extracting;
                    break;
                case Downloading:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Downloading in " + AppSettings.getSavePath(), Color.GREEN);
                    actualStatus = ClientStatus.Downloading;
                    break;
                case Disconnected:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(true);
                    setStatusText("Can't find Jenkins server on \"" + AppSettings.getServerAddress() + "\"", Color.RED);
                    actualStatus = ClientStatus.Disconnected;
                    break;
                default:
                    lastStatus = actualStatus;
                    progressIndicator.setVisible(false);
                    actualStatus = ClientStatus.Disconnected;
                    break;
            }
        }
        catch (Exception e)
        {
            System.out.println("(NewMainStyleController) Error on change status: " + e);
        }

    }

    private void setStatusText (String text, Color color)
    {
        try {
            Platform.runLater(
                    () -> {
                        statusLabel.setText(text);
                        //statusLabel.setTextFill(color);
                    }
            );
        }
        catch (Exception e) {
            System.out.println("(NewMainStyleController) Label error: " + e);
        }
    }

    private void trayMessage (String text)
    {
        Platform.runLater(
                () -> {
                    try {
                        String caption = "Jenkins Downloader";  //заголовок сообщения
                        if (AppSettings.isShowNotifications())
                            main.getTrayIcon().displayMessage(caption, text, TrayIcon.MessageType.INFO); //метод отображения сообщения в трее
                    }
                    catch (Exception e)
                    {
                        System.out.println("(NewMainStyleController) Can't display tray message: " + e);
                    }
                }
        );
    }

    private void sleep(int timeout)
    {
        try
        {
            TimeUnit.SECONDS.sleep(timeout);
        }
        catch (Exception err)
        {
            System.out.println("(NewMainStyleController) (sleep) Can't call sleep method: " + err);
        }
    }

    public static Stage getSettingsStage()
    {
        return settingsStage;
    }

    public static Stage getHelpStage() { return helpStage;}

    public JenkinsJobs getListOfJobs()
    {
        return allFoundJobs;
    }
}
