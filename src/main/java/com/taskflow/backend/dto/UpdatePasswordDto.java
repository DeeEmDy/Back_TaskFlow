package com.taskflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Constraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.taskflow.backend.exception.PasswordMismatchException;

import java.lang.annotation.ElementType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@UpdatePasswordDto.MatchPassword
public class UpdatePasswordDto {

    @NotBlank(message = "El email no puede estar vacío")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "El formato del email no es válido"
    )
    @Size(max = 40, message = "El email no puede exceder los 40 caracteres")
    private String email;

    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Pattern(
            regexp = "^(?=(.*[0-9]){2})(?=(.*[a-z]){2})(?=(.*[A-Z]){2})(?=(.*[@.,_]){1}).{8,40}$",
            message = "La contraseña debe tener entre 8 y 40 caracteres, incluir al menos 2 números, "
            + "2 minúsculas, 2 mayúsculas y al menos un carácter especial, solo se permiten: ., _, @"
    )
    private String newPassword;

    @NotBlank(message = "La confirmación de contraseña no puede estar vacía")
    @Pattern(
            regexp = "^(?=(.*[0-9]){2})(?=(.*[a-z]){2})(?=(.*[A-Z]){2})(?=(.*[@.,_]){1}).{8,40}$",
            message = "La confirmación de contraseña debe tener entre 8 y 40 caracteres, incluir al menos 2 números, "
            + "2 minúsculas, 2 mayúsculas y al menos un carácter especial, solo se permiten: ., _, @"
    )
    private String repeatNewPassword;

    // Anotación personalizada para validar que las contraseñas coincidan
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PasswordMatchValidator.class)
    public @interface MatchPassword {

        String message() default "Las contraseñas no coinciden";

        Class<?>[] groups() default {};

        Class<? extends jakarta.validation.Payload>[] payload() default {};
    }

    public static class PasswordMatchValidator implements ConstraintValidator<MatchPassword, UpdatePasswordDto> {

        @Override
        public boolean isValid(UpdatePasswordDto dto, ConstraintValidatorContext context) {
            if (dto == null) {
                return true; // O manejar como un error, dependiendo de tu caso
            }
            String newPassword = dto.getNewPassword();
            String repeatNewPassword = dto.getRepeatNewPassword();

            // Verifica si las contraseñas coinciden
            if (newPassword == null || !newPassword.equals(repeatNewPassword)) {
                // Se lanza una excepción personalizada si las contraseñas no coinciden
                throw new PasswordMismatchException("Las contraseñas no coinciden");
            }

            return true; // Las contraseñas coinciden
        }
    }
}
