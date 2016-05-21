package com.dahuo.android.cinema;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.dahuo.android.cinema.model.SeatMo;

import static android.graphics.Bitmap.createScaledBitmap;

/**
 * @author YanLu
 */
public class SeatTableView extends View {
    Bitmap seat_sale, seat_sold, seat_selected;
    Bitmap SeatSale, SeatSold, SeatSelected;

    private int seatWidth;// seat size : width == height
    private int defWidth;// default value

    // scale
    public float mScaleFactor = 1.f;
    public float mPosX = .0f;
    public float mPosY = .0f;


    private SeatMo[][] seatTable;
    private int rowSize;
    private int columnSize;

    private Paint linePaint;// the center line

    int width;
    int height;

    public SeatTableView(Context context) {
        super(context, null);

    }

    public SeatTableView(Context context, AttributeSet attr) {
        super(context, attr);

        SeatSale = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.seat_sale);
        SeatSold = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.seat_sold);
        SeatSelected = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.seat_selected);

        setVerticalScrollBarEnabled(true);
        setHorizontalScrollBarEnabled(true);

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw visible seat
        if(defWidth < 10) {
            throw new IllegalArgumentException("the width must > 10, the value is " + defWidth);
        }
        seatWidth = (int) (defWidth * mScaleFactor);
        width = getMeasuredWidth();
        height = getMeasuredHeight();


        // sale
        if (seat_sale == null) {
            seat_sale = createScaledBitmap(SeatSale, seatWidth, seatWidth, true);
        }
        // sold
        if (seat_sold == null) {
            seat_sold = createScaledBitmap(SeatSold, seatWidth, seatWidth, true);
        }
        // selected
        if (seat_sold == null) {
            seat_sold = createScaledBitmap(SeatSelected, seatWidth, seatWidth, true);
        }

        // draw begin
        int m = (int)(mPosY + seatWidth);
        m = m >= 0 ? 0 : -m / seatWidth;
        int n = Math.min(rowSize - 1, m + (height / seatWidth) + 2);//两边多显示1列,避免临界的突然消失的现象
        for (int i = m; i <= n; i++) {
            //
            if (linePaint != null) {
                canvas.drawLine((columnSize * seatWidth) / 2 + mPosX, i * (seatWidth) + mPosY,
                        (columnSize * seatWidth) / 2 + mPosX, i * (seatWidth) + seatWidth + mPosY, linePaint);
            }
            int k = (int)(mPosX + seatWidth + 0.5f);
            k = k > 0 ? 0 : -k / seatWidth;
            int l = Math.min(columnSize - 1, k + (width / seatWidth) + 2);// draw +2 column
            for (int j = k; j <= l; j++) {

                if (seatTable[i][j] != null) {
                    switch (seatTable[i][j].status) {
                        case -1:
                        case 0: {
                            canvas.drawBitmap(seat_sold, j * (seatWidth) + mPosX, i * (seatWidth) + mPosY, null);
                            break;
                        }
                        case 1: {
                            canvas.drawBitmap(seat_sale, j * (seatWidth) + mPosX, i * (seatWidth) + mPosY, null);
                            break;
                        }
                        case 2: {
                            canvas.drawBitmap(seat_selected, j * (seatWidth) + mPosX, i * (seatWidth) + mPosY, null);
                            break;
                        }
                        default: {
                            break;
                        }

                    }

                }
            }
        }

    }



    public void setSeatTable(SeatMo[][] seatTable) {
        this.seatTable = seatTable;
    }

    public int getRowSize() {
        return rowSize;
    }

    public void setRowSize(int rowSize) {
        this.rowSize = rowSize;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }


    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }

    public void setDefWidth(int defWidth) {
        this.defWidth = defWidth;
    }

    public int getSeatWidth() {
        return seatWidth;
    }

}
