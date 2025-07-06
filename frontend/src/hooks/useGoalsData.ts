import { useState, useEffect } from 'react';
import { DashboardGoal, GoalGroup } from '@/types/dashboard';
import { apiClient } from '@/lib/api';

export const useGoalsData = () => {
  const [goals, setGoals] = useState<DashboardGoal[]>([]);
  const [goalGroups, setGoalGroups] = useState<GoalGroup[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchGoalsData = async () => {
      try {
        setIsLoading(true);
        setError(null);

        // Fetch goal groups
        const goalGroupsData = await apiClient.get('/goal-groups');

        // Fetch goals
        const goalsData = await apiClient.get('/goals');

        // Fix: Map goalGroupId and goalGroupName from nested goalGroup object
        const transformedGoals: DashboardGoal[] = goalsData.map((goal: any) => ({
          id: goal.id.toString(),
          name: goal.name,
          description: goal.description,
          goalGroupId: goal.goalGroup?.id?.toString() || '',
          goalGroupName: goal.goalGroup?.name || '',
          targetYear: goal.year || new Date().getFullYear(),
          targetValue: goal.targetValue || 0,
          currentValue: goal.currentValue || 0,
          linkedSubareaIds: [] // Will be populated from goal-subarea relationships
        }));

        // Transform goal groups data
        const transformedGoalGroups: GoalGroup[] = goalGroupsData.map((group: any) => ({
          id: group.id.toString(),
          name: group.name,
          description: group.description,
          goals: transformedGoals.filter(goal => goal.goalGroupId === group.id.toString())
        }));

        setGoals(transformedGoals);
        setGoalGroups(transformedGoalGroups);

        // Note: Goal-subarea relationships are now handled by the useDashboardWithRelationships hook
        // This hook is kept for backward compatibility but doesn't fetch relationships anymore

      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch goals data');
      } finally {
        setIsLoading(false);
      }
    };

    fetchGoalsData();
  }, []);

  return { goals, goalGroups, isLoading, error };
}; 