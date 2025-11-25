package voz_do_povo_api.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import voz_do_povo_api.controller.requests.Images
import voz_do_povo_api.controller.requests.PublicationData
import voz_do_povo_api.controller.requests.ReportRequest
import voz_do_povo_api.repository.VozDoPovoRepository
import java.util.UUID

@Service
class ReportService @Autowired constructor(
    private val gridFs: ReactiveGridFsTemplate,
    val vozDoPovoRepository: VozDoPovoRepository,
    val javaMailSender: JavaMailSender
){

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
                        images = publication.report.images
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

    fun sendEmail (pub: PublicationData): Mono<String> {
         return getImageBytes(pub.id!!)
                    .flatMap { bytes -> //n entra aqui
                        sendEmailImages(
                            to = listOf(pub.userRequest.email, "vozdocidadao01@gmail.com"),
                            subject = "Voz do cidadão",
                            htmlBody = """
    <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
        
        <h2 style="color: #0A84FF; margin-bottom: 10px;">
            📢 Novo Relatório Recebido
        </h2>

        <div style="background: #F7F9FA; padding: 15px 20px; border-radius: 10px; margin-bottom: 20px;">
            <h3 style="margin: 0 0 10px 0; color: #333;">
                📝 Tipo de denúncia:
            </h3>
            <p style="margin: 0; font-size: 16px; font-weight: bold;">
                ${pub.report.report}
            </p>
        </div>

        <div style="background: #FFFFFF; padding: 15px 20px; border-radius: 10px; border: 1px solid #DDD; margin-bottom: 20px;">
            <h3 style="margin: 0 0 10px 0; color: #333;">📄 Descrição</h3>
            <p style="margin: 0; line-height: 1.5;">
                ${pub.report.reportDescription}
            </p>
        </div>

        <div style="background: #FFFFFF; padding: 15px 20px; border-radius: 10px; border: 1px solid #DDD; margin-bottom: 20px;">
            <h3 style="margin: 0 0 10px 0; color: #333;">📍 Endereço do Relato</h3>
            <p style="margin: 5px 0;"><strong>Rua:</strong> ${pub.reportAddressRequest.street}</p>
            <p style="margin: 5px 0;"><strong>Número:</strong> ${pub.reportAddressRequest.number}</p>
            <p style="margin: 5px 0;"><strong>Cidade:</strong> ${pub.reportAddressRequest.city}</p>
            <p style="margin: 5px 0;"><strong>Estado:</strong> ${pub.reportAddressRequest.state}</p>
            <p style="margin: 5px 0;"><strong>Complemento:</strong> ${pub.reportAddressRequest.complement}</p>
            <p style="margin: 5px 0;"><strong>País:</strong> ${pub.reportAddressRequest.country}</p>
        </div>

        <h3 style="margin: 0 0 10px 0;">📸 Imagens enviadas:</h3>
        <p style="color: #777; margin-bottom: 15px;">As imagens estão anexadas abaixo.</p>

    </div>
""".trimIndent(),
                            images = bytes
                        ).then(
                            Mono.just("Email sent successfully"))
                    }
    }

    fun getImageBytes(id: String): Mono<List<ByteArray>> {

        return getImageId(id)
            .flatMap { pub ->
                val imgIds = pub.report.images.mapNotNull { it.id }
                Flux.fromIterable(imgIds)
                    .flatMap { imageId ->
                        gridFs.findOne(Query(Criteria.where("_id").`is`(imageId)))
                            .flatMap { gridFs.getResource(it) }
                            .flatMap { data ->
                                DataBufferUtils.join(data.content).map { buffer ->
                                    val bytes = ByteArray(buffer.readableByteCount())
                                    buffer.read(bytes)
                                    DataBufferUtils.release(buffer)
                                    bytes
                                }
                            }
                    }
                    .collectList()
            }
    }

    fun sendEmailImages(
        to: List<String>,
        subject: String,
        htmlBody: String,
        images: List<ByteArray>
    ): Mono<Void> {

        return Mono.fromRunnable {

            val mimeMessage = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

            helper.setFrom("vozdocidadao01@gmail.com")
            helper.setTo(to.toTypedArray())
            helper.setSubject(subject)

            val htmlImages = images.indices.joinToString("") { index ->
                """<img src="cid:image$index" style="max-width: 500px; display:block; margin-bottom:10px;" />"""
            }

            helper.setText(
                """
            <html>
                <body>
                    $htmlBody
                    <br/><br/>
                    $htmlImages
                </body>
            </html>
            """.trimIndent(), true
            )
            images.forEachIndexed { index, bytes ->
                helper.addInline("image$index", ByteArrayResource(bytes), "image/jpeg")
            }

            javaMailSender.send(mimeMessage)
        }
    }

    fun getImageId(id: String): Mono<PublicationData> {
        return vozDoPovoRepository.findById(id).flatMap { publication ->
            Mono.just(publication ?: throw NoSuchElementException("Image ID not found"))
        }
    }
}