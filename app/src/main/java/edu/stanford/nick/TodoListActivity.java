package edu.stanford.nick;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

// Main activity -- shows data list, has a few controls.
public class TodoListActivity extends ListActivity {
	private TodoDB mDB;  // Our connection to the database.
	private SimpleCursorAdapter mCursorAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDetail(0, true);  // true = create new
			}
		});
		
        // Start up DB connection (closed in onDestroy).
        mDB = new TodoDB(this);
        mDB.open();

        // Get the "all rows" cursor. startManagingCursor() is built in for the common case,
        // takes care of closing etc. the cursor.
		Cursor cursor = mDB.queryAll();
		startManagingCursor(cursor);

		// Adapter: maps cursor keys, to R.id.XXX fields in the row layout.
		String[] from = new String[] { TodoDB.KEY_TITLE, TodoDB.KEY_STATE };
		int[] to = new int[] { R.id.rowtext, R.id.rowtext2 };
		mCursorAdapter = new SimpleCursorAdapter(this, R.layout.row2, cursor, from, to);
		
		// Map "state" int to text in the row -- intercept the setup of each row view,
		// fiddle with the data for the state column.
		mCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == TodoDB.INDEX_STATE) {
					TextView textView = (TextView) view;
					if (cursor.getInt(TodoDB.INDEX_STATE) > 0) {
						textView.setText(" (done) ");
					}
					else {
						textView.setText("");
					}
					return true;  // i.e. we handled it
			    }
			    return false;  // i.e. the system should handle it
			}
			});
		
		// Alternative: also have row.xml layout with just one text field. No ViewBinder
		// needed for that simpler approach.

		setListAdapter(mCursorAdapter);
		registerForContextMenu(getListView());

		// Placing a clickable control inside a list is nontrivial unfortunately.
		// see bug: http://code.google.com/p/android/issues/detail?id=3414
		
    }
    
    // Placing this next to onCreate(), help to remember to mDB.close().
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDB.close();
	}
    
    // Create menu when the select the menu button.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pref_menu, menu);
        return true;
    }
    
    // Called for menu item select. Return true if we handled it.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.prefs:
        		// open prefs, previous lecture
        		return true;
        	
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    
    
    // Create context menu for click-hold in list.
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_menu, menu);
	}
    
    // Context menu item-select.
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.menu_detail:
				startDetail(info.id, false);
				return true;
				
			case R.id.menu_delete:
				remove(info.id);
				return true;				
			default:
				return super.onContextItemSelected(item);
		}
	}
    

    // Removes the given rowId from the database, updates the UI.
    public void remove(long rowId) {
		mDB.deleteRow(rowId);
		//mCursorAdapter.notifyDataSetChanged();  // confusingly, this does not work
		mCursorAdapter.getCursor().requery();  // need this
    }

	
	public static final String EXTRA_ROWID = "rowid";
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long rowId) {
		super.onListItemClick(l, v, position, rowId);
		startDetail(rowId, false);
	}
	
	// Starts the detail activity, either edit existing or create new.
	public void startDetail(long rowId, boolean create) {
		Intent intent = new Intent(this, DetailActivity.class);
		// Our convention: add rowId to edit existing. To create add nothing.
		if (!create) {
			intent.putExtra(EXTRA_ROWID, rowId);
		}
		startActivity(intent);
		// Easy bug: remember to add to add a manifest entry for the detail activity
	}
}


/*
Customizing how the data goes into each list/row (use with row2 layout)

		mCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == TodoDB.INDEX_STATE) {
					TextView tv = (TextView) view;
					if (cursor.getInt(TodoDB.INDEX_STATE) > 0) {
						tv.setText(" (done) ");
					}
					else {
						tv.setText("");
					}
					return true;
			    }
			    return false;
			}
			});
*/
