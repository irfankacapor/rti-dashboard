"use client";
import React, { useState, useEffect } from 'react';
import { WizardContainer } from '@/components/wizard/WizardContainer';
import { Alert, Button } from '@mui/material';
import { useWizardStore as useStepperStore } from '@/lib/store/useWizardStore';
import { useWizardStore as useAreaStore } from '@/store/wizardStore';
import { AreasStep } from '@/components/wizard/AreasStep';
import { SubareasStep } from '@/components/wizard/SubareasStep';
import { CsvProcessingStep } from '@/components/wizard/CsvProcessingStep';
import { IndicatorManagementStep } from '@/components/wizard/IndicatorManagementStep';
import { GoalManagementStep } from '@/components/wizard/GoalManagementStep';
import { Save as SaveIcon } from '@mui/icons-material';
import Snackbar from '@mui/material/Snackbar';

export default function WizardPage() {
  const { currentStep, setStepValid, setStepCompleted, nextStep, steps, setCurrentStep } = useStepperStore();
  const { dirtyAreas, dirtySubareas, saveStep, hasUnsavedChanges, isSaving } = useAreaStore();

  // Only user-created areas (not default)
  const userAreas = dirtyAreas.filter(a => !a.isDefault);
  const skipButton = currentStep === 1;
  const skipDisabled = userAreas.length > 0;
  
  // New: error and loading state for Next button
  const [saveError, setSaveError] = useState<string | null>(null);
  const [nextLoading, setNextLoading] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  
  // --- Stepper state sync: ensure all previous steps are marked as completed if their data is present ---
  useEffect(() => {
    // Step 1: Areas (always valid/completed)
    setStepValid(1, true);
    setStepCompleted(1, true);
    // Step 2: Subareas (completed if at least one subarea)
    setStepValid(2, dirtySubareas.length > 0);
    setStepCompleted(2, dirtySubareas.length > 0);
    // Step 3: Upload (completed if at least one indicator)
    // We can't check indicators here, but step 3 is always valid if step 2 is completed
    setStepValid(3, true);
    setStepCompleted(3, true);
    // Step 4: Indicators (handled by IndicatorManagementStep, but ensure at least one indicator)
    // Step 5: Goals (always valid/completed)
    setStepValid(5, true);
    setStepCompleted(5, true);
  }, [dirtyAreas, dirtySubareas, setStepValid, setStepCompleted]);

  const handleSkip = () => {
    setStepValid(1, true);
    setStepCompleted(1, true);
    nextStep();
  };

  const handleNext = async () => {
    setSaveError(null);
    setNextLoading(true);
    try {
      // Save current step data to backend first
      if (hasUnsavedChanges()) {
        await saveStep(currentStep);
      }
      
      // Only mark step as completed after successful save
      setStepCompleted(currentStep, true);
      
      // Move to next step
      nextStep();
    } catch (err: any) {
      setSaveError(err?.message || 'Failed to save step. Please try again.');
    } finally {
      setNextLoading(false);
    }
  };

  const handleSave = async () => {
    setSaveError(null);
    setNextLoading(true);
    try {
      if (hasUnsavedChanges()) {
        await saveStep(currentStep);
      }
      setSaveSuccess(true);
    } catch (err: any) {
      setSaveError(err?.message || 'Failed to save step. Please try again.');
    } finally {
      setNextLoading(false);
    }
  };

  const handleNavigateToStep = (stepIndex: number) => {
    setCurrentStep(stepIndex);
  };

  // Check if next button should be disabled
  const isNextDisabled = () => {
    if (currentStep === 1) {
      // Step 1 is always valid (can be skipped)
      return false;
    }
    
    if (currentStep === 2) {
      // Step 2 requires at least one subarea
      return dirtySubareas.length === 0;
    }
    
    if (currentStep === 4) {
      // Step 4 requires at least one indicator
      // This will be handled by the IndicatorManagementStep component
      return false;
    }
    
    return false;
  };

  // Step rendering logic
  let stepComponent = null;
  if (currentStep === 1) {
    stepComponent = <AreasStep />;
  } else if (currentStep === 2) {
    stepComponent = <SubareasStep />;
  } else if (currentStep === 3) {
    stepComponent = <CsvProcessingStep />;
  } else if (currentStep === 4) {
    stepComponent = <IndicatorManagementStep onNavigateToStep={handleNavigateToStep} />;
  } else if (currentStep === 5) {
    stepComponent = <GoalManagementStep />;
  } else {
    stepComponent = <div>Step not implemented</div>;
  }

  return (
    <WizardContainer
      title={`Step ${currentStep}: ${
        currentStep === 1 ? 'Areas Management' :
        currentStep === 2 ? 'Subareas Management' :
        currentStep === 3 ? 'Data Upload' :
        currentStep === 4 ? 'Indicator Review & Management' :
        currentStep === 5 ? 'Goal Management' :
        ''
      }`}
      subtitle={
        currentStep === 1 ? 'Configure the main areas for your dashboard' :
        currentStep === 2 ? 'Configure subareas and assign them to areas' :
        currentStep === 3 ? 'Upload and map your indicator data from CSV' :
        currentStep === 4 ? 'Review, edit, and manage your indicators before proceeding to goals' :
        currentStep === 5 ? 'Manage your goals' :
        ''
      }
      onNext={handleNext}
      nextDisabled={isNextDisabled() || isSaving || nextLoading}
      skipButton={skipButton}
      skipDisabled={skipDisabled}
      onSkip={handleSkip}
      renderExtraButtons={() => (
        <>
          <Button
            variant="outlined"
            startIcon={<SaveIcon />}
            onClick={handleSave}
            disabled={isSaving || nextLoading}
            sx={{ mr: 2 }}
            data-testid="wizard-save-button"
          >
            Save
          </Button>
        </>
      )}
    >
      {saveError && (
        <Alert severity="error" sx={{ mb: 2 }}>{saveError}</Alert>
      )}
      {stepComponent}
      <Snackbar
        open={saveSuccess}
        autoHideDuration={2500}
        onClose={() => setSaveSuccess(false)}
        message="Progress saved!"
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </WizardContainer>
  );
} 