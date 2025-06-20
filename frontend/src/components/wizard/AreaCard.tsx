import React from 'react';
import { Paper, Box, Typography, IconButton, Chip, Tooltip } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { Area } from '@/types/areas';

interface AreaCardProps {
  area: Area;
  onEdit: (area: Area) => void;
  onDelete: (area: Area) => void;
}

export const AreaCard: React.FC<AreaCardProps> = ({ area, onEdit, onDelete }) => {
  return (
    <Paper elevation={1} sx={{ p: 2, position: 'relative', minHeight: 160 }}>
      <Box display="flex" alignItems="center" justifyContent="space-between">
        <Typography variant="h6" fontWeight="bold" sx={{ wordBreak: 'break-word' }}>
          {area.name || 'Untitled Area'}
        </Typography>
        <Box>
          <Tooltip title="Edit Area">
            <IconButton aria-label="edit" onClick={() => onEdit(area)} size="small">
              <EditIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete Area">
            <IconButton aria-label="delete" onClick={() => onDelete(area)} size="small" disabled={area.isDefault}>
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>
      <Typography variant="body2" color="text.secondary" sx={{ mt: 1, mb: 2, minHeight: 40 }}>
        {area.description || 'No description provided.'}
      </Typography>
      <Box display="flex" alignItems="center" gap={1}>
        <Chip label={area.code} size="small" color="default" variant="outlined" />
        {area.isDefault && (
          <Chip label="Default Area" size="small" color="primary" variant="filled" />
        )}
      </Box>
      <Typography variant="caption" color="text.disabled" sx={{ position: 'absolute', bottom: 8, right: 8 }}>
        {area.createdAt instanceof Date ? area.createdAt.toLocaleDateString() : new Date(area.createdAt).toLocaleDateString()}
      </Typography>
    </Paper>
  );
}; 