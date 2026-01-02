package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.request.CreateUserRequest;
import fr.isima.glia.donoplatform.request.LoginRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.response.JwtResponse;
import fr.isima.glia.donoplatform.security.jwt.JwtUtils;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IUserService userService;

    // ============================================================
    // REGISTER - JSON (API)
    // ============================================================
    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> registerJson(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("User registered successfully", userService.convertToDto(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ============================================================
    // REGISTER - HTML (Web)
    // ============================================================
    @GetMapping(value = "/register", produces = MediaType.TEXT_HTML_VALUE)
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new CreateUserRequest());
        return "auth/register";
    }

    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String registerHtml(@Valid @ModelAttribute("user") CreateUserRequest request,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.createUser(request);
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // ============================================================
    // LOGIN - JSON (API) → Retourne JWT
    // ============================================================
    @PostMapping(value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> loginJson(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateTokenForUser(authentication);
            DonoUserDetails userDetails = (DonoUserDetails) authentication.getPrincipal();
            JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), jwt);

            return ResponseEntity.ok(new ApiResponse("Login Successful", jwtResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid credentials", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ============================================================
    // LOGIN - HTML (Web) → Géré par Spring Security formLogin()
    // ============================================================
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String showLoginPage(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String registered,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (registered != null) {
            model.addAttribute("success", "Registration successful! You can now login.");
        }
        return "auth/login";
    }

    // Note: Le POST /auth/login pour HTML est géré automatiquement par
    // Spring Security via .loginProcessingUrl("/auth/login") dans DonoConfig
}