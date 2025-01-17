package com.example.bkhome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import de.hdodenhof.circleimageview.CircleImageView;
//van phong connected to 172.28.11.119:5555

//thao - hang: connected to 172.28.11.53:5555
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        startMQTT("BK2024");


        niceTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int initStatus) {
                if (initStatus == TextToSpeech.SUCCESS) {
                    niceTTS.setLanguage(Locale.forLanguageTag("VI"));
                    talkToMe("Xin chào các bạn, tôi là hệ thống tiếp tân của khoa Khoa học và Kĩ thuật Máy tính");
                }else{
                    Log.d("ChatGPT", "Init fail");
                }
            }
        });
        initSystem();
        AudioManager am =
                (AudioManager) this.peekAvailableContext().getSystemService(Context.AUDIO_SERVICE);

        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
    }



    private int DATA_CHECKING = 0;
    private TextToSpeech niceTTS;

    public void talkToMe(final String sentence) {

        Log.d("ChatGPT", "Talk to me " + sentence);
        String speakWords = sentence;
        niceTTS.speak(speakWords, TextToSpeech.QUEUE_FLUSH, null);
    }




    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        sb.append('I');
        sb.append('Y');
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
    MQTTHelper mqttHelper;
    public void startMQTT(String username){
        mqttHelper = new MQTTHelper(this, username, getRandomString(50));
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("mqtt", topic + "  : " + message.toString());


                if(topic.toLowerCase().indexOf("v1") >=0){
                    int index = new Random().nextInt(getAssistantCall().size());

                    if(index > getAssistantCall().size() - 1)
                        index = getAssistantCall().size();

                    String strCall = getAssistantCall().get(index).replaceAll("XXX", message.toString());
                    talkToMe(strCall);
                }

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnNext) {
            counterTimer = 0;
            currentIndex++; if(currentIndex >= listData.size()) currentIndex =0;
            updateUI();
        }
        if(v.getId() == R.id.btnPrevious){
            counterTimer = 0;
            currentIndex--; if(currentIndex <0) currentIndex = listData.size() - 1;
            updateUI();
        }
        if(v.getId() == R.id.btnVoiceCall){
            sendMQTTData();
        }
    }


    public void sendMQTTData(){

        String data = listData.get(currentIndex).getStrName();

        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);



        byte[] b = data.getBytes(Charset.forName("UTF-8"));
        //msg.setPayload(new byte[]{'4','5'});
        msg.setPayload(b);
        try {
            mqttHelper.mqttAndroidClient.publish("BK2024/feeds/V1", msg);

        }catch (MqttException e){

        }

    }

    private TextView txtName, txtJobDescription, txtPhoneCall, txtEmail;
    private ImageButton btnNext, btnPrevious, btnVoiceCall;
    private CircleImageView imgAvatar;
    private int currentIndex = 0;
    List<AssistantModel> listData = null;
    private int counterTimer = 0;


    private void initSystem(){
        listData = getAssistantList();
        txtName = findViewById(R.id.txtName);
        txtJobDescription = findViewById(R.id.txtJobDescription);
        txtPhoneCall = findViewById(R.id.txtPhoneCall);
        txtEmail = findViewById(R.id.txtEmail);
        imgAvatar = findViewById(R.id.imgAvatar);
        setupTimerUpdate();

        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnVoiceCall = findViewById(R.id.btnVoiceCall);

        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnVoiceCall.setOnClickListener(this);

    }


    private void updateUI(){

        txtName.setText(listData.get(currentIndex).getStrName());
        txtJobDescription.setText(listData.get(currentIndex).getStrJobDescription());
        txtPhoneCall.setText(listData.get(currentIndex).getStrPhoneCall());
        txtEmail.setText(listData.get(currentIndex).getStrEmail());
        Drawable drawable = ContextCompat.getDrawable(this, listData.get(currentIndex).getmDrawableLogo());
        imgAvatar.setImageDrawable(drawable);
    }

    private void setupTimerUpdate(){
        Timer aTimer = new Timer();
        TimerTask aTask = new TimerTask() {
            @Override
            public void run() {
                counterTimer++;
                if(counterTimer >= 15) {
                    counterTimer = 0;
                    currentIndex ++;
                    if(currentIndex >= listData.size()) currentIndex = 0;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI();
                        }
                    });
                }
            }
        };
        aTimer.schedule(aTask, 1000,1000);
    }



    public static List<String> getAssistantCall(){
        List<String> listCall = new ArrayList<>();
        listCall.add("Cô XXX ơi, có người liên hệ kìa!");
        listCall.add("Xin chào cô XXX, có người liên hệ");
        listCall.add("Có người liên hệ cô XXX ơi!");
        return listCall;
    }


    public static List<AssistantModel> getAssistantList(){
        List<AssistantModel> list = new ArrayList<>();

        list.add(new AssistantModel("TRẦN THỊ THU TRANG", "Trợ lý Quản lý SV; Trợ lý Quan hệ Doanh nghiệp/ Cựu sinh viên; Thực tập ngoài Trường; Học kỳ Doanh nghiệp; Nhận đăng ký ĐCLV, ĐACN, ĐAMHKTMT, ĐATN, LVTN; Quản lý Thư viện Khoa", "thutrangcse@hcmut.edu.vn", "7847", R.drawable.thu_trang_2));
        list.add(new AssistantModel("VÕ THỊ NHƯ HÀ", "Giáo vụ Đại học (CC, CN, CT, QT)", "havtn@hcmut.edu.vn", "7847", R.drawable.nhuha));
        list.add(new AssistantModel("NGUYỄN THỊ KIM CƯƠNG", "Thư ký văn phòng; Giáo vụ hệ VLVH; Thư ký khoa học công nghệ, Nghiên cứu khoa học CB", "kcuong@hcmut.edu.vn", "7847", R.drawable.kim_cuong_2));
        list.add(new AssistantModel("TRƯƠNG NHƯ VI", "Giáo vụ Đại học (CQ, B2, TN, SN, Liên thông Đại học - Cao học)", "vitruong@hcmut.edu.vn", "7847", R.drawable.nhu_vi));

        list.add(new AssistantModel("NGUYỄN LÊ PHƯƠNG THẢO", "Giáo vụ Sau Đại học; Thư ký dự án ABET", "phuongthao@hcmut.edu.vn", "5832", R.drawable.thao_tina_3));
        list.add(new AssistantModel("LÊ THỊ THÚY HẰNG", "Thư ký tổng hợp; Nhân sự", "hangle@hcmut.edu.vn", "5832", R.drawable.thuy_hang_3));
        list.add(new AssistantModel("LÊ HOÀNG LAN", "Kế toán - Tài chính", "lhlan@hcmut.edu.vn", "5845", R.drawable.hoang_lan));
        list.add(new AssistantModel("TRẦN THỊ LỆ PHÚC", "Thư ký văn phòng chuyên trách Văn phòng Khoa CS. Dĩ An (P.607-BK.B6); Phó Bí thư Đoàn Khoa; Thư ký nghiên cứu khoa học SV", "lephuc@hcmut.edu.vn", "5845", R.drawable.lephuc));

        return list;
    }

}