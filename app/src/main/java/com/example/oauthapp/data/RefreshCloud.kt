package com.example.oauthapp.data

data class RefreshCloud(
    private val accessToken: String,
    private val refreshToken: String
) : CloudObject {

    fun save(save: TokenStorage.Save) {
        save.updateTokens(accessToken, refreshToken)
    }

}
