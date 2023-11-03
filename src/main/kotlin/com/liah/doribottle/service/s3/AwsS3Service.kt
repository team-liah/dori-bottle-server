package com.liah.doribottle.service.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.util.IOUtils
import com.liah.doribottle.service.s3.dto.AwsS3UploadResultDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class AwsS3Service(
    private val amazonS3: AmazonS3,
    @Value("\${app.aws.s3.bucket}") private val bucket: String
) {
    fun uploadWithPublicRead(file: MultipartFile, path: String): AwsS3UploadResultDto {
        val saveFileName = "${UUID.randomUUID()}"
        val key = "${path}/$saveFileName"

        val tm = TransferManagerBuilder.standard()
            .withS3Client(amazonS3)
            .build()

        val request = PutObjectRequest(
            bucket,
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
            url = amazonS3.getUrl(bucket, key).toString()
        )
    }
}