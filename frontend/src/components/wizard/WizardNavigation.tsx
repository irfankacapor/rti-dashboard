'use client';
import React from 'react';
import {
  Box,
  Button
} from '@mui/material';
import { ArrowBack, ArrowForward } from '@mui/icons-material';
import { useRouter } from 'next/navigation';
import { useWizardStore } from '@/lib/store/useWizardStore';

interface WizardNavigationProps {
  onNext?: () => void;
  onPrev?: () => void;
  nextDisabled?: boolean;
  nextLabel?: string;
  prevLabel?: string;
  showNext?: boolean;
  showPrev?: boolean;
  skipButton?: boolean;
  skipDisabled?: boolean;
  onSkip?: () => void;
  onComplete?: () => void;
}

export const WizardNavigation: React.FC<WizardNavigationProps> = ({
  onNext,
  onPrev,
  nextDisabled = false,
  nextLabel = 'Next',
  prevLabel = 'Back',
  showNext = true,
  showPrev = true,
  skipButton = false,
  skipDisabled = false,
  onSkip,
  onComplete,
}) => {
  const { 
    currentStep, 
    steps, 
    nextStep, 
    prevStep,
    canProceedToStep 
  } = useWizardStore();
  const router = useRouter();

  const isFirstStep = currentStep === 1;
  const isLastStep = currentStep === steps.length;
  const canProceedNext = canProceedToStep(currentStep + 1);

  const handleNext = () => {
    if (onNext) {
      onNext();
    } else {
      nextStep();
    }
  };

  const handlePrev = () => {
    if (onPrev) {
      onPrev();
    } else {
      prevStep();
    }
  };

  const handleComplete = () => {
    if (onComplete) {
      onComplete();
    } else {
      // Default behavior: redirect to dashboard
      router.push('/en/dashboard');
    }
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box>
          {showPrev && !isFirstStep && (
            <Button
              variant="outlined"
              startIcon={<ArrowBack />}
              onClick={handlePrev}
              data-testid="wizard-prev-button"
            >
              {prevLabel}
            </Button>
          )}
        </Box>

        <Box display="flex" gap={2}>
          {skipButton && !isLastStep && (
            <Button
              variant="outlined"
              color="secondary"
              onClick={onSkip}
              disabled={skipDisabled}
              data-testid="wizard-skip-button"
            >
              Skip
            </Button>
          )}
          {showNext && !isLastStep && (
            <Button
              variant="contained"
              endIcon={<ArrowForward />}
              onClick={handleNext}
              disabled={nextDisabled}
              data-testid="wizard-next-button"
            >
              {nextLabel}
            </Button>
          )}
          {isLastStep && (
            <Button
              variant="contained"
              onClick={handleComplete}
              disabled={nextDisabled}
              data-testid="wizard-finish-button"
            >
              Complete Setup
            </Button>
          )}
        </Box>
      </Box>
    </Box>
  );
}; 