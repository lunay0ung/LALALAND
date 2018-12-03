package com.example.luna.lalaland.All.IdeaNote;

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
 * Created by LUNA on 2018-10-25.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder>{

    private Context mCtx;
    private List<NoteItem> noteItemList;
    private NoteAdapterListner noteAdapterListner;



    public NoteAdapter(Context mCtx, List<NoteItem> chatItemList, NoteAdapterListner noteAdapterListner){
        //액티비티에서 직접 리스트 정보를 가져오는 인터페이스를 사용하려면 생성자에 포함시켜줘야 함
        //그리고 액티비티에서 아래왜 같이 implements해줘야
        //implements BroadcastAdapter.BroadcastAdapterListener
        this.mCtx = mCtx;
        this.noteItemList = chatItemList;
        this.noteAdapterListner = noteAdapterListner;
    }//ChatAdapter

    //새로운 뷰홀더 생성
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
        View view = layoutInflater.inflate(R.layout.note_list, parent, false);

        return new MyViewHolder(view);
    }//onCreateViewHolder

    //view의 내용을 해당 포지션의 데이터로 바꿈
    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.MyViewHolder holder, int position) {
        //아이템의 specific postion을 가져옴
        NoteItem noteItem = noteItemList.get(position);


        //데이터를 뷰홀더의 뷰에 매치
        holder.iv_fakeBtn.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.simple_play_btn_24px));
        holder.tv_title.setText(noteItem.getTitle());
        holder.tv_content.setText(noteItem.getContent());

        //fakeBtn 이미지의 경우 녹음파일이 있는 경우에만 보임
        if(!noteItem.isVoiceNote) {
            holder.iv_fakeBtn.setVisibility(View.GONE);
        }

        if(noteItem.isVoiceNote){
            holder.iv_fakeBtn.setVisibility(View.VISIBLE);
        }

    }//onBindViewHolder

    //데이터셋의 크기 리턴
    @Override
    public int getItemCount() {
        return noteItemList.size();
    }//getItemCount

    //chat_layout에 존재하는 위젯을 바인딩
    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_title, tv_content;
        ImageView iv_fakeBtn;


        public MyViewHolder(View itemView) {
            super(itemView);

            iv_fakeBtn = itemView.findViewById(R.id.iv_fakeBtn);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_content = itemView.findViewById(R.id.tv_content);



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noteAdapterListner.onNoteItemSelected(noteItemList.get(getAdapterPosition()));
                }//onClick
            });//setOnClickListener
        }//MyViewHolder
    }//MyViewHolder class

    public interface NoteAdapterListner { //챗아이템클래스 공유 인터페이스
        void onNoteItemSelected(NoteItem noteItem);
    }//ChatAdapterListner

    public void addItem (String title, String content)
    {
         NoteItem noteItem = new NoteItem(title, content, false);
        noteItemList.add(noteItem);
    }

    public void addItem(String fileName, String title, String content)
    {
        NoteItem noteItem = new NoteItem(fileName, title, content, true);
        noteItemList.add(noteItem);
    }

    public void addItem(NoteItem noteItem)
    {
        noteItemList.add(noteItem);
    }
}//NoteAdapter
