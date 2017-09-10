package arsdev.pp.ua.androidappparser;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Parse extends AppCompatActivity {
    Button parse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parse);
        parse = (Button) findViewById(R.id.button);
        parse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> ids = getIntent().getStringArrayListExtra("PATH");
                for(String id :ids){
                   String apkPath =  "/storage/emulated/0/" + id +"/"+ id + ".apk";
                    try {
                        m_parse(apkPath, id);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public void m_parse(String apkPath, final String id) throws Exception{
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
             PackageManager.GET_ACTIVITIES);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String app_name = getBody("http://projects.arsdev.pp.ua/project_001/APP_NAME/?url=https://play.google.com/store/apps/details?id=" + id);
                    File file_ =new File("/storage/emulated/0/app_names.txt");
                    if(!file_.exists()){
                        file_.createNewFile();
                    }
                    FileWriter _writer = new FileWriter(file_, true);
                    _writer.write(app_name + "\n");
                    _writer.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        File package_name = new File("/storage/emulated/0/app_list.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(package_name , true));
            Log.i("ActivityInfo", "Package name is " + info.packageName);
            writer.write(info.packageName + "\n");
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        File app_info = new File("/storage/emulated/0/" + id + "/app_info.txt");
        try {
            FileOutputStream out = new FileOutputStream(app_info);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            for (android.content.pm.ActivityInfo a : info.activities) {
                writer.write(a.name + "\n");
                Log.i("ActivityInfo", a.name);
            }
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.i("APP_PARSE", id);
    }
    static String getBody(String url) throws Exception{
        URL _url = new URL(url);
        URLConnection con = _url.openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);
        Document doc = Jsoup.parse(body);
        String text = doc.body().text();
        return text;
    }
}
