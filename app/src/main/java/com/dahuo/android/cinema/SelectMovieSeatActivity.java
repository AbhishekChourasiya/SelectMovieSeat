package com.dahuo.android.cinema;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.almeros.android.multitouch.MoveGestureDetector;
import com.dahuo.android.cinema.model.SeatMo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.graphics.Bitmap.createScaledBitmap;

/**
 * @author YanLu
 */
public class SelectMovieSeatActivity extends Activity implements OnTouchListener {
    private final static String TAG = SelectMovieSeatActivity.class.getSimpleName();
    private Matrix mMatrix = new Matrix();
    private float mPreScaleFactor = 1.0f;
    private float mScaleFactor = 1.0f;
    private float mPreFocusX = 0.f;
    private float mFocusX = 0.f;
    private float mPreFocusY = 0.f;
    private float mFocusY = 0.f;

    private ScaleGestureDetector mScaleDetector;
    private MoveGestureDetector mMoveDetector;

    SeatTableView seatTableView;
    LinearLayout rowView;
    private SeatMo[][] seatTable;

    public List<SeatMo> selectedSeats;

    private int screenWidth;
    private int minLeft;
    private int minTop;
    private int defWidth;
    private AlphaAnimation alpha;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        Resources resources = getResources();
        screenWidth = resources.getDisplayMetrics().widthPixels;
        defWidth = resources.getDimensionPixelSize(R.dimen.padding_20dp);

        initSeatTable();
        selectedSeats = new ArrayList<SeatMo>();
        seatTableView = (SeatTableView) findViewById(R.id.seatviewcont);
        rowView = (LinearLayout) findViewById(R.id.seatraw);
        //设置透明度
        alpha = new AlphaAnimation(0.6F, 0.6F);
        alpha.setDuration(0); // Make animation instant
        alpha.setFillAfter(true); // Tell it to persist after the animation ends


        //居中线的画笔
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        seatTableView.setSeatTable(seatTable);
        seatTableView.setRowSize(maxRow);
        seatTableView.setColumnSize(maxColumn);
        seatTableView.setOnTouchListener(this);
        seatTableView.setLinePaint(paint);
        seatTableView.setDefWidth(defWidth);
        onChanged();

		// Setup Gesture Detectors
		mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
		mMoveDetector = new MoveGestureDetector(this, new MoveListener());
	}

    int[] oldClick = new int[2];
   	int[] newClick = new int[2];
    boolean eatClick = true;// when drag, ignore click

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                oldClick = getClickPoint(event);
                eatClick = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                eatClick = true;
                break;
            case MotionEvent.ACTION_UP:
                newClick = getClickPoint(event);
                int i = newClick[0];
                int j = newClick[1];
                if (!eatClick && i != -1 && j != -1 && i == oldClick[0] && j == oldClick[1] ) {
                    if (seatTable[i][j].status == 1) {
                        seatTable[i][j].status = 2;
                        selectedSeats.add(seatTable[i][j]);
                        Toast.makeText(this,seatTable[i][j].seatName, Toast.LENGTH_SHORT ).show();
                    } else {
                        seatTable[i][j].status = 1;
                        selectedSeats.remove(seatTable[i][j]);
                    }
                }
                break;
        }


        mScaleDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);
        float diffScal = Math.abs(mPreScaleFactor - mScaleFactor);
        float diffY = Math.abs(mPreFocusY - mFocusY);
        float diffX = Math.abs(mPreFocusX - mFocusX);
        //Log.i(TAG, "diffScal = " + diffScal + ", preSeatWidth = " + preSeatWidth + ", diffY ＝ " + diffY + ", diffX = " + diffX);
        if (!eatClick || diffY > 5 || diffX > 5 || diffScal > 0.01) {// avoid too many draw
            mMatrix.reset();
            mPreScaleFactor = mScaleFactor;
            mPreFocusY = mFocusY;
            mPreFocusX = mFocusX;
            //drag area
            minLeft = (int) (defWidth * mScaleFactor * maxColumn) - screenWidth;
            mFocusX = minLeft > 0 ?
                    Math.max(-minLeft, Math.min(mFocusX, defWidth * mScaleFactor))
                    : Math.max(0, Math.min(mFocusX, defWidth * mScaleFactor));
            minTop = (int) (defWidth * mScaleFactor * maxRow) - seatTableView.getMeasuredHeight();
            mFocusY = minTop > 0 ? Math.max(-minTop, Math.min(mFocusY, 0)) : 0;
            mMatrix.postScale(mScaleFactor, mScaleFactor);


            seatTableView.mScaleFactor = mScaleFactor;
            seatTableView.mPosX = mFocusX;
            seatTableView.mPosY = mFocusY;

            int seatWidth = (int) (defWidth * mScaleFactor);

            seatTableView.seat_sale = createScaledBitmap(seatTableView.SeatSale, seatWidth, seatWidth, true);
            seatTableView.seat_sold = createScaledBitmap(seatTableView.SeatSold, seatWidth, seatWidth, true);
            seatTableView.seat_selected = createScaledBitmap(seatTableView.SeatSelected, seatWidth, seatWidth, true);
            seatTableView.invalidate();
            onChanged();
        }

		return true;
	}

    //左侧的座位列号
    public void onChanged() {
        rowView.removeAllViews();
        rowView.setPadding(getResources().getDimensionPixelSize(R.dimen.padding_1dp),
                (int) (mFocusY), 0, 0);
        //rowView.setBackgroundColor(getResources().getColor(R.color.black));
//        rowView.startAnimation(alpha);
        for (int i = 0; i < seatTableView.getRowSize(); i++) {
            TextView textView = new TextView(SelectMovieSeatActivity.this);

            for (int j = 0; j < seatTableView.getColumnSize(); j++) {
                if (seatTable[i][j] != null) {
                    textView.setText(seatTable[i][j].rowName);
                    break;
                }
            }
            textView.setTextSize(8.0f * mScaleFactor);
            textView.setTextColor(Color.LTGRAY);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, (int)(defWidth * mScaleFactor)));
            textView.setPadding(getResources().getDimensionPixelSize(R.dimen.padding_2dp), 0,
                    getResources().getDimensionPixelSize(R.dimen.padding_2dp), 0);
            rowView.addView(textView);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor(); // scale change since previous event
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 6.0f));

			return true;
		}
	}

	
	private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
		@Override
		public boolean onMove(MoveGestureDetector detector) {
			PointF d = detector.getFocusDelta();
            eatClick = d.x > 1 || d.y > 1;
			mFocusX += d.x;
			mFocusY += d.y;

			return true;
		}
	}


    private int maxRow = 26;
    private int maxColumn = 60;
    private void initSeatTable() {
        seatTable = new SeatMo[maxRow][maxColumn];// mock data
        for (int i = 0; i < maxRow; i++) {
            for (int j = 0; j < maxColumn; j++) {
                SeatMo seat = new SeatMo();
                seat.row = i;
                seat.column = j;
                seat.rowName = String.valueOf((char)('A' + i));
                seat.seatName = seat.rowName + " Row" + (j + 1) + " Seat";
                seat.status = randInt(-2, 1);
                seatTable[i][j] = seat.status == -2 ? null : seat;
            }
        }
    }

    public  int randInt(int min, int max) {

        Random rand = new Random();

        return rand.nextInt((max - min) + 1) + min;
    }

    int[] noSeat = {-1, -1};
    //click position(x, y)
    public int[] getClickPoint(MotionEvent event) {
        float currentXPosition = event.getX() - mFocusX;
        float currentYPosition = event.getY() - mFocusY;
        float area = seatTableView.getSeatWidth();
        for (int i = 0; i < seatTableView.getRowSize(); i++) {
            for (int j = 0; j < seatTableView.getColumnSize(); j++) {
                if ((j * area) < currentXPosition
                        && currentXPosition < j * area + area
                        && (i * area) < currentYPosition
                        && currentYPosition < i * area + area
                        && seatTable[i][j] != null
                        && seatTable[i][j].status >= 1) {

                    return new int[]{i, j};
                }
            }
        }
        return noSeat;
    }

}