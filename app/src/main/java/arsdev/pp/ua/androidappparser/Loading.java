package arsdev.pp.ua.androidappparser;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Loading extends AppCompatActivity {
    public static  String PATH;
    public static  String ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String a = getBody("http://projects.arsdev.pp.ua/project_001/apps.txt");
                            String[] b = a.split("\\n+");
                            ArrayList<String> ids = new ArrayList<>();
                            for(int i = 0;i<b.length;i++){
                                String line = b[i];
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TextView view = (TextView) findViewById(R.id.loading);
                                        view.setText("Подождите, загружаеться");
                                    }
                                });
                                m_parse(line);
                                String parts[] = line.split("=");
                                String id = parts[1];
                                ids.add(i, id);
                            }
                            finish(ids);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }                    }
                });
                t.start();

    }
    public void m_parse(String line) throws Exception{
        String parts[] = line.split("=");
        final String id = parts[1];
        String body = getBody("http://projects.arsdev.pp.ua/project_001/APKGet/?id=" + id);
        Log.i("APP_PARSER", body);
        downloadFile(body, id + ".apk", id);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView view = (TextView) findViewById(R.id.loading);
                Toast.makeText(Loading.this, "Готово", Toast.LENGTH_LONG).show();
                view.setText("Готово");
            }
        });
    }
    public void finish(ArrayList<String> strings){
        Intent intent = new Intent(Loading.this, Parse.class);
        intent.putExtra("PATH",strings);
        startActivity(intent);
    }
    String getBody(String url) throws Exception{
        URL _url = new URL(url);
        URLConnection con = _url.openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);
        return body;
    }
    void downloadFile(String _url, String _name, String id) {
        try {
            URL u = new URL(_url);
            Loading.ID = id;
            DataInputStream stream = new DataInputStream(u.openStream());
            byte[] buffer = IOUtils.toByteArray(stream);
            File file = new File("/storage/emulated/0/" + id +"/"+ _name);
            file.getParentFile().mkdirs(); 
            file.createNewFile();
            Loading.PATH = file.getAbsolutePath();
            FileOutputStream outputStream = new FileOutputStream(file, false);
            outputStream.write(buffer);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
