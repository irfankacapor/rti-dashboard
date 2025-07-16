"use client";
import React from "react";
import { useRouter, useParams } from "next/navigation";
import { Container, Box, Typography, Button, Paper, Stack } from "@mui/material";

export default function AdminPage() {
  const router = useRouter();
  const params = useParams();
  const locale = (params?.locale as string) || "en";

  return (
    <Container maxWidth="sm">
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
      >
        <Paper elevation={3} sx={{ p: 6, borderRadius: 4, width: "100%" }}>
          <Stack spacing={4} alignItems="center">
            <Typography variant="h4" fontWeight="bold" gutterBottom>
              Admin Panel
            </Typography>
            <Stack spacing={2} width="100%">
              <Button
                variant="contained"
                color="primary"
                fullWidth
                size="large"
                onClick={() => router.push(`/${locale}/wizard`)}
              >
                Setup Dashboard
              </Button>
              <Button
                variant="outlined"
                color="primary"
                fullWidth
                size="large"
                disabled
                sx={{ opacity: 0.5 }}
              >
                Manage Users (Coming Soon)
              </Button>
              <Button
                variant="outlined"
                color="primary"
                fullWidth
                size="large"
                disabled
                sx={{ opacity: 0.5 }}
              >
                Edit Dashboard Data (Coming Soon)
              </Button>
            </Stack>
          </Stack>
        </Paper>
      </Box>
    </Container>
  );
} 