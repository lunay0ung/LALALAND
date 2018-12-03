package com.example.luna.lalaland.All.Broadcaster;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.luna.lalaland.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by LUNA on 2018-09-27.
 */

public class BroadcastAdapter extends RecyclerView.Adapter<BroadcastAdapter.MyViewHolder> {

    private Context mCtx;
    //방송목록을 저장할 리스트
    private List<Broadcast> broadcastList;
    int resource;
    private BroadcastAdapterListener broadcastAdapterListener; //인터페이스

    //원래 있떤 것
    public BroadcastAdapter(Context mCtx, List<Broadcast> broadcastList, BroadcastAdapterListener broadcastAdapterListener) {
       this.mCtx = mCtx;
       this.broadcastList = broadcastList;
       this.broadcastAdapterListener = broadcastAdapterListener;
    }//BroadcastAdapter

    //혹시 몰라서 만듦 (액티비티에서 resource부분에 레이아웃을 직접 pick해서 넣어주도록 하는 생성자)
//    public BroadcastAdapter(Context mCtx, int resource, List<Broadcast> broadcastList){
//        this.mCtx = mCtx;
//        this.resource = resource;
//        this.broadcastList = broadcastList;
//    }//

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.broadcast_list, parent, false);
        //여기서 inflater.inflate(R.layaout.broadcast_list, null); 로 하면 뷰가 제대로 보이지 않음(한쪽으로 쏠려서 일부밖에 안보임ㅠㅠ)...이것때문에 2시간 정도 씀 ㅎ
        return new MyViewHolder(view);
    }//MyViewHolder

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //아이템의 specific position을 가져옴
        Broadcast broadcast = broadcastList.get(position);

        //데이터를 뷰홀더의 뷰에 매치해줌
        holder.tv_title.setText(broadcast.getTitle());
        holder.tv_streamer.setText(broadcast.getStreamer());
        //holder.iv_thumbnail.setImageDrawable(mCtx.getResources().getDrawable(broadcast.getThumbnail()));


        String thumbnailUrl = broadcast.getThumbnail(); //썸네일 주소
        String defaultImage = "http://13.124.23.131/lalaland/thumbnail/crowd.jpg";



        //라이브 방송 중일 때만 라이브 마크를 붙인다
        if(broadcast.getIsLive()){
            holder.iv_liveMark.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.live_256));
            holder.tv_viewsOrViewers.setText("시청자수 ");
            holder.tv_data.setText(broadcast.getViewers()+"");
            holder.tv_measure.setText("명");
            holder.iv_thumbnail.setBackgroundColor(mCtx.getResources().getColor(R.color.mylightPurple)); //아직 썸네일 없으니 임시로 로딩
            holder.tv_genre.setText("#"+broadcast.getGenre());
            holder.tv_date.setText(broadcast.getStarted_at());
        } else {
            holder.iv_liveMark.setVisibility(View.GONE);
            holder.tv_viewsOrViewers.setText("조회수 ");
            holder.tv_data.setText(broadcast.getViews()+"");
            holder.tv_measure.setText("회");
            holder.tv_genre.setText("#"+broadcast.getGenre());
            Log.d("썸네일 주소", thumbnailUrl);
            Glide.with(mCtx)        //썸네일 지정
                    .load(thumbnailUrl)
                    .into(holder.iv_thumbnail);

            holder.tv_date.setText(broadcast.getStarted_at());
        }


    }//onBindViewHolder

    public void setBroadcastList(Context mCtx, final List<Broadcast> broadcastList){
        this.mCtx = mCtx;
        if(this.broadcastList == null) {
            this.broadcastList = broadcastList;
        } else {
            final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return BroadcastAdapter.this.broadcastList.size();
                }

                @Override
                public int getNewListSize() {
                    return broadcastList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return BroadcastAdapter.this.broadcastList.get(oldItemPosition)
                            == broadcastList.get(newItemPosition);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Broadcast broadcast = BroadcastAdapter.this.broadcastList.get(oldItemPosition);
                    return true;
                }
            });//callback
            this.broadcastList = broadcastList;
            result.dispatchUpdatesTo(this);
        }//else
    }//setBroadcastList

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }//getItemCount

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_thumbnail, iv_liveMark;
        TextView tv_title;
        TextView tv_streamer;
        TextView tv_genre;
        TextView tv_viewsOrViewers, tv_measure, tv_data;
        TextView tv_date; //방송 시작 시간

        public MyViewHolder(View itemView){
            super(itemView);

            iv_thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            iv_liveMark = itemView.findViewById(R.id.iv_liveMark); //라이브 마크
            tv_streamer = itemView.findViewById(R.id.tv_streamer);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_genre = itemView.findViewById(R.id.tv_genre);
            tv_viewsOrViewers = itemView.findViewById(R.id.tv_viewsOrViewers); //라이브-> '시청자수' , vod -> '조회수'
            tv_measure = itemView.findViewById(R.id.tv_measure); //라이브 -> '명' , vod -> '회'
            tv_data = itemView.findViewById(R.id.tv_data);
            tv_date = itemView.findViewById(R.id.tv_date);



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   broadcastAdapterListener.onBroadcastSelected(broadcastList.get(getAdapterPosition()));
                }//onClick
            });//setOnClickListener

        }//

    }//MyViewHolder class

    //broadcast 클래스 정보 공유 인터페이스
    public interface BroadcastAdapterListener {
        void onBroadcastSelected(Broadcast broadcast);
    }

    public void removeItem(int position) {
        broadcastList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }//removeItem
}//BroadcastAdapter
