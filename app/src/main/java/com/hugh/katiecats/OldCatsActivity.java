package com.hugh.katiecats;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.hugh.katiecats.util.Animations;
import com.hugh.katiecats.util.ImgurUtil;
import com.hugh.katiecats.util.OnTouchListener;

import java.io.IOException;
import java.util.List;

public class OldCatsActivity extends Activity {

    private final static String LOGTAG = "OldCatsActivity";

    private static int CURRENT_INDEX;
    private List<Uri> savedCats;

    ImageView catView, downCircle;
    TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_cat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (toolbar != null) {
            setActionBar(toolbar);
        }

        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        savedCats = ImgurUtil.getSavedCats();
        if (savedCats.isEmpty()) {
            errorText = (TextView) findViewById(R.id.old_cat_error_text);
            errorText.setVisibility(View.VISIBLE);
        }
        else {
            catView = (ImageView) findViewById(R.id.old_cat_imageview);
            catView.setImageURI(savedCats.get(CURRENT_INDEX));
            createTouchListener();
        }

        downCircle = (ImageView) findViewById(R.id.down_circle_contract);
        enterActivityAnimation();
    }

    private void createTouchListener() {
        catView.setOnTouchListener(new OnTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                catView.setImageURI(savedCats.get(getValidIndex(false)));
            }

            @Override
            public void onSwipeRight() {
                catView.setImageURI(savedCats.get(getValidIndex(true)));
            }

            @Override
            public void onLongP() {
                catView.buildDrawingCache();
                Bitmap catPic = catView.getDrawingCache();
                showOnPressDialog(catPic);
            }
        });
    }

    private void showOnPressDialog(final Bitmap catPicture) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action")
                .setItems(new String[] {"Set wallpaper", "Share...", "Delete"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                wallpaperCat(catPicture);
                                break;
                            case 1:
                                shareCat();
                                break;
                            case 2:
                                deleteCat(catPicture);
                                break;
                        }
                    }
                });

        builder.create().show();
    }

    private void deleteCat(final Bitmap catPicture) {
        final Uri catToDelete = savedCats.get(CURRENT_INDEX);
        catView.setImageURI(savedCats.get(getValidIndex(false)));                               // move to next cat

        ImgurUtil.deleteCat(this, catToDelete);
        savedCats = ImgurUtil.getSavedCats();

        Snackbar.make(catView, "Cat Deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent notifyFilesystem = ImgurUtil.saveCat(catPicture, catToDelete);
                        savedCats = ImgurUtil.getSavedCats();
                        catView.setImageURI(savedCats.get(getValidIndex(true)));                // move to prev (undeleted) cat
                        sendBroadcast(notifyFilesystem);
                    }
                })
                .show();
    }

    private void shareCat() {
        Intent share = ImgurUtil.shareCat(catView.getDrawingCache());
        startActivity(share);
    }

    private void wallpaperCat(Bitmap catPicture) {
        //TODO scale wallpaper more intelligently - maybe even let user choose scale types
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            wallpaperManager.setBitmap(catPicture);
            catView.destroyDrawingCache();
            Toast.makeText(this, "Wallpaper applied :)", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getValidIndex(boolean stepBack) {
        CURRENT_INDEX = !stepBack ? ++CURRENT_INDEX : --CURRENT_INDEX;

        CURRENT_INDEX = CURRENT_INDEX % savedCats.size();
        if (CURRENT_INDEX < 0) {
            CURRENT_INDEX = savedCats.size() + CURRENT_INDEX;
        }

        return CURRENT_INDEX;
    }

    @Override
    public void onBackPressed() {
        HomeActivity.FROM_OLD_CATS = true;
        exitActivityAnimation();
    }

    private void enterActivityAnimation() {
        Animations.contract(this, downCircle);
    }

    private void exitActivityAnimation() {
        Animations.expand(this, downCircle, new Animations.PostAnimAction() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_old_cat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        catView.buildDrawingCache();
        Bitmap catPicture = catView.getDrawingCache();

        switch (item.getItemId()) {
            case R.id.old_cat_wallpaper:
                wallpaperCat(catPicture);
                break;
            case R.id.old_cat_share:
                shareCat();
                break;
            case R.id.old_cat_delete:
                deleteCat(catPicture);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                Log.wtf(LOGTAG, "not one of our menuitems - id = " + item.getItemId());
                break;
        }

        catView.destroyDrawingCache();
        return true;
    }
}
