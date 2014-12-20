package hipchat

import com.thoughtworks.go.plugin.api.annotation.Extension
import com.thoughtworks.go.plugin.api.response.validation.{ ValidationError, ValidationResult }
import com.thoughtworks.go.plugin.api.task.{ Task, TaskConfig, TaskView }

@Extension
class HipChatNotificationTask extends Task {

  import HipChatNotificationTask._

  override def config(): TaskConfig = {
    val config = new TaskConfig()
    config.addProperty(NOTIFICATION_TYPE).withDefault("other")
    config.addProperty(MESSAGE)
    config.addProperty(ROOM).withDefault("DevTools") //todo: pull room list from server, allow configuration of default
    config
  }

  override def executor() = new HipChatNotificationTaskExecutor()

  override def view(): TaskView = new TaskView {
    val displayValue = "Hipchat notification"

    def template() = {
      try {
        io.Source.fromInputStream(getClass.getResourceAsStream("/views/task.template.html")).mkString
      } catch {
        case e: Exception => "Failed to find template: " + e.getMessage
      }
    }
  }

  override def validate(configuration: TaskConfig): ValidationResult = {
    val result = new ValidationResult()

    val msg = Option(configuration.getValue(MESSAGE))
    msg.filter(_.length > 10000).foreach(_ => result.addError(new ValidationError(MESSAGE, "Message must be less than 10k chars")))

    val room = Option(configuration.getValue(ROOM))
    room.filter(_.trim.isEmpty).foreach(_ => result.addError(new ValidationError(ROOM, "Room cannot be empty")))

    result
  }

}

object HipChatNotificationTask {
  val NOTIFICATION_TYPE = "NotificationType"
  val MESSAGE = "HipchatMessage"
  val ROOM = "HipchatRoom"
}