package com.ferratum.poc.graphapi.graphapipoc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.CustomTypeAdapter;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.ferratum.poc.graphapi.graphapipoc.type.CustomType;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://uniweb-api.dev.ferratum.com/api/graph";

    private static final String SUBSCRIPTION_BASE_URL = "";

    private ApolloClient apolloClient;

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        apolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
//                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(SUBSCRIPTION_BASE_URL, okHttpClient))
                .build();

        ApolloCall<ProcessStartMutation.Data> entryDetailQuery = null;
        try {
            entryDetailQuery = apolloClient
                    .mutate(new ProcessStartMutation.Builder().jsonobject(new JSONObject("{\"entityUid\":\"CE-FBM_MB_TC\",\"channel\":\"SE_MB_MA\"}")).build());

            disposables.add(Rx2Apollo.from(entryDetailQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(dataResponse -> Log.d("TAG", dataResponse.toString()), throwable -> Log.d("TAG", throwable.getMessage())));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static class JsonCustomTypeAdapter implements CustomTypeAdapter<JSONObject> {
        @Override
        public JSONObject decode(@Nonnull String value) {
            try {
                return new JSONObject(value);
            } catch (JSONException e) {
                return null;
            }
        }

        @Nonnull
        @Override
        public String encode(@Nonnull JSONObject value) {
            return value.toString();
        }
    }
}