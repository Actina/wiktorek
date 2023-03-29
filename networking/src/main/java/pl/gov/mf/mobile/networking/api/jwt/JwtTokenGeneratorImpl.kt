package pl.gov.mf.mobile.networking.api.jwt

import io.jsonwebtoken.Jwts
import org.joda.time.DateTime
import pl.gov.mf.etoll.commons.TimeUtils
import java.security.PrivateKey
import javax.inject.Inject

class JwtTokenGeneratorImpl @Inject constructor() : JwtTokenGenerator {

    override fun generate(
        applicationId: String,
        privateKey: PrivateKey
    ): JwtToken {

        val date = DateTime().toDateTimeISO()
        val formattedDate =
            date.toString(TimeUtils.DefaultDateTimeFormatter)
        val enc =
            Jwts.builder()
                .setHeaderParam("alg", "RS256")
                .claim("applicationId", applicationId)
                .claim("date", formattedDate)
                .signWith(privateKey)
                .compact()
                .split(".")

        return JwtToken(enc[0] + ".." + enc[2], applicationId, formattedDate)
    }
}