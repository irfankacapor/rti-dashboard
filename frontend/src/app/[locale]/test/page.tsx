"use client";
import { useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { Box, Typography, CircularProgress } from '@mui/material';

export default function TestPage() {
  const router = useRouter();
  const params = useParams();
  const locale = (params?.locale as string) || "en";

  useEffect(() => {
    // Redirect to dashboard immediately
    router.push(`/${locale}/dashboard`);
  }, [router, locale]);

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
        Redirecting to dashboard...
      </Typography>
    </Box>
  );
}