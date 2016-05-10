package bk2suz.spendtrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

/**
 * Created by sujoy on 10/5/16.
 */
public class SpendingActivity extends AppCompatActivity {
    public static final String TAG_RECORD = "tag_record";
    public static final String SPENDING_RECORD = "spending_record";

    private SpendingRecord mSpendingRecord = null;
    private TagRecord mTagRecord = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spending);


        Intent intent = getIntent();
        mTagRecord = (TagRecord) intent.getParcelableExtra(TAG_RECORD);

        Button btnSave = (Button) findViewById(R.id.button_save);
        Button btnCancel = (Button) findViewById(R.id.button_cancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpendingRecord == null) {
                    Date date = ((DateView)findViewById(R.id.date_view)).getDate();
                    String purpose = ((EditText)findViewById(R.id.edt_purpose)).getText().toString();
                    String amount = ((EditText)findViewById(R.id.edt_amount)).getText().toString();
                    SpendingRecord.addNew(mTagRecord, date, purpose, Float.parseFloat(amount));
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
