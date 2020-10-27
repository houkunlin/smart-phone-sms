package com.houkunlin.sms.entity

import java.security.Principal


/**
 *
 * @ClassName: User
 * @Description: 客户端用户
 * @author cheng
 * @date 2017年9月29日 下午3:02:54
 */
data class User(private val name: String) : Principal {
    override fun getName() = name
}
