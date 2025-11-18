package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.response.AdminDashboardResponse;
import com.follysitou.sygpress.dto.response.UserDashboardResponse;
import com.follysitou.sygpress.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Tableaux de bord Admin et User")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer le tableau de bord administrateur",
            description = "Retourne les statistiques complètes pour l'administrateur: revenus, clients, tendances, etc.")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Récupérer le tableau de bord utilisateur",
            description = "Retourne les informations opérationnelles pour le gérant: livraisons du jour, en-cours, alertes, etc.")
    public ResponseEntity<UserDashboardResponse> getUserDashboard() {
        return ResponseEntity.ok(dashboardService.getUserDashboard());
    }
}
