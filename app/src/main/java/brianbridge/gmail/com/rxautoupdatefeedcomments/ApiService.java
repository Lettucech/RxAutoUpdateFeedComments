package brianbridge.gmail.com.rxautoupdatefeedcomments;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;

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
		int start = page < 2 ? 0 : (page - 1) * PAGE_SIZE;
		int end = start + PAGE_SIZE;
		List<String> loadedComment = new ArrayList<>();

		if (start <= DATA.size()) {
			if (end > DATA.size()) {
				end = DATA.size();
			}

			for (int i = start; i < end; i++) {
				loadedComment.add(DATA.get(i));
			}
		}
		return Observable.just(loadedComment);
	}

	public static void addNewItem() {
		DATA.add(0, String.valueOf(DATA.size() + 1));
	}
}
