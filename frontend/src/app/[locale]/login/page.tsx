"use client";
import React, { useState } from "react";
import { useRouter, useParams } from "next/navigation";
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  Alert,
  Stack,
} from "@mui/material";
import LockOpenIcon from "@mui/icons-material/LockOpen";
import { useAuth } from "@/hooks/useAuth";

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export default function LoginPage() {
  const router = useRouter();
  const params = useParams();
  const locale = (params?.locale as string) || "en";
  const { refresh, user } = useAuth();

  const [usernameOrEmail, setUsernameOrEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ usernameOrEmail, password }),
        credentials: "include",
      });
      if (res.ok) {
        const userRes = await fetch(`${API_BASE}/me`, { credentials: "include" });
        const userData = userRes.ok ? await userRes.json() : null;
        if (userData?.role === "ADMIN") {
          router.push(`/${locale}/admin`);
        } else {
          router.push(`/${locale}/dashboard`);
        }
        refresh();
      } else {
        const data = await res.json().catch(() => ({}));
        setError(data?.message || "Invalid credentials");
      }
    } catch (err) {
      setError("Network error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

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
          <Stack spacing={3} alignItems="center">
            <LockOpenIcon color="primary" sx={{ fontSize: 48 }} />
            <Typography variant="h4" fontWeight="bold">
              Login
            </Typography>
            {error && <Alert severity="error">{error}</Alert>}
            <Box component="form" onSubmit={handleSubmit} width="100%">
              <Stack spacing={2}>
                <TextField
                  label="Username or Email"
                  value={usernameOrEmail}
                  onChange={(e) => setUsernameOrEmail(e.target.value)}
                  fullWidth
                  required
                  autoFocus
                />
                <TextField
                  label="Password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  fullWidth
                  required
                />
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  fullWidth
                  size="large"
                  disabled={loading}
                  startIcon={<LockOpenIcon />}
                >
                  {loading ? "Logging in..." : "Login"}
                </Button>
              </Stack>
            </Box>
            <Button
              variant="text"
              color="primary"
              onClick={() => router.push(`/${locale}/register`)}
            >
              Don't have an account? Register
            </Button>
          </Stack>
        </Paper>
      </Box>
    </Container>
  );
} 