package com.zinipay
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SmsReceiver : BroadcastReceiver() {
    private val client = OkHttpClient()
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras ?: return
            val pdus = bundle.get("pdus") as Array<*>
            for (pdu in pdus) {
                val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                val sender = smsMessage.displayOriginatingAddress
                if (sender == "16247" || sender == "16167" || sender == "bKash" || sender == "Nagad") {
                    sendToWebhook(sender, smsMessage.messageBody)
                }
            }
        }
    }
    private fun sendToWebhook(sender: String, body: String) {
        val json = JSONObject()
        json.put("sender", sender)
        json.put("body", body)
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://zinipay-backend.tentionfree.workers.dev/api/sms/webhook")
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) { response.close() }
        })
    }
}
