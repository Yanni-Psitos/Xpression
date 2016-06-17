package ypsitos.xpression;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {

    public static Button mSelectButton;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1; //Request int for camera & external storage permissions.
    private static final int SELECT_PICTURE = 2; //Request int for gallery browser.

    private String selectedImagePath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectButton = (Button)findViewById(R.id.btnSelectPhoto);
        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    public void selectImage() { //Method that encapsulates the image selection process within a dialog click.
        final CharSequence[] items = {"Take Photo", "Choose From Library", "Cancel"}; //Dialog item names.
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo"); //Title for the dialog appears as such.
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
                } else {
                    Log.v("Accessed permissions?","Yes, and allowed.");
                    Intent intent = new Intent();   //Creates gallery browser intent.
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, //Begins gallery browser intent for a result (URI to use for later on).
                            "Select Picture"), SELECT_PICTURE);
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
                    Toast.makeText(MainActivity.this, "Did not fucking work.", Toast.LENGTH_SHORT).show();

                    //TODO Create a block of code that denies access to the gallery, and re-requests permissions.
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            //TODO Check if there are any other possible case lines to check for.
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {    //Called when the activity is started for the result and receives the picture selection (or not).
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
            }
        }
    }
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            Log.v("uri: ","URI not received.");
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            Log.v("Grabbed URI?", "Chyeah bruh.");
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

}