package com.houkunlin.smart_phone_sms_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


class MainActivity : FlutterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.SMS_FINANCIAL_TRANSACTIONS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS
        )
        requestPermissions(permissions)
    }

    /**
     * 申请权限
     * @param permissions see Manifest.permission.INTERNET
     * @see Manifest.permission.INTERNET
     */
    private fun requestPermissions(permissions: Array<String>) {
        val disagree = mutableListOf<String>()
        val reject = mutableListOf<String>()
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED) {
                disagree.add(it)
            }
        }
        if (disagree.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, disagree.toTypedArray(), 24)
        }
        permissions.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                reject.add(it)
            }
        }
        if (reject.isNotEmpty()) {
            //用户拒绝过改权限时，执行此处跳转到需要权限的说明
            Log.i("权限申请", "用户拒绝权限：$reject")
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        val tag = "FlutterPlugin"
        val smsManager = SmsManager.getSmsManagerForSubscriptionId(2)
//        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.houkunlin/test_sms")
//                .setMethodCallHandler { call, result ->
//                    val method = call.method
//
//                    Log.i(tag, "调试4：${method}")
//                    smsManager.sendTextMessage("+8615577405667", null, "这条短信是自动发送的", null, null);
//
//                    result.success("发送短信调用成功")
//                }
        val manager = context.getSystemService(SubscriptionManager::class.java)!!
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.houkunlin/SubscriptionManager")
                .setMethodCallHandler(ProxyObjectMethodCallHandler(manager, "SubscriptionManager"))
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.houkunlin/SmsManager")
                .setMethodCallHandler(ProxyObjectMethodCallHandler(smsManager, "SmsManager"))
    }
}
