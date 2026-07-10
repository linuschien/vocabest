import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
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
import { toast } from 'sonner';

export default function BehaviorProvider({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();
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
            await submitAnswer.mutateAsync(payload);
            toast.success('Answer submitted!');
          } else if (ref === 'triggerSearch') {
            const trigger = store.get('/actions/triggerSearch') as any;
            if (trigger) trigger();
            store.set('/data/lastSearchTriggered', Date.now());
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
