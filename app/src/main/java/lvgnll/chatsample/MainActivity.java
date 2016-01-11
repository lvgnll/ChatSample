package lvgnll.chatsample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.google.gson.Gson;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.pubnub.api.Callback;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor SPEdit;
    private String TAG = "sample";
    ListView messagesView;
    MessageAdapter messageAdapter;
    EditText messageInput;
    Button sendButton;

    String mTargetChannel = "pusher";

    private LambdaInvokerFactory factory;

    private Pubnub mPubNub;
    private String stdByChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        factory = Util.getLamFactory(this.getApplicationContext());

        this.mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        if (!this.mSharedPreferences.contains(Constants.USER_NAME)){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        SPEdit = this.mSharedPreferences.edit();

        ListView listView = (ListView) findViewById(R.id.userlistView);
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, Constants.USER_LIST);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView av, View v, int position, long arg3) {
                mTargetChannel = av.getItemAtPosition(position).toString();
            }
        });
        messageInput = (EditText) findViewById(R.id.message_input);
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);


        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        RadioButton radioPusher = (RadioButton) findViewById(R.id.radioPusher);
        RadioButton radioPubNub = (RadioButton) findViewById(R.id.radioPubnub);
        if (this.mSharedPreferences.getBoolean(Constants.PUSHERORNOT, true)) {
            radioPusher.setChecked(true);
            radioPubNub.setChecked(false);
        }else {
            radioPubNub.setChecked(false);
            radioPubNub.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioPusher:
                        SPEdit.putBoolean(Constants.PUSHERORNOT, true);
                        break;
                    case R.id.radioPubnub:
                        SPEdit.putBoolean(Constants.PUSHERORNOT, false);
                        break;
                }
                SPEdit.apply();
            }
        });

        //init Pusher
        Pusher mPusher = new Pusher(Constants.PUSHER_KEY);
        mPusher.unsubscribe(this.mSharedPreferences.getString(Constants.USER_NAME, ""));
        Channel channel = mPusher.subscribe(this.mSharedPreferences.getString(Constants.USER_NAME, ""));
        for(int i = 0; i < adapter.getCount(); i++) {
            channel.bind(listView.getItemAtPosition(i).toString(), new SubscriptionEventListener() {
                @Override
                public void onEvent(String channelName, String eventName, final String data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            Log.d(TAG, data);
                            Message message = gson.fromJson(data, Message.class);
                            message.setTimeStamp();
                            message.setApiFromPusher(true);
                            messageAdapter.add(message);
                            messagesView.setSelection(messageAdapter.getCount() - 1);
                        }
                    });
                }
            });
        }
        mPusher.connect();

        //init PubNub
        this.stdByChannel = this.mSharedPreferences.getString(Constants.USER_NAME, "");
        initPubNub();
    }

    /**
     * init PubNub and subscribe to standby channel
     */
    public void initPubNub(){
        this.mPubNub = new Pubnub(Constants.PUBNUB_PUB_KEY, Constants.PUBNUB_SUB_KEY);
        this.mPubNub.setUUID(this.mSharedPreferences.getString(Constants.USER_NAME, ""));
        subscribeStdBy();
    }

    /**
     * Subscribe to yourself channel
     */
    private void subscribeStdBy(){
        try {
            this.mPubNub.subscribe(this.stdByChannel, new Callback() {
                @Override
                public void successCallback(String channel, final Object data) {
                    Log.d("PubNub", "MESSAGE: " + data.toString());
                    if (!(data instanceof JSONObject)) return;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            Message message = gson.fromJson(data.toString(), Message.class);
                            message.setTimeStamp();
                            message.setApiFromPusher(false);
                            messageAdapter.add(message);
                            messagesView.setSelection(messageAdapter.getCount() - 1);
                        }
                    });
                }

                @Override
                public void connectCallback(String channel, Object message) {
                    Log.d("PubNub", "CONNECTED: " + message.toString());
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d("PubNub","ERROR: " + error.toString());
                }
            });
        } catch (PubnubException e){
            Log.d("PubNub","PubnubException");
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.remove(Constants.USER_NAME);
        editor.apply();
    }
    @Override
    public void onClick(View v) {
        postMessage();
    }
    private void postMessage()  {
        final LamInter myInterface = factory.build(LamInter.class);
        String msg = messageInput.getText().toString();

        Message myself = new Message(this.mSharedPreferences.getString(Constants.USER_NAME, ""), msg, System.currentTimeMillis(), true);
        Log.d(TAG, myself.toString());
        if (this.mSharedPreferences.getBoolean(Constants.PUSHERORNOT, true)) {
            //Pusher
            myself.setApiFromPusher(true);
            LamReq request = new LamReq(mTargetChannel, myself.getMessage(), myself.getSender(), myself.getTimeStamp());
            new AsyncTask<LamReq, Void, LamRes>() {
                @Override
                protected LamRes doInBackground(LamReq... params) {
                    try {
                        return myInterface.pusherAgent(params[0]);
                    } catch (LambdaFunctionException lfe) {
                        Log.e(TAG, "Failed to invoke echo", lfe);
                        return null;
                    }
                }
            }.execute(request);
        }else {
            //PubNub
            myself.setApiFromPusher(false);
            Callback callback = new Callback() {
                public void successCallback(String channel, Object response) {
                    Log.d(TAG, response.toString());
                }

                public void errorCallback(String channel, PubnubError error) {
                    Log.e(TAG, error.toString());
                }
            };
            mPubNub.publish(mTargetChannel, myself.toJSON(), callback);
        }
        messageAdapter.add(myself);
        messagesView.setSelection(messageAdapter.getCount() - 1);
    }
}
