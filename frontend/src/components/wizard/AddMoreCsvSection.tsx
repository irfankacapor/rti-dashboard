'use client';
import React from 'react';
import {
  Paper,
  Typography,
  Button,
  Box,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';

interface AddMoreCsvSectionProps {
  onNavigateToCsv: () => void;
}

export const AddMoreCsvSection: React.FC<AddMoreCsvSectionProps> = ({
  onNavigateToCsv,
}) => {
  return (
    <Paper sx={{ p: 3, mt: 3, textAlign: 'center' }}>
      <Typography variant="h6" gutterBottom>
        Add More Indicators from CSV
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Upload additional CSV files to extract more indicators for your dashboard
      </Typography>
      <Button
        variant="outlined"
        size="large"
        startIcon={<AddIcon />}
        onClick={onNavigateToCsv}
      >
        Upload Additional CSV Data
      </Button>
    </Paper>
  );
}; 