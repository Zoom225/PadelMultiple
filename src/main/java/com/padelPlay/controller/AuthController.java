package com.padelPlay.controller;

import com.padelPlay.dto.request.LoginRequest;
import com.padelPlay.dto.response.LoginResponse;
import com.padelPlay.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = " Points de terminaison pour l'authentification des administrateurs. " +
        "Les membres ne s'authentifient PAS ici — ils sont identifiés uniquement par leur matricule " +
        "et n'ont pas besoin de jeton pour accéder aux points de terminaison destinés aux membres." +
        "Cette authentification est réservée exclusivement aux administrateurs (GLOBAL ou SITE)" +
        "qui doivent accéder aux points de terminaison de gestion protégés." +
        "Processus d'authentification :" +
        "1. L'administrateur envoie son adresse e-mail et son mot de passe via POST /api/auth/login." +
        "2. Le serveur valide les identifiants et renvoie un jeton JWT valable pendant 24 heures."+
        "3. L'administrateur inclut le jeton dans toutes les requêtes suivantes via l'en-tête Authorization :" +
        "Authorization: Bearer <token>" +
        "4. Le jeton encode l'adresse e-mail et le rôle de l'administrateur (GLOBAL ou SITE)." +
        "Remarque de sécurité : le même message d'erreur est renvoyé que l'adresse e-mail n'existe pas" +
        "ou que le mot de passe soit incorrect — cela empêche les attaques par énumération d'utilisateurs.")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Administrator login",
            description = "Authentifie un administrateur à l'aide de son adresse e-mail et de son mot de passe." +
                    "Renvoie un jeton JWT à utiliser dans l'en-tête Authorization pour tous les points de terminaison protégés." +
                    "Le jeton est valable pendant 24 heures. " +
                    " La réponse comprend également les informations de profil et le rôle de l'administrateur : " +
                    " - GLOBAL : peut consulter et gérer tous les sites." +
                    "- SITE : peut uniquement gérer le site qui lui a été attribué." +
                    "Important : ce point de terminaison est public et ne nécessite aucun jeton existant." +
                    "N'utilisez PAS ce point de terminaison pour l'authentification des membres — les membres utilisent directement leur matricule."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials — wrong email or password. " +
                    "The same message is returned for both cases to prevent user enumeration.",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation error — email format invalid or fields missing",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
