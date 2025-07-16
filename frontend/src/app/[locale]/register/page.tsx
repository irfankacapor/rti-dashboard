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
import PersonAddIcon from "@mui/icons-material/PersonAdd";

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export default function RegisterPage() {
  const router = useRouter();
  const params = useParams();
  const locale = (params?.locale as string) || "en";

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, email, password }),
      });
      if (res.ok) {
        setSuccess(true);
        setTimeout(() => router.push(`/${locale}/login`), 1200);
      } else {
        const data = await res.json().catch(() => ({}));
        setError(data?.message || "Registration failed");
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
            <PersonAddIcon color="primary" sx={{ fontSize: 48 }} />
            <Typography variant="h4" fontWeight="bold">
              Register
            </Typography>
            {error && <Alert severity="error">{error}</Alert>}
            {success && <Alert severity="success">Registration successful! Redirecting to login...</Alert>}
            <Box component="form" onSubmit={handleSubmit} width="100%">
              <Stack spacing={2}>
                <TextField
                  label="Username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  fullWidth
                  required
                  autoFocus
                />
                <TextField
                  label="Email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  fullWidth
                  required
                />
                <TextField
                  label="Password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  fullWidth
                  required
                  helperText="At least 8 characters"
                />
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  fullWidth
                  size="large"
                  disabled={loading}
                  startIcon={<PersonAddIcon />}
                >
                  {loading ? "Registering..." : "Register"}
                </Button>
              </Stack>
            </Box>
            <Button
              variant="text"
              color="primary"
              onClick={() => router.push(`/${locale}/login`)}
            >
              Already have an account? Login
            </Button>
          </Stack>
        </Paper>
      </Box>
    </Container>
  );
} 