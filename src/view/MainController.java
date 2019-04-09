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


public class MainController
{
    private static Stage settingsStage, helpStage;
    private static final String settingsImageURL = "image/settings(small).png";
    private static final String helpImageURL = "image/help.png";
    private final int WIDTH = 810;
    private final int HEIGHT = 800;

    private static ObservableList<Job> allJobs = FXCollections.observableArrayList ();

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
        allJobs.clear();
//        downloadableJobs.clear();

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

    private void connect()
    {
        allJobs.clear();

        botFlowPane.getChildren().clear();

        Runnable runnableGetJobList = () -> {

            if ( isJenkins(AppSettings.getServerAddress()) ) {
                setStatus(ClientStatus.Updating);
                trayMessage("Updating job list");

                getJobListFromServer(AppSettings.getServerAddress());

                sortJobsByTime();
                showJobs();

                setStatus(ClientStatus.Connected);
            }
            else
                setStatus(ClientStatus.Disconnected);
        };

        Thread threadGetJobList = new Thread(runnableGetJobList);
        threadGetJobList.start();
    }

    private void getJobListFromServer(String serverAddress)  //Получение списка работ
    {
        System.out.println("(MainController) (getJobListFromServer) Getting job list from server...");

        Iterator jobsFromServerIterator = getJobsIterator(serverAddress);

        while ( jobsFromServerIterator.hasNext() ) {
            Element element = (Element) jobsFromServerIterator.next();  //берем следующую работу

            String jobName = getJobNameFromElement(element);
            int jobID = getJobIDFromElement(element);
            Job.JobStatusListing status = getJobStatusFromServer(element);

            allJobs.add(new Job(jobName, jobID, status));
            System.out.println("(MainController) (getJobListFromServer) Job found: " + jobName);
        }
        System.out.println("(MainController) (getJobListFromServer) Job list formatted.");
    }

    @FXML
    public void onSettingsClick()   //Открытие настроек
    {
        Window stage = Main.getStage();
        try {
            Parent settingsRoot = FXMLLoader.load(getClass().getClassLoader().getResource("view/Settings.fxml"));
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
        Window stage = Main.getStage();
        try {
            Parent helpRoot = FXMLLoader.load(getClass().getClassLoader().getResource("view/Help.fxml"));
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
                    try {
                        out = refreshAllJobsStatus(AppSettings.getServerAddress());   //обновляем статусы всех работ. Возврашает строку с навзаниями работ у которых статус изменился

                        if (!out.equals("No jobs has been updated")) {
                            writeToLog(out);
                            trayMessage(out);
                        }
                    }
                    catch (Exception e) {
                        System.out.println("(MainController) (endlessUpdateStatusOfJobs) Error on job status updating: " + e);
                        if (actualStatus == ClientStatus.Connected)
                            setStatus(ClientStatus.Disconnected);
                    }
                }
                sleep(1);

            } while (true);
        };
        return updateStatus;
    }

    private void showJobs()
    {
        Iterator iterator = allJobs.iterator();

        Platform.runLater(
                () -> {
                    while (iterator.hasNext())
                    {
                        Job job = (Job)iterator.next();

                        if (job.isFile() || AppSettings.isShowAllJobs())
                            botFlowPane.getChildren().addAll(job);
                    }
                }
        );
    }

    private void sortJobsByTime()
    {
        JobDateComparator comparator = new JobDateComparator();
        FXCollections.sort(allJobs, comparator);
        System.out.println("(MainController) (sortJobsByTime) 'allJobs' sorted by time.");
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
                    System.out.println("(MainController) (writeToLog) Log msg:  " + text);
                    Date date = new Date();
                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

//                    try {
//                        logTextArea.setText(formatForDateNow.format(date) + ": " + text + "\n" + logTextArea.getText());
//                    } catch (Exception e) {
//                        System.out.println("(MainController) Error on write to log: " + e);
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
            System.out.println("(MainController) Error on change status: " + e);
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
            System.out.println("(MainController) Error on change status: " + e);
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
            System.out.println("(MainController) Label error: " + e);
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
                        System.out.println("(MainController) Can't display tray message: " + e);
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
            System.out.println("(MainController) (sleep) Can't call sleep method: " + err);
        }
    }

    public static Stage getSettingsStage()
    {
        return settingsStage;
    }

    public static Stage getHelpStage() { return helpStage;}

    public ObservableList getListOfJobs()
    {
        return allJobs;
    }


    private Iterator getJobsIterator(String serverAddress)
    {
        try
        {
            Document document = Jsoup.connect(serverAddress).get(); //получаем копию страницы в виде документа
            Elements elements = document.select("tr[class*=job-status]");   //создаем список tr-элементов страницы которые содержат текст "job-status" внутри себя
            return elements.iterator();  //создаем итератор по элментам страницы содержащим имена работ
        }
        catch (IOException e)
        {
            System.out.println("(MainController) (getJobsIterator) Error: " + e);
            return null;
        }
    }



    private Job.JobStatusListing getJobStatusFromServer(Element element)    //получения статуса работы по элементу
    {
        Elements statusElem = element.select("img");
        Iterator statusIterator = statusElem.iterator();

        while (statusIterator.hasNext())
        {
            String jobStatus;
            Element statusElement = (Element) statusIterator.next();
            jobStatus = statusElement.attr("alt");

            switch (jobStatus)    //далее нужно считать строку и выбрать соответствующий статус
            {
                case "Успешно":
                    return Job.JobStatusListing.Успешно;
                case "Провалилось":
                    return Job.JobStatusListing.Провалилось;
                case "Прервано":
                    return Job.JobStatusListing.Прервано;
                case "Приостановлено":
                    return Job.JobStatusListing.Приостановлено;
                case "В процессе":
                    return Job.JobStatusListing.Впроцессе;
                case "built":
                    return Job.JobStatusListing.built;
                default:
                    return Job.JobStatusListing.Неизвестно;
            }
        }
        return Job.JobStatusListing.Ошибка;
    }

    private String getJobNameFromElement(Element element)
    {
        String jobName = element.attr("id");                         //находим строку начинающуюся с "id"
        jobName = jobName.substring(4, jobName.length());                       //берем символы с 4 до последнего, это и есть имя работы
        return jobName;
    }

    private int getJobIDFromElement(Element element)
    {
        String _s1 = element.child(3).text();
        int posNumber = _s1.indexOf("#");
        String stringJobID = _s1.substring(posNumber + 1);
        return getIntJobID(stringJobID);
    }

    private int getIntJobID(String stringJobID)
    {
        int jobID = -1;
        try {
            jobID = Integer.parseInt(stringJobID);
        }
        catch (NumberFormatException e) {
            //System.out.println("Error in getting job ID (int): " + e);
        }
        return jobID;
    }

    private String refreshAllJobsStatus(String address)
    {
        String out = "";

        Iterator iteratorListOfJobs = getJobsIterator(address);

        while ( iteratorListOfJobs.hasNext() ) {
            Element element = (Element) iteratorListOfJobs.next();                  //берем следующую работу

            String jobName                  = getJobNameFromElement(element);
            int jobID                       = getJobIDFromElement(element);
            Job.JobStatusListing status     = getJobStatusFromServer(element);

            for (int i = 0; i < allJobs.size(); i++)
            {
                Job job = allJobs.get(i);

                if ( job.getJobName().equals(jobName) && (!job.getJobStatus().equals(status)) )
                {
                    allJobs.get(i).setJobStatus(status);

                    if (allJobs.get(i).getJobID() != jobID)
                        allJobs.get(i).setJobID(jobID);
                    System.out.println("\n(MainController)" + allJobs.get(i).getJobName() + " changed status to: " + allJobs.get(i).getJobStatus());


                    out = formationStringWithChangedJobs(out, allJobs.get(i));
                    break;
                }
            }
        }

        if (!out.equals(""))
            out = out.substring(0, out.length() - 1);   //если были изменения в статусах то убираем последний перенос строки
        else
            out = "No jobs has been updated";           //иначе формируем строку для вывода в консоль

        Date date = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

        System.out.println(formatForDateNow.format(date) + "/ Status of auto update:" + ": " + out);

        return out;
    }

    public void updateJobStatus (Job job)
    {
        Iterator iteratorListOfJobs = getJobsIterator(AppSettings.getServerAddress());

        while ( iteratorListOfJobs.hasNext() ) {
            Element element = (Element) iteratorListOfJobs.next();                  //берем следующую работу

            String jobName = getJobNameFromElement(element);

            if (jobName.equals(job.getJobName()))
            {
                int jobID = getJobIDFromElement(element);
                Job.JobStatusListing status = getJobStatusFromServer(element);

                job.setJobStatus(status);
                job.setJobID(jobID);

                //TODO: добавить функции для изменения состояния джобы, иконки состояния джобы, описания джобы, времени изменения состояния джобы, номера сбокри джобы
                //job.changeJobStatusInCard(status, card.getJob().getLastChange(), jobID);
            }
        }
    }

    private String formationStringWithChangedJobs(String actualString, Job job)
    {
        if ( job.getJobStatus() == Job.JobStatusListing.Впроцессе )   //Если джоба в процессе то не нужно отображать номер
            if (job.getVisibleName().equals(""))
                actualString = actualString + "\"" + job.getJobName() + "\" changed status to: " + "\"В процессе.\"" + "\n";
            else
                actualString = actualString + "\"" + job.getVisibleName() + "\" changed status to: " + "\"В процессе.\"" + "\n";
        else
        if (job.getVisibleName().equals(""))
            actualString = actualString + "\"" + job.getJobName() + " (#" + job.getJobID() + ")\" changed status to: \"" + job.getJobStatus() + "\"\n";
        else
            actualString = actualString + "\"" + job.getVisibleName() + " (#" + job.getJobID() + ")\" changed status to: \"" + job.getJobStatus() + "\"\n";

        return actualString;
    }
}
