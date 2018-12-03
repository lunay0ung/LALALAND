package com.example.luna.lalaland.All.Blockchain;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luna.lalaland.R;

import java.util.List;

/**
 * Created by LUNA on 2018-10-29.
 */

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.MyViewHolder>{

    private Context mCtx;
    private List<TransactionHistory> txList;
    private TxAdapterListner txAdapterListner;

    public TransactionHistoryAdapter(Context mCtx, List<TransactionHistory> txList, TxAdapterListner txAdapterListner){
        this.mCtx = mCtx;
        this.txList = txList;
        this.txAdapterListner = txAdapterListner;
    }//TransactionHistoryAdapter

    @NonNull
    @Override
    public TransactionHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
        View view = layoutInflater.inflate(R.layout.token_transfer_list, parent, false);

        return new MyViewHolder(view);
    }//onCreateViewHolder

    @Override
    public void onBindViewHolder(@NonNull TransactionHistoryAdapter.MyViewHolder holder, int position) {
        //아이템의 specific position을 가져옴
        TransactionHistory txHistory = txList.get(position);

        holder.iv_sent.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.up_grey32));
        //holder.iv_received.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.down_purple32));
        holder.tv_amount.setText(String.valueOf(txHistory.getValue()));
        holder.tv_time.setText(String.valueOf(txHistory.getTime()));
        holder.tv_counterpart.setText(txHistory.getTo());

        if(txHistory.getValue().intValue() <= 1) {
            holder.tv_lnc.setText("LUT");
        } else {
            holder.tv_lnc.setText("LUTs");
        }

    }//onBindViewHolder

    @Override
    public int getItemCount() {
        return txList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_sent, iv_received;
        TextView tv_counterpart, tv_amount, tv_time, tv_lnc;

        public MyViewHolder(View itemView) {
           super(itemView);

            iv_sent = itemView.findViewById(R.id.iv_sent);
            iv_received = itemView.findViewById(R.id.iv_received);
            tv_counterpart = itemView.findViewById(R.id.tv_counterpart);
            tv_amount = itemView.findViewById(R.id.tv_amount);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_lnc = itemView.findViewById(R.id.tv_lnc);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txAdapterListner.onTxSelected(txList.get(getAdapterPosition()));
                }//onClick
            });

        }//MyViewHolder
    }//class MyViewHolder

    public interface TxAdapterListner {
        void onTxSelected(TransactionHistory transactionHistory);
    }//TxAdapterListner

}//TransactionHistoryAdapter
