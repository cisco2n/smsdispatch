package com.example.smsdispatch;

import android.app.Service;
import android.content.*;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by cisco on 13-5-20.
 */
public class dipatchService extends Service {
    private IncomingSmsCallReceiver mIncomingSmsCallReceiver;
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("depatchService","depatchService");
        mIncomingSmsCallReceiver = new IncomingSmsCallReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.setPriority(1000);
        mIntentFilter.addAction("android.intent.action.PHONE_STATE");
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mIncomingSmsCallReceiver, mIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mIncomingSmsCallReceiver);
    }

    private class IncomingSmsCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("IncomingSmsCallReceiver", "Action:" + action);
            if("android.provider.Telephony.SMS_RECEIVED".equals(action)){
                SmsMessage sms = getMessagesFromIntent(intent)[0];
                String number = sms.getOriginatingAddress();
                String content = sms.getMessageBody();
                Log.e("IncomingSmsCallReceiver", "Incomng Number: " + number);

                Date date = new Date(sms.getTimestampMillis());
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                String sendtime = format.format(date);
                SmsManager manager = SmsManager.getDefault();
                String transNum = "18620351305";
                String dispatch =" "+ number + "|" + sendtime + "|" + content;
                Log.e("IncomingSmsCallReceiver"," "+ dispatch);
                ArrayList<String> parts = manager.divideMessage(content);//(dispatch);
                for(String text : parts){
                    manager.sendTextMessage(transNum, null, text, null, null);
                }
                //manager.sendMultipartTextMessage(transNum, null, parts, null, null);
                Toast.makeText(context,"Send " + number + "|" + sendtime + "|" + content, Toast.LENGTH_SHORT).show();
                ArrayList<String> cont = manager.divideMessage(content);
                for(String text : cont){
                    insertSmsContent(number,text);
                }
                abortBroadcast();
            }
        }

        public final  SmsMessage[] getMessagesFromIntent(Intent intent) {
            Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
            byte[][] pduObjs = new byte[messages.length][];
            for (int i = 0; i < messages.length; i++) {
                pduObjs[i] = (byte[]) messages[i];
            }

            byte[][] pdus = new byte[pduObjs.length][];
            int pduCount = pdus.length;
            SmsMessage[] msgs = new SmsMessage[pduCount];

            for (int i = 0; i < pduCount; i++) {
                pdus[i] = pduObjs[i];
                msgs[i] = SmsMessage.createFromPdu(pdus[i]);
            }

            return msgs;
        }
        /*
                private void changeSMSstate(String num, Context context)
                {
                    Cursor cursor;
                    ContentResolver cr  = context.getContentResolver();
                    Log.e("changeSMSstate1", "Incomng Number: " + num);
                    cursor = cr.query(Uri.parse("content://sms/inbox"), new String[]{"_id", "address", "read"}, " address=? and read=?", new String[]{num, "0"}, "date desc");
                    if (cursor != null){
                        ContentValues values = new ContentValues();
                        values.put("read", "1");
                        //�޸Ķ���Ϊ�Ѷ�ģʽ
                        Log.e("changeSMSstate2", "Incomng Number: " + num);
                        cursor.moveToFirst();
                        while (cursor.isLast()){
                            //���µ�ǰδ������״̬Ϊ�Ѷ�
                            int state = getContentResolver().update(Uri.parse("content://sms/inbox"), values, " _id=?", new String[]{""+cursor.getInt(0)});
                            cursor.moveToNext();
                            Log.e("changeSMSstate3", "state state: " + state);
                        }
                    }
                }
        */
        private void insertSmsContent(String mNumber,String message)
        {
            /** �õ�������ֻ����� **/
            String number = mNumber.toString();
            /** �õ�����Ķ������� **/
            String text = message.toString();
            /** �ֻ����� ���������� ���費Ϊ�� **/
            if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(text)) {
                //sendSMS(number, text);
                /**�����͵Ķ��Ų������ݿ�**/
                ContentValues values = new ContentValues();
                //����ʱ��
                values.put("date", System.currentTimeMillis());
                //�Ķ�״̬
                values.put("read", 1);
                //1Ϊ�� 2Ϊ��
                values.put("type", 1);
                //�ʹ����
                values.put("address", number);
                //�ʹ�����
                values.put("body", text);
                //������ſ�
                getContentResolver().insert(Uri.parse("content://sms"),values);
            }

        }

    }

}