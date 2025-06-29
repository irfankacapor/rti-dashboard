import React from 'react';
import {
  Box,
  List,
  ListItem,
  ListItemText,
  ListItemButton,
  Typography,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  LinearProgress
} from '@mui/material';
import { ExpandMore as ExpandMoreIcon, Flag as FlagIcon } from '@mui/icons-material';
import { GoalsSidebarProps, DashboardGoal, GoalGroup } from '@/types/dashboard';

export const GoalsSidebar: React.FC<GoalsSidebarProps> = ({
  goals,
  goalGroups,
  highlightedGoals,
  onGoalHover
}) => {
  const handleGoalHover = (goalId: string) => {
    onGoalHover([goalId]);
  };

  const handleGoalLeave = () => {
    onGoalHover([]);
  };

  const calculateProgress = (goal: DashboardGoal) => {
    if (goal.targetValue === 0) return 0;
    return Math.min((goal.currentValue / goal.targetValue) * 100, 100);
  };

  const getProgressColor = (progress: number) => {
    if (progress >= 80) return 'success';
    if (progress >= 60) return 'warning';
    return 'error';
  };

  if (goalGroups.length === 0) {
    return (
      <Box sx={{ p: 2, textAlign: 'center' }}>
        <Typography variant="body2" color="text.secondary">
          No goals configured yet.
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', overflow: 'auto' }}>
      {goalGroups.map((group) => (
        <Accordion key={group.id} defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <FlagIcon fontSize="small" />
              <Typography variant="subtitle1" fontWeight="bold">
                {group.name}
              </Typography>
              <Chip 
                label={group.goals.length} 
                size="small" 
                color="primary" 
                variant="outlined"
              />
            </Box>
          </AccordionSummary>
          <AccordionDetails sx={{ p: 0 }}>
            <List dense>
              {group.goals.map((goal) => {
                const progress = calculateProgress(goal);
                const isHighlighted = highlightedGoals.includes(goal.id);
                
                return (
                  <ListItem
                    key={goal.id}
                    disablePadding
                    sx={{
                      backgroundColor: isHighlighted ? 'action.hover' : 'transparent',
                      transition: 'background-color 0.2s ease'
                    }}
                  >
                    <ListItemButton
                      onMouseEnter={() => handleGoalHover(goal.id)}
                      onMouseLeave={handleGoalLeave}
                      sx={{ flexDirection: 'column', alignItems: 'stretch', p: 2 }}
                    >
                      <Box sx={{ width: '100%', mb: 1 }}>
                        <Typography variant="body2" fontWeight="medium" gutterBottom>
                          {goal.name}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" display="block">
                          {goal.description}
                        </Typography>
                      </Box>
                      
                      <Box sx={{ width: '100%', mb: 1 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                          <Typography variant="caption" color="text.secondary">
                            Progress
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {progress.toFixed(1)}%
                          </Typography>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={progress}
                          color={getProgressColor(progress) as any}
                          sx={{ height: 4, borderRadius: 2 }}
                        />
                      </Box>
                      
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Chip
                            label={`${goal.currentValue}/${goal.targetValue}`}
                            size="small"
                            variant="outlined"
                          />
                          <Chip
                            label={goal.targetYear}
                            size="small"
                            color="primary"
                            variant="filled"
                          />
                        </Box>
                        {goal.linkedSubareaIds.length > 0 && (
                          <Chip
                            label={`${goal.linkedSubareaIds.length} subarea${goal.linkedSubareaIds.length === 1 ? '' : 's'}`}
                            size="small"
                            color="secondary"
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </ListItemButton>
                  </ListItem>
                );
              })}
            </List>
          </AccordionDetails>
        </Accordion>
      ))}
    </Box>
  );
}; 