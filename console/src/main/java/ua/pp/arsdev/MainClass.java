package ua.pp.arsdev;
import org.w3c.dom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.xml.parsers.*;

public class MainClass {
    public static final String ADB_PATH = "C:\\Users\\arsen\\AppData\\Local\\Android\\sdk\\platform-tools\\adb.exe";
    public static boolean DEBUG = true;
    public static void main(String[] args)throws Exception {
        if(DEBUG) {
            install();
        }
        try {
            if(DEBUG){registerNames();}
            startAllActivities();
            clearAll();
        }catch (Exception e){
            System.out.println("UNABLE TO START ACTIVITY: " + e.getMessage());
        }
    }
    public static void registerNames() throws Exception{
       Utils.runProcess(true,ADB_PATH + " pull /storage/emulated/0/app_names.txt E:\\apps\\");
    }
    public static void clearAll(){
        File file = new File("E:\\apps");
        File[] list = file.listFiles();
        for (File file1 : list){
                clear(file1.listFiles());
        }
    }
    public static void install(){
        List<String> out = Utils.runProcess(true,ADB_PATH + " shell cat /storage/emulated/0/app_list.txt\n");
        if(out != null){
            for(String folder : out){
                try {
                    Utils.runProcess(true, ADB_PATH + " pull /storage/emulated/0/" + folder + "/" + folder + ".apk" + " E:\\apps\\" + folder + ".apk");
                    Utils.runProcess(true, ADB_PATH + " install E:\\apps\\" + folder + ".apk");
                    File file = new File("E:\\apps\\" + folder + ".apk");
                    file.delete();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    public  static void startAllActivities() throws Exception {
        List<String> out = Utils.runProcess(true, ADB_PATH + " shell cat /storage/emulated/0/app_list.txt\n");
        if (out != null) {
            for (String folder : out) {
                File file = new File("E:\\apps\\" + folder);
                file.mkdir();
                List<String> out_1 = Utils.runProcess(true, ADB_PATH + " shell cat /storage/emulated/0/" + folder + "/app_info.txt");
                if (out_1 != null) {
                    for (String activity_name : out_1) {
                        System.out.println("ACTIVITY_NAME IS: " + activity_name);
                        startActivity(folder, activity_name);
                        Thread.sleep(3000);
                        tmp(folder);
                        dump(folder);
                        boolean login = false;
                        if (isLoginForm(new File("E:\\apps\\" + folder + "\\tmp\\window_dump.xml"))) {
                            System.out.println("LOGIN FORM, ATAS!!!");
                            login = true;
                            String[] data = getDataFromConsole();
                            String login_ = data[0];
                            String pass = data[1];
                            ArrayList<String> editPos = getEditTextPosition(new File("E:\\apps\\" + folder + "\\tmp\\window_dump.xml"));
                            int loginPosX = Integer.parseInt(editPos.get(0).split("_")[0]);
                            int loginPosY = Integer.parseInt(editPos.get(0).split("_")[1]);
                            int passPosX = Integer.parseInt(editPos.get(1).split("_")[0]);
                            int passPosY = Integer.parseInt(editPos.get(1).split("_")[1]);
                            click(loginPosX, loginPosY);
                            Thread.sleep(500);
                            inputText(login_);
                            Thread.sleep(1000);
                            click(passPosX, passPosY);
                            Thread.sleep(500);
                            inputText(pass);
                            Thread.sleep(500);
                            click(passPosX - 100, passPosY);
                        }
                        ArrayList<String> bounds = parsePositionFromXml(new File("E:\\apps\\" + folder + "\\tmp\\window_dump.xml"));
                        for (int i = 0; i < bounds.size(); i++) {
                            String out1 = bounds.get(i);
                            String x = out1.split("_")[0];
                            String y = out1.split("_")[1];
                            click(Integer.parseInt(x), Integer.parseInt(y));
                            if (!login) {
                                Thread.sleep(3000);
                                _takeScreenshot(folder, String.valueOf(i));
                                Thread.sleep(1000);
                                startActivity(folder, activity_name);
                            }else{
                                Thread.sleep(60000);
                            }
                        }
                    }
                }
                System.out.println("GOOD " + folder);
            }
        }

    }
        public static void tmp(String folder){
            new File("E:\\apps\\" + folder + "\\tmp").mkdir();
        }
        public static void dump(String folder){
            Utils.runProcess(true,"adb shell uiautomator dump");
            Utils.runProcess(true, "adb pull /sdcard/window_dump.xml E:\\apps\\" + folder + "\\tmp\\window_dump.xml");
        }
        public static void goHome(){
            Utils.runProcess(true,"adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME");
        }
    public static void clear(File[] listOfFiles){
        if(listOfFiles != null) {
            System.out.println("NOT_NULL");
            for(int i = 0;i<listOfFiles.length;i++) {
                File currentFile = listOfFiles[i];
                if(!currentFile.isDirectory()){
                for (int d = 0; d < listOfFiles.length; d++) {
                    if (d != i && !listOfFiles[d].isDirectory()) {
                        File nextFile = listOfFiles[d];
                        if (currentFile.exists() && nextFile.exists()) {
                            boolean similar = compareImage(currentFile, nextFile);
                            if (similar && currentFile.exists() && nextFile.exists()) {
                                boolean a = nextFile.delete();
                                System.out.println(a);
                            }
                        }
                    }
                }
            }
            }
        }
    }
    public static void _takeScreenshot(String folder, String i) throws Exception{
        takeScreenShot(folder, i);
        Thread.sleep(2000);
    }
    public static void startActivity(String folder, String activity_name)throws  Exception{
        Utils.runProcess(true,ADB_PATH + " shell su 0 am start -n \"" + folder + "/" + activity_name + "\"");
        File file = new File("E:\\apps\\view.xml");
        if(!file.exists()){
            file.createNewFile();
        }
    }
    public static void click(int x, int y){
        Utils.runProcess(true, ADB_PATH + " shell input tap " + x + " " + y);
    }
    public static void takeScreenShot(String folder, String i){
        Utils.runProcess(true,ADB_PATH + " shell screencap -p /storage/emulated/0/" + folder + "/" + i + ".png");
        Utils.runProcess(true,ADB_PATH + " pull /storage/emulated/0/" + folder + "/" + i + ".png " + "E:\\apps\\" + folder + "\\" + i + ".png");
        Utils.runProcess(true,ADB_PATH + " shell rm /storage/emulated/0/" + folder + "/" + i + ".png");
    }
    public static boolean compareImage(File fileA, File fileB) {
        try {
            // take buffer data from botm image files //
            BufferedImage biA = ImageIO.read(fileA);
            BufferedImage biA1 = biA.getSubimage(0, 75, 768,1280 -75);
            DataBuffer dbA = biA1.getData().getDataBuffer();
            int sizeA = dbA.getSize();
            BufferedImage biB = ImageIO.read(fileB);
            BufferedImage biB1 = biB.getSubimage(0, 75, 768,1280 - 75);
            DataBuffer dbB = biB1.getData().getDataBuffer();
            int sizeB = dbB.getSize();
            // compare data-buffer objects //
            if(sizeA == sizeB) {
                for(int i=0; i<sizeA; i++) {
                    if(dbA.getElem(i) != dbB.getElem(i)) {
                        return false;
                    }
                }
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            System.out.println("Failed to compare image files ...");
            return  false;
        }
    }
    public static ArrayList<String> parsePositionFromXml(File file) throws Exception{
        ArrayList<String> coordinates = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);
        NodeList nodeList = document.getElementsByTagName("node");
        for(int x=0,size= nodeList.getLength(); x<size; x++) {
            String bounds = nodeList.item(x).getAttributes().getNamedItem("bounds").getNodeValue() + "\n";
            String out = bounds.split("\\]\\[")[0];
            out = out.replaceAll("\\[", "");
            String[] xy = out.split(",");
            coordinates.add(xy[0] +  "_" + xy[1]);
        }
        return coordinates;
    }
    public static String[] getDataFromConsole() throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String login = reader.readLine();
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(System.in));
        String password = reader1.readLine();
        String[] data = new String[2];
        data[0] = login;
        data[1] = password;
        data[0] = data[0].replaceAll(" ", "");
        data[1] = data[1].replaceAll(" ", "");
        return data;
    }
    public static boolean haveEditText(File file) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        int trust = 0;
        Document document = db.parse(file);
        NodeList nodeList = document.getElementsByTagName("node");
        for(int x=0,size= nodeList.getLength(); x<size; x++) {
            String NameClass = nodeList.item(x).getAttributes().getNamedItem("class").getNodeValue() + "\n";
            if(NameClass.equals("android.widget.EditText")){
                System.out.println("HAVE EDIT TEXTS");
                trust += 1;
            }
        }
        return trust >= 2;
    }
    public static ArrayList<String> getEditTextPosition(File file) throws Exception{
        ArrayList<String> coordinates = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);
        NodeList nodeList = document.getElementsByTagName("node");
        for(int x=0,size= nodeList.getLength(); x<size; x++) {
            String NameClass = nodeList.item(x).getAttributes().getNamedItem("class").getNodeValue() + "\n";
            if(NameClass.contains("EditText")){
                String bounds = nodeList.item(x).getAttributes().getNamedItem("bounds").getNodeValue() + "\n";
                String out = bounds.split("\\]\\[")[0];
                out = out.replaceAll("\\[", "");
                String[] xy = out.split(",");
                coordinates.add(xy[0] +  "_" + xy[1]);
            }
        }
        return coordinates;
    }
    public static boolean isLoginForm(File file) throws Exception{
        Reader fileReader = new FileReader(file);
        BufferedReader bufReader = new BufferedReader(fileReader);
        StringBuilder sb = new StringBuilder();
        String line = bufReader.readLine();
        while( line != null){
            sb.append(line);
            line = bufReader.readLine();
        }
        String xml = sb.toString().toLowerCase();
        String[] keywords = {
                "вхід", "вход", "log in", "sign in", //Заголовок
                "login", "email", "e-mail", "phone number", //Логін
                "pass", "password", "pwd","" //Пароль
        };
        int trust = 0;
        for(String keyword : keywords){
            if(xml.contains(keyword)){
                trust += 1;
            }
        }
        if(trust > 3){
            return true;
        }
        return false;
    }
    public static void inputText(String text){
        Utils.runProcess(true, "adb shell input text \"" + text + "\"");
    }
}
