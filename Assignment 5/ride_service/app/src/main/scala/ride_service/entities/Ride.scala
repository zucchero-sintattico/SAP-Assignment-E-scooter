package ride_service.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import java.time.LocalDateTime

@Document(collection = "rides")
case class Ride (
  @Id
  id: String = null,
  startLocation: String,
  endLocation: String,
  startTime: LocalDateTime,
  endTime: LocalDateTime
)
