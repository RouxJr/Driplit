package com.example.s215131746.driplit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.support.v4.content.ContextCompat.startActivity;


public class GeneralMethods {
    Context context;
    public GeneralMethods(Context c)
    {
        context= c;
    }

    public void openWebPage(String url) {
       // Uri webpage = Uri.parse(url);
      //  Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
       // if (intent.resolveActivity(getPackageManager()) != null) {
       //     startActivity(context, intent);
      //  }
    }

    public void writeToFile(String data,String fileName) {

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String readFromFile(String fileName) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }
    public String[] Read(String fileName,String splitter)
    {
        return readFromFile(fileName).split(splitter);
    }
}
