package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
public class User {

    @Null(groups = Marker.OnCreate.class, message = "При создании пользователя id должен быть null")
    @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении пользователя id не может быть null")
    @Positive(groups = Marker.OnUpdate.class, message = "При обновлении пользователя id должен быть положительным целым числом")
    private Integer id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull
    private LocalDate birthday;

    private final Set<Integer> friends = new HashSet<>();

    public @Null(groups = Marker.OnCreate.class, message = "При создании пользователя id должен быть null") @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении пользователя id не может быть null") @Positive(groups = Marker.OnUpdate.class, message = "При обновлении пользователя id должен быть положительным целым числом") Integer getId() {
        return this.id;
    }

    public @NotBlank(message = "Email не может быть пустым") @Email(message = "Некорректный формат email") String getEmail() {
        return this.email;
    }

    public @NotBlank(message = "Логин не может быть пустым") @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы") String getLogin() {
        return this.login;
    }

    public String getName() {
        return this.name;
    }

    public @PastOrPresent(message = "Дата рождения не может быть в будущем") @NotNull LocalDate getBirthday() {
        return this.birthday;
    }

    public Set<Integer> getFriends() {
        return this.friends;
    }

    public void setId(@Null(groups = Marker.OnCreate.class, message = "При создании пользователя id должен быть null") @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении пользователя id не может быть null") @Positive(groups = Marker.OnUpdate.class, message = "При обновлении пользователя id должен быть положительным целым числом") Integer id) {
        this.id = id;
    }

    public void setEmail(@NotBlank(message = "Email не может быть пустым") @Email(message = "Некорректный формат email") String email) {
        this.email = email;
    }

    public void setLogin(@NotBlank(message = "Логин не может быть пустым") @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы") String login) {
        this.login = login;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthday(@PastOrPresent(message = "Дата рождения не может быть в будущем") @NotNull LocalDate birthday) {
        this.birthday = birthday;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof User)) return false;
        final User other = (User) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$login = this.getLogin();
        final Object other$login = other.getLogin();
        if (this$login == null ? other$login != null : !this$login.equals(other$login)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$birthday = this.getBirthday();
        final Object other$birthday = other.getBirthday();
        if (this$birthday == null ? other$birthday != null : !this$birthday.equals(other$birthday)) return false;
        final Object this$friends = this.getFriends();
        final Object other$friends = other.getFriends();
        if (this$friends == null ? other$friends != null : !this$friends.equals(other$friends)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof User;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $login = this.getLogin();
        result = result * PRIME + ($login == null ? 43 : $login.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $birthday = this.getBirthday();
        result = result * PRIME + ($birthday == null ? 43 : $birthday.hashCode());
        final Object $friends = this.getFriends();
        result = result * PRIME + ($friends == null ? 43 : $friends.hashCode());
        return result;
    }

    public String toString() {
        return "User(id=" + this.getId() + ", email=" + this.getEmail() + ", login=" + this.getLogin() + ", name=" + this.getName() + ", birthday=" + this.getBirthday() + ", friends=" + this.getFriends() + ")";
    }
}
