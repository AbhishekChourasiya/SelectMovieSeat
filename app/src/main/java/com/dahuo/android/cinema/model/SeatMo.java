package com.dahuo.android.cinema.model;

/**
 * @author YanLu
 */
public class SeatMo extends BaseMo {

    /**
     * seat full name
     */
    public String seatName;
    /**
     * row Name
     */
	public String rowName;
    /**
     * row index
     */
	public int row;
    /**
     * column index
     */
	public int column;

    /**
     * seat status:1：available，0：sold，-1：unavailable
     */
	public int status;


}
