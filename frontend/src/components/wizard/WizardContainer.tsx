'use client';
import React from 'react';
import { Box, Paper, Typography, Fade, Divider } from '@mui/material';
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
  renderExtraButtons?: () => React.ReactNode;
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
  renderExtraButtons,
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
          <>
            <Divider sx={{ my: 3 }} />
            <Box display="flex" flexDirection="row" alignItems="center" justifyContent="space-between">     
            
              <Box flexGrow={1}>
                <WizardNavigation
                  onNext={onNext}
                  onPrev={onPrev}
                  nextDisabled={nextDisabled}
                  nextLabel={nextLabel}
                  skipButton={skipButton}
                  skipDisabled={skipDisabled}
                  onSkip={onSkip}
                />
              </Box>
              <Box marginLeft="1rem">{renderExtraButtons && renderExtraButtons()}</Box>
            </Box>
          </>
        )}
      </Paper>
    </Box>
  );
}; 