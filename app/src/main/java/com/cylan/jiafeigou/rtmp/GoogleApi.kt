package com.cylan.jiafeigou.rtmp

import com.cylan.jiafeigou.utils.ContextUtils
import com.google.android.gms.common.Scopes
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.youtube.YouTube
import java.io.File
import java.io.InputStreamReader

/**
 * Created by yanzhendong on 2017/9/11.
 */


val JSON_FACTORY = JacksonFactory()
val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()

object GoogleApi {


    fun authorize(userid: String): Credential {
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(ContextUtils.getContext().assets.open("")))
        val authorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets.details.clientId,
                clientSecrets.details.clientSecret,
                arrayListOf(Scopes.PLUS_ME))
                .setDataStoreFactory(FileDataStoreFactory(File(ContextUtils.getContext().filesDir, "google")))
                .build()
        val credential = authorizationCodeFlow.loadCredential(userid)
        YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        return credential
    }
}