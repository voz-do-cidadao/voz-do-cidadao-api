package voz_do_povo_api.repository

import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import voz_do_povo_api.controller.requests.PublicationData

@Repository
interface VozDoPovoRepository : ReactiveCrudRepository<PublicationData, String> {

    @Query("{ 'userRequest.email': ?0 }")
    fun findAllByEmail(email: String): Flux<PublicationData>

}