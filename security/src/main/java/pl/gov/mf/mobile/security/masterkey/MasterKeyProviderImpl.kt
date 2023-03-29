package pl.gov.mf.mobile.security.masterkey

import android.content.Context
import androidx.security.crypto.MasterKey
import io.reactivex.Single
import javax.inject.Inject

class MasterKeyProviderImpl @Inject constructor(private val context: Context) :
    MasterKeyProvider {
    override fun getMasterKey(keyAlias: String): Single<MasterKey> = Single.create { emitter ->
        emitter.onSuccess(
            MasterKey.Builder(context, keyAlias)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        )
    }

    override fun getMasterKeySynch(keyAlias: String): MasterKey =
        MasterKey.Builder(context, keyAlias)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()


}