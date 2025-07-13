import React, { useState, useEffect, useRef } from 'react';
import { Modal, Box, Typography, IconButton, ToggleButton, ToggleButtonGroup, MenuItem, Select, CircularProgress, Table, TableBody, TableCell, TableHead, TableRow, Paper, FormControl, Stack, Grid, useMediaQuery, useTheme, Fade, Collapse } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import IndividualIndicatorChart from './charts/IndividualIndicatorChart';
import { useIndicatorData, useIndicatorDimensionValues } from '../hooks/useApi';
import { 
  processChartData, 
  determineTimeGranularity, 
  buildTimeRangeOptions, 
  generateDisplayLabel, 
  cleanTimeLabel,
  AggregationType
} from '../utils/chartDataProcessor';
import { IndicatorChartData } from '../types/indicators';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';

interface IndividualIndicatorModalProps {
  open: boolean;
  onClose: () => void;
  indicatorId: string;
  indicatorData: any;
  subareaId?: string;
  comprehensiveData?: any; // NEW: Pass comprehensive data to avoid API calls
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

const AGGREGATION_OPTIONS: { label: string; value: AggregationType }[] = [
  { label: 'Sum', value: 'sum' },
  { label: 'Average', value: 'average' },
  { label: 'Min', value: 'min' },
  { label: 'Max', value: 'max' },
  { label: 'Median', value: 'median' },
  { label: 'Count', value: 'count' },
];

// Helper to group raw data by dimension value
function groupRawDataByDimension(indicatorValues: any[], selectedDimension: string): Record<string, number[]> {
  const groups: Record<string, number[]> = {};
  indicatorValues.forEach((item: any) => {
    let dimValue = '';
    if (["time", "year", "month", "day", "timestamp"].includes(selectedDimension) && (item.year || item.month || item.day || item.timeValue || item.timestamp)) {
      dimValue = item.year || item.month || item.day || item.timeValue || item.timestamp;
    } else if (selectedDimension === "location" && item.locationValue) {
      dimValue = item.locationValue;
    } else if (item[selectedDimension] !== undefined) {
      dimValue = item[selectedDimension];
    } else if (item.customDimensions && item.customDimensions[selectedDimension]) {
      dimValue = item.customDimensions[selectedDimension];
    }
    if (!dimValue) return;
    if (!groups[dimValue]) groups[dimValue] = [];
    groups[dimValue].push(item.value);
  });
  return groups;
}

// Helper to aggregate values
function aggregateValues(values: number[], aggregationType: string): number | null {
  if (!values || values.length === 0) return null;
  switch (aggregationType) {
    case 'sum':
      return values.reduce((a, b) => a + b, 0);
    case 'average':
      return values.reduce((a, b) => a + b, 0) / values.length;
    case 'min':
      return Math.min(...values);
    case 'max':
      return Math.max(...values);
    case 'median': {
      const sorted = [...values].sort((a, b) => a - b);
      const mid = Math.floor(sorted.length / 2);
      return sorted.length % 2 !== 0 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
    }
    case 'count':
      return values.length;
    default:
      return values.reduce((a, b) => a + b, 0);
  }
}

const IndividualIndicatorModal: React.FC<IndividualIndicatorModalProps> = ({ 
  open, 
  onClose, 
  indicatorId, 
  indicatorData, 
  subareaId,
  comprehensiveData 
}) => {
  const theme = useTheme();
  const isSmallScreen = useMediaQuery(theme.breakpoints.down('sm'));
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');
  const [selectedDimension, setSelectedDimension] = useState<string>('');
  const [timeRange, setTimeRange] = useState('1Y');
  const [viewMode, setViewMode] = useState<'chart' | 'table'>('chart');
  const [aggregationType, setAggregationType] = useState<AggregationType>('sum');
  const [showAggregation, setShowAggregation] = useState(false);
  const aggregationTypePerDimension = useRef<{ [dim: string]: AggregationType }>({});
  const [openRows, setOpenRows] = React.useState<Record<string, boolean>>({});
  
  // Use subarea data if available, otherwise fall back to API calls
  const useSubareaData = comprehensiveData && comprehensiveData.dimensionMetadata && comprehensiveData.dimensionMetadata[indicatorId];

  // Only make API calls if subarea data is not available
  const { data: dimensionMeta, loading: dimensionMetaLoading } = useIndicatorDimensionValues(
    useSubareaData ? '' : indicatorId, 
    useSubareaData ? undefined : subareaId
  );

  // Get all available dimensions robustly
  const availableDimensions: string[] = React.useMemo(() => {
    if (useSubareaData) {
      const metadata = comprehensiveData.dimensionMetadata[indicatorId];
      if (metadata && metadata.availableDimensions) {
        return metadata.availableDimensions.map((dim: any) => dim.type);
      }
    } else if (dimensionMeta && dimensionMeta.availableDimensions) {
      // Extract the 'type' field from each dimension info object
      return dimensionMeta.availableDimensions.map((dim: any) => dim.type);
    }
    return ['time'];
  }, [useSubareaData, comprehensiveData, indicatorId, dimensionMeta]);

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

  // Use subarea data for aggregated data if available
  const aggregatedData = useSubareaData ? comprehensiveData.aggregatedData : {};

  // Fetch indicator data (raw or aggregated depending on selected dimension)
  const { data: indicatorDataPoints, loading, error } = useIndicatorData(
    useSubareaData ? '' : indicatorId, // Don't make API call if we have subarea data
    timeRange,
    selectedDimension,
    defaultDimension,
    availableDimensions,
    useSubareaData ? undefined : subareaId
  );

  // Use subarea data for chart data if available
  const chartDataFromSubarea = useSubareaData && aggregatedData[selectedDimension] ? 
    Object.entries(aggregatedData[selectedDimension]).map(([key, value]) => ({
      label: key,
      value: value as number
    })) : [];

  // Get time series data from comprehensiveData if available
  const timeSeriesData = useSubareaData && comprehensiveData.indicatorTimeSeriesData && comprehensiveData.indicatorTimeSeriesData[indicatorId] ? 
    comprehensiveData.indicatorTimeSeriesData[indicatorId].map((point: { year: string; value: number }) => ({
      year: point.year,
      value: point.value
    })) : [];

  // Gather indicator values for the selected dimension
  const indicatorValues = React.useMemo(() => {
    // Prefer originalDataPoints if available
    if (indicatorDataPoints?.originalDataPoints && Array.isArray(indicatorDataPoints.originalDataPoints)) {
      return indicatorDataPoints.originalDataPoints;
    }
    // Try timeSeriesData for subarea
    if (useSubareaData && comprehensiveData?.indicatorTimeSeriesData && comprehensiveData.indicatorTimeSeriesData[indicatorId]) {
      return comprehensiveData.indicatorTimeSeriesData[indicatorId];
    }
    return [];
  }, [indicatorDataPoints, useSubareaData, comprehensiveData, indicatorId]);

  // Determine if any dimension value has multiple indicator values
  const hasMultiplePerDimension = React.useMemo(() => {
    if (!indicatorValues || !selectedDimension) return false;
    const grouped: Record<string, number> = {};
    indicatorValues.forEach((item: any) => {
      let dimValue = '';
      if (selectedDimension === 'time' && (item.timeValue || item.timestamp || item.year)) {
        dimValue = item.timeValue || item.timestamp || item.year;
      } else if (selectedDimension === 'location' && item.locationValue) {
        dimValue = item.locationValue;
      } else if (item[selectedDimension] !== undefined) {
        dimValue = item[selectedDimension];
      } else if (item.customDimensions && item.customDimensions[selectedDimension]) {
        dimValue = item.customDimensions[selectedDimension];
      }
      if (!dimValue) return;
      grouped[dimValue] = (grouped[dimValue] || 0) + 1;
    });
    return Object.values(grouped).some(count => count > 1);
  }, [indicatorValues, selectedDimension]);

  // Animate aggregation dropdown in/out
  useEffect(() => {
    setShowAggregation(hasMultiplePerDimension);
  }, [hasMultiplePerDimension]);

  // Persist aggregation type per dimension
  useEffect(() => {
    if (showAggregation) {
      aggregationTypePerDimension.current[selectedDimension] = aggregationType;
    }
  }, [aggregationType, selectedDimension, showAggregation]);
  useEffect(() => {
    if (showAggregation && aggregationTypePerDimension.current[selectedDimension]) {
      setAggregationType(aggregationTypePerDimension.current[selectedDimension]);
    } else if (!showAggregation) {
      setAggregationType('sum');
    }
  }, [selectedDimension, showAggregation]);

  // Use aggregation logic if needed
  const chartData: IndicatorChartData[] = React.useMemo(() => {
    if (indicatorValues && selectedDimension) {
      return processChartData({
        selectedDimension,
        defaultDimension,
        data: indicatorDataPoints,
        aggregationType,
        indicatorValues,
      });
    }
    // fallback to old logic
    if (useSubareaData && selectedDimension === 'time' && timeSeriesData.length > 0) {
      return timeSeriesData.map((point: { year: string; value: number }) => ({
        label: point.year,
        value: point.value
      }));
    }
    if (useSubareaData && chartDataFromSubarea.length > 0) {
      return chartDataFromSubarea;
    }
    return processChartData({
      selectedDimension,
      defaultDimension,
      data: indicatorDataPoints
    });
  }, [indicatorValues, selectedDimension, defaultDimension, indicatorDataPoints, aggregationType, useSubareaData, timeSeriesData, chartDataFromSubarea]);

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
  const tableData = indicatorDataPoints?.originalDataPoints || [];

  // Meta info string
  const metaInfo = React.useMemo(() => {
    if (!indicatorValues || indicatorValues.length === 0) return '';
    const uniqueValues = Array.from(new Set(indicatorValues.map((item: any) => {
      let dimValue = '';
      if (selectedDimension === 'time' && (item.timeValue || item.timestamp || item.year)) {
        dimValue = item.timeValue || item.timestamp || item.year;
      } else if (selectedDimension === 'location' && item.locationValue) {
        dimValue = item.locationValue;
      } else if (item[selectedDimension] !== undefined) {
        dimValue = item[selectedDimension];
      } else if (item.customDimensions && item.customDimensions[selectedDimension]) {
        dimValue = item.customDimensions[selectedDimension];
      }
      return dimValue;
    }).filter(Boolean)));
    let range = '';
    if (selectedDimension === 'time' && uniqueValues.length > 1) {
      const sorted = uniqueValues.map(Number).filter(n => !isNaN(n)).sort((a, b) => a - b);
      if (sorted.length > 1) range = `(${sorted[0]} - ${sorted[sorted.length - 1]})`;
    }
    return `Data available for ${uniqueValues.length} ${selectedDimension}${uniqueValues.length !== 1 ? 's' : ''} ${range}`;
  }, [indicatorValues, selectedDimension]);

  return (
    <Modal open={open} onClose={onClose} aria-labelledby="indicator-modal-title" aria-modal="true" role="dialog">
      <Box sx={{
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: isSmallScreen ? '98vw' : 800,
        bgcolor: 'background.paper',
        boxShadow: 24,
        p: isSmallScreen ? 2 : 4,
        borderRadius: 2,
        maxHeight: '90vh',
        overflowY: 'auto',
        minWidth: isSmallScreen ? 'unset' : 350,
      }}>
        {/* Header */}
        <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
          <Box>
            <Typography id="indicator-modal-title" variant="h6" component="h2">
              {indicatorData?.name || 'Indicator'}
            </Typography>
            {indicatorData?.unit && (
              <Typography variant="body2" color="text.secondary">
                Unit: {indicatorData.unit}
              </Typography>
            )}
            {indicatorData?.description && (
              <Typography variant="body2" color="text.secondary">
                {indicatorData.description}
              </Typography>
            )}
          </Box>
          <IconButton aria-label="Close" onClick={onClose}><CloseIcon /></IconButton>
        </Stack>

        {/* Meta info */}
        {metaInfo && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1, mb: 2 }}>
            {metaInfo}
          </Typography>
        )}

        {/* Controls Row */}
        <Stack
          direction={isSmallScreen ? 'column' : 'row'}
          spacing={2}
          alignItems={isSmallScreen ? 'stretch' : 'center'}
          sx={{ mt: 2, mb: 2 }}
        >
          {/* View toggle */}
          <ToggleButtonGroup
            value={viewMode}
            exclusive
            onChange={(_, v) => v && setViewMode(v)}
            size="small"
            aria-label="View mode"
          >
            <ToggleButton value="chart" aria-label="Chart view">Chart</ToggleButton>
            <ToggleButton value="table" aria-label="Table view">Table</ToggleButton>
          </ToggleButtonGroup>

          {/* Chart type toggle (only in chart view) */}
          {viewMode === 'chart' && (
            <ToggleButtonGroup
              value={chartType}
              exclusive
              onChange={(_, v) => v && setChartType(v)}
              size="small"
              aria-label="Chart type"
            >
              {chartTypes.map((type) => (
                <ToggleButton key={type.value} value={type.value} aria-label={type.label}>{type.label}</ToggleButton>
              ))}
            </ToggleButtonGroup>
          )}

          {/* Dimension selector */}
          {availableDimensions.length > 0 && (
            <FormControl size="small" sx={{ minWidth: 120 }}>
              <Select
                value={selectedDimension}
                onChange={e => setSelectedDimension(e.target.value)}
                displayEmpty
                inputProps={{ 'aria-label': 'Select dimension' }}
              >
                {availableDimensions.map((dim) => (
                  <MenuItem key={dim} value={dim} aria-label={dim}>
                    {dim.charAt(0).toUpperCase() + dim.slice(1)}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}

          {/* Aggregation selector (animated) */}
          <Fade in={showAggregation} unmountOnExit>
            <Box>
              <FormControl size="small" sx={{ minWidth: 140 }}>
                <Select
                  value={aggregationType}
                  onChange={e => setAggregationType(e.target.value as AggregationType)}
                  displayEmpty
                  inputProps={{ 'aria-label': 'Select aggregation' }}
                >
                  {AGGREGATION_OPTIONS.map(opt => (
                    <MenuItem key={opt.value} value={opt.value} aria-label={opt.label}>{opt.label}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>
          </Fade>

          {/* Time range selector (only for time dimension) */}
          {selectedDimension === 'time' && timeRanges.length > 0 && (
            <FormControl size="small" sx={{ minWidth: 80 }}>
              <Select
                value={timeRange}
                onChange={e => setTimeRange(e.target.value)}
                displayEmpty
                inputProps={{ 'aria-label': 'Select time range' }}
              >
                {timeRanges.map((range) => (
                  <MenuItem key={range.value} value={range.value} aria-label={range.label}>
                    {range.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}
        </Stack>

        {/* Chart/Table area will be refactored next */}
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
              (() => {
                return (
                  <Box>
                    <IndividualIndicatorChart
                      data={chartData}
                      chartType={chartType}
                      xAxisFormatter={cleanTimeLabel}
                      aggregationType={aggregationType}
                    />
                  </Box>
                );
              })()
            )}
            {viewMode === 'table' && (
              (() => {
                // Group raw data by dimension value
                const groups: Record<string, number[]> = groupRawDataByDimension(indicatorValues, selectedDimension);
                const groupKeys: string[] = Object.keys(groups).sort((a, b) => {
                  const aNum = parseInt(a, 10);
                  const bNum = parseInt(b, 10);
                  if (!isNaN(aNum) && !isNaN(bNum)) return aNum - bNum;
                  return a.localeCompare(b);
                });
                const toggleRow = (key: string) => setOpenRows((prev) => ({ ...prev, [key]: !prev[key] }));
                // Table headers
                const dimLabel = selectedDimension.charAt(0).toUpperCase() + selectedDimension.slice(1);
                const aggLabel = aggregationType && aggregationType !== 'sum' ? `Value (${aggregationType.charAt(0).toUpperCase() + aggregationType.slice(1)})` : 'Value';
                return (
                  <Paper sx={{ mt: 2, maxHeight: 400, overflow: 'auto' }}>
                    <Table size="small" aria-label="Indicator values table">
                      <TableHead>
                        <TableRow>
                          <TableCell>{dimLabel}</TableCell>
                          <TableCell>{aggLabel}</TableCell>
                          <TableCell>Raw Value(s)</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {groupKeys.length === 0 && (
                          <TableRow>
                            <TableCell colSpan={3} align="center">No data available for this indicator.</TableCell>
                          </TableRow>
                        )}
                        {groupKeys.map((key: string) => {
                          const values = groups[key];
                          const showExpand = values.length > 1;
                          return (
                            <React.Fragment key={key}>
                              <TableRow>
                                <TableCell>{key}</TableCell>
                                <TableCell>{aggregateValues(values, aggregationType)?.toFixed(2)}</TableCell>
                                <TableCell>
                                  {showExpand ? (
                                    <>
                                      <IconButton size="small" onClick={() => toggleRow(key)} aria-label={openRows[key] ? 'Collapse' : 'Expand'}>
                                        {openRows[key] ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                                      </IconButton>
                                    </>
                                  ) : (
                                    values[0]?.toFixed(2)
                                  )}
                                </TableCell>
                              </TableRow>
                              {showExpand && (
                                <TableRow>
                                  <TableCell colSpan={3} sx={{ p: 0, border: 0 }}>
                                    <Collapse in={openRows[key]} timeout="auto" unmountOnExit>
                                      <Box sx={{ margin: 1 }}>
                                        <Typography variant="body2" color="text.secondary">Raw Values:</Typography>
                                        <ul style={{ margin: 0, paddingLeft: 16 }}>
                                          {values.map((v: number, idx: number) => (
                                            <li key={idx}>{v.toFixed(2)}</li>
                                          ))}
                                        </ul>
                                      </Box>
                                    </Collapse>
                                  </TableCell>
                                </TableRow>
                              )}
                            </React.Fragment>
                          );
                        })}
                      </TableBody>
                    </Table>
                  </Paper>
                );
              })()
            )}
          </>
        )}
      </Box>
    </Modal>
  );
};

export default IndividualIndicatorModal; 