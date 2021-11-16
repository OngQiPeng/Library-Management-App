package com.example.librarymanagementapp;

import com.google.type.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

public class BorrowHistory implements Comparable<BorrowHistory>{
    String bookId;
    String bookName;
    String userId;
    String userName;
    String borrowDateTime;
    String returnDateTime;
    String status;

    public BorrowHistory(String bookId, String bookName, String userId, String userName, String borrowDateTime, String returnDateTime, String status) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.userId = userId;
        this.userName = userName;
        this.borrowDateTime = borrowDateTime;
        this.returnDateTime = returnDateTime;
        this.status = status;
    }

    public String getBookId() { return bookId; }

    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }

    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getBorrowDateTime() { return borrowDateTime; }

    public void setBorrowDateTime(String borrowDateTime) { this.borrowDateTime = borrowDateTime; }

    public String getReturnDateTime() { return returnDateTime; }

    public void setReturnDateTime(String returnDateTime) { this.returnDateTime = returnDateTime; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    @Override
    public int compareTo(BorrowHistory o)
    {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        try
        {
            return dateFormat.parse(o.getBorrowDateTime()).compareTo(dateFormat.parse(borrowDateTime));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return 0;
    }
}
