"use client";
import React from "react";
import { Container, Box, Typography, Paper } from "@mui/material";

export default function ManageUsersPage() {
  return (
    <Container maxWidth="sm">
      <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center" minHeight="60vh">
        <Paper elevation={3} sx={{ p: 6, borderRadius: 4, width: "100%" }}>
          <Typography variant="h5" fontWeight="bold" gutterBottom>
            Manage Users
          </Typography>
          <Typography color="text.secondary">This feature is coming soon.</Typography>
        </Paper>
      </Box>
    </Container>
  );
} 