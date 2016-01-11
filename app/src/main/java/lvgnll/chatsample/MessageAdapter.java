package lvgnll.chatsample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class MessageAdapter extends BaseAdapter {
    Context messageContext;
    ArrayList<Message> messageList;

    private static class MessageViewHolder {
        public ImageView thumbnailImageView;
        public TextView senderView;
        public TextView bodyView;
        public TextView time;
        public TextView apiFrom;
    }

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        messageList = messages;
        messageContext = context;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder;

        if (convertView == null){
            LayoutInflater messageInflater = (LayoutInflater) messageContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.message, null);
            holder = new MessageViewHolder();

            // set the holder's properties to elements in `message.xml`
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.img_thumbnail);
            holder.senderView = (TextView) convertView.findViewById(R.id.message_sender);
            holder.bodyView = (TextView) convertView.findViewById(R.id.message_body);
            holder.time = (TextView) convertView.findViewById(R.id.timestamp);

            // assign the holder to the view we will return
            convertView.setTag(holder);
        } else {

            // otherwise fetch an already-created view holder
            holder = (MessageViewHolder) convertView.getTag();
        }

        // get the message from its position in the ArrayList
        Message message = (Message) getItem(position);

        // set the elements' contents
        holder.senderView.setText(message.getSender());
        holder.bodyView.setText(message.getMessage());
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(50, 50);

        if(message.isSelf()) {
            layout.gravity = Gravity.END;
            holder.thumbnailImageView.setLayoutParams(layout);
            holder.bodyView.setGravity(Gravity.END);
            holder.senderView.setGravity(Gravity.END);
            holder.time.setGravity(Gravity.END);
            if (message.isPusher()) {
                holder.time.setText(formatTimeStamp(message.getTimeStamp()) + " to Pusher");
            }else {
                holder.time.setText(formatTimeStamp(message.getTimeStamp()) + " to PubNub");
            }
        } else {
            layout.gravity = Gravity.START;
            holder.thumbnailImageView.setLayoutParams(layout);
            holder.bodyView.setGravity(Gravity.START);
            holder.senderView.setGravity(Gravity.START);
            holder.time.setGravity(Gravity.START);
            if (message.isPusher()) {
                holder.time.setText(formatTimeStamp(message.getTimeStamp()) + " from Pusher");
            }else {
                holder.time.setText(formatTimeStamp(message.getTimeStamp()) + " from PubNub");
            }
        }

        Picasso.with(messageContext).
                load("https://twitter.com/" + message.getSender() + "/profile_image?size=original").
                placeholder(R.mipmap.ic_launcher).
                into(holder.thumbnailImageView);

        return convertView;
    }

    public void add(Message message) {
        messageList.add(message);
        notifyDataSetChanged();
    }

    public static String formatTimeStamp(long timeStamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        return formatter.format(calendar.getTime());
    }
}
