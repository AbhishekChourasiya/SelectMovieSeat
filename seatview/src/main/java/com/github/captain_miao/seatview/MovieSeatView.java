package com.github.captain_miao.seatview;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * @author YanLu
 * @since 16/10/01
 */
public class MovieSeatView extends View {
    private static final String TAG = "MovieSeatView";
    public final boolean isDebug = true;

    private int mIconOnSaleResId;
    private int mIconSoldResId;
    private int mIconSelectedResId;

    private float mSeatWidth;
    private float mSeatHeight;
    private boolean mShowOverview;



    private Bitmap mIconOnSale;
    private Bitmap mIconSold;
    private Bitmap mIconSelected;


    private Paint mPaint = new Paint();


    private BaseSeatMo[][] mSeatTable;
    private Matrix mMatrix = new Matrix();
    private Matrix mDrawMatrix = new Matrix();
    float[] mMatrixValues = new float[9];

    private float mScaleX;
    private float mScaleY;
    private float mZoom;

    private SeatPresenter mPresenter;

    private Canvas mCanvas;
    public MovieSeatView(Context context) {
        super(context);
    }

    public MovieSeatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovieSeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSeatView(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MovieSeatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initSeatView(context, attrs);
    }

    private void initSeatView(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MovieSeatView);
        if(typedArray.hasValue(R.styleable.MovieSeatView_iconOnSale)
                && typedArray.hasValue(R.styleable.MovieSeatView_iconSold)
                && typedArray.hasValue(R.styleable.MovieSeatView_iconSelected)){
            mIconOnSaleResId = typedArray.getResourceId(R.styleable.MovieSeatView_iconOnSale, 0);
            mIconSoldResId = typedArray.getResourceId(R.styleable.MovieSeatView_iconSold, 0);
            mIconSelectedResId = typedArray.getResourceId(R.styleable.MovieSeatView_iconSelected, 0);

            float seatPadding = typedArray.getDimension(R.styleable.MovieSeatView_seatPadding, 0);
            mSeatWidth = typedArray.getDimension(R.styleable.MovieSeatView_seatWidth,
                    getResources().getDimension(R.dimen.default_seat_width)) + seatPadding;
            mSeatHeight = typedArray.getDimension(R.styleable.MovieSeatView_seatHeight,
                    getResources().getDimension(R.dimen.default_seat_height)) + seatPadding;
            mShowOverview = typedArray.getBoolean(R.styleable.MovieSeatView_showOverView, true);

            typedArray.recycle();
        } else {
            typedArray.recycle();
            throw new RuntimeException("must has iconSeatOnSale, iconSeatSold and iconSeatSelected");
        }

    }



    @Override
    protected void onDraw(Canvas canvas) {
        if (mSeatTable != null && mSeatTable.length > 0) {
            drawSeat(canvas);
        }


    }

    private void drawOneSeat(Canvas canvas, int row , int column) {

        if (mIconOnSale == null) {
            mIconOnSale = BitmapFactory.decodeResource(getResources(), mIconOnSaleResId);
            mScaleX = mSeatWidth / mIconOnSale.getWidth();
            mScaleY = mSeatHeight / mIconOnSale.getHeight();
        }
        if (mIconSold == null) {
            mIconSold = BitmapFactory.decodeResource(getResources(), mIconSoldResId);
        }
        if (mIconSelected == null) {
            mIconSelected = BitmapFactory.decodeResource(getResources(), mIconSelectedResId);
        }

        mZoom = getMatrixValue(Matrix.MSCALE_X);
        float translateX = getMatrixValue(Matrix.MTRANS_X);
        float translateY = getMatrixValue(Matrix.MTRANS_Y);
        float scaleX = mZoom;
        float scaleY = mZoom;
        float top = row * mSeatHeight * scaleY + translateY;


        float left = row * mSeatWidth * scaleX + translateX;


        mDrawMatrix.setTranslate(left, top);
        mDrawMatrix.postScale(mScaleX, mScaleY, left, top);
        mDrawMatrix.postScale(scaleX, scaleY, left, top);

        BaseSeatMo seat = mSeatTable[row][column];
        if (seat != null) {
            if (seat.isOnSale()) {
                canvas.drawBitmap(mIconOnSale, mDrawMatrix, mPaint);
            } else if (seat.isSold()) {
                canvas.drawBitmap(mIconSold, mDrawMatrix, mPaint);
            } else if (seat.isSelected()) {
                canvas.drawBitmap(mIconSelected, mDrawMatrix, mPaint);
            } else {
                Log.d(TAG, "It's skip " + seat.getSeatName());
            }
        }


    }

    private void drawSeat(Canvas canvas) {
        long startTime = System.currentTimeMillis();

        if(mIconOnSale == null){
            mIconOnSale = BitmapFactory.decodeResource(getResources(), mIconOnSaleResId);
            mScaleX = mSeatWidth / mIconOnSale.getWidth();
            mScaleY = mSeatHeight / mIconOnSale.getHeight();
        }
        if(mIconSold == null){
            mIconSold = BitmapFactory.decodeResource(getResources(), mIconSoldResId);
        }
        if(mIconSelected == null){
            mIconSelected = BitmapFactory.decodeResource(getResources(), mIconSelectedResId);
        }

        mZoom = getMatrixValue(Matrix.MSCALE_X);
        float translateX = getMatrixValue(Matrix.MTRANS_X);
        float translateY = getMatrixValue(Matrix.MTRANS_Y);
        float scaleX = mZoom;
        float scaleY = mZoom;
        int row = mSeatTable.length;
        int column = mSeatTable[0].length;
        for (int i = 0; i < row; i++) {
            float top = i * mSeatHeight * scaleY+ translateY;

            float bottom = top + mSeatHeight * scaleY;
            // ?
            if (bottom < 0 || top > getHeight()) {
                continue;
            }

            for (int j = 0; j < column; j++) {
                float left = j * mSeatWidth * scaleX + translateX;

                float right = (left + mSeatWidth * scaleY);
                if (right < 0 || left > getWidth()) {
                    continue;
                }

                mDrawMatrix.setTranslate(left, top);
                mDrawMatrix.postScale(mScaleX, mScaleY, left, top);
                mDrawMatrix.postScale(scaleX, scaleY, left, top);

                BaseSeatMo seat = mSeatTable[i][j];
                if(seat != null) {
                    if (seat.isOnSale()) {
                        canvas.drawBitmap(mIconOnSale, mDrawMatrix, mPaint);
                    } else if (seat.isSold()) {
                        canvas.drawBitmap(mIconSold, mDrawMatrix, mPaint);
                    } else if (seat.isSelected()) {
                        canvas.drawBitmap(mIconSelected, mDrawMatrix, mPaint);
                    } else {
                        Log.d(TAG, "It's skip " + seat.getSeatName());
                    }
                }


            }
        }

        if (isDebug) {
            long drawTime = System.currentTimeMillis() - startTime;
            Log.d(TAG, "draw seat time(ms):" + drawTime);
        }
    }


    public void setSeatTable(BaseSeatMo[][] seatTable) {
        this.mSeatTable = seatTable;
        invalidate();
    }

    private int downX, downY;
    private boolean pointer;
    int lastX;
    int lastY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        int x = (int) event.getX();
        super.onTouchEvent(event);

        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            pointer = true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointer = false;
                downX = x;
                downY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mScaling) {
                    int downDX = Math.abs(x - downX);
                    int downDY = Math.abs(y - downY);
                    if ((downDX > 10 || downDY > 10) && !pointer) {
                        int dx = x - lastX;
                        int dy = y - lastY;
                        mMatrix.postTranslate(dx, dy);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                autoScale();

                int downDX = Math.abs(x - downX);
                int downDY = Math.abs(y - downY);
                if ((downDX > 10 || downDY > 10) && !pointer) {
                    //autoScroll();
                }

                break;
        }
        lastY = y;
        lastX = x;

        return true;
    }



    private float getMatrixValue(int matrixType) {
        mMatrix.getValues(mMatrixValues);
        return mMatrixValues[matrixType];
    }

    public SeatPresenter getPresenter() {
        return mPresenter;
    }

    public void setPresenter(SeatPresenter presenter) {
        mPresenter = presenter;
    }

    private boolean mScaling;
    private boolean mFirstScale;
    float scaleX, scaleY;
    ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(getContext(),
            new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaling = true;
            float scaleFactor = detector.getScaleFactor();
            if (getMatrixValue(Matrix.MSCALE_Y) * scaleFactor > 3) {
                scaleFactor = 3 / getMatrixValue(Matrix.MSCALE_Y);
            }
            if (mFirstScale) {
                scaleX = detector.getCurrentSpanX();
                scaleY = detector.getCurrentSpanY();
                mFirstScale = false;
            }

            if (getMatrixValue(Matrix.MSCALE_Y) * scaleFactor < 0.5) {
                scaleFactor = 0.5f / getMatrixValue(Matrix.MSCALE_Y);
            }
            mMatrix.postScale(scaleFactor, scaleFactor, scaleX, scaleY);
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mScaling = false;
            mFirstScale = true;
        }
    });

    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            onClickSeat(e);
            return super.onSingleTapConfirmed(e);
        }
    });



    boolean isNeedDrawSeatBitmap = true;

    boolean isDrawOverviewBitmap = false;
    private boolean onClickSeat(MotionEvent e) {
        float x = e.getX() - getMatrixValue(Matrix.MTRANS_X);
        float y = e.getY() - getMatrixValue(Matrix.MTRANS_Y);
        float rowNum = mSeatTable.length;
        float columnNum = mSeatTable[0].length;
        float w = (mSeatWidth) * getMatrixValue(Matrix.MSCALE_X);
        float h = (mSeatHeight) * getMatrixValue(Matrix.MSCALE_Y);
        int positionRow = (int)(y / h);
        int positionColumn = (int)( x / w);
        int row = positionRow <= rowNum ? positionRow: -1;
        int column = positionColumn <= columnNum ? positionColumn: -1;
        if(row >= 0 && column >= 0) {
            BaseSeatMo seat = mSeatTable[row][column];
            if (seat != null) {
                if (mPresenter != null && mPresenter.onClickSeat(row, column, seat)) {
                    isNeedDrawSeatBitmap = true;
                    isDrawOverviewBitmap = true;
                    float currentScaleY = getMatrixValue(Matrix.MSCALE_Y);

                    if (currentScaleY < 1.7) {
                        scaleX = e.getX();
                        scaleY = e.getY();
                        zoomAnimate(currentScaleY, 1.9f);
                    }
                    //drawOneSeat(null, row, column);
                    //invalidate();
                }
            }
        }

        return true;
    }

    private void autoScale() {

        if (getMatrixValue(Matrix.MSCALE_X) > 2.2) {
            zoomAnimate(getMatrixValue(Matrix.MSCALE_X), 2.0f);
        } else if (getMatrixValue(Matrix.MSCALE_X) < 0.98) {
            zoomAnimate(getMatrixValue(Matrix.MSCALE_X), 1.0f);
        }
    }

    private void zoom(float zoom) {
        float z = zoom / getMatrixValue(Matrix.MSCALE_X);
        mMatrix.postScale(z, z, scaleX, scaleY);
        invalidate();
    }

    private void zoomAnimate(float cur, float tar) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(cur, tar);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        ZoomAnimation zoomAnim = new ZoomAnimation();
        valueAnimator.addUpdateListener(zoomAnim);
        valueAnimator.addListener(zoomAnim);
        valueAnimator.setDuration(400);
        valueAnimator.start();
    }

    class ZoomAnimation extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mZoom = (Float) animation.getAnimatedValue();
            zoom(mZoom);
        }

    }
}
