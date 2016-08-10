package brianbridge.gmail.com.rxautoupdatefeedcomments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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
	private Double loadedPage = 0.0;
	private List<String> fetchBuffer = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		intervalIndicatorTextView = (TextView) findViewById(R.id.textView_intervalIndicator);
		loadedPageTextView = (TextView) findViewById(R.id.textView_loadedPage);
		listView = (ListView) findViewById(R.id.listView);
		loadNextPageButton = (Button) findViewById(R.id.btn_loadNextPage);
		addNewItemButton = (Button) findViewById(R.id.btn_addNewItem);

		loadNextPageButton.setOnClickListener(this);
		addNewItemButton.setOnClickListener(this);

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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_addNewItem:
				ApiService.addNewItem();
				break;
			case R.id.btn_loadNextPage:
				if (autoUpdateSubscription != null && !autoUpdateSubscription.isUnsubscribed()) {
					autoUpdateSubscription.unsubscribe();
					intervalIndicatorTextView.setText("Auto Update Stopped");
				}
				int pageToLoad = loadedPage.intValue() + 1;
				Log.d(TAG, "Page to load: " + pageToLoad);
				ApiService.fetchComment(pageToLoad)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(new Subscriber<List<String>>() {
							@Override
							public void onCompleted() {
								Log.d(TAG, "onCompleted");
								startAutoUpdate();
							}

							@Override
							public void onError(Throwable e) {
								Log.e(TAG, e.toString());
							}

							@Override
							public void onNext(List<String> strings) {
								Log.d(TAG, "Next Page: " + strings.toString());

								if (strings.size() < ApiService.PAGE_SIZE) {
									loadNextPageButton.setEnabled(false);
									loadNextPageButton.setText("End of list");
								}

								listAdapter.addAll(strings);
								loadedPage = (double) listAdapter.getCount() / ApiService.PAGE_SIZE;
								loadedPageTextView.setText(String.format(loadedPageFormat, loadedPage));
							}
						});
				break;
			default:
				Log.w(TAG, "unhandled");
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
						Log.d(TAG, "Get comments: " + strings.toString());
						if (listAdapter.getCount() == 0) {
							listAdapter.addAll(strings);
							loadedPage = 1.0;
							loadedPageTextView.setText(String.format(loadedPageFormat, loadedPage));
						} else {
							if (listAdapter.containsAny(strings)) {
								listAdapter.addAll(strings);
								loadedPage = (double) listAdapter.getCount() / ApiService.PAGE_SIZE;
								loadedPageTextView.setText(String.format(loadedPageFormat, loadedPage));
							} else {
								unsubscribe();
								fetchBuffer.addAll(strings);
								fetchUntilExistCommentFound(loadedPage.intValue() + 1);
							}
						}
					}
				});
	}

	private void fetchUntilExistCommentFound(final int page) {
		ApiService.fetchComment(page)
				.subscribe(new Subscriber<List<String>>() {
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
						Log.d(TAG, "Get Comments: " + fetchBuffer.toString());
						if (strings.isEmpty()) {
							fetchBuffer.clear();
							startAutoUpdate();
						} else {
							fetchBuffer.addAll(strings);
							if (!listAdapter.containsAny(fetchBuffer)) {
								fetchUntilExistCommentFound(page + 1);
							} else {
								listAdapter.addAll(fetchBuffer);
								loadedPage = (double) listAdapter.getCount() / ApiService.PAGE_SIZE;
								loadedPageTextView.setText(String.format(loadedPageFormat, loadedPage));
								fetchBuffer.clear();
								startAutoUpdate();
							}
						}
					}
				});
	}
}
