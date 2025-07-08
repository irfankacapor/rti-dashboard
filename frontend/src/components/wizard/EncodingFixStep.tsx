import React, { useState, useEffect, useMemo } from 'react';
import {
  Box, Typography, Paper, Table, TableBody, TableCell,
  TableHead, TableRow, Button, TextField, Alert,
  Accordion, AccordionSummary, AccordionDetails,
  Chip, IconButton, LinearProgress
} from '@mui/material';
import { 
  ExpandMore, AutoFixHigh, CheckCircle, Warning,
  Visibility, VisibilityOff 
} from '@mui/icons-material';
import { DimensionMapping, EncodingIssue } from '@/types/csvProcessing';
import { detectEncodingIssues, applyEncodingFixes } from '@/utils/encodingDetection';

interface EncodingFixStepProps {
  csvData: string[][];
  dimensionMappings: DimensionMapping[];
  onApplyFixes: (fixedData: string[][], appliedFixes: number) => void;
  onSkip: () => void;
}

export const EncodingFixStep: React.FC<EncodingFixStepProps> = ({
  csvData, dimensionMappings, onApplyFixes, onSkip
}) => {
  const [isScanning, setIsScanning] = useState(true);
  const [detectedIssues, setDetectedIssues] = useState<EncodingIssue[]>([]);
  const [userReplacements, setUserReplacements] = useState<Map<string, string>>(new Map());
  const [showDetails, setShowDetails] = useState<Set<string>>(new Set());
  
  // Run detection on mount
  useEffect(() => {
    setIsScanning(true);
    // Add small delay to show scanning state
    const timer = setTimeout(() => {
      const issues = detectEncodingIssues(csvData, dimensionMappings);
      // Only keep auto-detected (KNOWN_ENCODING) issues
      const autoDetected = issues.filter(issue => issue.issueType === 'KNOWN_ENCODING');
      setDetectedIssues(autoDetected);
      // Initialize user replacements with suggested fixes
      const initialReplacements = new Map<string, string>();
      autoDetected.forEach(issue => {
        if (issue.suggestedReplacement) {
          initialReplacements.set(issue.problematicText, issue.suggestedReplacement);
        }
      });
      setUserReplacements(initialReplacements);
      setIsScanning(false);
    }, 500);
    
    return () => clearTimeout(timer);
  }, [csvData, dimensionMappings]);
  
  const hasValidFixes = useMemo(() => {
    return Array.from(userReplacements.values()).some(replacement => replacement.trim().length > 0);
  }, [userReplacements]);
  
  const handleReplacementChange = (problematicText: string, newReplacement: string) => {
    const newReplacements = new Map(userReplacements);
    if (newReplacement.trim() === '') {
      newReplacements.delete(problematicText);
    } else {
      newReplacements.set(problematicText, newReplacement);
    }
    setUserReplacements(newReplacements);
  };
  
  const handleApplyFixes = () => {
    const fixedData = applyEncodingFixes(csvData, userReplacements);
    const appliedFixesCount = userReplacements.size;
    onApplyFixes(fixedData, appliedFixesCount);
  };
  
  const toggleDetails = (issueText: string) => {
    const newShowDetails = new Set(showDetails);
    if (newShowDetails.has(issueText)) {
      newShowDetails.delete(issueText);
    } else {
      newShowDetails.add(issueText);
    }
    setShowDetails(newShowDetails);
  };
  
  if (isScanning) {
    return (
      <Box>
        <Typography variant="h6" gutterBottom>
          Scanning for Character Encoding Issues
        </Typography>
        <Box sx={{ mb: 2 }}>
          <LinearProgress />
        </Box>
        <Typography variant="body2" color="text.secondary">
          Checking mapped data for encoding problems...
        </Typography>
      </Box>
    );
  }
  
  if (detectedIssues.length === 0) {
    return (
      <Box>
        <Alert severity="success" icon={<CheckCircle />} sx={{ mb: 3 }}>
          <Typography variant="h6">No Encoding Issues Detected</Typography>
          <Typography variant="body2">
            Your data appears to be properly encoded. Ready to proceed!
          </Typography>
        </Alert>
        <Button 
          variant="contained" 
          size="large"
          onClick={() => onApplyFixes(csvData, 0)}
        >
          Continue to Data Processing
        </Button>
      </Box>
    );
  }
  
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Character Encoding Issues Detected
      </Typography>
      
      <Alert severity="warning" icon={<Warning />} sx={{ mb: 3 }}>
        Found {detectedIssues.length} type{detectedIssues.length !== 1 ? 's' : ''} of encoding issues 
        affecting {detectedIssues.reduce((sum, issue) => sum + issue.occurrenceCount, 0)} cell{detectedIssues.reduce((sum, issue) => sum + issue.occurrenceCount, 0) !== 1 ? 's' : ''}.
        Review and apply fixes below.
      </Alert>
      
      <Paper elevation={1} sx={{ mb: 3 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Issue</TableCell>
              <TableCell>Fix</TableCell>
              <TableCell>Count</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {detectedIssues.map((issue, index) => (
              <TableRow key={`${issue.problematicText}-${index}`}>
                <TableCell>
                  <Chip 
                    label={issue.problematicText}
                    color="error"
                    size="small"
                    sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}
                  />
                </TableCell>
                <TableCell>
                  <TextField
                    size="small"
                    fullWidth
                    value={userReplacements.get(issue.problematicText) || ''}
                    onChange={(e) => handleReplacementChange(issue.problematicText, e.target.value)}
                    placeholder={issue.suggestedReplacement || 'Enter replacement'}
                    sx={{ minWidth: 120 }}
                  />
                </TableCell>
                <TableCell>
                  <Chip 
                    label={issue.occurrenceCount}
                    size="small"
                    variant="outlined"
                    color="primary"
                  />
                </TableCell>
                <TableCell>
                  <Chip 
                    label={'Auto-detected'}
                    size="small"
                    color={'success'}
                    variant="outlined"
                  />
                </TableCell>
                <TableCell>
                  <IconButton
                    size="small"
                    onClick={() => toggleDetails(issue.problematicText)}
                    title="Show/hide details"
                  >
                    {showDetails.has(issue.problematicText) ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        
        {/* Details sections */}
        {detectedIssues.map((issue, index) => (
          showDetails.has(issue.problematicText) && (
            <Accordion key={`details-${issue.problematicText}-${index}`} expanded>
              <AccordionSummary>
                <Typography variant="subtitle2">
                  Details for "{issue.problematicText}"
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Location</TableCell>
                      <TableCell>Dimension</TableCell>
                      <TableCell>Original</TableCell>
                      <TableCell>Preview</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {issue.locations.slice(0, 10).map((location: any, locIndex: number) => (
                      <TableRow key={locIndex}>
                        <TableCell>
                          <Typography variant="caption">
                            Row {location.rowIndex + 1}, Col {location.colIndex + 1}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={location.dimensionType} size="small" variant="outlined" />
                        </TableCell>
                        <TableCell>
                          <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                            {location.originalValue.length > 30 
                              ? location.originalValue.substring(0, 30) + '...'
                              : location.originalValue
                            }
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'success.main' }}>
                            {userReplacements.has(issue.problematicText) 
                              ? location.originalValue.replaceAll(issue.problematicText, userReplacements.get(issue.problematicText)!).substring(0, 30)
                              : 'Set replacement to preview'
                            }
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ))}
                    {issue.locations.length > 10 && (
                      <TableRow>
                        <TableCell colSpan={4}>
                          <Typography variant="caption" color="text.secondary" align="center">
                            ... and {issue.locations.length - 10} more locations
                          </Typography>
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </AccordionDetails>
            </Accordion>
          )
        ))}
      </Paper>
      
      <Box display="flex" gap={2} alignItems="center">
        <Button
          variant="contained"
          startIcon={<AutoFixHigh />}
          onClick={handleApplyFixes}
          disabled={!hasValidFixes}
          size="large"
        >
          Apply {userReplacements.size} Fix{userReplacements.size !== 1 ? 'es' : ''}
        </Button>
        
        <Button 
          variant="outlined" 
          onClick={onSkip}
          size="large"
        >
          Skip Fixes
        </Button>
        
        <Typography variant="caption" color="text.secondary">
          {hasValidFixes 
            ? `${userReplacements.size} of ${detectedIssues.length} issues have fixes specified`
            : 'No fixes specified yet'
          }
        </Typography>
      </Box>
    </Box>
  );
}; 