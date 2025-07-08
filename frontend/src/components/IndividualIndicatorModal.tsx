import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, IconButton, ToggleButton, ToggleButtonGroup, MenuItem, Select, CircularProgress, Table, TableBody, TableCell, TableHead, TableRow, Paper, FormControl } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TimeSeriesChart from './charts/TimeSeriesChart';
import { useIndicatorData, useIndicatorDimensionValues } from '../hooks/useApi';
import { 
  processChartData, 
  determineTimeGranularity, 
  buildTimeRangeOptions, 
  generateDisplayLabel, 
  cleanTimeLabel 
} from '../utils/chartDataProcessor';
import { IndicatorDataResponse, IndicatorChartData } from '../types/indicators';

interface IndividualIndicatorModalProps {
  open: boolean;
  onClose: () => void;
  indicatorId: string;
  indicatorData: any;
}

const style = {
  position: 'absolute' as const,
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 800,
  bgcolor: 'background.paper',
  boxShadow: 24,
  p: 4,
  borderRadius: 2,
  maxHeight: '90vh',
  overflowY: 'auto',
};

const chartTypes = [
  { label: 'Bar', value: 'bar' },
  { label: 'Line', value: 'line' },
];

const IndividualIndicatorModal: React.FC<IndividualIndicatorModalProps> = ({ open, onClose, indicatorId, indicatorData }) => {
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');
  const [selectedDimension, setSelectedDimension] = useState<string>('');
  const [timeRange, setTimeRange] = useState('1Y');
  const [viewMode, setViewMode] = useState<'chart' | 'table'>('chart');
  const { data: dimensionMeta, loading: dimensionMetaLoading } = useIndicatorDimensionValues(indicatorId);

  // Get all available dimensions robustly
  const availableDimensions: string[] = React.useMemo(() => {
    if (dimensionMeta && dimensionMeta.availableDimensions) {
      // Extract the 'type' field from each dimension info object
      return dimensionMeta.availableDimensions.map((dim: any) => dim.type);
    }
    return ['time'];
  }, [dimensionMeta]);

  // Determine the default dimension (prefer 'time', else first available)
  const defaultDimension = React.useMemo(() => {
    if (availableDimensions.includes('time')) return 'time';
    return availableDimensions[0] || 'time';
  }, [availableDimensions]);

  // Set default dimension on load
  useEffect(() => {
    if (availableDimensions.length > 0 && !selectedDimension) {
      setSelectedDimension(defaultDimension);
    }
  }, [availableDimensions, selectedDimension, defaultDimension]);

  // Fetch indicator data (raw or aggregated depending on selected dimension)
  const { data, loading, error } = useIndicatorData(
    indicatorId,
    timeRange,
    selectedDimension,
    defaultDimension,
    availableDimensions
  );

  // Process chart data using utility function
  const chartData: IndicatorChartData[] = React.useMemo(() => {
    return processChartData({
      selectedDimension,
      defaultDimension,
      data
    });
  }, [selectedDimension, defaultDimension, data]);

  // Determine time granularity and build time range options
  const timeGranularity = React.useMemo(() => 
    determineTimeGranularity(chartData), [chartData]
  );
  
  const timeRanges = React.useMemo(() => 
    buildTimeRangeOptions(timeGranularity), [timeGranularity]
  );

  // Generate display label
  const displayLabel = React.useMemo(() => 
    generateDisplayLabel(selectedDimension, chartData), [selectedDimension, chartData]
  );

  // Only show error if not just empty data
  const showError = error && !loading && !chartData.length;

  // Prepare table data from original dataPoints
  const tableData = data?.originalDataPoints || [];

  return (
    <Modal open={open} onClose={onClose} aria-labelledby="indicator-modal-title">
      <Box sx={style}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography id="indicator-modal-title" variant="h6">
            {indicatorData?.name || 'Indicator'}
          </Typography>
          <IconButton onClick={onClose}><CloseIcon /></IconButton>
        </Box>
        
        <Box display="flex" alignItems="center" sx={{ mt: 2, mb: 2 }}>
          <ToggleButtonGroup
            value={viewMode}
            exclusive
            onChange={(_, v) => v && setViewMode(v)}
            size="small"
            sx={{ mr: 2 }}
          >
            <ToggleButton value="chart">Chart</ToggleButton>
            <ToggleButton value="table">Table</ToggleButton>
          </ToggleButtonGroup>
          
          {viewMode === 'chart' && (
            <>
              <ToggleButtonGroup
                value={chartType}
                exclusive
                onChange={(_, v) => v && setChartType(v)}
                size="small"
                sx={{ mr: 2 }}
              >
                {chartTypes.map((type) => (
                  <ToggleButton key={type.value} value={type.value}>{type.label}</ToggleButton>
                ))}
              </ToggleButtonGroup>
              
              {availableDimensions.length > 0 && (
                <FormControl size="small" sx={{ minWidth: 120, mr: 2 }}>
                  <Select
                    value={selectedDimension}
                    onChange={e => setSelectedDimension(e.target.value)}
                    displayEmpty
                  >
                    {availableDimensions.map((dim) => (
                      <MenuItem key={dim} value={dim}>
                        {dim.charAt(0).toUpperCase() + dim.slice(1)}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}
              
              {timeRanges.length > 0 && (
                <FormControl size="small" sx={{ minWidth: 80 }}>
                  <Select
                    value={timeRange}
                    onChange={e => setTimeRange(e.target.value)}
                    displayEmpty
                  >
                    {timeRanges.map((range) => (
                      <MenuItem key={range.value} value={range.value}>
                        {range.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}
            </>
          )}
        </Box>

        {loading ? (
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        ) : showError ? (
          <Typography color="error" sx={{ p: 2 }}>
            {error}
          </Typography>
        ) : (
          <>
            {viewMode === 'chart' && (
              <Box>
                {chartData.length >= 1 ? (
                  <TimeSeriesChart 
                    data={chartData} 
                    chartType={chartType} 
                    xAxisFormatter={cleanTimeLabel}
                  />
                ) : (
                  <Typography color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
                    No data available for this indicator.
                  </Typography>
                )}
              </Box>
            )}

            {viewMode === 'table' && (
              data?.originalDataPoints && data.originalDataPoints.length > 0 ? (
                <Paper sx={{ mt: 2, maxHeight: 400, overflow: 'auto' }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Timestamp</TableCell>
                        <TableCell>Value</TableCell>
                        {availableDimensions.filter((dim: string) => dim !== 'time').map((dim: string) => (
                          <TableCell key={dim}>{dim.charAt(0).toUpperCase() + dim.slice(1)}</TableCell>
                        ))}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {data.originalDataPoints.map((row: any, index: number) => (
                        <TableRow key={index}>
                          <TableCell>{row.timestamp || 'N/A'}</TableCell>
                          <TableCell>{row.value?.toFixed(2) || 'N/A'}</TableCell>
                          {availableDimensions.filter((dim: string) => dim !== 'time').map((dim: string) => (
                            <TableCell key={dim}>
                              {row.dimensions && row.dimensions[dim] ? row.dimensions[dim] : 'N/A'}
                            </TableCell>
                          ))}
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </Paper>
              ) : chartData.length > 0 ? (
                <Paper sx={{ mt: 2, maxHeight: 400, overflow: 'auto' }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>{displayLabel}</TableCell>
                        <TableCell>Value</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {chartData.map((row, index) => (
                        <TableRow key={index}>
                          <TableCell>{row.label}</TableCell>
                          <TableCell>{row.value?.toFixed(2) || 'N/A'}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </Paper>
              ) : (
                <Typography color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
                  No data available for this indicator.
                </Typography>
              )
            )}
          </>
        )}
      </Box>
    </Modal>
  );
};

export default IndividualIndicatorModal; 