package curtin.edu.lecture_6_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    Button loadImage;
    ImageView picture;
    ProgressBar progressBar;
    EditText searchKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadImage = findViewById(R.id.loadImage);
        picture = findViewById(R.id.picureId);
        progressBar = findViewById(R.id.progressBarId);
        searchKey = findViewById(R.id.inputSearch);

        picture.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picture.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                String searchValues = searchKey.getText().toString();
                new MyTask().execute(searchValues);
                /*
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pictureRetrievalTask(searchValues);
                    }
                });
                thread.start();*/
            }
        });
    }
    //Using AsyncTask to get remote data

    private class MyTask extends AsyncTask<String, Void, Bitmap>
    {
        private Bitmap result;

        @Override
        protected Bitmap doInBackground(String... params)
        {
            Uri.Builder urL = Uri.parse("https://pixabay.com/api/").buildUpon();
            urL.appendQueryParameter("key", "23319229-94b52a4727158e1dc3fd5f2db");
            urL.appendQueryParameter("q", params[0]);
            String urlString = urL.build().toString();

            //Establihs connection with api server and search api for searchKey image
            HttpURLConnection conn = openConnection(urlString);
            String data = "";
            try
            {
                if(isConnectionOkay(conn) == false)
                {
                    throw new IllegalArgumentException("cant establish connection");
                }
                else {
                    data = downloadToString(conn);
                    Log.d("testing data", data);
                }
            }
            catch(Exception e) { e.printStackTrace(); }
            finally {
                conn.disconnect();
            }

            //build image url string from image url
            String imageUrl = getImageLargeUrl(data);
            Uri.Builder url = Uri.parse(imageUrl).buildUpon();
            String urlString2 = url.build().toString();
            Log.d("Hello", "ImageUrl: "+urlString2);

            HttpURLConnection connection = openConnection(urlString2);
            try
            {
                if(isConnectionOkay(connection) == false)
                {
                    throw new IllegalArgumentException("cant connect to image url");
                }
                else
                {
                    result = downloadToBitmap(connection);
                    Log.d("testing bitmap", result.toString());
                }
            }
            catch(Exception e) { e.printStackTrace(); }
            finally {
                connection.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            //Log.d("tetsing bitmap result", result);
            picture.setImageBitmap(result);
            picture.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }


    //The Efficient new way of using remote data
/*
    private String searchRemoteAPI(String searchKey){
        String data = null;
        Uri.Builder url = Uri.parse("https://pixabay.com/api/").buildUpon();
        url.appendQueryParameter("key","23319229-94b52a4727158e1dc3fd5f2db");
        url.appendQueryParameter("q",searchKey);
        String urlString = url.build().toString();
        Log.d("Hello", "pictureRetrievalTask: "+urlString);

        HttpURLConnection connection = openConnection(urlString);
        if(connection == null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"Check Internet",Toast.LENGTH_LONG).show();
                }
            });

        }
        else if (isConnectionOkay(connection) == false){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"Problem with downloading",Toast.LENGTH_LONG).show();
                }
            });

        }
        else{
            data = downloadToString(connection);
            if(data !=null) {
                Log.d("Hello", data);
            }
            else{
                Log.d("Hello", "Nothing returned");
            }
            connection.disconnect();
        }

        return data;
    }
*/
    /*
    private void pictureRetrievalTask(String searchKey) {

        String data = searchRemoteAPI(searchKey);
        if(data != null){
            String imageUrl = getImageLargeUrl(data);
            if(imageUrl !=null) {
                Log.d("Hello", imageUrl);
                Bitmap image = getImageFromUrl(imageUrl);
                if (image!=null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            picture.setImageBitmap(image);
                            picture.setVisibility(View.VISIBLE);

                        }
                    });
                }
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"No search results",Toast.LENGTH_LONG).show();
                    }
                });
            }

        }

    }

    private Bitmap getImageFromUrl(String imageUrl){

        Bitmap image = null;

        Uri.Builder url = Uri.parse(imageUrl).buildUpon();
        String urlString = url.build().toString();
        Log.d("Hello", "ImageUrl: "+urlString);

        HttpURLConnection connection = openConnection(urlString);
        if(connection == null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"Check Internet",Toast.LENGTH_LONG).show();
                }
            });

        }
        else if (isConnectionOkay(connection) == false){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"Problem with downloading",Toast.LENGTH_LONG).show();
                }
            });

        }
        else{
            image = downloadToBitmap(connection);
            if(image !=null) {
               // Log.d("Hello", image.toString());
            }
            else{
                Log.d("Hello", "Nothing returned");
            }
            connection.disconnect();
        }

        return image;
    }*/

    private String getImageLargeUrl(String data){
        String imageUrl = null;
        try {
            JSONObject jBase = new JSONObject(data);
            JSONArray jHits = jBase.getJSONArray("hits");
            if(jHits.length()>0){
                JSONObject jHitsItem = jHits.getJSONObject(0);
                imageUrl = jHitsItem.getString("largeImageURL");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imageUrl;
    }

    private HttpURLConnection openConnection(String urlString)  {

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conn;
    }

    private boolean isConnectionOkay(HttpURLConnection conn){
        try {
            if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String downloadToString(HttpURLConnection conn){
        String data = null;
        try {
            InputStream inputStream = conn.getInputStream();
            byte[] byteData = IOUtils.toByteArray(inputStream);
            data = new String(byteData, StandardCharsets.UTF_8);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return data;
    }

    private Bitmap downloadToBitmap(HttpURLConnection conn){

       // Log.d("Hello", String.valueOf(conn.getContentLength()));
        setProgressBar(conn.getContentLength());
        Bitmap data = null;
        try {
            InputStream inputStream = conn.getInputStream();
            byte[] byteData = getByteArrayFromInputStream(inputStream);
            Log.d("Hello", String.valueOf(byteData.length));
            data = BitmapFactory.decodeByteArray(byteData,0,byteData.length);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return data;
    }

    private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        int progress = 0;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            progress = progress+nRead;
            animateProgressBar(progress);
        }

        return buffer.toByteArray();
    }


    private void setProgressBar(int max){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setMin(0);
                progressBar.setMax(max);
            }
        });

    }

    private void animateProgressBar(int value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(value);
            }
        });

    }



}