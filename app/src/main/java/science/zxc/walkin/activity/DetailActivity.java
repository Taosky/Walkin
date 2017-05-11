package science.zxc.walkin.activity;


import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import science.zxc.walkin.R;
import science.zxc.walkin.db.Record;

import science.zxc.walkin.util.BitmapUtil;

public class DetailActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        TextView detialSteps = (TextView) findViewById(R.id.detail_steps);
        TextView detialDistances = (TextView) findViewById(R.id.detail_distances);
        ImageView detailImage = (ImageView) findViewById(R.id.detail_image);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Record record = (Record) getIntent().getSerializableExtra("record_data");
        detialSteps.setText(record.getSteps());
        detialDistances.setText(record.getDistance());
        detailImage.setImageBitmap(BitmapUtil.getBitmapFromByte(record.getImage()));

    }


}
