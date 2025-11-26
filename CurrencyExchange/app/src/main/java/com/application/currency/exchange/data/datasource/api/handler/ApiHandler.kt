package com.application.currency.exchange.data.datasource.api.handler

import com.application.currency.exchange.domain.entity.network.NetworkResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException
import retrofit2.Response

suspend inline fun <reified T : Any> handleApi(execute: suspend () -> Response<Any>): NetworkResult<T> {
    return try {
        val response = execute()
        val body = response.body()

        body?.let {
            val gson = Gson()
            val json = gson.toJson(response.body())
            val type = object : TypeToken<T>() {}.type
            val data = gson.fromJson<T>(json, type)
            NetworkResult.Success(data)
        } ?: run {
            NetworkResult.Error(code = response.code(), message = response.message())
        }
    } catch (exc: HttpException) {
        NetworkResult.Error(code = exc.code(), message = exc.message())
    } catch (exc: Throwable) {
        exc.printStackTrace()
        NetworkResult.Exception(exc)
    }
}