package com.epam.brn.service.impl

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.ListObjectsV2Result
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.epam.brn.cloud.AwsCloudService
import com.epam.brn.config.AwsConfig
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AwsCloudServiceTest {

    companion object {
        const val X_AMZ_SIGNATURE = "x-amz-signature"
        const val X_AMZ_META_UUID = "x-amz-meta-uuid"
        const val X_AMZ_SERVER_SIDE_ENCRYPTION = "x-amz-server-side-encryption"
        const val X_AMZ_CREDENTIAL = "x-amz-credential"
        const val X_AMZ_ALGORITHM = "x-amz-algorithm"
        const val X_AMZ_DATE = "x-amz-date"
        const val ACL = "acl"
        const val BUCKET = "bucket"

        const val TEST_ACCESS_RULE = "private"
        const val TEST_BUCKET = "somebucket"
        const val TEST_UUID = "c49791b2-b27b-4edf-bac8-8734164c20e6"
        const val TEST_AMZ_DATE = "20200130T113917Z"
        const val TEST_ENC_ALGORYTHM = "AES256"
        const val TEST_HASH_ALGORYTHM = "AWS4-HMAC-SHA256"
        const val TEST_EXPIRATION_DATE = "2020-01-30T21:39:17.114Z"
        const val TEST_CREDENTIAL = "AKIAI7KLKATWVCMEKGPA/20200130/us-east-2/s3/aws4_request"
        const val TEST_FILEPATH = "tasks/\${filename}"
        const val TEST_DATE = "20200130"

        const val FILE = "file"
        const val FOLDER = "folder/"
        const val SUBFILE = "folder/file"
        const val SUBFOLDER = "folder/folder/"
        const val ANOTHER_FILE = "file3"
        const val ANOTHER_FOLDER = "folder3/"
        const val ANOTHER_SUBFILE = "folder3/file3"
        const val ANOTHER_SUBFOLDER = "folder3/folder3/"
    }

    @InjectMockKs
    lateinit var awsCloudService: AwsCloudService

    @MockK
    lateinit var awsConfig: AwsConfig

    @Test
    fun `should get correct signature for client upload`() {
        // GIVEN
        every { awsConfig.secretAccessKey } returns "99999999999999999999999999999"
        every { awsConfig.region } returns "us-east-2"
        every { awsConfig.serviceName } returns "s3"
        every { awsConfig.bucketLink } returns "http://somebucket.s3.amazonaws.com"

        val conditions: AwsConfig.Conditions = AwsConfig.Conditions(
            TEST_DATE,
            TEST_BUCKET, TEST_ACCESS_RULE,
            TEST_UUID,
            TEST_CREDENTIAL,
            TEST_AMZ_DATE,
            TEST_EXPIRATION_DATE,
            TEST_FILEPATH,
            "", "", ""
        )
        every { awsConfig.buildConditions(any()) } returns conditions

        // WHEN
        val actual = awsCloudService.uploadForm("")

        // THEN
        val expected: Map<String, Any> = mapOf(
            "action" to "http://somebucket.s3.amazonaws.com",
            "input" to listOf(
                mapOf("policy" to "ew0KICAiY29uZGl0aW9ucyIgOiBbIHsNCiAgICAiYnVja2V0IiA6ICJzb21lYnVja2V0Ig0KICB9LCB7DQogICAgImFjbCIgOiAicHJpdmF0ZSINCiAgfSwgWyAic3RhcnRzLXdpdGgiLCAiJGtleSIsICJ0YXNrcy8ke2ZpbGVuYW1lfSIgXSwgew0KICAgICJ4LWFtei1tZXRhLXV1aWQiIDogImM0OTc5MWIyLWIyN2ItNGVkZi1iYWM4LTg3MzQxNjRjMjBlNiINCiAgfSwgew0KICAgICJ4LWFtei1zZXJ2ZXItc2lkZS1lbmNyeXB0aW9uIiA6ICJBRVMyNTYiDQogIH0sIHsNCiAgICAieC1hbXotY3JlZGVudGlhbCIgOiAiQUtJQUk3S0xLQVRXVkNNRUtHUEEvMjAyMDAxMzAvdXMtZWFzdC0yL3MzL2F3czRfcmVxdWVzdCINCiAgfSwgew0KICAgICJ4LWFtei1hbGdvcml0aG0iIDogIkFXUzQtSE1BQy1TSEEyNTYiDQogIH0sIHsNCiAgICAieC1hbXotZGF0ZSIgOiAiMjAyMDAxMzBUMTEzOTE3WiINCiAgfSBdLA0KICAiZXhwaXJhdGlvbiIgOiAiMjAyMC0wMS0zMFQyMTozOToxNy4xMTRaIg0KfQ=="),
                mapOf(X_AMZ_SIGNATURE to "4d39e2b2ac5833352544d379dadad1ffba3148d9936d814f36f50b7af2cd8e8e"),
                mapOf("key" to TEST_FILEPATH),
                mapOf(ACL to TEST_ACCESS_RULE),
                mapOf(X_AMZ_META_UUID to TEST_UUID),
                mapOf(X_AMZ_SERVER_SIDE_ENCRYPTION to TEST_ENC_ALGORYTHM),
                mapOf(X_AMZ_CREDENTIAL to TEST_CREDENTIAL),
                mapOf(X_AMZ_ALGORITHM to TEST_HASH_ALGORYTHM),
                mapOf(X_AMZ_DATE to TEST_AMZ_DATE)
            )
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should convert to base64 string`() {
        // GIVEN
        val expected =
            "ew0KICAiY29uZGl0aW9ucyIgOiBbIHsNCiAgICAiYnVja2V0IiA6ICJzb21lYnVja2V0Ig0KICB9LCB7DQogICAgImFjbCIgOiAicHJpdmF0ZSINCiAgfSwgWyAic3RhcnRzLXdpdGgiLCAiJGtleSIsICJ0YXNrcy8ke2ZpbGVuYW1lfSIgXSwgew0KICAgICJ4LWFtei1tZXRhLXV1aWQiIDogImM0OTc5MWIyLWIyN2ItNGVkZi1iYWM4LTg3MzQxNjRjMjBlNiINCiAgfSwgew0KICAgICJ4LWFtei1zZXJ2ZXItc2lkZS1lbmNyeXB0aW9uIiA6ICJBRVMyNTYiDQogIH0sIHsNCiAgICAieC1hbXotY3JlZGVudGlhbCIgOiAiQUtJQUk3S0xLQVRXVkNNRUtHUEEvMjAyMDAxMzAvdXMtZWFzdC0yL3MzL2F3czRfcmVxdWVzdCINCiAgfSwgew0KICAgICJ4LWFtei1hbGdvcml0aG0iIDogIkFXUzQtSE1BQy1TSEEyNTYiDQogIH0sIHsNCiAgICAieC1hbXotZGF0ZSIgOiAiMjAyMDAxMzBUMTEzOTE3WiINCiAgfSBdLA0KICAiZXhwaXJhdGlvbiIgOiAiMjAyMC0wMS0zMFQyMTozOToxNy4xMTRaIg0KfQ=="
        val conditions = hashMapOf(
            "expiration" to TEST_EXPIRATION_DATE,
            "conditions" to
                listOf(
                    hashMapOf(BUCKET to TEST_BUCKET),
                    hashMapOf(ACL to TEST_ACCESS_RULE),
                    arrayOf("starts-with", "\$key", TEST_FILEPATH),
                    hashMapOf(X_AMZ_META_UUID to TEST_UUID),
                    hashMapOf(X_AMZ_SERVER_SIDE_ENCRYPTION to TEST_ENC_ALGORYTHM),
                    hashMapOf(X_AMZ_CREDENTIAL to TEST_CREDENTIAL),
                    hashMapOf(X_AMZ_ALGORITHM to TEST_HASH_ALGORYTHM),
                    hashMapOf(X_AMZ_DATE to TEST_AMZ_DATE)
                )
        )

        // WHEN
        val base64 = awsCloudService.toJsonBase64(conditions)

        // THEN
        assertEquals(expected, base64)
    }

    @Test
    fun `should get folder list without pagination`() {
        // GIVEN
        val mockS3 = mockk<AmazonS3>()
        val result: ListObjectsV2Result = listObjectsV2Result(listOf(FILE, FOLDER, SUBFILE, SUBFOLDER))
        every { result.isTruncated } returns false
        every { awsConfig.amazonS3 } returns mockS3
        every { awsConfig.bucketName } returns TEST_BUCKET
        every { mockS3.listObjectsV2(any<ListObjectsV2Request>()) } returns result

        // WHEN
        val listBucket = awsCloudService.listBucket()

        // THEN
        val expected: List<String> = listOf(FOLDER, SUBFOLDER)
        assertEquals(expected, listBucket)
    }

    @Test
    fun `should get folder list with pagination`() {
        // GIVEN
        val mockS3 = mockk<AmazonS3>()
        val result: ListObjectsV2Result = listObjectsV2Result(listOf(FILE, FOLDER, SUBFILE, SUBFOLDER))
        every { result.isTruncated } returns true
        every { result.nextContinuationToken } returns "asd"

        val result2: ListObjectsV2Result =
            listObjectsV2Result(listOf(ANOTHER_FILE, ANOTHER_FOLDER, ANOTHER_SUBFILE, ANOTHER_SUBFOLDER))
        every { result2.isTruncated } returns false

        every { awsConfig.amazonS3 } returns mockS3
        every { awsConfig.bucketName } returns TEST_BUCKET
        every { mockS3.listObjectsV2(any<ListObjectsV2Request>()) } returnsMany listOf(result, result2)

        // WHEN
        val listBucket = awsCloudService.listBucket()

        // THEN
        val expected: List<String> = listOf(FOLDER, SUBFOLDER, ANOTHER_FOLDER, ANOTHER_SUBFOLDER)
        assertEquals(expected, listBucket)
    }

    private fun listObjectsV2Result(keys: List<String>): ListObjectsV2Result {
        val result = mockk<ListObjectsV2Result>()
        val objectSummaries: List<S3ObjectSummary> = toObjectSummaries(keys)

        every { result.objectSummaries } returns objectSummaries
        return result
    }

    private fun toObjectSummaries(keys: List<String>): List<S3ObjectSummary> =
        keys.map { S3ObjectSummary().apply { key = it } }.toList()
}
