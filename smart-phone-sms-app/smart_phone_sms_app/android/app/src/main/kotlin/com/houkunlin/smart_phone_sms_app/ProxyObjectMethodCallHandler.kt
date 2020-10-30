package com.houkunlin.smart_phone_sms_app

import android.util.Log
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.stream.Collectors

class ProxyObjectMethodCallHandler(private val proxyObject: Any, private val TAG: String) : MethodChannel.MethodCallHandler {
    private val mapper = ObjectMapper()

    init {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.registerModule(SimpleModule().setSerializerModifier(object : BeanSerializerModifier() {
            override fun changeProperties(config: SerializationConfig, beanDesc: BeanDescription, beanProperties: List<BeanPropertyWriter>): List<BeanPropertyWriter> {
                return beanProperties.stream().map { bpw: BeanPropertyWriter? ->
                    object : BeanPropertyWriter(bpw) {
                        @Throws(Exception::class)
                        override fun serializeAsField(bean: Any, gen: JsonGenerator, prov: SerializerProvider) {
                            try {
                                super.serializeAsField(bean, gen, prov)
                            } catch (e: Exception) {
                                Log.i(TAG, String.format("ignoring %s for field '%s' of %s instance", e.javaClass.name, this.name, bean.javaClass.name))
                            }
                        }
                    }
                }.collect(Collectors.toList())
            }
        }))
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.i(TAG, "call ${call.method}(${call.arguments})")
        try {
            val arguments = call.arguments
            val methods = proxyObject.javaClass.methods
            val notParameterMethod = methods.find { it.name == call.method && it.parameterCount == 0 }
            val oneParameterMethod = methods.find { it.name == call.method && it.parameterCount == 1 }

            val defaultMethod = methods.find { method ->
                // 调用方法名相同
                if (call.method != method.name) {
                    return@find false
                }
                // 方法的参数数量
                val parameterCount = method.parameterCount

                // 判断参数数量是否一致
                val parameterTypes = method.parameterTypes

                // 传递到调用方法参数的参数列表
                val args = arrayListOf<Any?>()
                var isOk = true

                when (arguments) {
                    is Array<*> -> {
                        args.addAll(arguments)
                        if (parameterCount != arguments.size) {
                            return@find false
                        }
                        arguments.forEachIndexed { index, parameter ->
                            if (parameter == null) {
                                return@forEachIndexed
                            }
                            if (!parameterTypes[index].isAssignableFrom(parameter.javaClass)) {
                                // 参数类型不一致，标记为失败
                                isOk = false
                                return@forEachIndexed
                            }
                        }
                    }
                    is Iterable<*> -> {
                        args.addAll(arguments)
                        if (parameterCount != arguments.count()) {
                            return@find false
                        }
                        arguments.forEachIndexed { index, parameter ->
                            if (parameter == null) {
                                return@forEachIndexed
                            }
                            if (!parameterTypes[index].isAssignableFrom(parameter.javaClass)) {
                                // 参数类型不一致，标记为失败
                                isOk = false
                                return@forEachIndexed
                            }
                        }
                    }
                    null -> {
                        return@find false
                    }
                    else -> {
                        return@find false
                    }
                }
                if (isOk) {
                    // Log.i(TAG, "${call.method} -> 找到方法匹配、参数匹配的执行方法 $method")
                    val resultObject = method.invoke(proxyObject, *args.toArray())
                    success(call, result, resultObject)
                }
                return@find isOk
            }
            if (defaultMethod != null) {
                // defaultMethod 不为 null 的时候就已经调用过了，因此可以直接返回
                return
            }
            if (arguments == null) {
                if (notParameterMethod != null) {
                    // Log.i(TAG, "${call.method} -> 执行默认无参数方法 $notParameterMethod")
                    val resultObject = notParameterMethod.invoke(proxyObject)
                    success(call, result, resultObject)
                } else {
                    result.notImplemented()
                }
            } else {
                if (oneParameterMethod != null) {
                    // Log.i(TAG, "${call.method} -> 执行只有一个参数的默认方法 $notParameterMethod")
                    val resultObject = oneParameterMethod.invoke(proxyObject, arguments)
                    success(call, result, resultObject)
                } else {
                    result.notImplemented()
                }
            }
        } catch (e: Exception) {
            Log.i(TAG, "${call.method} -> 执行方法异常 $e")
            result.error(call.method, "调用失败", e)
        }
    }

    private fun success(call: MethodCall, result: MethodChannel.Result, resultObject: Any?) {
        val map = mapOf(
                "method" to call.method,
                "result" to resultObject
        )
        // Log.i(TAG, "${call.method} -- result -> ${mapper.writeValueAsString(map)}")

        result.success(mapper.writeValueAsString(map))
    }
}
