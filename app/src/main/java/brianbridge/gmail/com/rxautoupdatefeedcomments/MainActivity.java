package brianbridge.gmail.com.rxautoupdatefeedcomments;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = MainActivity.class.getSimpleName();
	public static final int UPDATE_INTERVAL_IN_SECOND = 5;

	private TextView intervalIndicatorTextView;
	private TextView loadedPageTextView;
	private ListView listView;
	private Button loadNextPageButton;
	private Button addNewItemButton;
	private Subscription autoUpdateSubscription;
	private CommentListAdapter listAdapter;
	private String intervalIndicatorStringFormat;
	private String loadedPageFormat;
	private int loadedPage = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		intervalIndicatorTextView = (TextView) findViewById(R.id.textView_intervalIndicator);
		loadedPageTextView = (TextView) findViewById(R.id.textView_loadedPage);
		listView = (ListView) findViewById(R.id.listView);
		loadNextPageButton = (Button) findViewById(R.id.btn_loadNextPage);
		addNewItemButton = (Button) findViewById(R.id.btn_addNewItem);

		intervalIndicatorStringFormat = getString(R.string.intervalIndicatorFormat);
		loadedPageFormat = getString(R.string.loadedPageFormat);

		listAdapter = new CommentListAdapter(this, android.R.layout.simple_list_item_1);
		listView.setAdapter(listAdapter);
		startAutoUpdate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (autoUpdateSubscription != null && !autoUpdateSubscription.isUnsubscribed()) {
			autoUpdateSubscription.unsubscribe();
		}
	}

	private void startAutoUpdate() {
		autoUpdateSubscription = Observable.interval(0, 1, TimeUnit.SECONDS, Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.filter(new Func1<Long, Boolean>() {
					@Override
					public Boolean call(Long aLong) {
						int reminder = (int) ((aLong + 1) % UPDATE_INTERVAL_IN_SECOND);
						intervalIndicatorTextView.setText(String.format(intervalIndicatorStringFormat, UPDATE_INTERVAL_IN_SECOND - reminder));
						return reminder == 0;
					}
				})
				.observeOn(Schedulers.io())
				.flatMap(new Func1<Long, Observable<List<String>>>() {
					@Override
					public Observable<List<String>> call(Long aLong) {
						return ApiService.fetchComment(1);
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<List<String>>() {
					@Override
					public void onStart() {
						Log.d(TAG, "onStart");
					}

					@Override
					public void onCompleted() {
						Log.d(TAG, "onCompleted");
					}

					@Override
					public void onError(Throwable e) {
						Log.e(TAG, e.toString());
					}

					@Override
					public void onNext(List<String> strings) {
						Collections.reverse(strings);
						listAdapter.addAll(strings);
						listView.smoothScrollToPosition(listAdapter.getCount());
						loadedPage = (int) Math.ceil(listAdapter.getCount() / ApiService.PAGE_SIZE);
						loadedPageTextView.setText(String.format(load));
					}
				});
	}
}
