package com.hugh.katiecats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hugh.katiecats.util.Animations;
import com.hugh.katiecats.util.ImgurUtil;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class NewCatsActivity extends Activity implements View.OnClickListener {

    private static final String LOGTAG = "New Cat Activity";

    private static int currentGalleryPage = 0;
    private static Set<String> urlSet;

    ConnectivityManager connectivityManager;
    FloatingActionButton newCat;
    ImageView catView, upCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (toolbar != null) {
            setActionBar(toolbar);
        }

        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        upCircle = (ImageView) findViewById(R.id.up_circle_contract);
        enterActivityAnimation();

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        newCat = (FloatingActionButton) findViewById(R.id.fab_new_cat);
        newCat.setOnClickListener(this);

        catView = (ImageView) findViewById(R.id.cat_imageview);
        this.onClick(null);                                                             // load a cat when the activity starts
    }

    @Override
    public void onClick(View v) {
        if (isConnectedToInternet()) {
            AsyncDisplayCat asyncDisplayCat = new AsyncDisplayCat();
            asyncDisplayCat.execute();
        }
        else {
            Toast.makeText(NewCatsActivity.this, "No internet connection detected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        HomeActivity.FROM_NEW_CATS = true;
        exitActivityAnimation();
    }

    private void enterActivityAnimation() {
        Animations.contract(this, upCircle);
    }

    private void exitActivityAnimation() {
        Animations.expand(this, upCircle, new Animations.PostAnimAction() {
            @Override
            public void execute() {
                superBack();
            }
        });
    }

    private void superBack() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private boolean isConnectedToInternet() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_cat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        catView.buildDrawingCache();

        switch (item.getItemId()) {
            case R.id.new_cat_save:
                if (isExternalStorageWritable()) {
                    Intent notifyFilesystem = ImgurUtil.saveCat(catView.getDrawingCache());
                    if (notifyFilesystem != null) {
                        sendBroadcast(notifyFilesystem);
                        Toast.makeText(this, "Cat Saved!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this, "Error - cat not saved!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Log.e(LOGTAG, "external storage not writable - state is " + Environment.getExternalStorageState());
                }
                break;
            case R.id.new_cat_share:
                Intent share = ImgurUtil.shareCat(catView.getDrawingCache());
                startActivity(share);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                Log.wtf(LOGTAG, "not one of our menuitems - id = " + item.getItemId());
        }

        catView.destroyDrawingCache();
        return true;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        // TODO handle marshmallow permissions model http://developer.android.com/training/permissions/requesting.html
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Async Task to display a cat from imgur's catpictures gallery in the imageview
     */
    class AsyncDisplayCat extends AsyncTask<Void, Set<String>, Bitmap> {
        final static private String LOGTAG = "image getter";
        ProgressWheel progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressWheel.spin();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            /* being null or empty means we have no more cat urls so we need to get more */
            if (urlSet == null || urlSet.isEmpty()) {
                urlSet = ImgurUtil.getImageUrls(ImgurUtil.IMAGE_SIZE.MEDIUM, currentGalleryPage);
                Log.d(LOGTAG, "urlSet from page " + currentGalleryPage + " initialized with " + urlSet.size() + " elems");
                currentGalleryPage++;                             // increment current page so we get cats from the next page next time the set is refilled
            }

            Bitmap bitmap = null;
            String url = urlSet.iterator().next();
            urlSet.remove(url);                                   // basically doing a stack pop - removing the first element of the set
            try {
                Log.d(LOGTAG, "urlSet now contains " + urlSet.size() + " elems");
                InputStream bitmapStream = (InputStream) new URL(url).getContent();

                int maxWidth = catView.getMaxWidth();
                int maxHeight = catView.getMaxHeight();
                bitmap = ImgurUtil.decodeSampledBitmapFromStream(bitmapStream, maxWidth, maxHeight);
            }
            catch (MalformedURLException mue) {
                Log.e(LOGTAG, "malformed url in async task - was " + url, mue);
            }
            catch (IOException ioe) {
                Log.e(LOGTAG, "i/o exception in async task", ioe);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap scaledImage) {
            progressWheel.stopSpinning();
            if (scaledImage != null) {
                catView.setImageBitmap(scaledImage);
            }
        }
    }
}
