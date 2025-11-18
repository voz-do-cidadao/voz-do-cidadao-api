package voz_do_povo_api.service

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import reactor.core.publisher.Mono
import org.springframework.stereotype.Service
import voz_do_povo_api.controller.requests.Images
import voz_do_povo_api.controller.requests.PublicationData
import kotlin.jvm.java

@Service
class ReportImageService(
    private val template: ReactiveMongoTemplate,
    private val reportService: ReportService
) {
    fun uploadImageReport(publicationId: String, image: Images)
            : Mono<PublicationData> {

        val criteria = Criteria.where("_id").`is`(publicationId)
        val query = Query(criteria)

        val update = Update().addToSet("images", image) // ou addToSet para evitar duplicata

        template.update(PublicationData::class.java)
            .matching(query)
            .apply(update)
            .first()

        return reportService.updateReportImages(
            id = publicationId,
            image = image
        )
    }

}

