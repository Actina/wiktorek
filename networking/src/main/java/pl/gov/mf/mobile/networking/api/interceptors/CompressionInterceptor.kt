package pl.gov.mf.mobile.networking.api.interceptors

import net.jpountz.lz4.LZ4Factory
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import okio.IOException


class CompressionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url

        val factory = LZ4Factory.fastestInstance()
        val data = (original.bodyToString() ?: "").toByteArray(charset("UTF-8"))

        val decompressedLength = data.size
        val compressor = factory.fastCompressor()
        val maxCompressedLength = compressor.maxCompressedLength(decompressedLength)
        val compressed = ByteArray(maxCompressedLength)
        val compressedLength =
            compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength)
        val compressedWithoutZeros =
            compressed.slice(IntRange(0, compressedLength - 1)).toByteArray()

        val requestBuilder = original.newBuilder()
            .addHeader("X-Batch-Size", "$decompressedLength")
            .method(original.method, compressedWithoutZeros.toRequestBody())
            .url(originalHttpUrl)

        val request = requestBuilder.build()
        return chain.proceed(request)
    }

    private fun Request.bodyToString(): String? {
        return try {
            val copy: Request = newBuilder().build()
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            null
        }
    }
}