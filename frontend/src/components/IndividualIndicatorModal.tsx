import React, { useState } from 'react';
import { Modal, Box, Typography, IconButton, ToggleButton, ToggleButtonGroup, MenuItem, Select, CircularProgress } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TimeSeriesChart from './charts/TimeSeriesChart';
import { useIndicatorData } from '../hooks/useApi';

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
  width: 600,
  bgcolor: 'background.paper',
  boxShadow: 24,
  p: 4,
  borderRadius: 2,
  maxHeight: '90vh',
  overflowY: 'auto',
};

const timeRanges = [
  { label: '1M', value: '1M' },
  { label: '3M', value: '3M' },
  { label: '6M', value: '6M' },
  { label: '1Y', value: '1Y' },
];

const chartTypes = [
  { label: 'Bar', value: 'bar' },
  { label: 'Line', value: 'line' },
];

const IndividualIndicatorModal: React.FC<IndividualIndicatorModalProps> = ({ open, onClose, indicatorId, indicatorData }) => {
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');
  const [selectedDimension, setSelectedDimension] = useState<string>('');
  const [timeRange, setTimeRange] = useState('1Y');
  const { data, loading, error } = useIndicatorData(indicatorId, timeRange, selectedDimension);

  // Determine available dimensions and best default
  const availableDimensions = data?.dimensions || [];
  let defaultDimension = 'time';
  if (data) {
    if (data.timeSeries && data.timeSeries.length > 1) {
      defaultDimension = 'time';
    } else if (availableDimensions.length > 0) {
      // Find a dimension with >1 value
      const altDim = availableDimensions.find(dim =>
        data[dim] && Array.isArray(data[dim]) && data[dim].length > 1
      );
      if (altDim) defaultDimension = altDim;
    }
  }

  // Set default dimension on data load
  React.useEffect(() => {
    if (data && !selectedDimension) {
      setSelectedDimension(defaultDimension);
    }
  }, [data]);

  // Prepare chart data
  let chartData = [];
  if (data) {
    if (selectedDimension === 'time' && data.timeSeries && data.timeSeries.length > 1) {
      chartData = data.timeSeries;
    } else if (selectedDimension && data[selectedDimension] && Array.isArray(data[selectedDimension])) {
      chartData = data[selectedDimension];
    } else if (data.timeSeries && data.timeSeries.length === 1) {
      chartData = data.timeSeries;
    }
  }

  // Only show error if not just empty data
  const showError = error && !loading && !chartData.length;

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
            <Select
              value={selectedDimension}
              onChange={e => setSelectedDimension(e.target.value)}
              size="small"
              sx={{ mr: 2 }}
            >
              {['time', ...availableDimensions.filter(d => d !== 'time')].map(dim => (
                <MenuItem key={dim} value={dim}>{dim.charAt(0).toUpperCase() + dim.slice(1)}</MenuItem>
              ))}
            </Select>
          )}
          {selectedDimension === 'time' && (
            <Select
              value={timeRange}
              onChange={e => setTimeRange(e.target.value)}
              size="small"
            >
              {timeRanges.map(range => (
                <MenuItem key={range.value} value={range.value}>{range.label}</MenuItem>
              ))}
            </Select>
          )}
        </Box>
        {loading ? (
          <Box display="flex" justifyContent="center"><CircularProgress /></Box>
        ) : showError ? (
          <Typography color="error">Failed to load indicator data.</Typography>
        ) : chartData.length > 1 ? (
          <TimeSeriesChart data={chartData} chartType={chartType} />
        ) : chartData.length === 1 ? (
          <Box sx={{ textAlign: 'center', my: 4 }}>
            <Typography variant="h4" color="primary">{chartData[0].value}</Typography>
            <Typography variant="body2" color="text.secondary">{selectedDimension === 'time' ? chartData[0].label : selectedDimension}</Typography>
          </Box>
        ) : (
          <Typography color="text.secondary">No data available for this indicator.</Typography>
        )}
        <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
          {indicatorData?.description}
        </Typography>
      </Box>
    </Modal>
  );
};

export default IndividualIndicatorModal; 