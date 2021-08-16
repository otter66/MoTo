package com.otter66.newMoTo.Data

import java.io.Serializable

data class User(
    val id: String? = null,             //id
    val job: String? = null,            //직업
    val group: String? = null,          //회사 및 학교
    val department: String? = null,     // 학과 및 부서
    val profileImage: String? = null    // 프로필 사진
): Serializable
