package com.github.sahasbhop.apngview;

import android.support.annotation.NonNull;

import com.github.sahasbhop.flog.FLog;

import java.io.File;
import java.util.Arrays;

public class CacheManager {

    private static boolean VERBOSE = true;
    public static final long MAX_SIZE = 500000L; // ~500K

    private CacheManager() {

    }

    public static void checkCahceSize(File cacheDir, long maxSize) {
        long cacheSize = getDirSize(cacheDir);
        if (VERBOSE) FLog.v("checkCacheSize: %d", cacheSize);

        if (maxSize < 1 && cacheSize >= MAX_SIZE) {
            cleanDir(cacheDir, cacheSize - MAX_SIZE);

        } else if (maxSize > 0 && cacheSize >= maxSize) {
            cleanDir(cacheDir, cacheSize - maxSize);
        }
    }

    private static void cleanDir(File dir, long bytes) {
        long bytesDeleted = 0;
        File[] files = listFilesSortingByDate(dir);

        for (File file : files) {
            bytesDeleted += file.length();

            boolean isSuccess = file.delete();
            if (VERBOSE) FLog.v("Delete(%s): %s", isSuccess ? "success" : "failed", file.getPath());

            if (bytesDeleted >= bytes) {
                break;
            }
        }
    }

    private static long getDirSize(File dir) {
        long size = 0;
        File[] files = listFilesSortingByDate(dir);

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
        }

        return size;
    }

    public static File[] listFilesSortingByDate(File directory) {
        // Obtain the array of (file, timestamp) pairs.
        File[] files = directory.listFiles();
        Pair[] pairs = new Pair[files.length];

        for (int i = 0; i < files.length; i++) {
            pairs[i] = new Pair(files[i]);
        }

        // Sort them by timestamp.
        Arrays.sort(pairs);

        // Take the sorted pairs and extract only the file part, discarding the timestamp.
        for (int i = 0; i < files.length; i++) {
            files[i] = pairs[i].f;
        }

        return files;
    }

    static class Pair implements Comparable<Pair> {
        public long t;
        public File f;

        public Pair(File file) {
            f = file;
            t = file.lastModified();
        }

        public int compareTo(@NonNull Pair o) {
            long u = o.t;
            return t < u ? -1 : t == u ? 0 : 1;
        }
    }
}
