"use client";
import React, { useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { Box, Typography, CircularProgress } from '@mui/material';
import { 
  canAccessWizard, 
  canAccessAdmin, 
  canAccessDashboard,
  canAccessSubareaDetails,
  canAccessIndicatorModal,
  UserRole 
} from '@/utils/accessControl';

interface AccessGuardProps {
  children: React.ReactNode;
  requiredAccess: 'wizard' | 'admin' | 'dashboard' | 'subareaDetails' | 'indicatorModal';
  fallbackPath?: string;
}

export default function AccessGuard({ 
  children, 
  requiredAccess, 
  fallbackPath 
}: AccessGuardProps) {
  const { user, isLoading, isAuthenticated } = useAuth();
  const router = useRouter();
  const params = useParams();
  const locale = (params?.locale as string) || "en";

  useEffect(() => {
    if (isLoading) return;

    // If not authenticated, redirect to login
    if (!isAuthenticated) {
      router.push(`/${locale}/login`);
      return;
    }

    const userRole = user?.role as UserRole | null;
    let hasAccess = false;
    let defaultFallback = `/${locale}/dashboard`;

    switch (requiredAccess) {
      case 'wizard':
        hasAccess = canAccessWizard(userRole);
        break;
      case 'admin':
        hasAccess = canAccessAdmin(userRole);
        break;
      case 'dashboard':
        hasAccess = canAccessDashboard(userRole);
        break;
      case 'subareaDetails':
        hasAccess = canAccessSubareaDetails(userRole);
        break;
      case 'indicatorModal':
        hasAccess = canAccessIndicatorModal(userRole);
        break;
    }

    if (!hasAccess) {
      const redirectPath = fallbackPath || defaultFallback;
      router.push(redirectPath);
    }
  }, [isLoading, isAuthenticated, user, requiredAccess, fallbackPath, router, locale]);

  if (isLoading) {
    return (
      <Box 
        display="flex" 
        flexDirection="column" 
        alignItems="center" 
        justifyContent="center" 
        minHeight="50vh"
      >
        <CircularProgress />
        <Typography variant="body2" sx={{ mt: 2 }}>
          Checking access...
        </Typography>
      </Box>
    );
  }

  if (!isAuthenticated) {
    return (
      <Box 
        display="flex" 
        flexDirection="column" 
        alignItems="center" 
        justifyContent="center" 
        minHeight="50vh"
      >
        <Typography variant="h6" color="error">
          Authentication required
        </Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>
          Redirecting to login...
        </Typography>
      </Box>
    );
  }

  const userRole = user?.role as UserRole | null;
  let hasAccess = false;

  switch (requiredAccess) {
    case 'wizard':
      hasAccess = canAccessWizard(userRole);
      break;
    case 'admin':
      hasAccess = canAccessAdmin(userRole);
      break;
    case 'dashboard':
      hasAccess = canAccessDashboard(userRole);
      break;
    case 'subareaDetails':
      hasAccess = canAccessSubareaDetails(userRole);
      break;
    case 'indicatorModal':
      hasAccess = canAccessIndicatorModal(userRole);
      break;
  }

  if (!hasAccess) {
    return (
      <Box 
        display="flex" 
        flexDirection="column" 
        alignItems="center" 
        justifyContent="center" 
        minHeight="50vh"
      >
        <Typography variant="h6" color="error">
          Access Denied
        </Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>
          You don't have permission to access this page.
        </Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>
          Redirecting to dashboard...
        </Typography>
      </Box>
    );
  }

  return <>{children}</>;
} 