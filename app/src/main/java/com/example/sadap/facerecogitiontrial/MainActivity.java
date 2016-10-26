package com.example.sadap.facerecogitiontrial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private final String subscriptionKey="950559192d0d44f4b04a6d72e62492ee";
    private ProgressDialog detectionProgressDialog;
    private FaceServiceClient faceServiceClient;
    private final int PICK_IMAGE = 1;
    Button chooseButton;
    Bitmap originalBitmap ;

    ImageView selectedImage;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if ((requestCode == PICK_IMAGE && resultCode == RESULT_OK)) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    selectedImage.setImageBitmap(bitmap);
                    getFaceAttributes(bitmap);




                }

            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }



    }

    private Bitmap drawFace(Bitmap oBitmap, Face[] faces)
    {
        Bitmap bitmap = oBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int strokeWidth = 2;
        paint.setStrokeWidth(strokeWidth);

        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.FILL);
        paint2.setColor(Color.WHITE);
        paint2.setTextSize(10);


        if(faces != null)
        {
            for(Face face:faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(faceRectangle.left, faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height, paint );

                String ageAndGender = String.format("%s:%s", face.faceAttributes.age, face.faceAttributes.gender);
                canvas.drawText(ageAndGender, faceRectangle.left, faceRectangle.top-10, paint2);
            }
        }





        return bitmap;


    }




    private void getFaceAttributes(final Bitmap imageBitmap)
    {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        originalBitmap = imageBitmap;
        AsyncTask<InputStream, String, Face[]> getFaceAttributesTask = new AsyncTask<InputStream, String, Face[]>() {
            @Override
            protected Face[] doInBackground(InputStream... params) {
                publishProgress("Detecting ....");
                try
                {
                    Face[] result = faceServiceClient.detect(params[0],
                            true,
                            false,
                            new FaceServiceClient.FaceAttributeType[]{FaceServiceClient.FaceAttributeType.Age, FaceServiceClient.FaceAttributeType.Gender, FaceServiceClient.FaceAttributeType.Smile});
                    if(result == null)
                    {
                        publishProgress("Detection finished. Nothing detected...");
                        return null;
                    }
                    publishProgress(String.format("Detection finished. %d face detected", result.length));
                    Log.i("faceinfo", String.format("Detection finished. %d face detected", result.length));
                    for(Face face : result)
                    {

                        Log.i("age", String.format("Age. %s face detected", face.faceAttributes.age));
                        Log.i("gender", String.format("Detection finished. %s face detected", face.faceAttributes.gender));


                    }


                    return result;

                }
                catch(Exception ex)
                {
                    publishProgress("Detection failed");
                    ex.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPreExecute() {
                //TODO: show progress dialog
            }
            @Override
            protected void onProgressUpdate(String... progress) {
                //TODO: update progress
                //txtViewResult.setText(progress[0]);
            }
            @Override
            protected void onPostExecute(Face[] result) {
                //TODO: update face frames
                String strResult = "";
                if(result != null)
                {
                    strResult += String.format("Detection finished. %d face detected \n", result.length);
                    selectedImage.setImageBitmap(drawFace(originalBitmap, result));
                    for(Face face : result)
                    {

                        strResult += String.format("Age: %s  \n", face.faceAttributes.age);
                        strResult += String.format("gender: %s  \n", face.faceAttributes.gender);
                        strResult += String.format("smile: %s  \n", face.faceAttributes.smile);


                    }
                }



                //txtViewResult.setText(strResult);
            }

        };



        getFaceAttributesTask.execute(inputStream);
    }





    public void chooseImage(View view)
    {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            detectionProgressDialog = new ProgressDialog(this);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        faceServiceClient =
                new FaceServiceRestClient(subscriptionKey);
        chooseButton = (Button)findViewById(R.id.chooseButton);
        selectedImage = (ImageView)findViewById(R.id.selectedImage);
    }
}
