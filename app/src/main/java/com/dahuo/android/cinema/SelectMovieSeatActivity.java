package com.dahuo.android.cinema;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.dahuo.android.cinema.model.SeatMo;
import com.github.captain_miao.seatview.BaseSeatMo;
import com.github.captain_miao.seatview.MovieSeatView;
import com.github.captain_miao.seatview.SeatPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author YanLu
 */
public class SelectMovieSeatActivity extends Activity implements SeatPresenter {
    private static final String TAG = "SelectMovieSeat";
    private static final int MAX_SEATS = 5;

    MovieSeatView mMovieSeatView;
    private SeatMo[][] seatTable;

    public List<SeatMo> selectedSeats;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        initSeatTable();
        selectedSeats = new ArrayList<>();
        mMovieSeatView = (MovieSeatView) findViewById(R.id.seat_view);
        mMovieSeatView.setSeatTable(seatTable);
        mMovieSeatView.setPresenter(this);
	}


    private int maxRow = 10;
    private int maxColumn = 12;
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

    @Override
    public boolean onClickSeat(int row, int column, BaseSeatMo seat) {
        SeatMo seatMo = seatTable[row][column];

        if(seatMo != null){
            if (seatMo.isOnSale()) {
                if (selectedSeats.size() < MAX_SEATS) {
                    seatMo.setSelected();
                    selectedSeats.add(seatMo);
                    return true;
                } else {
                    Toast.makeText(this, getString(R.string.seat_max_number, MAX_SEATS), Toast.LENGTH_SHORT).show();
                }

            } else if (seatMo.isSelected()) {
                seatMo.setOnSale();
                selectedSeats.remove(seatMo);
                return true;
            }
        }
        return false;
    }
}