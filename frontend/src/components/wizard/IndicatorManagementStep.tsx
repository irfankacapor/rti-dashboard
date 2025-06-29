'use client';
import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Alert,
  CircularProgress,
  Snackbar,
  Paper,
  Grid,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Add as AddIcon,
  Upload as UploadIcon,
  Refresh as RefreshIcon,
  Download as DownloadIcon,
} from '@mui/icons-material';
import { useWizardStore } from '@/store/wizardStore';
import { useWizardStore as useMainWizardStore } from '@/lib/store/useWizardStore';
import { ManagedIndicator } from '@/types/indicators';
import { IndicatorTable } from './IndicatorTable';
import { AddIndicatorForm } from './AddIndicatorForm';
import { BulkIndicatorActions } from './BulkIndicatorActions';
import { AddMoreCsvSection } from './AddMoreCsvSection';

interface IndicatorManagementStepProps {
  onNavigateToStep: (stepIndex: number) => void;
}

export const IndicatorManagementStep: React.FC<IndicatorManagementStepProps> = ({
  onNavigateToStep,
}) => {
  const {
    dirtyIndicators,
    dirtySubareas,
    updateManagedIndicator,
    addManualIndicator,
    deleteManagedIndicator,
    bulkUpdateIndicators,
    bulkDeleteIndicators,
    validateIndicatorData,
    isLoadingIndicators,
    isSaving,
    fetchManagedIndicators,
  } = useWizardStore();

  // Main wizard store for step completion
  const setStepCompleted = useMainWizardStore((state) => state.setStepCompleted);
  const setStepValid = useMainWizardStore((state) => state.setStepValid);

  const [showAddForm, setShowAddForm] = useState(false);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState<string | null>(null);

  // Fetch indicators on component mount
  useEffect(() => {
    fetchManagedIndicators();
  }, [fetchManagedIndicators]);

  // Validation: at least 1 indicator required
  const validation = validateIndicatorData();
  const isStepValid = dirtyIndicators.length > 0 && validation.isValid;

  // Mark step as valid when validation passes
  useEffect(() => {
    setStepValid(4, isStepValid);
  }, [isStepValid, setStepValid]);

  const handleIndicatorUpdate = (id: string, updates: Partial<ManagedIndicator>) => {
    try {
      updateManagedIndicator(id, updates);
      setSnackbar('Indicator updated successfully');
    } catch (error) {
      setError('Failed to update indicator');
    }
  };

  const handleIndicatorDelete = (id: string) => {
    try {
      deleteManagedIndicator(id);
      setSelectedIds(selectedIds.filter(selectedId => selectedId !== id));
      setSnackbar('Indicator deleted successfully');
    } catch (error) {
      setError('Failed to delete indicator');
    }
  };

  const handleBulkUpdate = (updates: { id: string; updates: Partial<ManagedIndicator> }[]) => {
    try {
      bulkUpdateIndicators(updates);
      setSnackbar(`${updates.length} indicators updated successfully`);
    } catch (error) {
      setError('Failed to update indicators');
    }
  };

  const handleBulkDelete = (ids: string[]) => {
    try {
      bulkDeleteIndicators(ids);
      setSelectedIds([]);
      setSnackbar(`${ids.length} indicators deleted successfully`);
    } catch (error) {
      setError('Failed to delete indicators');
    }
  };

  const handleAddMoreCsv = () => {
    // Navigate back to CSV processing step
    onNavigateToStep(3);
  };

  const handleManualAdd = (indicatorData: any) => {
    try {
      addManualIndicator(indicatorData);
      setShowAddForm(false);
      setSnackbar('Indicator added successfully');
    } catch (error) {
      setError('Failed to add indicator');
    }
  };

  const selectedIndicators = dirtyIndicators.filter(indicator => 
    selectedIds.includes(indicator.id)
  );

  const stats = {
    total: dirtyIndicators.length,
    fromCsv: dirtyIndicators.filter(i => i.isFromCsv).length,
    manual: dirtyIndicators.filter(i => i.isManual).length,
    input: dirtyIndicators.filter(i => i.direction === 'input').length,
    output: dirtyIndicators.filter(i => i.direction === 'output').length,
    assigned: dirtyIndicators.filter(i => i.subareaId).length,
    unassigned: dirtyIndicators.filter(i => !i.subareaId).length,
  };

  if (isLoadingIndicators) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header with stats */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Typography variant="h6">Indicator Overview</Typography>
          <Box display="flex" gap={1}>
            <Button
              variant="outlined"
              startIcon={<AddIcon />}
              onClick={() => setShowAddForm(true)}
              disabled={isSaving}
            >
              Add Manual
            </Button>
            <Button
              variant="outlined"
              startIcon={<UploadIcon />}
              onClick={handleAddMoreCsv}
              disabled={isSaving}
            >
              Add More CSV
            </Button>
          </Box>
        </Box>

        <Grid container spacing={2}>
          <Grid xs={12} sm={6} md={3}>
            <Box textAlign="center">
              <Typography variant="h4" color="primary">
                {stats.total}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Total Indicators
              </Typography>
            </Box>
          </Grid>
          <Grid xs={12} sm={6} md={3}>
            <Box textAlign="center">
              <Typography variant="h4" color="success.main">
                {stats.fromCsv}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                From CSV
              </Typography>
            </Box>
          </Grid>
          <Grid xs={12} sm={6} md={3}>
            <Box textAlign="center">
              <Typography variant="h4" color="info.main">
                {stats.input}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Input Indicators
              </Typography>
            </Box>
          </Grid>
          <Grid xs={12} sm={6} md={3}>
            <Box textAlign="center">
              <Typography variant="h4" color="warning.main">
                {stats.output}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Output Indicators
              </Typography>
            </Box>
          </Grid>
        </Grid>

        {stats.unassigned > 0 && (
          <Alert severity="warning" sx={{ mt: 2 }}>
            {stats.unassigned} indicators are not assigned to any subarea
          </Alert>
        )}
      </Paper>

      {/* Validation errors */}
      {!validation.isValid && validation.errors.length > 0 && (
        <Alert severity="error" sx={{ mb: 3 }}>
          <Typography variant="subtitle2" gutterBottom>
            Please fix the following issues:
          </Typography>
          <ul style={{ margin: 0, paddingLeft: '20px' }}>
            {validation.errors.map((error, index) => (
              <li key={index}>{error}</li>
            ))}
          </ul>
        </Alert>
      )}

      {/* Bulk actions */}
      {selectedIds.length > 0 && (
        <BulkIndicatorActions
          selectedIndicators={selectedIndicators}
          subareas={dirtySubareas}
          onBulkUpdate={handleBulkUpdate}
          onBulkDelete={handleBulkDelete}
        />
      )}

      {/* Indicators table */}
      <Paper sx={{ mb: 3 }}>
        <IndicatorTable
          indicators={dirtyIndicators}
          subareas={dirtySubareas}
          selectedIds={selectedIds}
          onSelectionChange={setSelectedIds}
          onIndicatorUpdate={handleIndicatorUpdate}
          onIndicatorDelete={handleIndicatorDelete}
          onBulkUpdate={handleBulkUpdate}
          isSaving={isSaving}
        />
      </Paper>

      {/* Add more CSV section */}
      <AddMoreCsvSection onNavigateToCsv={handleAddMoreCsv} />

      {/* Add indicator form */}
      {showAddForm && (
        <AddIndicatorForm
          subareas={dirtySubareas}
          onIndicatorAdd={handleManualAdd}
          onCancel={() => setShowAddForm(false)}
        />
      )}

      {/* Error snackbar */}
      <Snackbar
        open={!!error}
        autoHideDuration={6000}
        onClose={() => setError(null)}
      >
        <Alert severity="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      </Snackbar>

      {/* Success snackbar */}
      <Snackbar
        open={!!snackbar}
        autoHideDuration={3000}
        onClose={() => setSnackbar(null)}
      >
        <Alert severity="success" onClose={() => setSnackbar(null)}>
          {snackbar}
        </Alert>
      </Snackbar>
    </Box>
  );
}; 