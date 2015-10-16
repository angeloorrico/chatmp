package br.com.cursoufba.chatmp.gui;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.cursoufba.chatmp.R;
import br.com.cursoufba.chatmp.conn.AMQPConnection;
import br.com.cursoufba.chatmp.model.MessageDTO;

public class ChatActivity extends AppCompatActivity {

    AMQPConnection conn = new AMQPConnection();

    LinearLayout linearLayout;

    Handler incomingMessageHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        linearLayout = (LinearLayout) findViewById(R.id.ll);
        final ScrollView scroll = (ScrollView) findViewById(R.id.scroll);

        incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");

                Gson gson = new Gson();
                MessageDTO messageDTO = gson.fromJson(message, MessageDTO.class);

                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss");

                linearLayout.addView(getChatRow(sdf.format(now)
                        + " (" + messageDTO.getUserId() + ")\n"
                        + messageDTO.getMessage(),
                        messageDTO.getUserId().equals(conn.MY_QUEUE_NAME)));

                scroll.fullScroll(View.FOCUS_DOWN);
            }
        };

        setupPublishButton();

        String usuario = getIntent().getStringExtra("Usuario");
        conn.MY_QUEUE_NAME = usuario;

        conn.subscribe(incomingMessageHandler);
    }

    private View getChatRow(String message, boolean isMyMessage) {
        View row = LayoutInflater.from(ChatActivity.this).inflate(R.layout.chat_row, null, false);
        TextView tvMessage = (TextView) row.findViewById(R.id.message_text);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvMessage.getLayoutParams();

        if (isMyMessage) {
            tvMessage.setBackgroundResource(R.drawable.speech_bubble_green);
            lp.gravity = Gravity.RIGHT;
        } else {
            tvMessage.setBackgroundResource(R.drawable.speech_bubble_orange);
            lp.gravity = Gravity.LEFT;
        }
        tvMessage.setLayoutParams(lp);

        tvMessage.setText(message);

        return row;
    }

    void setupPublishButton() {
        Button button = (Button) findViewById(R.id.publish);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText text = (EditText) findViewById(R.id.text);
                EditText to = (EditText) findViewById(R.id.ed_to);

                if (text.getText().toString().equals("")) {
                    Toast.makeText(ChatActivity.this, "Por favor, informe a mensagem.", Toast.LENGTH_SHORT).show();
                    text.requestFocus();

                    return;
                }

                MessageDTO msgDTO = new MessageDTO();
                msgDTO.setType(to.getText().toString().equals("") ? "fanout" : "direct");
                msgDTO.setUserId(conn.MY_QUEUE_NAME);
                msgDTO.setMessage(text.getText().toString());

                Gson gson = new Gson();
                String msgJSONString = gson.toJson(msgDTO);

                // ----------------------------------------------------
                // Cria a mensagem localmente na tela
                if (!to.getText().toString().equals("")) {
                    Bundle bundle = new Bundle();
                    Message msg = incomingMessageHandler.obtainMessage();

                    bundle.putString("msg", msgJSONString);
                    msg.setData(bundle);
                    incomingMessageHandler.sendMessage(msg);
                }
                // ----------------------------------------------------

                publishMessage(msgJSONString, to.getText().toString());
                text.setText("");
            }
        });
    }

    void publishMessage(String msgJSONString, String to) {
        // Adiciona a mensagem na fila interna para envio
        try {
            conn.routingKey = to;

            Log.d("","[q] " + msgJSONString);

            conn.queue.putLast(msgJSONString);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        conn.onDestroy();
        super.onDestroy();
    }

}