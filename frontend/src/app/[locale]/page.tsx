"use client";
import React from 'react';
import { 
  Container, 
  Box, 
  Typography, 
  Button, 
  Paper,
  Stack
} from '@mui/material';
import { PlayArrow } from '@mui/icons-material';
import { useRouter } from 'next/navigation';
import { useTranslations } from 'next-intl';

export default function LandingPage() {
  const router = useRouter();
  const t = useTranslations('Landing');

  const handleStartConfiguration = () => {
    router.push('/en/wizard');
  };

  return (
    <Container maxWidth="md">
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
        textAlign="center"
      >
        <Paper 
          elevation={3}
          sx={{ 
            p: 6, 
            borderRadius: 4,
            background: 'linear-gradient(145deg, #f5f5f5 0%, #ffffff 100%)'
          }}
        >
          <Stack spacing={4} alignItems="center">
            {/* Logo placeholder */}
            <Box
              sx={{
                width: 120,
                height: 120,
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                mb: 2
              }}
              data-testid="logo"
            >
              <Typography variant="h3" color="white" fontWeight="bold">
                RTI
              </Typography>
            </Box>

            <Typography variant="h3" component="h1" fontWeight="bold" color="primary">
              RTI Dashboard
            </Typography>
            
            <Typography variant="h6" color="text.secondary" maxWidth="400px">
              Create comprehensive indicator dashboards with multi-dimensional data analysis
            </Typography>

            <Button
              variant="contained"
              size="large"
              startIcon={<PlayArrow />}
              onClick={handleStartConfiguration}
              sx={{ 
                px: 4, 
                py: 2, 
                fontSize: '1.2rem',
                borderRadius: 3,
                textTransform: 'none'
              }}
              data-testid="start-configuration-button"
            >
              Start Configuration
            </Button>
          </Stack>
        </Paper>
      </Box>
    </Container>
  );
}