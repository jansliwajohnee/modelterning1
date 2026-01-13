package ioioi.it.mltraining.utils;

/**
 * @author jan.sliwa777@gmail.com
 * @since 12/01/2026
 */

public class ObjectUtils {

    public static Double nvl(Double value) {
        return value != null ? value : 0.0;
    }

    public static Integer nvl(Integer value) {
        return value != null ? value : 0;
    }
}
