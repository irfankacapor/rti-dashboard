'use client';
import React from 'react';
import { Box, Paper, Typography, Fade } from '@mui/material';
import { WizardStepper } from './WizardStepper';
import { WizardNavigation } from './WizardNavigation';
import { useWizardStore } from '@/lib/store/useWizardStore';

interface WizardContainerProps {
  children: React.ReactNode;
  title?: string;
  subtitle?: string;
  onNext?: () => void;
  onPrev?: () => void;
  nextDisabled?: boolean;
  nextLabel?: string;
  showNavigation?: boolean;
  skipButton?: boolean;
  skipDisabled?: boolean;
  onSkip?: () => void;
}

export const WizardContainer: React.FC<WizardContainerProps> = ({
  children,
  title,
  subtitle,
  onNext,
  onPrev,
  nextDisabled,
  nextLabel,
  showNavigation = true,
  skipButton = false,
  skipDisabled = false,
  onSkip,
}) => {
  const { currentStep, steps, isLoading } = useWizardStore();
  const currentStepData = steps.find(s => s.id === currentStep);

  return (
    <Box data-testid="wizard-container">
      <WizardStepper />
      
      <Paper elevation={2} sx={{ p: 4 }}>
        <Box mb={3}>
          <Typography variant="h4" gutterBottom data-testid="wizard-step-title">
            {title || currentStepData?.label || `Step ${currentStep}`}
          </Typography>
          {subtitle && (
            <Typography variant="body1" color="text.secondary" data-testid="wizard-step-subtitle">
              {subtitle}
            </Typography>
          )}
        </Box>

        <Fade in={!isLoading} timeout={300}>
          <Box data-testid="wizard-step-content">
            {children}
          </Box>
        </Fade>

        {showNavigation && (
          <WizardNavigation
            onNext={onNext}
            onPrev={onPrev}
            nextDisabled={nextDisabled}
            nextLabel={nextLabel}
            skipButton={skipButton}
            skipDisabled={skipDisabled}
            onSkip={onSkip}
          />
        )}
      </Paper>
    </Box>
  );
}; 