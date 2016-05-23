package bk2suz.spendtrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
        if (mTagRecord == null) {
            mSpendingRecord = (SpendingRecord) intent.getParcelableExtra(SPENDING_RECORD);
            mTagRecord = TagRecord.getById(mSpendingRecord.getTagId());
            ((EditText)findViewById(R.id.edt_purpose)).setText(mSpendingRecord.getPurpose());
            ((EditText)findViewById(R.id.edt_amount)).setText(mSpendingRecord.getAmountString());
            ((DateView)findViewById(R.id.date_view)).setDate(mSpendingRecord.getDate());
        }
        ((TextView) findViewById(R.id.text_view_tag)).setText(mTagRecord.getName());

        Button btnSave = (Button) findViewById(R.id.button_save);
        Button btnCancel = (Button) findViewById(R.id.button_cancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String purpose = ((EditText)findViewById(R.id.edt_purpose)).getText().toString();
                String amount = ((EditText)findViewById(R.id.edt_amount)).getText().toString();
                Date date = ((DateView)findViewById(R.id.date_view)).getDate();
                float floatAmount;
                try {
                    floatAmount = Float.parseFloat(amount);
                } catch (NumberFormatException e) {
                    return;
                }
                ((Button) v).setEnabled(false);

                if (mSpendingRecord == null) {
                    SpendingRecord.addNew(mTagRecord, date, purpose, floatAmount);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mSpendingRecord.update(date, purpose, floatAmount);
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
