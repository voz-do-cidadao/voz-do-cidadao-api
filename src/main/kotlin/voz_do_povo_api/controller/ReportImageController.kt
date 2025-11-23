package voz_do_povo_api.controller

import org.bson.Document
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import voz_do_povo_api.controller.requests.Images
import voz_do_povo_api.controller.requests.PublicationData
import voz_do_povo_api.repository.VozDoPovoRepository
import voz_do_povo_api.service.ReportImageService
import java.time.Instant
import java.util.Base64

@RestController
@RequestMapping(value = ["/voz-do-povo"])
class ReportImageController(
    private val gridFs: ReactiveGridFsTemplate,
    private val service: ReportImageService,
    val vozDoPovoRepository: VozDoPovoRepository
) {

    @PostMapping("/reportImage/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadImageByReportId(
        @PathVariable id: String,
        @RequestPart("image") file: FilePart,
        request: ServerHttpRequest
    ): Mono<Map<String, Any>> {

        val baseUrl = "${request.uri.scheme}://${request.uri.host}:${request.uri.port}"

        val meta = Document("uploadedAt", Instant.now().toString())
            .append("contentType", file.headers().contentType?.toString())

        return gridFs.store(
            file.content(),
            file.filename(),
            MediaType.APPLICATION_OCTET_STREAM.toString(),
            meta
        ).flatMap { fileId ->
            val image = Images(
                id = fileId.toString(),     // TODO: Implement UUID generation for image ID
                url = "$baseUrl/images/${fileId}",
                contentType = file.headers().contentType?.toString(),
                filename = file.filename(),
                uploadedAt = Instant.now()
            )

            service.uploadImageReport(id, image)
                .flatMap {
                    Mono.just(
                        mapOf(
                            "publicationId" to id,
                            "image" to image
                        )
                    )
                }
        }
    }

    @GetMapping("/reportImage/{id}/{imagePosition}")
    fun findImageByReportId(
        @PathVariable id: String,
        @PathVariable imagePosition: Int
    ): Mono<ResponseEntity<ByteArray>> {

        val imageId = getImageId(id)
            .block()?.report?.images?.get(imagePosition)?.id
            ?: return Mono.error(NoSuchElementException("Image not found"))

        return gridFs.findOne(Query(Criteria.where("_id").`is`(imageId)))
            .switchIfEmpty(Mono.error(NoSuchElementException("File not found")))
            .flatMap { gridFs.getResource(it) }
            .flatMap { resource ->
                DataBufferUtils.join(resource.content)
                    .map { buffer ->
                        val bytes = ByteArray(buffer.readableByteCount())
                        buffer.read(bytes)
                        DataBufferUtils.release(buffer)

                        ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .header("Content-Disposition", "inline; filename=\"image.jpg\"")
                            .body(bytes)
                    }
            }
    }

    fun getImageId(id: String): Mono<PublicationData> {
        return vozDoPovoRepository.findById(id).flatMap { publication ->
            Mono.just(publication ?: throw NoSuchElementException("Image ID not found"))
        }
    }

}