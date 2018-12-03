package com.example.luna.lalaland.All.Blockchain;

/*
* 이더스캔 api를 이용해서 트랜잭션 내역 중 from, to, value 값을 가져와 트랜잭션 히스토리 클래스 객체 생성
* */

import java.math.BigInteger;
import java.util.Date;

public class TransactionHistory {

    String from, to;
    BigInteger value;
    String time;

    public TransactionHistory(String from, String to, BigInteger value, String time){
       this.from = from;
       this.to = to;
       this.value = value;
       this.time = time;
    }//

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }
}//public class TransactionHistory {

