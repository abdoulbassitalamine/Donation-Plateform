package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.request.CreateUserRequest;
import fr.isima.glia.donoplatform.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * HTML views for authentication
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthWebController {

    private final IUserService userService;

    // Show register form
    @GetMapping(value = "/register", produces = MediaType.TEXT_HTML_VALUE)
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new CreateUserRequest());
        return "auth/register";
    }

    // Register via form
    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String register(@Valid @ModelAttribute("user") CreateUserRequest request,
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

    // Show login form
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

    // POST /auth/login for HTML handled by Spring Security formLogin()
}