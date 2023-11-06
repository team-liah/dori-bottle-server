package com.liah.doribottle.web.admin.asset

import com.liah.doribottle.extension.currentUserLoginId
import com.liah.doribottle.service.s3.AwsS3Service
import com.liah.doribottle.service.s3.dto.AwsS3UploadResultDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/admin/api/asset")
class AssetController(
    private val awsS3Service: AwsS3Service
) {
    @Operation(summary = "에셋 업로드")
    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @RequestPart(value = "file", required = true) multipartFile: MultipartFile,
    ): AwsS3UploadResultDto {
        return awsS3Service.uploadWithPublicRead(multipartFile, "${currentUserLoginId()}")
    }
}