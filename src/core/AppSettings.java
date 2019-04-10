package core;



import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
//TODO: сделать сортировку конфиг файла.
public class AppSettings
{
    private static final String FILE_CONFIG_NAME = "jenkins.conf";
    private static final String CONFIG_FLAG = "SIMPLE"; //JSON or XML or SIMPLE

    private static String serverAddress;
    private static String savePath;

    private static boolean showNotifications;
    private static boolean showAllJobs;
    private static boolean autoUpdate;

    private static boolean showColumnJobName;
    private static boolean showColumnJobID;
    private static boolean showColumnJobStatus;
    private static boolean showColumnIsFile;
    private static boolean showColumnTagName;
    private static boolean showColTimeLastUpdate;

    private static int widthColumnJobName;
    private static int widthColumnJobID;
    private static int widthColumnJobStatus;
    private static int widthColumnIsFile;
    private static int widthColumnTagName;
    private static int widthColumnTimeLastUpdate;

    private static Properties properties;


    public static void loadConfigFile()  //загрузка настроек из файла
    {
        File configFile = new File(FILE_CONFIG_NAME);

        if (!configFile.exists())
            createConfigFile(configFile);

        switch (CONFIG_FLAG) {
            case "JSON":
                System.out.println("(AppSettings) (Load config file) Loading JSON config file");
                break;
            case "XML":
                System.out.println("(AppSettings) (Load config file) Loading XML config file");
                break;
            case "SIMPLE":
                try {
                    System.out.println("(AppSettings) (Load config file) Loading SIMPLE config file: " + configFile.getAbsolutePath());

                    FileInputStream input = new FileInputStream(configFile);
                    properties = new Properties();

                    properties.load(new InputStreamReader(input));  //  properties.load(new InputStreamReader(input, Charset.forName("UTF-8")))

                    serverAddress = properties.getProperty("serverAddress", "http:\\\\nix.mrcur.ru:8080");
                    savePath = properties.getProperty("path", "D:\\Jenkins Downloader");

                    showNotifications =  Boolean.valueOf(properties.getProperty("showNotifications", "true"));
                    showAllJobs = Boolean.valueOf(properties.getProperty("showAllJobs", "false"));
                    autoUpdate = Boolean.valueOf(properties.getProperty("autoUpdate", "true"));

                    widthColumnJobName = Integer.parseInt(properties.getProperty("widthColumnJobName", "150"));
                    widthColumnJobID = Integer.parseInt(properties.getProperty("widthColumnJobID", "100"));
                    widthColumnJobStatus = Integer.parseInt(properties.getProperty("widthColumnJobStatus", "100"));
                    widthColumnIsFile = Integer.parseInt(properties.getProperty("widthColumnIsFile", "100"));
                    widthColumnTagName = Integer.parseInt(properties.getProperty("widthColumnTagName", "100"));
                    widthColumnTimeLastUpdate = Integer.parseInt(properties.getProperty("widthColumnTimeLastUpdate", "150"));

                    showColumnJobName = Boolean.valueOf(properties.getProperty("showColumnJobName", "true"));
                    showColumnJobID  = Boolean.valueOf(properties.getProperty("showColumnJobID", "true"));
                    showColumnJobStatus = Boolean.valueOf(properties.getProperty("showColumnJobStatus", "true"));
                    showColumnIsFile = Boolean.valueOf(properties.getProperty("showColumnIsFile", "true"));
                    showColumnTagName = Boolean.valueOf(properties.getProperty("showColumnTagName", "true"));
                    showColTimeLastUpdate = Boolean.valueOf(properties.getProperty("showColTimeLastUpdate", "true"));
                    System.out.println("(AppSettings) (Load config file) SIMPLE config file loaded");
                }
                catch (Exception e)
                {
                    System.out.println("(AppSettings) (Load config file) Error on load config file: " + e);
                    configFile.delete();
                    createConfigFile(configFile);
                }
                break;
            default:
                System.out.println("(AppSettings) (Load config file) Loading DEFAULT config file");
                System.out.println("(AppSettings) (Load config file) File not found");
                break;
        }
    }

    public static String findTagInConfigFile(String nameOfJob)
    {
        String out = "";
        File configFile = new File(FILE_CONFIG_NAME);

        if (!configFile.exists())
            createConfigFile(configFile);

        switch (CONFIG_FLAG) {
            case "JSON":

                break;
            case "XML":

                break;
            case "SIMPLE":
                try {
                    FileInputStream input = new FileInputStream(configFile);
                    properties = new Properties();
                    properties.load(new InputStreamReader(input));  //  properties.load(new InputStreamReader(input, Charset.forName("UTF-8")))
                    out = properties.getProperty(nameOfJob + "_tag");

                } catch (Exception e) {
                    System.out.println("(AppSettings) (findTagInConfigFile) error on finding job: " + e);
                }
                break;
            default:
                out = "";
                System.out.println("(AppSettings) (findTagInConfigFile) File not found");
                break;
        }

        if (out == null)
            out = "";

        return out;
    }

    public static String findTimeInConfigFile(String nameOfJob)
    {
        String out = "";
        File configFile = new File(FILE_CONFIG_NAME);

        if (!configFile.exists())
            createConfigFile(configFile);

        switch (CONFIG_FLAG) {
            case "JSON":

                break;
            case "XML":

                break;
            case "SIMPLE":
                try {
                    FileInputStream input = new FileInputStream(configFile);
                    properties = new Properties();
                    properties.load(new InputStreamReader(input));  //  properties.load(new InputStreamReader(input, Charset.forName("UTF-8")))
                    out = properties.getProperty(nameOfJob + "_time");
                } catch (Exception e) {
                    System.out.println("(AppSettings) (findTimeInConfigFile) error on finding job: " + e);
                }
                break;
            default:
                out = "";
                System.out.println("(AppSettings) (findTimeInConfigFile) File not found");
                break;
        }

        if (out == null)
            out = "";

        return out;
    }

    public static double findSizeInConfigFile(String nameOfJob)
    {
        double out = -1;
        File configFile = new File(FILE_CONFIG_NAME);

        if (!configFile.exists())
            createConfigFile(configFile);

        switch (CONFIG_FLAG) {
            case "JSON":

                break;
            case "XML":

                break;
            case "SIMPLE":
                try {
                    FileInputStream input = new FileInputStream(configFile);
                    properties = new Properties();
                    properties.load(new InputStreamReader(input));  //  properties.load(new InputStreamReader(input, Charset.forName("UTF-8")))
                    out = Double.valueOf(properties.getProperty(nameOfJob + "_size"));
                } catch (Exception e) {
                    //System.out.println("(AppSettings) (findSizeInConfigFile) Job size not found: " + e);
                }
                break;
            default:
                out = -1;
                System.out.println("(AppSettings) (findSizeInConfigFile) File not found");
                break;
        }

        return out;
    }

    public static void changeSettingInConfig(String changeSetting, String newValue)
    {
        newValue = changeSetting + "\t\t" + newValue;
        newValue = newValue.replace("\\", "\\\\");

        File configFile = new File(FILE_CONFIG_NAME);

        if (!configFile.exists())
            createConfigFile(configFile);

        try
        {
            File tempFile = File.createTempFile("jenkins_", ".tmp",null);
            Files.copy(configFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            FileInputStream configInputStream = new FileInputStream(tempFile);
            properties.load(new InputStreamReader(configInputStream, Charset.forName("UTF-8")));
            configInputStream.close();

            BufferedReader bufferedReader;
            bufferedReader = new BufferedReader(new FileReader(tempFile));

            BufferedWriter bufferedWriter;
            bufferedWriter = new BufferedWriter(new FileWriter(configFile, false));

            String currentLine;
            boolean flag = true;

            while((currentLine = bufferedReader.readLine()) != null)
            {
                if(currentLine.trim().lastIndexOf(changeSetting) > -1) {
                    bufferedWriter.write(newValue + "\n");
                    flag = false;
                }
                else
                    bufferedWriter.write(currentLine + "\n");
            }

            if (flag)
                bufferedWriter.write(newValue + "\n");

            bufferedWriter.close();
            bufferedReader.close();
            //System.out.println("(AppSettings) (changeSettingInConfig) Changed in config file: " + newValue);
            //System.out.println("(AppSettings) (changeSettingInConfig) temp file delete:" + tempFile.delete());
        }
        catch (Exception e)
        {
            System.out.println("(AppSettings) (changeSettingInConfig) Error: " + e);
        }

    }

    private static void createConfigFile(File file)    //если конфигурационный файл не найден, то создаем новый с настрйоками по умолчанию
    {
        System.out.println("(AppSettings) (createConfigFile) Config file not found");

        switch (CONFIG_FLAG) {
            case  "SIMPLE":
                try {

                    FileWriter writer =  new FileWriter(new File(FILE_CONFIG_NAME));

                    writer.write("serverAddress\thttp://nix.mrcur.ru:8080\n");
                    writer.write("path\t\t\tD:\\\\Jenkins\n\n");

                    writer.write("showNotifications\t\t\ttrue\n");
                    writer.write("showAllJobs\t\t\tfalse\n");
                    writer.write("autoUpdate\t\t\ttrue\n\n");

                    writer.write("widthColumnJobName\t\t150\n");
                    writer.write("widthColumnJobID\t\t\t100\n");
                    writer.write("widthColumnJobStatus\t\t100\n");
                    writer.write("widthColumnIsFile\t\t100\n");
                    writer.write("widthColumnTagName\t\t100\n");
                    writer.write("widthColumnTimeLastUpdate\t\t150\n\n");

                    writer.write("showColumnJobName\t\ttrue\n");
                    writer.write("showColumnJobID\t\t\ttrue\n");
                    writer.write("showColumnJobStatus\t\ttrue\n");
                    writer.write("showColumnIsFile\t\ttrue\n");
                    writer.write("showColumnTagName\t\ttrue\n");
                    writer.write("showColTimeLastUpdate\t\ttrue\n\n");

                    writer.close();

                    System.out.println("(AppSettings) (createConfigFile) Config file was created");
                }
                catch (IOException err)
                {
                    System.out.println("(AppSettings) (createConfigFile) Can't create config file: " + err);
                }
                break;
            case "JSON":
                try {
                    if (file.createNewFile()) {
                        FileWriter writer =  new FileWriter(new File(FILE_CONFIG_NAME));

                        writer.close();
                    }
                }
                catch (IOException err)
                {
                    System.out.println("(AppSettings) (createConfigFile) Can't create config file: " + err);
                }
                break;
            case "XML":
                try {
                    if (file.createNewFile()) {
                        FileWriter writer =  new FileWriter(new File(FILE_CONFIG_NAME));

                        writer.close();
                    }
                }
                catch (IOException err)
                {
                    System.out.println("(AppSettings) (createConfigFile) Can't create config file: " + err);
                }
                break;
            default:
                System.out.println("(AppSettings) (createConfigFile) File not created");
                break;
        }
    }

//-----------------------------------
    public static String getSavePath() {
        return savePath;
    }

    public static String getServerAddress() {
        return serverAddress;
    }


//-----------------------------------
    public static void setSavePath(String savePath) {
        AppSettings.savePath = savePath;
        changeSettingInConfig("path", AppSettings.savePath);
    }

    public static void setServerAddress(String serverAddress) {
        AppSettings.serverAddress = serverAddress;
        changeSettingInConfig("serverAddress", AppSettings.serverAddress);
    }


//-----------------------------------
    public static boolean showColumnIsFile() {
        return showColumnIsFile;
    }

    public static boolean showColumnJobID() {
        return showColumnJobID;
    }

    public static boolean showColumnJobName() {
        return showColumnJobName;
    }

    public static boolean showColumnJobStatus() {
        return showColumnJobStatus;
    }

    public static boolean showColumnTagName() {
        return showColumnTagName;
    }

    public static boolean showColumnLastTimeUpdate() {
        return showColTimeLastUpdate;
    }

    public static void setShowAllJobs(boolean showAllJobs) {
        AppSettings.showAllJobs = showAllJobs;
        changeSettingInConfig("showAllJobs", String.valueOf(AppSettings.showAllJobs));
    }

    public static boolean isShowAllJobs() {
        return showAllJobs;
    }

    public static boolean isAutoUpdate() {
        return autoUpdate;
    }

    public static boolean isShowNotifications() {
        return showNotifications;
    }


//-----------------------------------
    public static void setShowColumnIsFile(boolean showColumnIsFile) {
        AppSettings.showColumnIsFile = showColumnIsFile;
        changeSettingInConfig("showColumnIsFile", String.valueOf(AppSettings.showColumnIsFile));
    }

    public static void setShowColumnJobID(boolean showColumnJobID) {
        AppSettings.showColumnJobID = showColumnJobID;
        changeSettingInConfig("showColumnJobID", String.valueOf(AppSettings.showColumnJobID));
    }

    public static void setShowColumnJobName(boolean showColumnJobName) {
        AppSettings.showColumnJobName = showColumnJobName;
        changeSettingInConfig("showColumnJobName", String.valueOf(AppSettings.showColumnJobName));
    }

    public static void setShowColumnJobStatus(boolean showColumnJobStatus) {
        AppSettings.showColumnJobStatus = showColumnJobStatus;
        changeSettingInConfig("showColumnJobStatus", String.valueOf(AppSettings.showColumnJobStatus));
    }

    public static void setShowColumnTagName(boolean showColumnTagName) {
        AppSettings.showColumnTagName = showColumnTagName;
        changeSettingInConfig("showColumnTagName", String.valueOf(AppSettings.showColumnTagName));
    }

    public static void setShowColTimeLastUpdate(boolean showColTimeLastUpdate) {
        AppSettings.showColTimeLastUpdate = showColTimeLastUpdate;
        changeSettingInConfig("showColTimeLastUpdate", String.valueOf(AppSettings.showColTimeLastUpdate));
    }

    public static void setAutoUpdate(boolean autoUpdate) {
        AppSettings.autoUpdate = autoUpdate;
        changeSettingInConfig("autoUpdate", String.valueOf(AppSettings.autoUpdate));
    }

    public static void setShowNotifications(boolean showNotifications) {
        AppSettings.showNotifications = showNotifications;
        changeSettingInConfig("showNotifications", String.valueOf(AppSettings.showNotifications));
    }


//-----------------------------------
    public static int getWidthColumnIsFile() {
        return widthColumnIsFile;
    }

    public static int getWidthColumnJobID() {
        return widthColumnJobID;
    }

    public static int getWidthColumnJobName() {
        return widthColumnJobName;
    }

    public static int getWidthColumnJobStatus() {
        return widthColumnJobStatus;
    }

    public static int getWidthColumnTagName() {
        return widthColumnTagName;
    }

    public static int getWidthColumnTimeLastUpdate() {
        return widthColumnTimeLastUpdate;
    }

}
