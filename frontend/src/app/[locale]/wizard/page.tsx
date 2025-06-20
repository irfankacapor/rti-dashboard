"use client";
import React from 'react';
import { WizardContainer } from '@/components/wizard/WizardContainer';
import { Typography, Box, Alert } from '@mui/material';
import { useWizardStore as useStepperStore } from '@/lib/store/useWizardStore';
import { useWizardStore as useAreaStore } from '@/store/wizardStore';
import { AreasStep } from '@/components/wizard/AreasStep';
import { SubareasStep } from '@/components/wizard/SubareasStep';
import { CsvProcessingStep } from '@/components/wizard/CsvProcessingStep';

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
    // For now, just go to the next step
    nextStep();
  };

  // Step rendering logic
  let stepComponent = null;
  if (currentStep === 1) {
    stepComponent = <AreasStep />;
  } else if (currentStep === 2) {
    stepComponent = <SubareasStep />;
  } else if (currentStep === 3) {
    stepComponent = <CsvProcessingStep />;
  } else {
    stepComponent = <div>Step not implemented</div>;
  }

  return (
    <WizardContainer
      title={`Step ${currentStep}: ${
        currentStep === 1 ? 'Areas Management' :
        currentStep === 2 ? 'Subareas Management' :
        currentStep === 3 ? 'Data Upload' :
        ''}`}
      subtitle={
        currentStep === 1 ? 'Configure the main areas for your dashboard' :
        currentStep === 2 ? 'Configure subareas and assign them to areas' :
        currentStep === 3 ? 'Upload and map your indicator data from CSV' :
        ''
      }
      onNext={handleNext}
      nextDisabled={false}
      skipButton={skipButton}
      skipDisabled={skipDisabled}
      onSkip={handleSkip}
    >
      {stepComponent}
    </WizardContainer>
  );
} 