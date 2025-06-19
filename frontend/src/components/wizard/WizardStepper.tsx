'use client';
import React from 'react';
import {
  Stepper,
  Step,
  StepLabel,
  StepButton,
  Box,
  LinearProgress,
  Typography,
  Paper,
  useTheme,
} from '@mui/material';
import { CheckCircle, RadioButtonUnchecked } from '@mui/icons-material';
import { useWizardStore } from '@/lib/store/useWizardStore';

interface WizardStepperProps {
  className?: string;
}

export const WizardStepper: React.FC<WizardStepperProps> = ({ className }) => {
  const theme = useTheme();
  const { 
    currentStep, 
    steps, 
    setCurrentStep, 
    canProceedToStep 
  } = useWizardStore();

  const completedSteps = steps.filter(step => step.isCompleted).length;
  const progressPercentage = (completedSteps / steps.length) * 100;

  const handleStepClick = (stepId: number) => {
    if (canProceedToStep(stepId)) {
      setCurrentStep(stepId);
    }
  };

  const getStepIcon = (step: any, index: number) => {
    const stepNumber = index + 1;
    const isActive = currentStep === stepNumber;
    const isCompleted = step.isCompleted;
    const canAccess = canProceedToStep(stepNumber);

    if (isCompleted) {
      return (
        <CheckCircle 
          sx={{ 
            color: theme.palette.success.main,
            fontSize: 24 
          }}
          data-testid={`step-${stepNumber}-completed-icon`}
        />
      );
    }

    return (
      <Box
        sx={{
          width: 24,
          height: 24,
          borderRadius: '50%',
          backgroundColor: isActive 
            ? theme.palette.primary.main 
            : canAccess 
              ? theme.palette.grey[400]
              : theme.palette.grey[300],
          color: isActive ? 'white' : canAccess ? 'white' : theme.palette.grey[500],
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '0.875rem',
          fontWeight: 'bold',
        }}
        data-testid={`step-${stepNumber}-number-icon`}
      >
        {stepNumber}
      </Box>
    );
  };

  return (
    <Paper 
      elevation={1} 
      sx={{ p: 3, mb: 3 }}
      className={className}
      data-testid="wizard-stepper"
    >
      <Box mb={2}>
        <Typography variant="h6" gutterBottom>
          Configuration Progress
        </Typography>
        <Box display="flex" alignItems="center" gap={2}>
          <LinearProgress
            variant="determinate"
            value={progressPercentage}
            sx={{ 
              flexGrow: 1, 
              height: 8, 
              borderRadius: 4,
              backgroundColor: theme.palette.grey[200],
              '& .MuiLinearProgress-bar': {
                borderRadius: 4,
              }
            }}
            data-testid="progress-bar"
          />
          <Typography variant="body2" color="text.secondary">
            {completedSteps}/{steps.length}
          </Typography>
        </Box>
      </Box>

      <Stepper 
        activeStep={currentStep - 1} 
        alternativeLabel
        data-testid="stepper"
      >
        {steps.map((step, index) => {
          const stepNumber = index + 1;
          const canAccess = canProceedToStep(stepNumber);
          
          return (
            <Step key={step.id} completed={step.isCompleted}>
              <StepButton
                onClick={() => handleStepClick(stepNumber)}
                disabled={!canAccess}
                data-testid={`step-button-${stepNumber}`}
              >
                <StepLabel
                  icon={getStepIcon(step, index)}
                  sx={{
                    '& .MuiStepLabel-label': {
                      fontSize: '0.875rem',
                      fontWeight: currentStep === stepNumber ? 'bold' : 'normal',
                      color: canAccess 
                        ? 'text.primary' 
                        : 'text.disabled'
                    }
                  }}
                >
                  {step.label}
                </StepLabel>
              </StepButton>
            </Step>
          );
        })}
      </Stepper>
    </Paper>
  );
}; 