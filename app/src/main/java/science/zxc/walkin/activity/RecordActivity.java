package science.zxc.walkin.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;


import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import science.zxc.walkin.R;
import science.zxc.walkin.activity.adapter.RecordAdapter;
import science.zxc.walkin.db.Record;

public class RecordActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.record_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        final List<Record> records = DataSupport.findAll(Record.class);
        RecordAdapter recordAdapter = new RecordAdapter(records);
        recyclerView.setAdapter(recordAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration());//设置分割线
        //条目点击
        recordAdapter.setOnItemClickListener(new RecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(RecordActivity.this,DetailActivity.class);
                intent.putExtra("record_data",records.get(position));//传递Record
                startActivity(intent);
            }
        });

    }
    private static class DividerItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //设置高度为1的间距
            outRect.set(0,0,0,20);
    }
    }
}
