"use client";
import React from "react";
import { Box, Container } from "@mui/material";
import AccessGuard from '@/components/common/AccessGuard';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <AccessGuard requiredAccess="admin">
      <Box sx={{ minHeight: "100vh", bgcolor: "grey.50" }}>
        <Container maxWidth="lg" sx={{ py: 4 }}>
          {children}
        </Container>
      </Box>
    </AccessGuard>
  );
} 