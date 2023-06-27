package com.liah.doribottle.web.sample

import com.liah.doribottle.common.exception.NotFoundException
import com.liah.doribottle.web.sample.vm.SampleResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/sample")
@RestController
class SampleController {
    companion object {
        val mem = mapOf(
            1 to SampleResponse("1", "test-1"),
            2 to SampleResponse("2", "test-2"),
            3 to SampleResponse("3", "test-3"),
            4 to SampleResponse("4", "test-4")
        )
    }

    @GetMapping("/{id}")
    fun sampleById(
        @PathVariable id: Int
    ): ResponseEntity<SampleResponse?> {
        val result = mem[id] ?: throw NotFoundException("")
        return ResponseEntity.ok(result)
    }
}