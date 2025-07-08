'use client';
import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Button,
  Alert,
  CircularProgress,
  Typography
} from '@mui/material';
import { v4 as uuidv4 } from 'uuid';
import Papa from 'papaparse';
import * as XLSX from 'xlsx';

import { WizardContainer } from './WizardContainer';
import { CsvUploadSection } from './CsvUploadSection';
import { CsvTable } from './CsvTable';
import { DimensionMappingPopup } from './DimensionMappingPopup';
import { IndicatorAssignment } from './IndicatorAssignment';
import { EncodingFixStep } from './EncodingFixStep';

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
import { useWizardStore } from '@/lib/store/useWizardStore';

const PHASES = [
  { id: 'upload', label: 'Upload CSV File' },
  { id: 'selection', label: 'Select Data Ranges' },
  { id: 'mapping', label: 'Map Dimensions' },
  { id: 'encoding', label: 'Encoding Fixes' },
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
  const [showConfirmation, setShowConfirmation] = useState(false);

  const [encoding, setEncoding] = useState<string>('utf-8');
  const [delimiter, setDelimiter] = useState<string>('comma');

  const { setCurrentStep } = useWizardStore();

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

  const handleFileUploaded = async (csvFile: CsvFile, selectedEncoding: string, selectedDelimiter: string) => {
    setState(prev => ({ ...prev, csvFile, isLoading: true, error: undefined }));
    setEncoding(selectedEncoding);
    setDelimiter(selectedDelimiter);

    try {
      // Parse CSV or Excel data
      let csvData: string[][] = [];
      const fileNameLower = csvFile.name.toLowerCase();
      if (fileNameLower.endsWith('.xlsx') || fileNameLower.endsWith('.xls')) {
        // Parse Excel file
        const arrayBuffer = await csvFile.file.arrayBuffer();
        const workbook = XLSX.read(arrayBuffer, { type: 'array' });
        const firstSheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[firstSheetName];
        csvData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
      } else {
        // Parse CSV data
        let csvText: string;
        if (selectedEncoding === 'utf-8' || selectedEncoding === 'utf-8-bom') {
          csvText = await csvFile.file.text();
          if (selectedEncoding === 'utf-8-bom' && csvText.charCodeAt(0) === 0xFEFF) {
            csvText = csvText.slice(1); // Remove BOM
          }
        } else {
          // Use TextDecoder for other encodings
          const arrayBuffer = await csvFile.file.arrayBuffer();
          const decoder = new TextDecoder(selectedEncoding);
          csvText = decoder.decode(arrayBuffer);
        }
        let delimiterChar = ',';
        if (selectedDelimiter === ';') delimiterChar = ';';
        else if (selectedDelimiter === '\t') delimiterChar = '\t';
        // Use PapaParse with custom delimiter
        const result = Papa.parse(csvText, { header: false, delimiter: delimiterChar });
        if (result.errors.length > 0) {
          throw new Error(`CSV parsing errors: ${result.errors.map(e => e.message).join(', ')}`);
        }
        csvData = result.data as string[][];
      }
      setState(prev => ({ 
        ...prev, 
        csvData, 
        currentPhase: 'selection',
        isLoading: false 
      }));
    } catch (error) {
      setState(prev => ({ 
        ...prev, 
        error: error instanceof Error ? error.message : 'Failed to process file',
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

      // Move to encoding fix step instead of directly to assignment
      setState(prev => ({
        ...prev,
        currentPhase: 'encoding',
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

  const handleApplyEncodingFixes = (fixedData: string[][], appliedFixesCount: number) => {
    // Update the csvData with fixed version
    setState(prev => ({ ...prev, csvData: fixedData }));

    // Now process the fixed data into indicators
    try {
      const tuples = generateDataTuples(state.dimensionMappings, fixedData);
      const indicatorMap = new Map<string, ProcessedIndicator>();
      tuples.forEach(tuple => {
        const indicatorName = tuple.coordinates.indicator_names || 'Unknown Indicator';
        if (!indicatorMap.has(indicatorName)) {
          indicatorMap.set(indicatorName, {
            id: uuidv4(),
            name: indicatorName,
            dimensions: Object.keys(tuple.coordinates).filter(key =>
              !['indicator_names', 'time', 'locations', 'unit', 'source'].includes(key)
            ),
            valueCount: 0,
            unit: tuple.coordinates.unit,
            source: tuple.coordinates.source,
            dataPoints: []
          });
        }
        const indicator = indicatorMap.get(indicatorName)!;
        indicator.valueCount++;
        const dataPoint = {
          value: parseFloat(tuple.value) || 0,
          timeValue: tuple.coordinates.time,
          timeType: 'year',
          locationValue: tuple.coordinates.locations,
          locationType: 'state',
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
      // Show success message if fixes were applied
      if (appliedFixesCount > 0) {
        // Add success notification here
        console.log(`Applied ${appliedFixesCount} encoding fixes successfully`);
      }
    } catch (error) {
      setState(prev => ({
        ...prev,
        error: error instanceof Error ? error.message : 'Failed to process fixed data',
        isLoading: false
      }));
    }
  };

  const handleSkipEncodingFixes = () => {
    // Process data without encoding fixes
    handleApplyEncodingFixes(state.csvData || [], 0);
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
      
      setShowConfirmation(true);
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

      case 'encoding':
        return (
          <EncodingFixStep
            csvData={state.csvData || []}
            dimensionMappings={state.dimensionMappings}
            onApplyFixes={handleApplyEncodingFixes}
            onSkip={handleSkipEncodingFixes}
          />
        );

      case 'assignment':
        return (
          <>
            <IndicatorAssignment
              indicators={state.processedIndicators}
              subareas={subareas}
              onAssign={handleIndicatorAssign}
              onSubmit={handleSubmitIndicators}
              isLoading={state.isLoading}
            />
            {showConfirmation && (
              <Box mt={3}>
                <Alert severity="success" sx={{ mb: 2 }}>
                  Indicators submitted successfully!
                </Alert>
                <Box display="flex" gap={2}>
                  <Button variant="contained" color="primary" onClick={() => setCurrentStep(4)}>
                    Go to Indicator Overview
                  </Button>
                  <Button variant="outlined" color="secondary" onClick={() => { setShowConfirmation(false); setState(prev => ({ ...prev, currentPhase: 'upload' })); }}>
                    Upload More Indicators
                  </Button>
                </Box>
              </Box>
            )}
          </>
        );

      default:
        return null;
    }
  };

  const getCurrentStepIndex = () => {
    return PHASES.findIndex(phase => phase.id === state.currentPhase);
  };

  return (
    <>
      {state.error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {state.error}
        </Alert>
      )}

      {renderPhaseContent()}

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
    </>
  );
}; 