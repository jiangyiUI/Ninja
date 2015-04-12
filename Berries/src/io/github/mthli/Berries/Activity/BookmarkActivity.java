package io.github.mthli.Berries.Activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import io.github.mthli.Berries.Database.Record;
import io.github.mthli.Berries.Database.RecordAction;
import io.github.mthli.Berries.R;
import io.github.mthli.Berries.Unit.IntentUnit;
import io.github.mthli.Berries.Unit.ViewUnit;
import io.github.mthli.Berries.View.DialogAdapter;
import io.github.mthli.Berries.View.ListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkActivity extends Activity {
    private ListView listView;
    private ListAdapter adapter;
    private List<Record> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(
                    getResources().getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher),
                    getResources().getColor(R.color.blue_500)
            );
            setTaskDescription(description);
            getActionBar().setElevation(ViewUnit.dp2px(this, 2));
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);

        RecordAction action = new RecordAction(this);
        action.open(false);
        list = action.listBookmarks();
        action.close();

        listView = (ListView) findViewById(R.id.list);
        listView.setEmptyView(findViewById(R.id.list_empty));

        adapter = new ListAdapter(this, R.layout.list_item, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Record record = list.get(position);
                Intent intent = new Intent();
                intent.putExtra(IntentUnit.TITLE, record.getTitle());
                intent.putExtra(IntentUnit.URL, record.getURL());
                intent.putExtra(IntentUnit.INCOGNITO, false);
                intent.putExtra(IntentUnit.RESTORE, false);
                setResult(IntentUnit.RESULT_BOOKMARK, intent);
                finish();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                showListMenu(position);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bookmark_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        RecordAction action = new RecordAction(this);
        action.open(true);

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.bookmark_menu_search:
                break;
            case R.id.bookmark_menu_import:
                break;
            case R.id.bookmark_menu_backup:
                break;
            case R.id.bookmark_menu_clear:
                action.clearBookmarks();
                list.clear();
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }

        action.close();
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO
    }

    private void showListMenu(final int location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog, null, false);
        builder.setView(linearLayout);

        String[] strings = getResources().getStringArray(R.array.list_menu);
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(strings));

        ListView listView = (ListView) linearLayout.findViewById(R.id.dialog_listview);
        DialogAdapter adapter = new DialogAdapter(this, R.layout.dialog_item, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        final AlertDialog dialog = builder.create();
        dialog.show();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                RecordAction action = new RecordAction(BookmarkActivity.this);
                action.open(true);
                Record record = BookmarkActivity.this.list.get(location);

                switch (position) {
                    case 0:
                        Toast.makeText(BookmarkActivity.this, R.string.list_toast_open_in_new_tab_successful, Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(BookmarkActivity.this, R.string.list_toast_open_in_incognito_tab_successful, Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        IntentUnit.share(BookmarkActivity.this, record.getTitle(), record.getURL());
                        break;
                    case 3:
                        action.deleteHistory(record);
                        BookmarkActivity.this.list.remove(location);
                        BookmarkActivity.this.adapter.notifyDataSetChanged();
                        Toast.makeText(BookmarkActivity.this, R.string.list_toast_delete_successful, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

                action.close();
                dialog.hide();
                dialog.dismiss();
            }
        });
    }
}