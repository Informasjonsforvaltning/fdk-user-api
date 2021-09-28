package no.fdk.userapi.contract

import no.fdk.userapi.utils.SSO_KEY
import no.fdk.userapi.utils.WiremockContext
import no.fdk.userapi.utils.apiGet
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Tag("contract")
class Terms : WiremockContext()  {

    @Nested
    internal inner class Altinn {

        @Test
        fun forbiddenWithWrongApiKey() {
            val response = apiGet(
                path = "/terms/altinn/11223344556",
                headers = mapOf(Pair("X-API-KEY", "wrong-key")))

            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun okWithEmptyBodyWhenNotFoundInAltinn() {
            val response = apiGet(
                path = "/terms/altinn/12345678901",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("", response["body"])
        }

        @Test
        fun chacksAllOrgsAltinnHasAssociatedWithThePerson() {
            val response = apiGet(
                path = "/terms/altinn/11223344556",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("910258028:1.0.0,123456789:0.0.0", response["body"])
        }

    }

    @Nested
    internal inner class Difi {

        @Test
        fun forbiddenWithWrongApiKey() {
            val response = apiGet(
                path = "/terms/difi?orgs=910258028",
                headers = mapOf(Pair("X-API-KEY", "wrong-key")))

            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun respondWithVersionZeroWhenNoAcceptationFound() {
            val response = apiGet(
                path = "/terms/difi?orgs=123456789",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("123456789:0.0.0", response["body"])
        }

        @Test
        fun checksAllGivenOrgs() {
            val response = apiGet(
                path = "/terms/difi?orgs=123456789,910258028,920210023",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("123456789:0.0.0,910258028:1.0.0,920210023:1.2.3", response["body"])
        }

    }

    @Nested
    internal inner class OsloKommune {

        @Test
        fun forbiddenWithWrongApiKey() {
            val response = apiGet(
                path = "/terms/oslokommune?orgnames=Drift",
                headers = mapOf(Pair("X-API-KEY", "wrong-key")))

            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun respondWithVersionZeroWhenNoAcceptationFound() {
            val response = apiGet(
                path = "/terms/oslokommune?orgnames=Drift",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("971183675:0.0.0", response["body"])
        }

        @Test
        fun checksAllGivenOrgs() {
            val response = apiGet(
                path = "/terms/oslokommune?orgnames=${java.net.URLEncoder.encode("Drift,Oslo Havn KF,Renovasjons- og gjenvinningsetaten", "utf-8")}",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("971183675:0.0.0,987592567:1.0.1,923954791:12.16.11", response["body"])
        }

    }
}
