import { useState, useEffect } from 'react';
import { DashboardArea, DashboardSubarea, DashboardGoal, GoalGroup } from '@/types/dashboard';
import { apiClient } from '@/lib/api';

interface BackendGoalResponse {
  id: number;
  goalGroup?: { id: number; name: string };
  name: string;
  description: string;
  year: number;
}

interface BackendAreaResponse {
  id: number;
  name: string;
  description: string;
  code: string;
}

interface BackendSubareaResponse {
  id: number;
  name: string;
  description: string;
  code: string;
  areaId?: number;
  areaName?: string;
  indicatorCount?: number;
}

interface BackendGoalGroupResponse {
  id: number;
  name: string;
  description: string;
}

interface DashboardWithRelationshipsResponse {
  areas: BackendAreaResponse[];
  subareas: BackendSubareaResponse[];
  goals: BackendGoalResponse[];
  goalGroups: BackendGoalGroupResponse[];
  relationships: {
    goalToSubareas: { [goalId: string]: string[] }; // goal ID → subarea IDs
    subareaToGoals: { [subareaId: string]: string[] }; // subarea ID → goal IDs
  };
  lastUpdated: string;
}

export const useDashboardWithRelationships = () => {
  const [areas, setAreas] = useState<DashboardArea[]>([]);
  const [subareas, setSubareas] = useState<DashboardSubarea[]>([]);
  const [goals, setGoals] = useState<DashboardGoal[]>([]);
  const [goalGroups, setGoalGroups] = useState<GoalGroup[]>([]);
  const [relationships, setRelationships] = useState<{
    goalToSubareas: { [goalId: string]: string[] };
    subareaToGoals: { [subareaId: string]: string[] };
  }>({ goalToSubareas: {}, subareaToGoals: {} });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardWithRelationships = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const response: DashboardWithRelationshipsResponse = await apiClient.get('/dashboard-data/dashboard-with-relationships');

        // Transform areas data to match DashboardArea interface
        const transformedAreas: DashboardArea[] = response.areas.map((area) => ({
          id: area.id.toString(),
          name: area.name,
          description: area.description,
          code: area.code,
          position: { x: 0, y: 0 }, // Will be calculated by layout engine
          performance: {
            score: 0, // Will be fetched from performance service
            colorCode: '#cccccc'
          },
          subareaCount: 0 // Will be calculated
        }));

        // Transform subareas data to match DashboardSubarea interface
        const transformedSubareas: DashboardSubarea[] = response.subareas.map((subarea) => ({
          id: subarea.id.toString(),
          name: subarea.name,
          description: subarea.description,
          code: subarea.code,
          areaId: subarea.areaId?.toString() || '',
          areaName: subarea.areaName || '',
          position: { x: 0, y: 0 }, // Will be calculated by layout engine
          performance: {
            score: 0, // Will be fetched from performance service
            colorCode: '#cccccc'
          },
          indicatorCount: subarea.indicatorCount || 0,
          goalCount: 0 // Will be calculated from relationships
        }));

        // Transform goals data to match DashboardGoal interface
        const transformedGoals: DashboardGoal[] = response.goals.map((goal) => ({
          id: goal.id.toString(),
          name: goal.name,
          description: goal.description,
          goalGroupId: goal.goalGroup?.id?.toString() || '',
          goalGroupName: goal.goalGroup?.name || '',
          targetYear: goal.year || new Date().getFullYear(),
          targetValue: 0, // Not available in backend DTO
          currentValue: 0, // Not available in backend DTO
          linkedSubareaIds: response.relationships.goalToSubareas[goal.id.toString()] || []
        }));

        // Transform goal groups data to match GoalGroup interface
        const transformedGoalGroups: GoalGroup[] = response.goalGroups.map((group) => ({
          id: group.id.toString(),
          name: group.name,
          description: group.description,
          goals: transformedGoals.filter(goal => goal.goalGroupId === group.id.toString())
        }));

        // Calculate subarea goal counts
        const subareasWithGoalCounts = transformedSubareas.map(subarea => ({
          ...subarea,
          goalCount: response.relationships.subareaToGoals[subarea.id]?.length || 0
        }));

        // Calculate area subarea counts
        const areasWithSubareaCounts = transformedAreas.map(area => ({
          ...area,
          subareaCount: subareasWithGoalCounts.filter(sub => sub.areaId === area.id).length
        }));

        setAreas(areasWithSubareaCounts);
        setSubareas(subareasWithGoalCounts);
        setGoals(transformedGoals);
        setGoalGroups(transformedGoalGroups);
        setRelationships(response.relationships);

      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch dashboard data with relationships');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardWithRelationships();
  }, []);

  return { 
    areas, 
    subareas, 
    goals, 
    goalGroups, 
    relationships, 
    isLoading, 
    error 
  };
}; 