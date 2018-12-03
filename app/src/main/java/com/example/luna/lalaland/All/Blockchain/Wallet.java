package com.example.luna.lalaland.All.Blockchain;

import android.os.Environment;
import android.util.Log;

import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by LUNA on 2018-11-01.
 */
/*
* 지갑을 생성하거나
* infura node와 연결, 지갑 잔액 정보를 가져올 수 있는 클래스
* 실제 트랜잭션은 contract 클래스에서 담당한다
*
* */
public class Wallet {

    String fileName;
    String path;
    String TAG = "지갑 클래스";

    private static Credentials credentials;
    private static Web3j web3j;
    private static TransactionReceipt transactionReceipt;

    public String contractAddress = "0x81473Ade1A5bf7443176343Cb5041Df441a53857";
    /*
    * end-client 노드와 연결 / infura API 사용
    * */
    public static boolean web3Connection() throws IOException {
        web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/9b6fb40f30bf480eb9299b103561cf53"));
        return  web3j != null;
    }


    /**
     * 지갑의 잔액을 가져옴
     * @throws InterruptedException
     * @throws ExecutionException
     * @return
     */
    public static BigInteger getBalance(String address){
        Log.d("지갑 클래스", "잔액 가져오기");
        Future<EthGetBalance> ethGetBalanceFuture = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync();
        try {
            return ethGetBalanceFuture.get().getBalance();
        }catch (Exception e){
            return BigInteger.ONE;
        }
    }


    //지갑이 없다면 지갑을 생성
    public String createWallet(String password) throws Exception {
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
        fileName = WalletUtils.generateLightNewWalletFile(password, new File(path));
        return path +"/"+ fileName;
    }

    //지갑이 있다면 비밀번호를 통해 정보를 불러옴
    public Credentials loadCredentials(String password, String fileName) throws Exception {
         credentials = WalletUtils.loadCredentials(
                password, "/storage/emulated/0/Download/"+fileName);
        Log.d(TAG+"Loading credentials", "Credentials loaded");
        return credentials;
    }

    public Web3j constructWeb3(String URL) throws IOException {
        Web3j web3 = Web3jFactory.build(new HttpService(URL));  // defaults to http://localhost:8545/
        Web3ClientVersion web3ClientVersion;
        web3ClientVersion = web3.web3ClientVersion().send();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        Log.i("Web3 verison", clientVersion);
        return web3;
    }

    public static String getWalletAddress(){
        return credentials.getAddress();
    }




    public String createBipWallet() throws Exception {
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
        Bip39Wallet bip39Wallet = WalletUtils.generateBip39Wallet("password", new File(path));
        String filename = bip39Wallet.getFilename();
        String mnemonic = bip39Wallet.getMnemonic();
        return "Success";
    }

    //TODO 지갑 정보가 등록돼있는지 아닌지 확인
    public void checkWalletExist() {

    }

}//Wallet
