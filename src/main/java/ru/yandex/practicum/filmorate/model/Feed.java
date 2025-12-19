package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feed {

    @NotNull(message = "Идентификатор события не может отсутствовать.")
    Integer eventId;

    @PastOrPresent(message = "Время события не может быть в будущем.")
    @NotNull(message = "Время события не может отсутствовать.")
    Long timestamp;

    @JsonProperty("eventType")
    @NotNull(message = "Тип события не может отсутствовать.")
    FeedType feedType;

    @JsonProperty("operation")
    @NotNull(message = "Операция события не может отсутствовать.")
    FeedOperation feedOperation;

    @NotNull(message = "Идентификатор пользователя не может отсутствовать.")
    Integer userId;

    @NotNull(message = "Идентификатор сущности с которой связано событие не может отсутствовать.")
    Integer entityId;
}
