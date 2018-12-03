package com.example.luna.lalaland.All.Blockchain;

import android.util.Log;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import rx.Subscription;


/*
* 트랜잭션을
* */

public class Contract {

    private static final String TAG = "컨트랙트";



    private Web3j web3j;
    private Credentials credentials;
    public TransactionManager transactionManager;
    public TransactionReceipt transactionReceipt;
    Subscription subscription;
    public BigInteger gasPrice = BigInteger.valueOf(3000); //성공률을 높이려면 좀더 높게 잡아도 좋음
    public BigInteger gasLimit = BigInteger.valueOf(5000000);
    String tokenName;
    String tokenSymbol;
    public String contractAddress = "0x81473Ade1A5bf7443176343Cb5041Df441a53857";
    String toAddress, fromAddress, log;
    BigInteger value;


    public Contract(Web3j web3j, Credentials credentials) {
        this.web3j = web3j;
        this.credentials = credentials;
    }

    public BigInteger decimals() throws Exception {
        LunToken contract = LunToken.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
        Log.d(TAG, "컨트랙트 로드 / decimals");
        return contract.decimals().sendAsync().get();
    }

    /*컨트랙트 로드*/
    public void load() throws Exception {

        LunToken contract = LunToken.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
        Log.d(TAG, "컨트랙트 로드 / load");
    }


    /*owner가 가진 토큰 금액 확인*/
    public BigInteger balanceOf(String owner) throws Exception {

        LunToken contract = LunToken.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
        Log.d(TAG, "컨트랙트 로드 / balanceOf");
        return contract.balanceOf(owner).sendAsync().get();

    }


    /*송금*/
    public TransactionReceipt transfer(String _to, BigInteger _value)  throws Exception {
        LunToken contract = LunToken.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
        Log.d(TAG, "컨트랙트 로드 / transfer");
        return contract.transfer(_to, _value).sendAsync().get();
    }//transfer


    /*송금--이걸로 송금하는 거 아니라는 걸 기억해두기 위해 삭제 대신 주석처리*/
//    public TransactionReceipt transferFrom(String _from, String _to, BigInteger _value)  throws Exception {
//        LunToken contract = LunToken.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
//        Log.d(TAG, "컨트랙트 로드 / transfer");
//        return contract.transferFrom(_from, _to, _value).sendAsync().get();
//    }//transfer


    //반환되는 decimal에 맞춰 잔액을 format함
    public BigDecimal formatBalance(BigInteger balance, int decimal) {
        BigDecimal result = new BigDecimal(balance);
        result = result.divide(BigDecimal.TEN.pow(decimal));
        return result;
    }

    //decimal에 맞춰 잔액을 reformat함
    public BigDecimal reformatBalance(BigInteger balance, int decimal) {
        BigDecimal result = new BigDecimal(balance);
        result = result.multiply(BigDecimal.TEN.pow(decimal));
        return result;
    }

}//contract
