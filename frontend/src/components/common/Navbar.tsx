"use client";
import React, { useState } from "react";
import { AppBar, Toolbar, Typography, IconButton, Button, Menu, MenuItem, Avatar, Box } from "@mui/material";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import EditIcon from "@mui/icons-material/Edit";
import { useAuth } from "@/hooks/useAuth";
import { useDashboardEditMode } from "@/hooks/useDashboardEditMode";
import { useRouter, useParams, usePathname } from "next/navigation";
import { canAccessWizard, canAccessAdmin, UserRole } from "@/utils/accessControl";

export default function Navbar() {
  const { user, isAuthenticated, isLoading, logout } = useAuth();
  const { isEditMode, toggleEditMode } = useDashboardEditMode();
  const router = useRouter();
  const pathname = usePathname();
  const params = useParams();
  const locale = (params?.locale as string) || "en";
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleLogoClick = () => {
    router.push(`/${locale}/dashboard`);
  };

  const handleLogin = () => {
    router.push(`/${locale}/login`);
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    await logout();
    handleMenuClose();
    router.push(`/${locale}/dashboard`);
  };

  const handleWizardClick = () => {
    router.push(`/${locale}/wizard`);
    handleMenuClose();
  };

  const handleAdminClick = () => {
    router.push(`/${locale}/admin`);
    handleMenuClose();
  };

  const handleEditModeToggle = () => {
    toggleEditMode();
  };

  const userRole = user?.role as UserRole | null;
  const canAccessWizardPage = canAccessWizard(userRole);
  const canAccessAdminPage = canAccessAdmin(userRole);
  const canEditDashboard = canAccessWizardPage; // Same logic as wizard access

  return (
    <AppBar position="static" color="default" elevation={1} sx={{ mb: 2 }}>
      <Toolbar>
        <Typography
          variant="h6"
          component="div"
          sx={{ flexGrow: 1, cursor: "pointer", fontWeight: "bold" }}
          onClick={handleLogoClick}
        >
          RTI Dashboard
        </Typography>
        {isLoading ? null : isAuthenticated ? (
          <>
            {/* Dashboard Edit Mode Button - only show on dashboard page for admin/manager */}
            {canEditDashboard && pathname?.includes('/dashboard') && (
              <IconButton
                color={isEditMode ? "primary" : "default"}
                onClick={handleEditModeToggle}
                title={isEditMode ? "Exit Edit Mode" : "Enter Edit Mode"}
                sx={{ mr: 1 }}
              >
                <EditIcon />
              </IconButton>
            )}
            <IconButton color="inherit" onClick={handleMenuOpen} size="large">
              {user?.username ? (
                <Avatar sx={{ bgcolor: "primary.main" }}>
                  {user.username.charAt(0).toUpperCase()}
                </Avatar>
              ) : (
                <AccountCircleIcon fontSize="large" />
              )}
            </IconButton>
            <Menu
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              onClose={handleMenuClose}
              anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
              transformOrigin={{ vertical: "top", horizontal: "right" }}
            >
              {canAccessWizardPage && (
                <MenuItem onClick={handleWizardClick}>Setup Wizard</MenuItem>
              )}
              {canAccessAdminPage && (
                <MenuItem onClick={handleAdminClick}>Admin Panel</MenuItem>
              )}
              <MenuItem onClick={handleLogout}>Sign out</MenuItem>
            </Menu>
          </>
        ) : (
          <Button color="primary" variant="outlined" onClick={handleLogin}>
            Log in
          </Button>
        )}
      </Toolbar>
    </AppBar>
  );
} 