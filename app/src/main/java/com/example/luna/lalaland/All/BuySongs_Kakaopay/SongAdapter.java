package com.example.luna.lalaland.All.BuySongs_Kakaopay;

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
 * Created by LUNA on 2018-10-23.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder>{


    private Context mCtx;
    private List<SongItem> songItemList;
    private SongAdapterListner songAdapterListner; //인터페이스용

    public SongAdapter(Context mCtx, List<SongItem> songItemList, SongAdapterListner songAdapterListner) {
        this.mCtx = mCtx;
        this.songItemList = songItemList;
        this.songAdapterListner = songAdapterListner;
    }//SongAdapter


    @Override
    public SongAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
        View view = layoutInflater.inflate(R.layout.song_list, parent, false);

        return new SongAdapter.MyViewHolder(view);
    }//onCreateViewHolder


    @Override
    public void onBindViewHolder(@NonNull SongAdapter.MyViewHolder holder, int position) {
        //아이템의 specific position을 가져옴
        SongItem songItem = songItemList.get(position);

        holder.iv_albumImage.setImageDrawable(mCtx.getResources().getDrawable(songItem.getAlbumImage()));
        holder.iv_wonMark.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.won_64));
        holder.tv_title.setText(songItem.getTitle());
        holder.tv_artist.setText(songItem.getArtist());
        holder.tv_price.setText(songItem.getPrice()+"");

    }//onBindViewHolder

    @Override
    public int getItemCount() {
        return songItemList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_albumImage, iv_wonMark;
        TextView tv_title, tv_artist, tv_price;

        public MyViewHolder(View itemView) {
            super(itemView);

            iv_albumImage = itemView.findViewById(R.id.iv_albumImage);
            iv_wonMark = itemView.findViewById(R.id.iv_wonMark);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_artist = itemView.findViewById(R.id.tv_artist);
            tv_price = itemView.findViewById(R.id.tv_price);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    songAdapterListner.onSongSelected(songItemList.get(getAdapterPosition()));
                }//onClick
            });//OnClickListener

        }//MyViewHolder
    }//MyViewHolder

    //songItem 클래스 정보 공유 인터페이스
    public interface SongAdapterListner{
        void onSongSelected(SongItem songItem);
    }//SongAdapterListner

    public void removeItem(int position){
        songItemList.remove(position);
        notifyItemRemoved(position);
    }//removeItem

}//SongAdapter
