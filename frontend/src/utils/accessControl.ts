/**
 * Access Control System
 * 
 * This file defines the role-based access control for the RTI Dashboard frontend.
 * It aligns with the backend security configuration where:
 * - ADMIN: Full access to all features including wizard and admin panel
 * - MANAGER: Access to wizard for data management, but not admin panel
 * - USER: Read-only access to dashboard, subarea details, and indicator modal
 * 
 * All data modification endpoints in the backend require ADMIN or MANAGER roles.
 * All GET endpoints are publicly accessible.
 */

export type UserRole = "ADMIN" | "MANAGER" | "USER";

export interface AccessControlConfig {
  canAccessWizard: boolean;
  canAccessAdmin: boolean;
  canAccessDashboard: boolean;
  canAccessSubareaDetails: boolean;
  canAccessIndicatorModal: boolean;
}

export function getUserAccess(userRole: UserRole | null): AccessControlConfig {
  if (!userRole) {
    return {
      canAccessWizard: false,
      canAccessAdmin: false,
      canAccessDashboard: true,
      canAccessSubareaDetails: true,
      canAccessIndicatorModal: true,
    };
  }

  switch (userRole) {
    case "ADMIN":
      return {
        canAccessWizard: true,
        canAccessAdmin: true,
        canAccessDashboard: true,
        canAccessSubareaDetails: true,
        canAccessIndicatorModal: true,
      };
    case "MANAGER":
      return {
        canAccessWizard: true,
        canAccessAdmin: false, // Managers can't access admin panel
        canAccessDashboard: true,
        canAccessSubareaDetails: true,
        canAccessIndicatorModal: true,
      };
    case "USER":
      return {
        canAccessWizard: false,
        canAccessAdmin: false,
        canAccessDashboard: true,
        canAccessSubareaDetails: true,
        canAccessIndicatorModal: true,
      };
    default:
      return {
        canAccessWizard: false,
        canAccessAdmin: false,
        canAccessDashboard: true,
        canAccessSubareaDetails: true,
        canAccessIndicatorModal: true,
      };
  }
}

export function canAccessWizard(userRole: UserRole | null): boolean {
  return getUserAccess(userRole).canAccessWizard;
}

export function canAccessAdmin(userRole: UserRole | null): boolean {
  return getUserAccess(userRole).canAccessAdmin;
}

export function canAccessDashboard(userRole: UserRole | null): boolean {
  return getUserAccess(userRole).canAccessDashboard;
}

export function canAccessSubareaDetails(userRole: UserRole | null): boolean {
  return getUserAccess(userRole).canAccessSubareaDetails;
}

export function canAccessIndicatorModal(userRole: UserRole | null): boolean {
  return getUserAccess(userRole).canAccessIndicatorModal;
} 