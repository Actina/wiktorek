package pl.gov.mf.mobile.security.masterkey

import androidx.security.crypto.MasterKey
import io.reactivex.Single

interface MasterKeyProvider {
    fun getMasterKey(keyAlias: String): Single<MasterKey>
    fun getMasterKeySynch(keyAlias: String): MasterKey
}