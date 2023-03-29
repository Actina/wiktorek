package pl.gov.mf.mobile.networking.api.interceptors

//@Deprecated
//class JwtTokenInterceptor @Inject constructor(private val tokenProvider: JwtTokenValueProvider) :
//    Interceptor {
//
//    override fun intercept(chain: Chain): Response {
//
//        val original = chain.request()
//
//        if (original.url.encodedPath.contains("register") && original.method.uppercase(Locale.ROOT)
//                .contentEquals("POST")
//        ) {
//            return chain.proceed(original)
//        }
//
//        // jwt refresh token
//        if (original.url.encodedPath.contains("authenticate") && original.method.uppercase(Locale.ROOT)
//                .contentEquals(
//                    "POST"
//                )
//        ) {
//            val originalHttpUrl = original.url
//            val requestBuilder = original.newBuilder()
//                .addHeader("x-jws-signature", tokenProvider.getRefreshToken())
//                .url(originalHttpUrl)
//
//            val request = requestBuilder.build()
//            return chain.proceed(request)
//        }
//        // jwt access token
//
//        val originalHttpUrl = original.url
//        val requestBuilder = original.newBuilder()
//            .addHeader("Authorization", "Bearer ${tokenProvider.getAccessToken()}")
//            .url(originalHttpUrl)
//
//        val request = requestBuilder.build()
//        return chain.proceed(request)
//    }
//}
//
//interface JwtTokenValueProvider {
//    fun getRefreshToken(): String
//    fun getAccessToken(): String
//}