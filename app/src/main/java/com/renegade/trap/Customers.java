package com.renegade.trap;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by brent on 8/12/17.
 */

public class Customers {
    private static final String TAG = Customers.class.getSimpleName();

    private static double GPS_OFFSET = 0.001553D;

    public static List<Customer> readTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        List<Customer> customers = new ArrayList<>();

        try {
            while (( line = buffreader.readLine()) != null) {
                String[] values = line.split("\",");
                if (values.length >= 7 ) {
                    try {
                        Customer customer = new Customer(values[0].replace("\"",""), values[1].replace("\"",""), values[2].replace("\"",""), values[3].replace("\"",""), values[4].replace("\"",""), Double.valueOf(values[5].replace("\"","")), Double.valueOf(values[6].replace("\"","")));
                        customers.add(customer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return customers;
    }
    public static Customer findClosest(Context ctx, double latitude, double longitude, List<Customer> customers, String locationStr) {
        Customer nearestCustomer = null;
        float[] results = new float[4];
        float closest = 0.0f;
        File output = null;
        BufferedWriter bw = null;
        FileOutputStream fos = null;


        try {
            output = createTempFile(ctx);
            fos = new FileOutputStream(output);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write("Location="+latitude+" "+longitude);
            bw.newLine();
            bw.write("string="+locationStr);
            bw.newLine();
            for (Customer customer : customers ) {
                if (customer.getLatitude() < (latitude+ GPS_OFFSET) && customer.getLatitude() > latitude- GPS_OFFSET &&
                        customer.getLongitude() > (longitude-GPS_OFFSET)  && customer.getLongitude() < (longitude+GPS_OFFSET)) {
                    Location.distanceBetween(latitude, longitude, customer.getLatitude(), customer.getLongitude(), results);
                    bw.write("Distance to: "+customer.getName()+" ="+results[0]);
                    bw.newLine();
                    bw.flush();
                    if (results.length > 0) {
                        if (nearestCustomer == null) {
                            closest = results[0];
                            nearestCustomer = customer;
                        }
                        else {
                            if (results[0] < closest) {
                                closest = results[0];
                                nearestCustomer = customer;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (fos != null) {
                try {
                    if (bw != null) {
                        bw.flush();
                    }
                    fos.close();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return nearestCustomer;
    }

    public static void geocode(Context ctx, List<Customer> customers) {
        File output = null;
        BufferedWriter bw = null;
        FileOutputStream fos = null;

        try {
            Geocoder geocoder = new Geocoder(ctx);
            output = new File(FileUtil.getAlbumStorageDir(), "output.csv" );
            fos = new FileOutputStream(output);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (Customer customer : customers ) {
                List<Address> addresses = geocoder.getFromLocationName(customer.getAddress()+","+customer.getCity()+","+customer.getState()+","+customer.getZip(), 1);
                for (Address address: addresses) {
                    System.out.println(address);
                    if (address.hasLatitude()) {
                        customer.setLatitude(address.getLatitude());
                    }
                    if (address.hasLongitude()) {
                        customer.setLongitude(address.getLongitude());
                    }
                }
                if (addresses.isEmpty()) {
                    Log.i(TAG, "NO LAT/LON for ADDRESS: "+customer.toString());
                }
                bw.write(customer.toString());
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static File createTempFile(Context ctx) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "GPS_" + timeStamp + "_";
        File storageDir = FileUtil.getAlbumStorageDir();
        File file = File.createTempFile(
                fileName,  /* prefix */
                ".txt",         /* suffix */
                storageDir      /* directory */
        );
        MediaScannerConnection.scanFile(ctx, new String[] {file.toString()}, null, null);
        return file;
    }

}
