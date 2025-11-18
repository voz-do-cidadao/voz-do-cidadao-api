package voz_do_povo_api.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import voz_do_povo_api.controller.requests.Images
import voz_do_povo_api.controller.requests.PublicationData
import voz_do_povo_api.controller.requests.ReportRequest
import voz_do_povo_api.repository.VozDoPovoRepository
import java.util.UUID

@Service
class ReportService @Autowired constructor(val vozDoPovoRepository: VozDoPovoRepository){

    fun createReport (publicationData: PublicationData) : Mono<PublicationData> {
        publicationData.id = UUID.randomUUID().toString()
        return vozDoPovoRepository.save(publicationData)
    }

    fun findReport (id: String) : Mono<PublicationData> {
        return vozDoPovoRepository.findById(id)
    }

    fun findReportByEmail (email: String) : Flux<PublicationData> {
        return vozDoPovoRepository.findAllByEmail(email)
    }

    fun updateReportImages (id: String, image: Images) : Mono<PublicationData> {
        return vozDoPovoRepository.findById(id)
            .flatMap { publication ->
                val updatedImage = PublicationData(
                    id = publication.id,
                    userRequest = publication.userRequest,
                    reportAddressRequest = publication.reportAddressRequest,
                    report = ReportRequest(
                        images= publication.report.images
                            .let { imagesList ->
                                val updatedImages = imagesList.toMutableList()
                                updatedImages.add(image)
                                updatedImages
                            },
                        report = publication.report.report,
                        reportDescription = publication.report.reportDescription,
                        reportCategory = publication.report.reportCategory
                    )
                )

                vozDoPovoRepository.save(updatedImage)
            }
    }
}
