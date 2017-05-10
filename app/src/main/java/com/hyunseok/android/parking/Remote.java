package com.hyunseok.android.parking;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2017-03-06.
 */

public class Remote {

    public void getData(final Callback obj) {

        String urlString = obj.getUrl();

        if (!urlString.startsWith("http")) {
            urlString = "http://" + urlString;
        }

        // Network관련 서비스는 메인 쓰레드에서 돌리지 마라고 에러를 띄운다. => AsyncTask를 사용한다.
        new AsyncTask<String, Void, String>() { // 첫번째 파라미터는 받는 parameter, 세번째 파라미터는 return 값

            ProgressDialog dialog = new ProgressDialog(obj.getContext());

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Progress Dialog 세팅
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("불러오는 중...");

                dialog.show();
            }

            @Override
            protected String doInBackground(String... params) {
                String urlString = params[0];
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder result = new StringBuilder(); // String 연산을 빠르게 하기 위해 StringBuilder 사용
                        String lineOfData = "";
                        while ((lineOfData = br.readLine()) != null) {
                            result.append(lineOfData);
                        }

                        return result.toString();

                    } else {
                        Log.e("HTTPConnection", "Error Code = " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            // doInBackground 함수의 return 값이 넘어온다.
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                // 결과값 출력
                Log.i("Remote", result);

                dialog.dismiss();
                // remote 객체를 생성한 측의 callback 함수 호출
                obj.call(result);
            }
        }.execute(urlString);
    }

    public interface Callback {
        public Context getContext();
        public String getUrl();
        public void call(String jsonString);
        public ProgressDialog getProgress();
    }
}
