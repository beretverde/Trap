package com.renegade.trap;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
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

import static android.os.Environment.DIRECTORY_DCIM;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextInputEditText workOrderId =null;
    TextInputEditText locationName =null;
    Spinner city = null;

    ImageButton img1=null;
    ImageButton img2=null;
    ImageButton img3=null;
    ImageButton img4=null;
    ImageView imgView=null;
    Uri photoUri1= null;
    Uri photoUri2= null;
    Uri photoUri3= null;
    Uri photoUri4= null;

    // used to place the photo on screen
    int place;
    int targetW = 0;
    int targetH = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", "Build: "+Build.VERSION.SDK_INT);
        } else {
            Log.e("-->", " < 14");
        }

        targetW = 180;
        targetH = 180;

        if (savedInstanceState != null) {
            Bitmap image1 = savedInstanceState.getParcelable("bitmap1");
            img1.setImageBitmap(image1);
            Bitmap image2 = savedInstanceState.getParcelable("bitmap2");
            img2.setImageBitmap(image2);
            Bitmap image3 = savedInstanceState.getParcelable("bitmap3");
            img3.setImageBitmap(image3);
            Bitmap image4 = savedInstanceState.getParcelable("bitmap4");
            img4.setImageBitmap(image4);
            photoUri1 = savedInstanceState.getParcelable("photoUri1");
            photoUri2 = savedInstanceState.getParcelable("photoUri2");
            photoUri3 = savedInstanceState.getParcelable("photoUri3");
            photoUri4 = savedInstanceState.getParcelable("photoUri4");
        }

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                place = 1;
                imgView=img1;
                capturePhoto();
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                place = 2;
                imgView=img2;
                capturePhoto();
            }
        });

        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                place = 3;
                imgView=img3;
                capturePhoto();
            }
        });

        img4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                place = 4;
                imgView=img4;
                capturePhoto();
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
                    if (photoUri1 != null) {
                        document.add(new Paragraph("Trap 1"));
                        setPdfHeader(dto.getWorkId(), dto.getCityName(), dto.getLocName(), document);
                        addImageToPage(dto.getView(), document, photoUri1, page++);
                    }
                    if (photoUri2 != null) {
                        document.newPage();
                        document.add(new Paragraph("Trap 2"));
                        setPdfHeader(dto.getWorkId(), dto.getCityName(), dto.getLocName(), document);
                        addImageToPage(dto.getView(), document, photoUri2, page++);
                    }
                    if (photoUri3 != null) {
                        document.newPage();
                        document.add(new Paragraph("Tester"));
                        setPdfHeader(dto.getWorkId(), dto.getCityName(), dto.getLocName(), document);
                        addImageToPage(dto.getView(), document, photoUri3, page++);
                    }
                    if (photoUri4 != null) {
                        document.newPage();
                        document.add(new Paragraph("Work Order"));
                        setPdfHeader(dto.getWorkId(), dto.getCityName(), dto.getLocName(), document);
                        addImageToPage(dto.getView(), document, photoUri4, page++);
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

                dto.getEmail().putExtra(Intent.EXTRA_EMAIL, new String[] {"gary@renegadeoil.net"});
//                dto.getEmail().putExtra(Intent.EXTRA_EMAIL, new String[] {"tylerjacox@gmail.com","brentjacox@gmail.com"});
                dto.getEmail().putExtra(Intent.EXTRA_SUBJECT, dto.getLocName()+", "+dto.getCityName()+", #"+dto.getWorkId());

                ArrayList<Uri> photos = new ArrayList<>();
                photos.add(Uri.fromFile(file));
                dto.getEmail().putExtra(Intent.EXTRA_STREAM, photos);

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
            }
        }


    }

    private void setPdfHeader(String workId, String cityName, String locName, Document document) throws DocumentException {
        Date date = new Date();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        document.add(new Paragraph("#"+workId+" - "+locName+"/"+cityName+" @"+dateFormat.format(date)+" "+timeFormat.format(date)));
    }

    private void addImageToPage(View view, Document document, Uri photo, int pageNumber) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(view.getContext().getContentResolver(),photo);
            // get input stream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
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
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri file = data.getData();
            Bitmap thumbnail = data.getParcelableExtra("data");
            Bitmap scaled = Bitmap.createScaledBitmap(thumbnail, targetH, targetW, true);
            imgView.setImageBitmap(scaled);
            saveUri(file);
//            imgView.setImageURI(file);

//            imgView.setImageBitmap(decodeSampledBitmapFromFile(photoFile.getAbsoluteFile(), 400, 300));
//            imgView.setImageURI(photoUri);
        }
    }

    protected void saveUri(Uri uri) {
        switch(place) {
            case 1:
                photoUri1 = uri;
                break;
            case 2:
                photoUri2 = uri;
                break;
            case 3:
                photoUri3 = uri;
                break;
            case 4:
                photoUri4 = uri;
                break;
        }
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
    public static Bitmap decodeSampledBitmapFromFile(File file,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static boolean saveBitmap(Bitmap original, Bitmap.CompressFormat format, int quality, File outFile)
    {
        if(original == null)
            return false;

        try {
            FileOutputStream out = new FileOutputStream(outFile);
            boolean result = original.compress(format, quality, out);
            out.flush();
            out.close();
            return result;
        }
        catch(Exception e) {
            Log.d("saveBitmap", e.getMessage(), e);
        }
        return false;
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
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
        savedInstanceState.putParcelable("photoUri1", photoUri1);
        savedInstanceState.putParcelable("photoUri2", photoUri2);
        savedInstanceState.putParcelable("photoUri3", photoUri3);
        savedInstanceState.putParcelable("photoUri4", photoUri4);
        // etc.
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}
