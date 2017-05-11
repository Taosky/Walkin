package science.zxc.walkin.activity.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import science.zxc.walkin.R;
import science.zxc.walkin.db.Record;

/**
 * AUTH: Taosky
 * TIME: 2017/5/3 0003:下午 2:12.
 * MAIL: t@firefoxcn.net
 * DESC:
 */

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<Record> myRecordList;
    private OnItemClickListener mOnItemClickListener = null;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView time;
        TextView id;

        public ViewHolder(View itemView) {
            super(itemView);
            time = (TextView)itemView.findViewById(R.id.record_time);
            id = (TextView)itemView.findViewById(R.id.record_num);

        }
    }

    public RecordAdapter(List<Record> recordList){
        myRecordList = recordList;
    }

    //创建ViewHolder实例
    @Override
    public RecordAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    //注意这里使用getTag方法获取position
                    mOnItemClickListener.onItemClick(view, (int) view.getTag());
                }
            }
        });
        return holder;
    }

    //对recyclerView子项进行赋值
    @Override
    public void onBindViewHolder(RecordAdapter.ViewHolder holder, int position) {
        Record record = myRecordList.get(position);
        holder.time.setText(record.getDatetime());
        holder.id.setText(String.valueOf(record.getId()));
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return myRecordList.size();
    }



    //点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
