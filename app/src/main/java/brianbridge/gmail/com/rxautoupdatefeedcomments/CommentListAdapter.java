package brianbridge.gmail.com.rxautoupdatefeedcomments;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class CommentListAdapter extends ArrayAdapter<String> {
	public static final String TAG = CommentListAdapter.class.getSimpleName();
	private List<String> data = new ArrayList<>();

	public CommentListAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	public String getItem(int position) {
		return data.get(position);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public void add(String object) {
		if (!data.contains(object)) {
			data.add(object);
		}
		notifyDataSetChanged();
	}

	@Override
	public void addAll(Collection<? extends String> collection) {
		Observable.from(collection)
				.filter(new Func1<String, Boolean>() {
					@Override
					public Boolean call(String s) {
						return !data.contains(s);
					}
				})
				.subscribe(new Subscriber<String>() {
					@Override
					public void onCompleted() {
						Collections.sort(data, new Comparator<String>() {
							@Override
							public int compare(String s, String t1) {
								return Integer.valueOf(t1).compareTo(Integer.valueOf(s));
							}
						});
						Collections.reverse(data);
						notifyDataSetChanged();
					}

					@Override
					public void onError(Throwable e) {
						Log.e(TAG, e.toString());
					}

					@Override
					public void onNext(String s) {
						data.add(s);
					}
				});
	}

	public boolean containsAny(Collection<? extends String> collection) {
		return !Collections.disjoint(data, collection);
	}
}
