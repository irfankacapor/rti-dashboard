'use client';
import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Button,
  Alert,
  CircularProgress,
  Typography,
  Paper
} from '@mui/material';
import { v4 as uuidv4 } from 'uuid';
import Papa from 'papaparse';

import { WizardContainer } from './WizardContainer';
import { CsvUploadSection } from './CsvUploadSection';
import { CsvTable } from './CsvTable';
import { DimensionMappingPopup } from './DimensionMappingPopup';
import { IndicatorAssignment } from './IndicatorAssignment';

import { 
  CsvFile, 
  CellSelection, 
  DimensionMapping, 
  ProcessedIndicator,
  CsvProcessingState 
} from '@/types/csvProcessing';
import { Subarea } from '@/types/subareas';
import { csvProcessingService } from '@/services/csvProcessingService';
import { 
  generateDataTuples, 
  validateDimensionMappings,
  generateTuplePreview 
} from '@/utils/coordinateProcessor';
import { getSubareas } from '@/services/subareaService';

const PHASES = [
  { id: 'upload', label: 'Upload CSV File' },
  { id: 'selection', label: 'Select Data Ranges' },
  { id: 'mapping', label: 'Map Dimensions' },
  { id: 'assignment', label: 'Assign Indicators' }
];

export const CsvProcessingStep: React.FC = () => {
  const [state, setState] = useState<CsvProcessingState>({
    currentPhase: 'upload',
    csvFile: undefined,
    csvData: undefined,
    dimensionMappings: [],
    processedIndicators: [],
    isLoading: false,
    error: undefined
  });

  const [subareas, setSubareas] = useState<Subarea[]>([]);
  const [popupAnchor, setPopupAnchor] = useState<HTMLElement | null>(null);
  const [currentSelection, setCurrentSelection] = useState<CellSelection | null>(null);
  const tableRef = useRef<HTMLDivElement>(null);

  // Load subareas on component mount
  useEffect(() => {
    const loadSubareas = async () => {
      try {
        const subareasData = await getSubareas();
        setSubareas(subareasData);
      } catch (error) {
        console.error('Failed to load subareas:', error);
      }
    };
    loadSubareas();
  }, []);

  const handleFileUploaded = async (csvFile: CsvFile) => {
    setState(prev => ({ ...prev, csvFile, isLoading: true, error: undefined }));

    try {
      // Parse CSV data
      const csvText = await csvFile.file.text();
      const result = Papa.parse(csvText, { header: false });
      
      if (result.errors.length > 0) {
        throw new Error(`CSV parsing errors: ${result.errors.map(e => e.message).join(', ')}`);
      }

      const csvData = result.data as string[][];
      setState(prev => ({ 
        ...prev, 
        csvData, 
        currentPhase: 'selection',
        isLoading: false 
      }));
    } catch (error) {
      setState(prev => ({ 
        ...prev, 
        error: error instanceof Error ? error.message : 'Failed to process CSV file',
        isLoading: false 
      }));
    }
  };

  const handleFileRemoved = (fileId: string) => {
    setState({
      currentPhase: 'upload',
      csvFile: undefined,
      csvData: undefined,
      dimensionMappings: [],
      processedIndicators: [],
      isLoading: false,
      error: undefined
    });
  };

  const handleCellRangeSelect = (selection: CellSelection, event?: React.MouseEvent | React.KeyboardEvent) => {
    setCurrentSelection(selection);
    if (event && event.target instanceof HTMLElement) {
      setPopupAnchor(event.target);
    } else {
      setPopupAnchor(tableRef.current);
    }
  };

  const handleMappingConfirm = (mapping: DimensionMapping) => {
    setState(prev => ({
      ...prev,
      dimensionMappings: [...prev.dimensionMappings, mapping]
    }));
    setCurrentSelection(null);
    setPopupAnchor(null);
  };

  const handleMappingCancel = () => {
    setCurrentSelection(null);
    setPopupAnchor(null);
  };

  const handleRemoveMapping = (mappingId: string) => {
    setState(prev => ({
      ...prev,
      dimensionMappings: prev.dimensionMappings.filter(m => m.id !== mappingId)
    }));
  };

  const handleProcessMappings = async () => {
    if (!state.csvData) return;

    setState(prev => ({ ...prev, isLoading: true, error: undefined }));

    try {
      // Validate mappings
      const validation = validateDimensionMappings(state.dimensionMappings);
      if (!validation.isValid) {
        throw new Error(`Mapping validation failed: ${validation.errors.join(', ')}`);
      }

      // Generate data tuples
      const tuples = generateDataTuples(state.dimensionMappings, state.csvData);
      
      // Group by indicator name to create processed indicators
      const indicatorMap = new Map<string, ProcessedIndicator>();
      
      tuples.forEach(tuple => {
        const indicatorName = tuple.coordinates.indicator_names || 'Unknown Indicator';
        
        if (!indicatorMap.has(indicatorName)) {
          indicatorMap.set(indicatorName, {
            id: uuidv4(),
            name: indicatorName,
            dimensions: Object.keys(tuple.coordinates).filter(key => key !== 'indicator_names'),
            valueCount: 0,
            unit: tuple.coordinates.unit,
            source: tuple.coordinates.source,
            dataPoints: [] // Initialize empty array for data points
          });
        }
        
        const indicator = indicatorMap.get(indicatorName)!;
        indicator.valueCount++;
        
        // Add the actual data point with dimensional context
        const dataPoint = {
          value: parseFloat(tuple.value) || 0,
          timeValue: tuple.coordinates.time,
          timeType: 'year', // Default to year, could be made configurable
          locationValue: tuple.coordinates.locations,
          locationType: 'state', // Default to state, could be made configurable
          customDimensions: Object.entries(tuple.coordinates)
            .filter(([key, value]) => 
              key !== 'indicator_names' && 
              key !== 'time' && 
              key !== 'locations' && 
              key !== 'unit' && 
              key !== 'source' && 
              value
            )
            .reduce((acc, [key, value]) => ({ ...acc, [key]: value }), {})
        };
        
        indicator.dataPoints!.push(dataPoint);
      });

      const processedIndicators = Array.from(indicatorMap.values());
      
      setState(prev => ({
        ...prev,
        processedIndicators,
        currentPhase: 'assignment',
        isLoading: false
      }));
    } catch (error) {
      setState(prev => ({
        ...prev,
        error: error instanceof Error ? error.message : 'Failed to process mappings',
        isLoading: false
      }));
    }
  };

  const handleIndicatorAssign = (indicatorId: string, field: 'subareaId' | 'direction', value: string) => {
    setState(prev => ({
      ...prev,
      processedIndicators: prev.processedIndicators.map(indicator =>
        indicator.id === indicatorId
          ? { ...indicator, [field]: value }
          : indicator
      )
    }));
  };

  const handleSubmitIndicators = async () => {
    console.log('Starting submit indicators...');
    console.log('Processed indicators:', state.processedIndicators);
    
    setState(prev => ({ ...prev, isLoading: true, error: undefined }));

    try {
      // Submit to backend
      const response = await csvProcessingService.submitProcessedIndicators(state.processedIndicators);
      
      // Mark step as completed
      console.log('Indicators submitted successfully:', response);
      
      // Set loading to false and show success
      setState(prev => ({
        ...prev,
        isLoading: false,
        error: undefined
      }));
      
      // You could also show a success message here
      // For now, we'll just log it
      console.log(`Successfully created ${response.createdIndicators.length} indicators with ${response.totalFactRecords} fact records`);
      
    } catch (error) {
      console.error('Error submitting indicators:', error);
      setState(prev => ({
        ...prev,
        error: error instanceof Error ? error.message : 'Failed to submit indicators',
        isLoading: false
      }));
    }
  };

  const canProceedToMapping = state.dimensionMappings.length > 0;
  const canProceedToAssignment = state.processedIndicators.length > 0;

  const renderPhaseContent = () => {
    switch (state.currentPhase) {
      case 'upload':
        return (
          <CsvUploadSection
            onFileUploaded={handleFileUploaded}
            onFileRemoved={handleFileRemoved}
            uploadedFile={state.csvFile}
            disabled={state.isLoading}
          />
        );

      case 'selection':
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Select Data Ranges
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Select cells in the CSV table to map them to different dimensions. You must map indicator values and at least one dimension (e.g., time, location, or any other).
            </Typography>
            
            {state.csvData && (
              <CsvTable
                data={state.csvData}
                onCellRangeSelect={handleCellRangeSelect}
                existingMappings={state.dimensionMappings}
              />
            )}

            <Box mt={3} display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="body2" color="text.secondary">
                  {state.dimensionMappings.length > 0 
                    ? `${state.dimensionMappings.length} mapping(s) created`
                    : 'No mappings created yet'
                  }
                </Typography>
              </Box>
              
              <Button
                variant="contained"
                onClick={handleProcessMappings}
                disabled={!canProceedToMapping || state.isLoading}
              >
                {state.isLoading ? 'Processing...' : 'Process Mappings'}
              </Button>
            </Box>
          </Box>
        );

      case 'mapping':
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Review and Process Mappings
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Review your dimension mappings and process them to generate indicators.
            </Typography>
            
            {/* This phase is typically skipped as we go directly to assignment */}
            <Button
              variant="contained"
              onClick={handleProcessMappings}
              disabled={state.isLoading}
            >
              {state.isLoading ? 'Processing...' : 'Continue to Assignment'}
            </Button>
          </Box>
        );

      case 'assignment':
        return (
          <IndicatorAssignment
            indicators={state.processedIndicators}
            subareas={subareas}
            onAssign={handleIndicatorAssign}
            onSubmit={handleSubmitIndicators}
            isLoading={state.isLoading}
          />
        );

      default:
        return null;
    }
  };

  const getCurrentStepIndex = () => {
    return PHASES.findIndex(phase => phase.id === state.currentPhase);
  };

  return (
    <WizardContainer
      title="CSV Data Processing"
      subtitle="Upload and process CSV data to create indicators"
      nextDisabled={state.isLoading}
      showNavigation={false}
    >
      {state.error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {state.error}
        </Alert>
      )}

      <Stepper activeStep={getCurrentStepIndex()} orientation="vertical" sx={{ mb: 3 }}>
        {PHASES.map((phase, index) => (
          <Step key={phase.id} completed={index < getCurrentStepIndex()}>
            <StepLabel>{phase.label}</StepLabel>
            <StepContent>
              <Box sx={{ mb: 2 }}>
                {index === getCurrentStepIndex() && renderPhaseContent()}
              </Box>
            </StepContent>
          </Step>
        ))}
      </Stepper>

      <DimensionMappingPopup
        open={!!currentSelection && !!popupAnchor}
        anchorEl={popupAnchor}
        selection={currentSelection}
        onConfirm={handleMappingConfirm}
        onCancel={handleMappingCancel}
        existingMappings={state.dimensionMappings}
      />

      {state.isLoading && (
        <Box display="flex" justifyContent="center" p={3}>
          <CircularProgress />
        </Box>
      )}
    </WizardContainer>
  );
}; 