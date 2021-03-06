package ypsitos.xpression;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class MainActivity extends AppCompatActivity {

    public static Button mSelectButton;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1; //Request int for camera & external storage permissions.
    private static final int SELECT_PICTURE = 2;//Request int for gallery browser.
    private static final int REQUEST_IMAGE_CAPTURE = 3;

    private static final String CLOUD_VISION_API_KEY = "AIzaSyDLSL3CV9iHsMqHmKXb391xLK7E0rHQLEo";
    public static final String FILE_NAME = "temporary.jpg";

    private TextView mLoadingText;
    private ImageView mMainImage;
    private TextView mListOfItemsToPresentToTheUser; //TODO: Present a list to the user, remove an item that's found, store a number of points that is
    private TextView mPoints;

    private int points = 0;
    private String[] randomScavList = {"n/a","n/a","n/a","n/a","n/a","n/a","n/a","n/a","n/a",};
    private String[] preScavList= {"toilet", "coffee cup", "hair", "t-shirt", "hand", "pc game", "bottle"};




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListOfItemsToPresentToTheUser = (TextView) findViewById(R.id.usersList);
        mLoadingText = (TextView) findViewById(R.id.loadingTv);
        mMainImage = (ImageView) findViewById(R.id.ivImage);
        mPoints = (TextView) findViewById(R.id.pointsTv);

        mSelectButton = (Button) findViewById(R.id.btnSelectPhoto);
        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        int counter = 0;
        for (int i = 0; i < 6; i++) {                   //Cycles the random list possibilities and fills an array with unique ones to present to the user.
            String currentWord = getRandom(preScavList);
            if (!getAtIndex(randomScavList, counter).contains(currentWord) && !getAtIndex(randomScavList, counter+1).contains(currentWord) && !getAtIndex(randomScavList, counter+2).contains(currentWord) &&!getAtIndex(randomScavList, counter+3).contains(currentWord) &&!getAtIndex(randomScavList, counter+4).contains(currentWord) &&!getAtIndex(randomScavList, counter+5).contains(currentWord) &&!getAtIndex(randomScavList, counter+6).contains(currentWord)) {
                randomScavList[i] = currentWord;
            }
            else{
                i--;
            }
        }
        mLoadingText.setText(randomScavList[0].toString()+" "+randomScavList[1].toString()+" "+randomScavList[2].toString()+" "+randomScavList[3].toString()+" "+randomScavList[4].toString()+" "+randomScavList[5].toString());

        new MaterialShowcaseView.Builder(this)
                .setTarget(mLoadingText)
                .setDismissText("WOAH IT FUCKING WORKED")
                .setContentText("These are some amazing things for sure")
                .setDelay(100) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse("showcase_id") // provide a unique ID used to ensure it is only shown once
                .show();

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "showcase_id");

        sequence.setConfig(config);

        sequence.addSequenceItem(mLoadingText,
                "First", "GOT IT");

        sequence.addSequenceItem(mLoadingText,
                "Second", "GOT IT");

        sequence.addSequenceItem(mLoadingText,
                "Third", "GOT IT");

        sequence.start();
    }




    public void selectImage() { //Method that encapsulates the image selection process within a dialog click.
        final CharSequence[] items = {"Take Photo", "Choose From Library", "Cancel"}; //Dialog item names.
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Pick A Method!"); //Title for the dialog appears as such.
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) { //On click, results in cycling through possible permission requests, and ends in granting permissions to both the Camera and External Storage.
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Please allow XPRESSION to access your photos! (:", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else if(item == 1) {
                    Log.v("1 Accessed permissions?", "Yes, and allowed.");
                    Intent intent = new Intent();   //Creates gallery browser intent.
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, //Begins gallery browser intent for a result (URI to use for later on).
                            "Select Picture"), SELECT_PICTURE);
                }else if(item == 0){
                    Log.v("2 Accessed permissions?", "Yes, and allowed.");
                    Intent takePictureIntent = new Intent();
                    takePictureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE); //Begins the camera activity for a result (picture to immediately analyze).


                }

            }
        });
        builder.show();
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { //Called when the request permission result is received (or finished).
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  //Permission granted.
                    Intent intent = new Intent();   //Creates gallery browser intent.
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, //Begins gallery browser intent for a result (URI to use for later on).
                            "Select Picture"), SELECT_PICTURE);
                } else {
                    Toast.makeText(MainActivity.this, "Did not work.", Toast.LENGTH_SHORT).show();

                    //TODO Create a block of code that denies access to the gallery, and re-requests permissions.
                    // Permission denied. Disable the Functionality that depends on this permission.
                }
                return;
            }
            //TODO Check if there are any other possible case lines to check for.
            // Other 'case' lines to check for other permissions this app might request

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {    //Called when the activity is started for the result and receives the picture selection (or not).
            if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
                uploadImage(data.getData());//Puts the path of the URI, from the data, into selectedImageUri to transfer it below into a string.
            }
            if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                Bitmap unscaledBitmap = (Bitmap)extras.get("data");
                Bitmap scaledImageBitmap = scaleBitmapDown(unscaledBitmap,1200);
                mMainImage.setImageBitmap(scaledImageBitmap);
                try {
                    callCloudVision(scaledImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            }

    public String getPath(Uri uri) {
        // Safety check for the path reception.
        if (uri == null) {
            Log.v("uri: ", "URI not received.");
            return null;
        }
        // Tries to retrieve the image from the media store first, this will only work for images selected from gallery.
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // This is our fallback for the reception of the path.
        return uri.getPath();
    }

    public void uploadImage(Uri uri) { //Method to upload an image to Cloud Vision, by first turning the path clicked on by the gallery picker into a bitmap (which is the proper format required by GCV).
    Log.d("LogDan","Uri " + uri.toString());
        if (uri != null) { //If there IS a URI, proceed.
            try {
            Bitmap bitmap =
                    scaleBitmapDown(
                            MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                            1200);
                mMainImage.setImageBitmap(bitmap);
                callCloudVision(bitmap);


            } catch(IOException e){
                Log.d("Tagged","Image Uploading has failed.");
                Toast.makeText(this,"Did not work",Toast.LENGTH_SHORT);
            }
            }

        }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switches text to loading.
        mLoadingText.setText(R.string.loading_message);

        // Does the real work in an async task, because the network is needed for use.
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Adds the image grabbed to a byte array.
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG, just in case it's in a format that Android understands but not Cloud Vision.
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encodeing the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // Adds the features based on the type of detection called, and adds the results.
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d("Annotate: ", "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d("Annotate: ", "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d("Annotate: ", "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                mLoadingText.setText(result);
                for(int i=0;i<6;i++){
                    if(result.contains(randomScavList[i])){
                        points++;
                        mLoadingText.setText(points + " :Points"); //Todo: Make sure the points are in the proper place and not in the loadingtext widget/tv.
                    }
                }
            }
        }.execute();
    }
    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format("%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
            }
        } else {
            message += "nothing";
        }

        return message;
    }



    public String getRandom (String[] array){
        int random = new Random().nextInt(array.length);
        return array[random];
    }

    public String getAtIndex(String[] arrayOfStrings, int index){
        return arrayOfStrings[index];
    }

}