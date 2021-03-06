package aaa.bbb.ccc10;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;

import com.daimajia.androidanimations.library.Techniques;

import wail.splacher.com.splasher.lib.SplasherActivity;
import wail.splacher.com.splasher.models.SplasherConfig;
import wail.splacher.com.splasher.utils.Const;

public class SplashScreen extends SplasherActivity {
    @Override
    public void initSplasher(SplasherConfig splasherConfig) {
        splasherConfig.setReveal_start(Const.START_CENTER) // anitmation type ..
                //---------------
                .setAnimationDuration(3000) // Reveal animation duration ..
                //---------------
                .setLogo(R.drawable.wheel) // logo src..
                .setLogo_animation(Techniques.BounceIn) // logo animation ..
                .setAnimationLogoDuration(2000) // logo animation duration ..
                .setLogoWidth(500) // logo width ..
                //---------------
                .setSubtitle("Рулетка") // subtitle
                .setSubtitleColor(Color.parseColor("#03c70d")) // subtitle color
                .setSubtitleAnimation(Techniques.FadeIn) // subtitle animation (from Android View Animations) ..
                .setSubtitleSize(32) // subtitle text size ..
                //---------------
                .setSubtitleTypeFace(Typeface.createFromAsset(getAssets(), "fonts/Eccentric.ttf")); // subtitle font type ..
    }

    @Override
    public void onSplasherFinished() {
        Intent startMenu = new Intent(this, GameActivity.class);
        startActivity(startMenu);
        finish();
    }
}
