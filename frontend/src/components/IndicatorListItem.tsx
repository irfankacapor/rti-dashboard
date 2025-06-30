import React, { useState } from 'react';
import { Box, Typography, IconButton, Tooltip } from '@mui/material';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';
import IndividualIndicatorModal from './IndividualIndicatorModal';

interface IndicatorListItemProps {
  indicator: any;
}

const IndicatorListItem: React.FC<IndicatorListItemProps> = ({ indicator }) => {
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  return (
    <>
      <Box display="flex" alignItems="center" sx={{ cursor: 'pointer', py: 1 }} onClick={handleOpen}>
        <Tooltip title={indicator.direction === 'input' ? 'Input' : 'Output'}>
          {indicator.direction === 'input' ? (
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