package com.example.food.delivery.controller;

import com.example.food.delivery.dto.OwnerRegisterDto;
import com.example.food.delivery.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerAuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("ownerRegisterDto")) {
            model.addAttribute("ownerRegisterDto", new OwnerRegisterDto());
        }
        return "owner/register";
    }

    @PostMapping("/register")
    public String registerOwner(@Valid @ModelAttribute("ownerRegisterDto") OwnerRegisterDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "owner/register";
        }

        try {
            userService.registerOwner(dto);
            redirectAttributes.addFlashAttribute("success", "Partner registration successful! Please log in to manage your restaurant.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "owner/register";
        }
    }
}
