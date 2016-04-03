package com.hugh.katiecats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.hugh.katiecats.util.Animations;
import com.hugh.katiecats.util.DoubleButton;

public class HomeActivity extends Activity {

    private final static String LOGTAG = "Home Activity";

    public static boolean FROM_NEW_CATS, FROM_OLD_CATS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (toolbar != null) {
            setActionBar(toolbar);
        }

        DoubleButton doubleButton = (DoubleButton) findViewById(R.id.double_button);
        doubleButton.setOnClickListener(new DoubleButton.OnClickListener() {
            @Override
            public void clickUp() {
                Intent newCats = new Intent(HomeActivity.this, NewCatsActivity.class);
                exitActivityAnimation(R.id.up_circle, newCats);
            }

            @Override
            public void clickDown() {
                Intent oldCats = new Intent(HomeActivity.this, OldCatsActivity.class);
                exitActivityAnimation(R.id.down_circle, oldCats);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (FROM_OLD_CATS) {
            enterActivityAnimation(R.id.down_circle);
            FROM_OLD_CATS = false;
        }
        else if (FROM_NEW_CATS) {
            enterActivityAnimation(R.id.up_circle);
            FROM_NEW_CATS = false;
        }
    }

    private void enterActivityAnimation(int whichCircle) {
        View circle = initializeCircle(whichCircle);
        Animations.contract(this, circle);
    }

    private View initializeCircle(int resourceId) {
        ImageView expandingCircle = (ImageView) findViewById(resourceId);
        expandingCircle.bringToFront();
        return expandingCircle;
    }

    private void exitActivityAnimation(int whichCircle, final Intent newActivity) {
        View circle = initializeCircle(whichCircle);
        Animations.expand(this, circle, new Animations.PostAnimAction() {
            @Override
            public void execute() {
                seamlessTransition(newActivity);
            }
        });
    }

    private void seamlessTransition(Intent newActivity) {
        startActivity(newActivity);
        overridePendingTransition(0, 0);
    }
}
