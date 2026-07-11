import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { JSONUIProvider } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { store } from '@/store';
import { useOnboardUser } from '@/hooks/use-onboard-user';
import { useCreateWordBank } from '@/hooks/use-create-word-bank';
import { useDeleteWordBank } from '@/hooks/use-delete-word-bank';
import { useCreateQuizQuestion } from '@/hooks/use-create-quiz-question';
import { useDeleteQuizQuestion } from '@/hooks/use-delete-quiz-question';
import { useSubmitAnswer } from '@/hooks/use-submit-answer';
import { useUpdateUser } from '@/hooks/use-update-user';
import { whoamiKeys } from '@/hooks/use-whoami';
import { listDailyProgressesKeys } from '@/hooks/use-list-daily-progresses';
import { toast } from 'sonner';

export default function BehaviorProvider({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const onboardUser = useOnboardUser();
  const updateUser = useUpdateUser();
  const createWordBank = useCreateWordBank();
  const deleteWordBank = useDeleteWordBank();
  const createQuizQuestion = useCreateQuizQuestion();
  const deleteQuizQuestion = useDeleteQuizQuestion();
  const submitAnswer = useSubmitAnswer();

  const handlers = useMemo(() => {
    return {
      navigate: (params: any) => {
        if (params?.path) navigate(params.path);
      },
      openModal: (params: any) => {
        if (params?.id) store.set(`/modals/${params.id}`, true);
      },
      executeBehavior: async (params: any) => {
        const { ref, payload } = params ?? {};
        try {
          if (ref === 'onboardUser') {
            const mappedPayload = { ...payload };
            if (mappedPayload.targetLevel === '國中2000單字') mappedPayload.targetLevel = 'JUNIOR_HIGH';
            if (mappedPayload.targetLevel === '高中7000單字') mappedPayload.targetLevel = 'SENIOR_HIGH';
            
            const isFirstTime = store.get('/data/isFirstTimeOnboarding');
            if (isFirstTime) {
              await onboardUser.mutateAsync(mappedPayload);
              toast.success('Onboarding successful!');
            } else {
              const user = store.get('/data/user') as any;
              await updateUser.mutateAsync({
                id: user.id,
                payload: {
                  email: user.email,
                  role: user.role,
                  learningStreak: user.learningStreak,
                  targetLevel: mappedPayload.targetLevel,
                  dailyTargetQuestions: parseInt(mappedPayload.dailyTargetQuestions, 10)
                }
              });
              toast.success('Settings updated!');
            }
            store.set('/modals/onboarding-modal', false);
          } else if (ref === 'createWordBank') {
            await createWordBank.mutateAsync(payload);
            toast.success('Word created successfully.');
          } else if (ref === 'deleteWordBank') {
            await deleteWordBank.mutateAsync({ id: payload.id });
            toast.success('Word deleted successfully.');
          } else if (ref === 'createQuizQuestion') {
            await createQuizQuestion.mutateAsync(payload);
            toast.success('Question created successfully.');
          } else if (ref === 'deleteQuizQuestion') {
            await deleteQuizQuestion.mutateAsync({ id: payload.id, wordBankId: payload.wordBankId });
            toast.success('Question deleted successfully.');
          } else if (ref === 'submitAnswer') {
            const user = store.get('/data/user') as any;
            const result = await submitAnswer.mutateAsync({ ...payload, userId: user?.id }) as any;
            // Save result to store for the explanation modal
            store.set('/data/lastAnswerResult', result);
            store.set('/data/lastAnswerResultLabel', result?.isCorrect ? '✅ 答對了！' : '❌ 答錯了');
            store.set('/data/lastAnswerCorrectLabel', result?.isCorrect ? '' : `正確答案：${result?.correctAnswer}`);
            // Open explanation modal
            store.set('/modals/explanation-modal', true);
            // Refresh user data (learningStreak) and daily progress in background
            // Precise invalidation: do NOT invalidate getNextQuestion to avoid breaking the modal
            queryClient.invalidateQueries({ queryKey: whoamiKeys.all });
            queryClient.invalidateQueries({ queryKey: listDailyProgressesKeys.all });
          } else if (ref === 'triggerSearch') {
            const trigger = store.get('/actions/triggerSearch') as any;
            if (trigger) trigger(payload);
            store.set('/data/lastSearchTriggered', Date.now());
          } else if (ref === 'clearSearch') {
            const clear = store.get('/actions/clearSearch') as any;
            if (clear) clear();
            store.set('/data/lastSearchTriggered', Date.now());
          } else if (ref === 'pageChange') {
            const onPageChange = store.get('/actions/pageChange') as any;
            if (onPageChange) onPageChange(payload);
          } else if (ref === 'loadNextQuestion') {
            // Close explanation modal and load next question
            store.set('/modals/explanation-modal', false);
            const loadNext = store.get('/actions/loadNextQuestion') as any;
            if (loadNext) loadNext();
          } else {
            console.warn(`Unmapped behavior ref: ${ref}`, payload);
          }
        } catch (err: any) {
          toast.error(err.message || 'Operation failed');
        }
      }
    };
  }, [navigate, onboardUser, createWordBank, deleteWordBank, createQuizQuestion, deleteQuizQuestion, submitAnswer]);

  return (
    <JSONUIProvider registry={componentRegistry} store={store} handlers={handlers as any}>
      {children}
    </JSONUIProvider>
  );
}
