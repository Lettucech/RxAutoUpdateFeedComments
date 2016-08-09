package brianbridge.gmail.com.rxautoupdatefeedcomments;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;

/**
 * This class is simulating Rx Retrofit2
 */
public class ApiService {
	public static final String TAG = ApiService.class.getSimpleName();
	public static final int PAGE_SIZE = 25;
	public static ArrayList<String> DATA = new ArrayList<>();

	static {
		for (int i = 1; i <= 100; i++) {
			DATA.add(String.valueOf(i));
		}
		Collections.reverse(DATA);
	}

	public static Observable<List<String>> fetchComment(final int page) {
		int start = page < 2 ? 0 : page * PAGE_SIZE;

		List<String> loadedComment = new ArrayList<>();
		for (int i = start; i < start + PAGE_SIZE; i++) {
			loadedComment.add(DATA.get(i));
		}

		Log.d(TAG, loadedComment.toString());
		return Observable.just(loadedComment);
	}
}
