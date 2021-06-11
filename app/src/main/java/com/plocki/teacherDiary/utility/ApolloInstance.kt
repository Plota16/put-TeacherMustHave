package com.plocki.teacherDiary.utility

import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient


object ApolloInstance {
    private var apolloClient: ApolloClient? = null
    private var BASE_URL = "https://teacher-dairy.hasura.app/v1/graphql"

    init {
        val store = Store()
        buildApolloClient(store.retrieveToken())
    }

    private fun buildOkHttpClient(token: String?): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()

        if (token != null) {
            okHttpClient.addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder().method(original.method(), original.body())
                builder.header(
                    "Authorization",
                    "Bearer $token"
                )
                chain.proceed(builder.build())
            }
        }

        return okHttpClient.build()
    }

    private fun buildApolloClient(token: String?) {
        val okHttpClient = buildOkHttpClient(token)

        apolloClient = ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .build()
    }

    fun get() : ApolloClient{
        return  apolloClient!!
    }

}