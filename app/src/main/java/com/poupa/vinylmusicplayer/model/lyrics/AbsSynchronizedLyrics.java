package com.poupa.vinylmusicplayer.model.lyrics;

import android.util.Log;
import android.util.SparseArray;

import com.poupa.vinylmusicplayer.service.MusicService;

public abstract class AbsSynchronizedLyrics extends Lyrics {
    public static final String TAG = AbsSynchronizedLyrics.class.getSimpleName();
    private static final int TIME_OFFSET_MS = 500; // time adjustment to display line before it actually starts

    protected final SparseArray<String> lines = new SparseArray<>();
    protected int offset = 0;

    public String getLine(int time) {
        time += offset + AbsSynchronizedLyrics.TIME_OFFSET_MS;

        int lastLineTime = lines.keyAt(0);

        for (int i = 0; i < lines.size(); i++) {
            int lineTime = lines.keyAt(i);

            if (time >= lineTime) {
                lastLineTime = lineTime;
            } else {
                break;
            }
        }

        return lines.get(lastLineTime);
    }
    public class Line {
        public String text;
        public int time;
        public int index;
        Line(int _index, int _time, String _text) {
            index = _index;
            text = _text;
            time = _time;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder()
                    .append("index=").append(index)
                    .append(", time=").append(time)
                    .append(", text=[").append(text)
                    .append("]");
            return sb.toString();
        }
    }

    public Line getTimeLine(int time) {
        time += offset + AbsSynchronizedLyrics.TIME_OFFSET_MS;
        if (lines.size() == 0) {
            return null;
        }

        int index = lines.indexOfKey(time);
        if (index < 0) {
            index = (~index) -1;
        }
        if (index < 0) {
            index = 0;
        }
        if (index >= lines.size()) {
            Log.e(TAG, "[DVD] can't find lyric line at time " + time + ", index=" + index);
            return null;
        }
        return new Line(index, lines.keyAt(index), lines.valueAt(index));
    }

    public Line getNextTimeLine(Line line) {
        if (line.index < 0 || line.index >= lines.size()) {
            Log.e(TAG, "[DVD] failed to get next line from: " + line);
            return null;
        }
        if (line.index == lines.size()-1) {
            return null;
        }
        int index = line.index + 1;
        return new Line(index, lines.keyAt(index), lines.valueAt(index));
    }

    public boolean isSynchronized() {
        return true;
    }

    public boolean isValid() {
        parse(true);
        return valid;
    }

    public int getCount() {
        if (!isValid()) {
            return 0;
        }
        return lines.size();
    }

    @Override
    public String getText() {
        parse(false);

        if (valid) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.valueAt(i);
                sb.append(line).append("\r\n");
            }

            return sb.toString().trim().replaceAll("(\r?\n){3,}", "\r\n\r\n");
        }

        return super.getText();
    }
}
