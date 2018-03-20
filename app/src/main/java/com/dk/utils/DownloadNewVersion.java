package com.dk.utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadNewVersion extends AsyncTask<String,Integer,Boolean> {
    private static final String TAG = "DownloadNewVersion>>>";
    private ProgressDialog bar;
    @SuppressLint("StaticFieldLeak")
    private Context context;
    public void setContext(Context context){
        this.context = context;
    }

    private String version;
    public void setversion(String version){

        this.version = version;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        bar = new ProgressDialog(context);
        bar.setCancelable(false);
        bar.setMessage("Downloading...");
        bar.setIndeterminate(true);
        bar.setCanceledOnTouchOutside(false);
        bar.show();
    }
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        bar.setIndeterminate(false);
        bar.setMax(100);
        bar.setProgress(progress[0]);
        String msg = "";
        if(progress[0]>99){
            msg="Finishing... ";
        }else {
            msg="Downloading... "+progress[0]+"%";
        }
        bar.setMessage(msg);
    }
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        bar.dismiss();
        if(result){
            Toast.makeText(context,"Update Done",
                    Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"Error: Try Again",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected Boolean doInBackground(String... arg0) {
        Boolean flag = false;
        try {
            URL url = new URL("https://s3-ap-southeast-1.amazonaws.com/oqatt/oqatt-v."+version+".apk");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(false);
            c.connect();
            String PATH = Environment.getExternalStorageDirectory()+"/Download/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file,"oqatt-v."+version+".apk");
            if(outputFile.exists()){
                outputFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            InputStream is = c.getInputStream();
            if (c.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = c.getErrorStream();
                is = c.getInputStream();
            }
            else {
                Log.e(TAG, String.valueOf(c.getResponseCode()));
                return false;
            }
            int total_size = 1431692;//size of apk
            byte[] buffer = new byte[1024];
            int len1 = 0;
            int per = 0;
            int downloaded=0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
                downloaded +=len1;
                per = (int) (downloaded * 100 / total_size);
                publishProgress(per);
            }
            fos.close();
            is.close();
            OpenNewVersion(PATH);
            flag = true;
        } catch (Exception e) {
            Log.e(TAG, "Update Error: " + e.getMessage());
            try {
                throw e;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            flag = false;
        }
        return flag;
    }

    private void OpenNewVersion(String location) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(location + "oqatt-v."+version+".apk")),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
