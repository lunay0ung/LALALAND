package com.example.luna.lalaland.All.Chat;

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
 * Created by LUNA on 2018-10-03.
 * 동영상 스트리밍 시 보여줄 유저 간 채팅내용을 위한 리사이클러뷰 어댑터
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder>{

    private Context mCtx;
    //채팅내용을 보여줄 리스트
    private List<ChatItem> chatItemList;
    private ChatAdapterListner chatAdapterListner; //인터페이스

    public ChatAdapter(Context mCtx, List<ChatItem> chatItemList){
       this.mCtx = mCtx;
       this.chatItemList = chatItemList;
    }//

    public ChatAdapter(Context mCtx, List<ChatItem> chatItemList, ChatAdapterListner chatAdapterListner){
        //액티비티에서 직접 리스트 정보를 가져오는 인터페이스를 사용하려면 생성자에 포함시켜줘야 함
        //그리고 액티비티에서 아래왜 같이 implements해줘야
        //implements BroadcastAdapter.BroadcastAdapterListener
        this.mCtx = mCtx;
        this.chatItemList = chatItemList;
        this.chatAdapterListner = chatAdapterListner;
    }//ChatAdapter

    //새로운 뷰홀더 생성
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
        View view = layoutInflater.inflate(R.layout.chat_layout, parent, false);

        return new MyViewHolder(view);
    }//onCreateViewHolder

    //view의 내용을 해당 포지션의 데이터로 바꿈
    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.MyViewHolder holder, int position) {
        //아이템의 specific postion을 가져옴
        ChatItem chatItem = chatItemList.get(position);

        //데이터를 뷰홀더의 뷰에 매치
        //프로필 사진의 경우 아직 구현을 하지 않았으므로 기본 이미지를 넣어둠
        holder.iv_profile.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_account_circle));
        holder.tv_username.setText(chatItem.getUsername());
        holder.tv_message.setText(chatItem.getMessage());
    }//onBindViewHolder

    //데이터셋의 크기 리턴
    @Override
    public int getItemCount() {
        return chatItemList.size();
    }//getItemCount

    //chat_layout에 존재하는 위젯을 바인딩
    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_profile;
        TextView tv_username, tv_message;

        public MyViewHolder(View itemView) {
            super(itemView);

            iv_profile = itemView.findViewById(R.id.iv_profile);
            tv_username = itemView.findViewById(R.id.tv_username);
            tv_message = itemView.findViewById(R.id.tv_message);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatAdapterListner.onChatItemSelected(chatItemList.get(getAdapterPosition()));
                }//onClick
            });//setOnClickListener
        }//MyViewHolder
    }//MyViewHolder class

    public interface ChatAdapterListner { //챗아이템클래스 공유 인터페이스
        void onChatItemSelected(ChatItem chatItem);
    }//ChatAdapterListner

    //채팅내용 삭제 기능은 없을 예정 --> removeItem 메소드는 구현X
}//ChatAdapter
