package brianbridge.gmail.com.rxautoupdatefeedcomments;

public class MathUtil {
	public static int roundUpToInteger(double value) {
		if (value % 1 == 0) {
			return Double.valueOf(value).intValue();
		}
		return Double.valueOf(value).intValue() + 1;
	}
}
