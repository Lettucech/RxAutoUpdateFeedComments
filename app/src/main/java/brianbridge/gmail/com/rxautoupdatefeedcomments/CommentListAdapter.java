package brianbridge.gmail.com.rxautoupdatefeedcomments;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
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
}
