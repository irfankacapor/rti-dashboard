'use client';
import React, { useCallback, useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  LinearProgress,
  Alert,
  IconButton,
  Chip,
  MenuItem,
  Select,
  FormControl,
  InputLabel
} from '@mui/material';
import {
  CloudUpload as UploadIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckIcon,
  Error as ErrorIcon
} from '@mui/icons-material';
import { useDropzone } from 'react-dropzone';
import { v4 as uuidv4 } from 'uuid';
import { CsvFile } from '@/types/csvProcessing';

interface CsvUploadSectionProps {
  onFileUploaded: (csvFile: CsvFile, encoding: string, delimiter: string) => void;
  onFileRemoved: (fileId: string) => void;
  uploadedFile?: CsvFile;
  disabled?: boolean;
}

const MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB
const ACCEPTED_TYPES = ['.csv', 'text/csv'];

export const CsvUploadSection: React.FC<CsvUploadSectionProps> = ({
  onFileUploaded,
  onFileRemoved,
  uploadedFile,
  disabled = false
}) => {
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [encoding, setEncoding] = useState('utf-8');
  const [delimiter, setDelimiter] = useState(',');

  const onDrop = useCallback((acceptedFiles: File[], rejectedFiles: any[]) => {
    setError(null);
    
    // Handle rejected files
    if (rejectedFiles.length > 0) {
      const rejection = rejectedFiles[0];
      if (rejection.errors.some((e: any) => e.code === 'file-too-large')) {
        setError('File size exceeds 25MB limit');
      } else if (rejection.errors.some((e: any) => e.code === 'file-invalid-type')) {
        setError('Please upload a valid CSV file');
      } else {
        setError('Invalid file. Please try again.');
      }
      return;
    }

    const file = acceptedFiles[0];
    if (!file) return;

    // Validate file
    if (file.size > MAX_FILE_SIZE) {
      setError('File size exceeds 25MB limit');
      return;
    }

    if (!file.name.toLowerCase().endsWith('.csv') && file.type !== 'text/csv') {
      setError('Please upload a valid CSV file');
      return;
    }

    // Create CSV file object
    const csvFile: CsvFile = {
      id: uuidv4(),
      file,
      name: file.name,
      size: file.size,
      uploadedAt: new Date(),
      uploadStatus: 'uploading'
    };

    // Simulate upload progress
    setUploadProgress(0);
    const interval = setInterval(() => {
      setUploadProgress(prev => {
        if (prev >= 90) {
          clearInterval(interval);
          return 90;
        }
        return prev + 10;
      });
    }, 100);

    // Simulate upload completion
    setTimeout(() => {
      clearInterval(interval);
      setUploadProgress(100);
      
      // Update file status
      const completedFile: CsvFile = {
        ...csvFile,
        uploadStatus: 'uploaded',
        jobId: uuidv4() // Simulate job ID from backend
      };
      
      onFileUploaded(completedFile, encoding, delimiter);
    }, 2000);
  }, [onFileUploaded, encoding, delimiter]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/csv': ['.csv']
    },
    maxSize: MAX_FILE_SIZE,
    multiple: false,
    disabled
  });

  const handleRemoveFile = () => {
    if (uploadedFile) {
      onFileRemoved(uploadedFile.id);
      setUploadProgress(0);
      setError(null);
    }
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Upload CSV File
      </Typography>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Upload a CSV file containing your data. Maximum file size: 25MB
      </Typography>

      <Box display="flex" gap={2} mb={2}>
        <FormControl size="small">
          <InputLabel id="encoding-label">Encoding</InputLabel>
          <Select
            labelId="encoding-label"
            value={encoding}
            label="Encoding"
            onChange={e => setEncoding(e.target.value)}
            sx={{ minWidth: 140 }}
          >
            <MenuItem value="utf-8">UTF-8</MenuItem>
            <MenuItem value="utf-8-bom">UTF-8 with BOM</MenuItem>
            <MenuItem value="iso-8859-1">ISO-8859-1</MenuItem>
          </Select>
        </FormControl>
        <FormControl size="small">
          <InputLabel id="delimiter-label">Delimiter</InputLabel>
          <Select
            labelId="delimiter-label"
            value={delimiter}
            label="Delimiter"
            onChange={e => setDelimiter(e.target.value)}
            sx={{ minWidth: 140 }}
          >
            <MenuItem value=",">Comma (,)</MenuItem>
            <MenuItem value=";">Semicolon (;)</MenuItem>
            <MenuItem value="\t">Tab</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!uploadedFile ? (
        <Paper
          {...getRootProps()}
          sx={{
            border: '2px dashed',
            borderColor: isDragActive ? 'primary.main' : 'grey.300',
            borderRadius: 2,
            p: 4,
            textAlign: 'center',
            cursor: disabled ? 'not-allowed' : 'pointer',
            backgroundColor: isDragActive ? 'action.hover' : 'background.paper',
            transition: 'all 0.2s ease',
            '&:hover': {
              borderColor: disabled ? 'grey.300' : 'primary.main',
              backgroundColor: disabled ? 'background.paper' : 'action.hover'
            }
          }}
        >
          <input {...getInputProps()} />
          <UploadIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            {isDragActive ? 'Drop the CSV file here' : 'Drag & drop a CSV file here'}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            or click to browse files
          </Typography>
          <Button
            variant="outlined"
            disabled={disabled}
            onClick={(e) => e.stopPropagation()}
          >
            Browse Files
          </Button>
        </Paper>
      ) : (
        <Paper sx={{ p: 2 }}>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Box display="flex" alignItems="center" gap={2}>
              {uploadedFile.uploadStatus === 'uploading' && (
                <LinearProgress 
                  variant="determinate" 
                  value={uploadProgress} 
                  sx={{ width: 100 }}
                />
              )}
              {uploadedFile.uploadStatus === 'uploaded' && (
                <CheckIcon color="success" />
              )}
              {uploadedFile.uploadStatus === 'error' && (
                <ErrorIcon color="error" />
              )}
              
              <Box>
                <Typography variant="body1" fontWeight="medium">
                  {uploadedFile.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {(uploadedFile.file.size / 1024 / 1024).toFixed(2)} MB
                </Typography>
              </Box>
            </Box>
            
            <Box display="flex" alignItems="center" gap={1}>
              <Chip 
                label={uploadedFile.uploadStatus} 
                color={uploadedFile.uploadStatus === 'uploaded' ? 'success' : 
                       uploadedFile.uploadStatus === 'error' ? 'error' : 'default'}
                size="small"
              />
              <IconButton 
                onClick={handleRemoveFile}
                size="small"
                color="error"
              >
                <DeleteIcon />
              </IconButton>
            </Box>
          </Box>
          
          {uploadedFile.uploadStatus === 'error' && uploadedFile.error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {uploadedFile.error}
            </Alert>
          )}
        </Paper>
      )}
    </Box>
  );
}; 