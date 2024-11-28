package com.example.xdygq3;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Functions {

    public static void addTextRow(Context context, TableRow tableRow, Classes.Compat[] Compats) {
        for (Classes.Compat item : Compats) {
            TextView textView = new TextView(context);
            textView.setTag(item.tag);
            textView.setText(item.content);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);
            textView.setLayoutParams(layoutParams);
            textView.setPadding(10, 10, 10, 10);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, item.textSize);
            textView.setId(item.id);
            tableRow.addView(textView);
        }
    }

    public static void addEditRow(Context context, TableRow tableRow, Classes.Compat[] Compats) {
        for (Classes.Compat item : Compats) {
            EditText editText = new EditText(context);
            editText.setTag(item.tag);
            editText.setText(item.content);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);
            editText.setLayoutParams(layoutParams);
            editText.setPadding(10, 10, 10, 10);
            float density = context.getResources().getDisplayMetrics().density;
            int minWidthPx = (int) (item.minWidth * density + 0.5f);
            editText.setMinWidth(minWidthPx);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, item.textSize);
            editText.setId(item.id);
            tableRow.addView(editText);
        }
    }

    public static String getFile(Context context, String filename) {
        File file = new File(context.getFilesDir(), filename);
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel();
             FileLock ignored = fileChannel.lock(0, Long.MAX_VALUE, true)) { // 读取锁
            byte[] data = new byte[fis.available()];
            int ignored1 = fis.read(data);
            Log.d("getFile", "Getting file: " + filename);
            Log.d("getFile", "Content: " + new String(data));
            return new String(data);
        } catch (FileNotFoundException e) {
            Log.e("getFile", "File not found: " + filename, e);
//            createEmptyFile(context, filename);
            return "";
        } catch (IOException e) {
            Log.e("getFile", "Error reading file: " + filename, e);
            return "";
        }
    }

    public static void deleteFile(Context context, String filename) {
        try {
            File file = new File(context.getFilesDir(), filename);
            if (file.exists()) {
                if (file.delete()) {
                    Log.d("deleteFile", "Deleted file: " + filename);
                } else {
                    Log.e("deleteFile", "Failed to delete file: " + filename);
                }
            } else {
                Log.w("deleteFile", "File does not exist: " + filename);
            }
        } catch (Exception e) {
            Log.e("deleteFile", "Error deleting file: " + filename, e);
        }
    }

    public static Boolean checkFileExists(Context context, String filename) {
        File file = new File(context.getFilesDir(), filename);
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel();
             FileLock ignored = fileChannel.lock(0, Long.MAX_VALUE, true)) {
            Log.d("checkFileExists", "Got file: " + filename);
            return true;
        } catch (FileNotFoundException e) {
            Log.e("checkFileExists", "File not found: " + filename);
            createEmptyFile(context, filename);
            return false;
        } catch (IOException e) {
            Log.e("checkFileExists", "Error reading file: " + filename, e);
            return false;
        }
    }

    public static Call buildCall(String url, String cookie) {
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(shareData.getSSLContext().getSocketFactory(), shareData.trustAllCerts)
                .callTimeout(shareData.getConfig().callTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(shareData.getConfig().connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(shareData.getConfig().readTimeout, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", cookie)
                .build();
        return client.newCall(request);
    }


    private static void createEmptyFile(Context context, String filename) {
        File file = new File(context.getFilesDir(), filename);
        try (FileOutputStream fos = new FileOutputStream(file);
             FileChannel fileChannel = fos.getChannel();
             FileLock ignored1 = fileChannel.lock()) { // 写入锁
            fos.write("".getBytes());
        } catch (IOException e) {
            Log.e("createEmptyFile", "Error creating empty file: " + filename, e);
        }
    }

    public static void PutFile(Context context, String filename, String content) {
        Log.d("PutFile", "Putting file: " + filename);
        Log.d("PutFile", "Content: " + content);
        FileOutputStream fos;
        FileChannel channel = null;
        FileLock lock = null;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            channel = fos.getChannel();
            lock = channel.lock();
            fos.write(content.getBytes());
        } catch (IOException e) {
            Log.e("PutFile", "Error writing to file: " + filename, e);
        } catch (Exception e) {
            Log.e("PutFile", "Unexpected error while writing to file: " + filename, e);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    Log.e("PutFile", "Error releasing file lock: " + filename, e);
                }
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    Log.e("PutFile", "Error closing file channel: " + filename, e);
                }
            }
        }
    }

    public static CharSequence menuIconWithText(Drawable r, String title) {
        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }
}
