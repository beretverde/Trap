package com.renegade.trap;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DCIM;
import static com.renegade.trap.MainActivity.PERMISSIONS_STORAGE;
import static com.renegade.trap.MainActivity.REQUEST_EXTERNAL_STORAGE;
import static com.renegade.trap.MyLocationListenerGPS.verifyStoragePermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    TextInputEditText workOrderId =null;
    TextInputEditText locationName =null;
    Spinner city = null;

    ImageButton img1=null;
    ImageButton img2=null;
    ImageButton img3=null;
    ImageButton img4=null;
    ImageView imgView=null;
    PhotoExternals photo1= new PhotoExternals();
    PhotoExternals photo2= new PhotoExternals();
    PhotoExternals photo3= new PhotoExternals();
    PhotoExternals photo4= new PhotoExternals();
    File photoFile=null;

    // used to place the photo on screen
    int place;
    int targetW = 0;
    int targetH = 0;

    // GPSTracker class
    GPSTracker gps;

    List<Customer> customers = null;


    private static final int INITIAL_REQUEST=1337;
    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static String[] PERMISSIONS_STORAGE = {  Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE  };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        List<Customer>  convCust = Customers.readTextFilePreConv(getApplicationContext(), R.raw.additionaldata);
//        Customers.geocode(getApplicationContext(), convCust);
        customers = Customers.readTextFile(getApplicationContext(), R.raw.customers);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }
        verifyStoragePermissions(this);

        Log.i(TAG, "onCreate");
        gps = new GPSTracker(MainActivity.this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        img1= (ImageButton)findViewById(R.id.imageButton1);
        img2= (ImageButton)findViewById(R.id.imageButton2);
        img3= (ImageButton)findViewById(R.id.imageButton3);
        img4= (ImageButton)findViewById(R.id.imageButton4);
        workOrderId=(TextInputEditText)findViewById(R.id.work_order_id);
        locationName=(TextInputEditText)findViewById(R.id.locationName);
        city=(Spinner)findViewById(R.id.spinner);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        targetW = 180;
        targetH = 180;

        if (savedInstanceState != null) {
//            Bitmap image1 = savedInstanceState.getParcelable("bitmap1");
//            img1.setImageBitmap(image1);
//            Bitmap image2 = savedInstanceState.getParcelable("bitmap2");
//            img2.setImageBitmap(image2);
//            Bitmap image3 = savedInstanceState.getParcelable("bitmap3");
//            img3.setImageBitmap(image3);
//            Bitmap image4 = savedInstanceState.getParcelable("bitmap4");
//            img4.setImageBitmap(image4);
            photo1 = savedInstanceState.getParcelable("photo1");
            photo2 = savedInstanceState.getParcelable("photo2");
            photo3 = savedInstanceState.getParcelable("photo3");
            photo4 = savedInstanceState.getParcelable("photo4");
        }

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCompatiblePhoto(1, img1);
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCompatiblePhoto(2, img2);
            }
        });

        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCompatiblePhoto(1, img3);
            }
        });

        img4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCompatiblePhoto(1, img4);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);

                String workId =workOrderId.getText().toString();
                if (workId.isEmpty()) {
                    workOrderId.setError("Please enter work order number!");
                    return;
                }
                String cityName= city.getSelectedItem().toString();
                if (cityName.equalsIgnoreCase("Select City")) {
                    Toast.makeText(getApplicationContext(), "Please select the city!", Toast.LENGTH_LONG).show();
                    return;
                }

                String locName = locationName.getText().toString();
                if (locName.isEmpty()) {
                    locationName.setError("Please select the location!");
                    return;
                }

//                ProgressDialog dialog = new ProgressDialog(MainActivity.this);
//                dialog.setMessage("Building PDF document");
//                dialog.show();

                sendPdfViaEmail(view, workId, cityName, locName, email);


            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cities_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private void getCompatiblePhoto(int idx, ImageView img) {
        place = idx;
        imgView=img;
        if (Build.VERSION.SDK_INT > 19) {
            savePhotoLocation(dispatchTakePictureIntent());
        } else {
            capturePhoto();
        }
    }


    private void sendPdfViaEmail(View view, String workId, String cityName, String locName, Intent email) {
        PdfInfoDTO dto = new PdfInfoDTO(view, workId, cityName, locName, email);
        //                PdfDocument document = new PdfDocument();
        PdfRunner runner = new PdfRunner();
        runner.execute(dto);
    }

    private class PdfRunner extends AsyncTask<PdfInfoDTO, String, String> {
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Building Pdf File");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(PdfInfoDTO... params) {
            try {
                PdfInfoDTO dto = params[0];
                Document document = new Document(PageSize.LETTER);
                File file = null;
                OutputStream os;
                try {
                    file = createTempFile();
                    os = new FileOutputStream(file);
                    PdfWriter.getInstance(document, os);
                    document.open();
                    int page = 1;
                    if (photo4 != null && photo4.hasPhoto()) {
                        document.newPage();
                        document.add(new Paragraph("Invoice"));
                        setPdfHeader(dto.getWorkId(), dto.getCityName(), dto.getLocName(), document);
                        addImageToPage(dto.getView(), document, photo4, page++);
                    }
                    if (photo3 != null && photo3.hasPhoto()) {
                        document.newPage();
                        document.add(new Paragraph("Tester"));
                        setPdfHeader(dto.getWorkId(), dto.getCityName(), dto.getLocName(), document);
                        addImageToPage(dto.getView(), document, photo3, page++);
                    }

                    if (photo2 != null && photo2.hasPhoto()) {
                        document.newPage();
                        document.add(new Paragraph("Trap 2"));
                        setPdfHeader(dto.getWorkId(), dto.getCityName(), dto.getLocName(), document);
                        addImageToPage(dto.getView(), document, photo2, page++);
                    }
                    if (photo1 != null && photo1.hasPhoto()) {
                        document.newPage();
                        document.add(new Paragraph("Trap 1"));
                        setPdfHeader(dto.getWorkId(), dto.getCityName(), dto.getLocName(), document);
                        addImageToPage(dto.getView(), document, photo1, page++);
                    }
                    document.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                File root =  Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);
                File location = new File(root.getPath(), "Camera");
                File[] files = location.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.matches("\\d{8}_\\d{6}.jpg");
                    }
                });
                for (File theFile : files) {
                    theFile.delete();
                }
                dto.getEmail().setType("text/pdf");

                dto.getEmail().putExtra(Intent.EXTRA_EMAIL, new String[] {"renegadetrappics@gmail.com"});
//                dto.getEmail().putExtra(Intent.EXTRA_EMAIL, new String[] {"tylerjacox@gmail.com","brentjacox@gmail.com"});
                dto.getEmail().putExtra(Intent.EXTRA_SUBJECT, dto.getLocName()+", "+dto.getCityName()+", #"+dto.getWorkId());

                ArrayList<Uri> photos = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= 24) {
                    photos.add(FileProvider.getUriForFile(getApplicationContext(),"com.example.android.fileprovider",file));
                } else {
                    photos.add(Uri.fromFile(file));
                }

                dto.getEmail().putExtra(Intent.EXTRA_STREAM, photos);

                photo1=new PhotoExternals();
                photo2=new PhotoExternals();
                photo3=new PhotoExternals();
                photo4=new PhotoExternals();

                try {
                    if (dto.getEmail().resolveActivity(getPackageManager()) != null) {
                        startActivity(dto.getEmail());
                    }
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "true";
        }
        protected void onPostExecute(String status) {
            if (dialog.isShowing()) {
                dialog.dismiss();
                workOrderId.setText("");
                locationName.setText("");
                city.setSelection(((ArrayAdapter<String>)city.getAdapter()).getPosition("Select City"));
                img1.setImageResource(R.drawable.ic_menu_camera);
                img2.setImageResource(R.drawable.ic_menu_camera);
                img3.setImageResource(R.drawable.ic_menu_camera);
                img4.setImageResource(R.drawable.ic_menu_camera);
            }
        }


    }

    private void setPdfHeader(String workId, String cityName, String locName, Document document) throws DocumentException {
        Date date = new Date();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        document.add(new Paragraph("#"+workId+" - "+locName+"/"+cityName+" @"+dateFormat.format(date)+" "+timeFormat.format(date)));
    }

    private void addImageToPage(View view, Document document, PhotoExternals photo, int pageNumber) {
        Bitmap bitmap = null;
        try {
            String fileLocation = null;
            if (Build.VERSION.SDK_INT <= 19) {
                fileLocation = getRealPathFromUri(getApplicationContext(), photo.getPhotoUri());
            } else {
                fileLocation = photo.getPhotoFile().getAbsolutePath();
            }
            ExifInterface exifInterface = null;
            exifInterface = new ExifInterface(fileLocation);

            if (Build.VERSION.SDK_INT <= 19) {
                bitmap = MediaStore.Images.Media.getBitmap(view.getContext().getContentResolver(),photo.getPhotoUri());
            } else {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photo.getPhotoFile());

                bitmap = MediaStore.Images.Media.getBitmap(view.getContext().getContentResolver(),photoUri);
            }
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap finalBitmap = PhotoUtil.rotateBitmap(bitmap, orientation);


            // get input stream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            Image image = Image.getInstance(stream.toByteArray());
            image.scaleAbsolute(500, 500);
            document.add(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void capturePhoto()  {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private File dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
        Uri photoURI = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go

            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        return photoFile;

    }
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT > 19) {
                newOnActivityResult();
            } else {
                oldOnActivityResult(data);
            }

        }
    }

    protected void newOnActivityResult() {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(photoFile.getAbsoluteFile().getAbsolutePath());
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            setPic(imgView, photoFile.getAbsolutePath(), orientation);

            Customer customer = getCustomerLocation();
            if (customer != null) {
                locationName.setText(customer.getName());
                city.setSelection(((ArrayAdapter<String>)city.getAdapter()).getPosition(customer.getCity()));
                Log.i(TAG, customer.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void oldOnActivityResult(Intent data) {
        Uri file = data.getData();
        String fileLocation = getRealPathFromUri(getApplicationContext(), file);
        System.out.println(fileLocation);
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(fileLocation);

            Bitmap thumbnail = data.getParcelableExtra("data");
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap image = PhotoUtil.rotateBitmap(thumbnail, orientation);
            imgView.setImageBitmap(image);
            saveUri(file);

            Customer customer = getCustomerLocation();
            if (customer != null) {
                locationName.setText(customer.getName());
                city.setSelection(((ArrayAdapter<String>)city.getAdapter()).getPosition(customer.getCity()));
                Log.i(TAG, customer.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Customer getCustomerLocation() {
        Customer customer = null;
        if (gps.canGetLocation()) {
            Location location = gps.getLocation();
            if (location != null) {
                Log.i(TAG, location.toString());
                customer = Customers.findClosest(getApplicationContext(),location.getLatitude(), location.getLongitude(), customers, location.toString());
            }
        }
        return customer;
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    protected void saveUri(Uri uri) {
        switch(place) {
            case 1:
                photo1.setPhotoUri(uri);
                break;
            case 2:
                photo2.setPhotoUri(uri);
                break;
            case 3:
                photo3.setPhotoUri(uri);
                break;
            case 4:
                photo4.setPhotoUri(uri);
                break;
        }
    }

    protected void savePhotoLocation(File file) {
        switch(place) {
            case 1:
                photo1.setPhotoFile(file);
                break;
            case 2:
                photo2.setPhotoFile(file);
                break;
            case 3:
                photo3.setPhotoFile(file);
                break;
            case 4:
                photo4.setPhotoFile(file);
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }
    private File createTempFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "PDF_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                fileName,  /* prefix */
                ".pdf",         /* suffix */
                storageDir      /* directory */
        );

        return file;
    }
    private void setPic(ImageView view, String photoPath, int orientation) {
        // Get the dimensions of the View
        int targetW = view.getWidth();
        int targetH = view.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        Bitmap image = PhotoUtil.rotateBitmap(bitmap, orientation);

        view.setImageBitmap(image);
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
/*
        if (img1 != null) {
            Bitmap bitmap1 = ((BitmapDrawable)img1.getDrawable()).getBitmap();
            savedInstanceState.putParcelable("bitmap1", bitmap1);
        }
        if (img2 != null) {
            Bitmap bitmap2 = ((BitmapDrawable)img2.getDrawable()).getBitmap();
            savedInstanceState.putParcelable("bitmap2", bitmap2);
        }
        if (img3 != null) {
            Bitmap bitmap3 = ((BitmapDrawable)img3.getDrawable()).getBitmap();
            savedInstanceState.putParcelable("bitmap3", bitmap3);
        }
        if (img4 != null) {
            Bitmap bitmap4 = ((BitmapDrawable)img4.getDrawable()).getBitmap();
            savedInstanceState.putParcelable("bitmap4", bitmap4);
        }
*/
        savedInstanceState.putParcelable("photo1", photo1);
        savedInstanceState.putParcelable("photo2", photo2);
        savedInstanceState.putParcelable("photo3", photo3);
        savedInstanceState.putParcelable("photo4", photo4);
        // etc.
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}
class MyLocationListenerGPS implements LocationListener {
    Context context = null;
    public MyLocationListenerGPS(Context context) {
        this.context = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        String cityName = null;
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}
