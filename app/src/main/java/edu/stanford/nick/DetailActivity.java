package edu.stanford.nick;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

// Shows/edits the data for one row.
public class DetailActivity extends Activity {
	private TodoDB mDB;
	private Long mRowId;
	
	private EditText mEditText1;
	private EditText mEditText2;
	private CheckBox mCheckBox;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		setContentView(R.layout.detail);
		
		mEditText1 = (EditText) findViewById(R.id.editText1);
		mEditText2 = (EditText) findViewById(R.id.editText2);
		mCheckBox = (CheckBox) findViewById(R.id.checkBox1);

		mRowId = null;

		if (bundle == null) {  // initially, Intent -> extras -> rowID
			Bundle extras = getIntent().getExtras();
			if (extras != null && extras.containsKey(TodoListActivity.EXTRA_ROWID)) {
				mRowId = extras.getLong(TodoListActivity.EXTRA_ROWID);
			}
		}
		else {  // tricky: recover mRowId from kill destroy/create cycle
			mRowId = bundle.getLong(SAVE_ROW);
		}
		
		mDB = new TodoDB(this);
		mDB.open();

		dbToUI();
		
		
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();  // same as "back" .. either way we get onPause() to save
			}
		});
		// todo: could ponder having a cancel button, where throw away data

	}
	
	// note: put this next to onCreate, to remember to balance things
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDB.close();
	}

	// Copies database state up to the UI.
	private void dbToUI() {
		if (mRowId != null) {
			Cursor cursor = mDB.query(mRowId);
			// Note: a cursor should be closed after use, or "managed".
			
			// Could use cursor.getColumnIndex(columnName) to look up 0, 1, ... index
			// for each column name. Here use INDEX_ consts from TodoDB.
			mEditText1.setText(cursor.getString(TodoDB.INDEX_TITLE));
			mEditText2.setText(cursor.getString(TodoDB.INDEX_BODY));
			mCheckBox.setChecked(cursor.getInt(TodoDB.INDEX_STATE) > 0);
			
			cursor.close();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		save();
	}


	/** Save the state in the UI to the database, creating a new row or updating
	 * an existing row.
	 */
	private void save() {
		String title = mEditText1.getText().toString();
		String body = mEditText2.getText().toString();
		int done = 0;
		if (mCheckBox.isChecked()) done = 1;

		// Not null = edit of existing row, or it's new but we saved it previously,
		// so now it has a rowId anyway.
		if (mRowId != null) {
			mDB.updateRow(mRowId, mDB.createContentValues(title, body, done));
		}
		else {
			mRowId = mDB.createRow(mDB.createContentValues(title, body, done));
		}
	}

	// Tricky: preserve mRowId var when this activity is killed.
	// Note that the UI state is all saved automatically, so we just have to
	// save mRowID. See code in onCreate() that matches this save.
	public static final String SAVE_ROW = "saverow";
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(SAVE_ROW, mRowId);
	}
}