package aaa.bbb.ccc10;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import libs.mjn.prettydialog.PrettyDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GameActivity extends AppCompatActivity {

    private static final int SLOTS_COUNT = 38;
    private static final String SCORES_EXTRA = "scores_extra";
    private static final String BET_INDEX_EXTRA = "bet_index_extra";
    private static final String BET_TYPE_EXTRA = "bet_type_extra";
    private int mScores;
    private int mBetIndex;
    private int[] mBets;
    private String[] mBetTypes;
    private int mBetType; //0-zero, 1-red, 2-black

    private Button mBetButton;
    private ImageButton mRedButton;
    private ImageButton mBlackButton;
    private Button mSpinButton;
    private TextView mScoresView;
    private TextView mBetView;
    private TextView mBetTypeView;
    private CardView mRouletteWheelView;
    //because there is 38 sectors on the wheel (9.47 degrees each)
    private static final float FACTOR = 4.7368f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mBetButton = findViewById(R.id.btn_bet);
        mRedButton = findViewById(R.id.btn_bet_red);
        mBlackButton = findViewById(R.id.btn_bet_black);
        mSpinButton = findViewById(R.id.btn_spin);
        mScoresView = findViewById(R.id.tv_scores);

        mBetView = findViewById(R.id.tv_bet);
        mBetTypeView = findViewById(R.id.tv_bet_type);
        mRouletteWheelView = findViewById(R.id.cv_wheel);

        mBets = getResources().getIntArray(R.array.bets_array);
        mBetTypes = new String[]{"Зеро", "Красное", "Черное"};
        mBetType = 1;
        if (savedInstanceState == null) {
            mScores = getResources().getInteger(R.integer.default_score);
            setScoreAmounts();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SCORES_EXTRA, mScores);
        outState.putInt(BET_INDEX_EXTRA, mBetIndex);
        outState.putInt(BET_TYPE_EXTRA, mBetType);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mScores = savedInstanceState.getInt(SCORES_EXTRA);
        mBetIndex = savedInstanceState.getInt(BET_INDEX_EXTRA);
        mBetType = savedInstanceState.getInt(BET_TYPE_EXTRA);
        setScoreAmounts();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onSpinClick(View view) {
        setButtonsEnabled(false);
        decreaseScores();
        spinRoulette();
    }

    public void updateBet(View view) {
        int size = mBets.length;
        mBetIndex = mBetIndex >= size - 1 ? 0 : mBetIndex + 1;
        if (mBets[mBetIndex] > mScores) mBetIndex = 0;
        setScoreAmounts();
    }

    public void setBetBlack(View view) {
        mBetType = 2;
        setScoreAmounts();
    }

    public void setBetRed(View view) {
        mBetType = 1;
        setScoreAmounts();
    }

    public void showRules(View view) {
        Intent intent = new Intent(this, RulesActivity.class);
        startActivity(intent);
    }

    private void setScoreAmounts() {
        mScoresView.setText(String.valueOf(mScores));
        mBetView.setText(String.valueOf(mBets[mBetIndex]));
        mBetTypeView.setText(String.valueOf(mBetTypes[mBetType]));
    }

    private void decreaseScores() {
        mScores -= mBets[mBetIndex];
        if (mBets[mBetIndex] > mScores) mBetIndex = 0;
        setScoreAmounts();
        if (mScores == 0) mSpinButton.setEnabled(false);
    }

    private void spinRoulette() {
        int corner = 360 / SLOTS_COUNT;
        final int randPosition = corner * new Random().nextInt(SLOTS_COUNT);
        int MIN = 5;
        int MAX = 9;
        long TIME_IN_WHEEL = 1000;
        int randRotation = MIN + new Random().nextInt(MAX - MIN);
        final int truePosition = randRotation * 360 + randPosition;
        long totalTime = TIME_IN_WHEEL * randRotation + (TIME_IN_WHEEL / 360) * randPosition;

        Log.d("ROULETTE_ACTION", "randPosition : " + randPosition
                + " randRotation : " + randRotation
                + " totalTime : " + totalTime
                + " truePosition : " + truePosition);

        ObjectAnimator animator = ObjectAnimator
                .ofFloat(mRouletteWheelView, "rotation", 0f, (float) truePosition);
        animator.setDuration(totalTime);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mRouletteWheelView.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                checkWin(360 - (truePosition % 360));
                mRouletteWheelView.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    private void checkWin(int degrees) {
        Result result = getResult(degrees);
        if (result.color == mBetType) {
            mScores += mBets[mBetIndex] * 2;
            setScoreAmounts();
        }

        if (mScores <= 0) {
            showScoresDialog();
            return;
        }

        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean isEnabled) {
        mBetButton.setEnabled(isEnabled);
        mRedButton.setEnabled(isEnabled);
        mBlackButton.setEnabled(isEnabled);
        mSpinButton.setEnabled(isEnabled);
    }

    private void showScoresDialog() {
        PrettyDialog dialog = new PrettyDialog(this)
                .setIcon(R.drawable.pdlg_icon_info)
                .setTitle(getString(R.string.scores_dialog_title))
                .setMessage(getString(R.string.scores_dialog_text))
                .addButton(
                        getString(R.string.play_btn_title),
                        R.color.pdlg_color_white,
                        R.color.pdlg_color_green,
                        () -> {
                            Intent intent = new Intent(GameActivity.this, GameActivity.class);
                            startActivity(intent);
                        }
                );
        dialog.setCancelable(false);
        dialog.show();
    }

    private static class Result {
        private int number;
        private int color;

        private Result(int number, int color) {
            this.number = number;
            this.color = color;
        }
    }

    private Result getResult(int degrees) {
        if (degrees >= (FACTOR * 1) && degrees < (FACTOR * 3)) {
            return new Result(27, 1);
        } else if (degrees >= (FACTOR * 3) && degrees < (FACTOR * 5)) {
            return new Result(10, 2);
        } else if (degrees >= (FACTOR * 5) && degrees < (FACTOR * 7)) {
            return new Result(35, 1);
        } else if (degrees >= (FACTOR * 7) && degrees < (FACTOR * 9)) {
            return new Result(29, 2);
        } else if (degrees >= (FACTOR * 9) && degrees < (FACTOR * 11)) {
            return new Result(12, 1);
        } else if (degrees >= (FACTOR * 11) && degrees < (FACTOR * 13)) {
            return new Result(8, 2);
        } else if (degrees >= (FACTOR * 13) && degrees < (FACTOR * 15)) {
            return new Result(19, 1);
        } else if (degrees >= (FACTOR * 15) && degrees < (FACTOR * 17)) {
            return new Result(31, 2);
        } else if (degrees >= (FACTOR * 17) && degrees < (FACTOR * 19)) {
            return new Result(18, 1);
        } else if (degrees >= (FACTOR * 19) && degrees < (FACTOR * 21)) {
            return new Result(6, 2);
        } else if (degrees >= (FACTOR * 21) && degrees < (FACTOR * 23)) {
            return new Result(21, 1);
        } else if (degrees >= (FACTOR * 23) && degrees < (FACTOR * 25)) {
            return new Result(33, 2);
        } else if (degrees >= (FACTOR * 25) && degrees < (FACTOR * 27)) {
            return new Result(16, 1);
        } else if (degrees >= (FACTOR * 27) && degrees < (FACTOR * 29)) {
            return new Result(4, 2);
        } else if (degrees >= (FACTOR * 29) && degrees < (FACTOR * 31)) {
            return new Result(23, 1);
        } else if (degrees >= (FACTOR * 31) && degrees < (FACTOR * 33)) {
            return new Result(35, 2);
        } else if (degrees >= (FACTOR * 33) && degrees < (FACTOR * 35)) {
            return new Result(14, 1);
        } else if (degrees >= (FACTOR * 35) && degrees < (FACTOR * 37)) {
            return new Result(2, 2);
        } else if (degrees >= (FACTOR * 37) && degrees < (FACTOR * 39)) {
            return new Result(0, 0);
        } else if (degrees >= (FACTOR * 39) && degrees < (FACTOR * 41)) {
            return new Result(28, 2);
        } else if (degrees >= (FACTOR * 41) && degrees < (FACTOR * 43)) {
            return new Result(9, 1);
        } else if (degrees >= (FACTOR * 43) && degrees < (FACTOR * 45)) {
            return new Result(26, 2);
        } else if (degrees >= (FACTOR * 45) && degrees < (FACTOR * 47)) {
            return new Result(30, 1);
        } else if (degrees >= (FACTOR * 47) && degrees < (FACTOR * 49)) {
            return new Result(11, 2);
        } else if (degrees >= (FACTOR * 49) && degrees < (FACTOR * 51)) {
            return new Result(7, 1);
        } else if (degrees >= (FACTOR * 51) && degrees < (FACTOR * 53)) {
            return new Result(20, 2);
        } else if (degrees >= (FACTOR * 53) && degrees < (FACTOR * 55)) {
            return new Result(32, 1);
        } else if (degrees >= (FACTOR * 55) && degrees < (FACTOR * 57)) {
            return new Result(17, 2);
        } else if (degrees >= (FACTOR * 57) && degrees < (FACTOR * 59)) {
            return new Result(5, 1);
        } else if (degrees >= (FACTOR * 59) && degrees < (FACTOR * 61)) {
            return new Result(22, 2);
        } else if (degrees >= (FACTOR * 61) && degrees < (FACTOR * 63)) {
            return new Result(34, 1);
        } else if (degrees >= (FACTOR * 63) && degrees < (FACTOR * 65)) {
            return new Result(15, 2);
        } else if (degrees >= (FACTOR * 65) && degrees < (FACTOR * 67)) {
            return new Result(3, 1);
        } else if (degrees >= (FACTOR * 67) && degrees < (FACTOR * 69)) {
            return new Result(24, 2);
        } else if (degrees >= (FACTOR * 69) && degrees < (FACTOR * 71)) {
            return new Result(36, 1);
        } else if (degrees >= (FACTOR * 71) && degrees < (FACTOR * 73)) {
            return new Result(13, 2);
        } else if (degrees >= (FACTOR * 73) && degrees < (FACTOR * 75)) {
            return new Result(1, 1);
        } else {
            return new Result(0, 0);
        }
    }
}
