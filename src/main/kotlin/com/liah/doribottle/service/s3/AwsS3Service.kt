package com.liah.doribottle.service.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.util.IOUtils
import com.liah.doribottle.config.properties.AppProperties
import com.liah.doribottle.service.s3.dto.AwsS3UploadResultDto
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class AwsS3Service(
    appProperties: AppProperties,
    private val amazonS3: AmazonS3
) {
    val bucketName = appProperties.aws.s3.bucketName

    fun uploadWithPublicRead(file: MultipartFile, path: String): AwsS3UploadResultDto {
        val saveFileName = "${UUID.randomUUID()}"
        val key = "${path}/$saveFileName"

        val tm = TransferManagerBuilder.standard()
            .withS3Client(amazonS3)
            .build()

        val request = PutObjectRequest(
            bucketName,
            key,
            file.inputStream,
            ObjectMetadata().apply {
                this.contentType = file.contentType
                this.contentLength = IOUtils.toByteArray(file.inputStream).size.toLong()
            }
        )

        request.withCannedAcl(CannedAccessControlList.PublicRead)
        tm.upload(request).waitForCompletion()

        return AwsS3UploadResultDto(
            key = key,
            url = amazonS3.getUrl(bucketName, key).toString()
        )
    }
}