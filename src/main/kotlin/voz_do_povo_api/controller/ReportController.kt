package voz_do_povo_api.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import voz_do_povo_api.controller.requests.PublicationData
import voz_do_povo_api.service.ReportService

@RestController
@RequestMapping(value = ["/voz-do-povo"])
class ReportController (val reportService: ReportService) {

    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.CREATED)
    fun createReport(@RequestBody publicationData: PublicationData): Mono<PublicationData> {
        return reportService.createReport(publicationData)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findReport(@PathVariable id: String): Mono<PublicationData> {
        return reportService.findReport(id)
    }

    @GetMapping("/{email}/reports")
    @ResponseStatus(HttpStatus.OK)
    fun findReportsByEmail(@PathVariable email: String): Flux<PublicationData> {
        return reportService.findReportByEmail(email)
    }

}
