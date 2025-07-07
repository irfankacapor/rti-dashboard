import React, { useState } from 'react';
import { Box, Typography, IconButton, Tooltip } from '@mui/material';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';
import IndividualIndicatorModal from './IndividualIndicatorModal';
import StarIcon from '@mui/icons-material/Star';

interface IndicatorListItemProps {
  indicator: any;
  isAggregated?: boolean;
}

const IndicatorListItem: React.FC<IndicatorListItemProps> = ({ indicator, isAggregated = false }) => {
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  // Debug: log indicator and its direction
  React.useEffect(() => {
    // eslint-disable-next-line no-console
    console.log('Indicator in IndicatorListItem:', indicator, 'direction:', indicator.direction);
  }, [indicator]);

  const normalizedDirection = (indicator.direction || '').toLowerCase();

  return (
    <>
      <Box
        display="flex"
        alignItems="center"
        sx={{
          cursor: 'pointer',
          py: 1,
          px: '0.5rem',
          bgcolor: isAggregated ? '#e3f2fd' : 'inherit',
          border: isAggregated ? '1px solid #90caf9' : '1px solid transparent',
          borderRadius: 1,
          mb: 1
        }}
        onClick={handleOpen}
      >
        <Tooltip title={normalizedDirection === 'input' ? 'Input' : 'Output'}>
          {normalizedDirection === 'input' ? (
            <RadioButtonUncheckedIcon fontSize="small" sx={{ mr: 1 }} />
          ) : (
            <FiberManualRecordIcon fontSize="small" sx={{ mr: 1 }} />
          )}
        </Tooltip>
        <Typography variant="body1" sx={{ flex: 1 }}>{indicator.name}</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mx: 1 }}>{indicator.unit}</Typography>
        <Typography variant="body2" color="primary">{indicator.latestValue ?? '--'}</Typography>
      </Box>
      <IndividualIndicatorModal
        open={open}
        onClose={handleClose}
        indicatorId={indicator.id}
        indicatorData={indicator}
      />
    </>
  );
};

export default IndicatorListItem; 