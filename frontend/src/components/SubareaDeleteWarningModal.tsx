import React, { useState } from 'react';
import { 
  Modal, 
  Box, 
  Typography, 
  IconButton, 
  Button, 
  TextField, 
  Alert,
  List,
  ListItem,
  ListItemText,
  Divider
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import WarningIcon from '@mui/icons-material/Warning';
import { Subarea } from '@/types/subareas';
import { ManagedIndicator } from '@/types/indicators';

interface SubareaDeleteWarningModalProps {
  open: boolean;
  onClose: () => void;
  subarea: Subarea | null;
  relatedIndicators: ManagedIndicator[];
  onDelete: () => Promise<void>;
  onDeleteWithData: () => Promise<void>;
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

const SubareaDeleteWarningModal: React.FC<SubareaDeleteWarningModalProps> = ({
  open,
  onClose,
  subarea,
  relatedIndicators,
  onDelete,
  onDeleteWithData
}) => {
  const [confirmationText, setConfirmationText] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleDelete = async (withData: boolean = false) => {
    // Only require confirmation text if there are related indicators
    if (relatedIndicators.length > 0 && confirmationText !== 'CONFIRM DELETE') {
      setError('Please type "CONFIRM DELETE" to proceed');
      return;
    }

    setIsDeleting(true);
    setError(null);

    try {
      if (withData) {
        await onDeleteWithData();
      } else {
        await onDelete();
      }
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete subarea');
    } finally {
      setIsDeleting(false);
    }
  };

  const handleClose = () => {
    if (!isDeleting) {
      setConfirmationText('');
      setError(null);
      onClose();
    }
  };

  if (!subarea) return null;

  return (
    <Modal open={open} onClose={handleClose} aria-labelledby="subarea-delete-modal-title">
      <Box sx={style}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Box display="flex" alignItems="center">
            <WarningIcon color="warning" sx={{ mr: 1 }} />
            <Typography id="subarea-delete-modal-title" variant="h6" color="warning.main">
              Delete Subarea Warning
            </Typography>
          </Box>
          <IconButton onClick={handleClose} disabled={isDeleting}>
            <CloseIcon />
          </IconButton>
        </Box>

        <Alert severity="warning" sx={{ mb: 3 }}>
          <Typography variant="body1" sx={{ fontWeight: 'bold', mb: 1 }}>
            You are about to delete the subarea "{subarea.name}"
          </Typography>
          <Typography variant="body2">
            This action cannot be undone. Please review the related indicators below.
          </Typography>
        </Alert>

        {relatedIndicators.length > 0 ? (
          <>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Related Indicators ({relatedIndicators.length})
            </Typography>
            <Box sx={{ maxHeight: 200, overflowY: 'auto', mb: 3, border: 1, borderColor: 'divider', borderRadius: 1 }}>
              <List dense>
                {relatedIndicators.map((indicator, index) => (
                  <React.Fragment key={indicator.id}>
                    <ListItem>
                      <ListItemText
                        primary={indicator.name}
                        secondary={`${indicator.description || 'No description'} • Unit: ${indicator.unit || 'N/A'}`}
                      />
                    </ListItem>
                    {index < relatedIndicators.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </Box>

            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              <strong>Note:</strong> These indicators and their associated data will be affected by the deletion.
            </Typography>
          </>
        ) : (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            No indicators are currently associated with this subarea.
          </Typography>
        )}

        {relatedIndicators.length > 0 && (
          <>
            <Typography variant="body2" sx={{ mb: 2 }}>
              To confirm deletion, please type <strong>"CONFIRM DELETE"</strong> in the field below:
            </Typography>

            <TextField
              fullWidth
              value={confirmationText}
              onChange={(e) => setConfirmationText(e.target.value)}
              placeholder="Type CONFIRM DELETE"
              disabled={isDeleting}
              sx={{ mb: 2 }}
            />
          </>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box display="flex" justifyContent="flex-end" gap={2}>
          <Button 
            onClick={handleClose} 
            disabled={isDeleting}
            variant="outlined"
          >
            Cancel
          </Button>
          
          {relatedIndicators.length > 0 ? (
            <Button
              onClick={() => handleDelete(true)}
              disabled={confirmationText !== 'CONFIRM DELETE' || isDeleting}
              variant="contained"
              color="error"
            >
              {isDeleting ? 'Deleting...' : 'Delete with Data'}
            </Button>
          ) : (
            <Button
              onClick={() => handleDelete(false)}
              disabled={isDeleting}
              variant="contained"
              color="primary"
            >
              {isDeleting ? 'Deleting...' : 'Delete Subarea'}
            </Button>
          )}
        </Box>
      </Box>
    </Modal>
  );
};

export default SubareaDeleteWarningModal; 