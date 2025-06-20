"use client";
import React from 'react';
import { WizardContainer } from '@/components/wizard/WizardContainer';
import { Typography, Box, Alert } from '@mui/material';
import { useWizardStore as useStepperStore } from '@/lib/store/useWizardStore';
import { useWizardStore as useAreaStore } from '@/store/wizardStore';
import { AreasStep } from '@/components/wizard/AreasStep';

export default function WizardPage() {
  const { currentStep, setStepValid, setStepCompleted, nextStep } = useStepperStore();
  const { areas } = useAreaStore();

  // Simulate step validation for demo
  React.useEffect(() => {
    // For demo purposes, mark step 1 as valid after a short delay
    const timer = setTimeout(() => {
      setStepValid(1, true);
    }, 1000);

    return () => clearTimeout(timer);
  }, [setStepValid]);

  // Only user-created areas (not default)
  const userAreas = areas.filter(a => !a.isDefault);
  const skipButton = currentStep === 1;
  const skipDisabled = userAreas.length > 0;
  const handleSkip = () => {
    setStepValid(1, true);
    setStepCompleted(1, true);
    nextStep();
  };

  const handleNext = () => {
    // This will be implemented in the next step
    console.log('Next step logic will be implemented in Step 3');
  };

  return (
    <WizardContainer
      title={`Step ${currentStep}: Areas Management`}
      subtitle="Configure the main areas for your dashboard"
      onNext={handleNext}
      nextDisabled={false}
      skipButton={skipButton}
      skipDisabled={skipDisabled}
      onSkip={handleSkip}
    >
      <AreasStep />
    </WizardContainer>
  );
} 