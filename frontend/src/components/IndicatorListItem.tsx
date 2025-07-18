import React, { useState } from 'react';
import { Box, Typography, Tooltip } from '@mui/material';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';
import IndividualIndicatorModal from './IndividualIndicatorModal';
import { SubareaData } from '@/types/subareas';
import { Indicator } from '@/types/indicators';

interface IndicatorListItemProps {
  indicator: Indicator;
  isAggregated?: boolean;
  highlightedDimensionValue?: string | null;
  selectedDimension?: string;
  hasHighlightedDimensionValue?: boolean;
  subareaId?: string;
  subareaData?: SubareaData;
}

const IndicatorListItem: React.FC<IndicatorListItemProps> = ({ 
  indicator, 
  isAggregated = false, 
  highlightedDimensionValue, 
  selectedDimension, 
  hasHighlightedDimensionValue = false,
  subareaId,
  subareaData: comprehensiveData 
}) => {
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  const normalizedDirection = (indicator.direction || '').toLowerCase();

  const formatUnitDisplay = (indicator: Indicator) => {
    const parts = [];
    if (indicator.unitPrefix) parts.push(indicator.unitPrefix);
    if (indicator.unit) parts.push(indicator.unit);
    if (indicator.unitSuffix) parts.push(indicator.unitSuffix);
    return parts.length > 0 ? parts.join(' ') : '';
  };

  return (
    <>
      <Box
        display="flex"
        alignItems="center"
        sx={{
          cursor: 'pointer',
          py: 1,
          px: '0.5rem',
          bgcolor: hasHighlightedDimensionValue ? 'rgba(209, 196, 233, 0.45)' : isAggregated ? 'rgba(227, 242, 253, 0.45)' : 'inherit',
          border: hasHighlightedDimensionValue ? '2px solid #5a2fc2' : isAggregated ? '1px solid #90caf9' : '1px solid transparent',
          borderRadius: 1,
          mb: 1,
          transition: 'background 0.2s, border 0.2s',
        }}
        onClick={handleOpen}
      >
        <Tooltip title={normalizedDirection === 'input' ? 'Input' : 'Output'}>
          {normalizedDirection === 'input' ? (
            <FiberManualRecordIcon fontSize="small" sx={{ mr: 1 }} />
          ) : (
            <RadioButtonUncheckedIcon fontSize="small" sx={{ mr: 1 }} />
          )}
        </Tooltip>
        <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
          <Typography variant="body1" sx={{ flex: 1 }}>{indicator.name}</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mx: 1 }}>{formatUnitDisplay(indicator)}</Typography>
          {hasHighlightedDimensionValue && highlightedDimensionValue && selectedDimension && (
            <Typography variant="caption" color="secondary" sx={{ ml: 2 }}>
              {selectedDimension}: {highlightedDimensionValue}
            </Typography>
          )}
        </Box>
      </Box>
      <IndividualIndicatorModal
        open={open}
        onClose={handleClose}
        indicatorId={indicator.id}
        indicatorData={indicator}
        subareaId={subareaId}
        subareaData={comprehensiveData}
      />
    </>
  );
};

export default IndicatorListItem; 