import React, { useEffect } from 'react';
import { Renderer, useStateStore, useStateValue } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import spec from '@/schemas/wordbank-quizzes.render-schema.json';
import { useListQuizQuestions } from '@/hooks/use-list-quiz-questions';

export default function WordbankQuizzesPage() {
  const store = useStateStore();
  const activeWordBankId = useStateValue('/data/activeWordBankId');
  const page = useStateValue('/form/page') as number || 1;
  
  const filter: any = {
    ...(activeWordBankId ? { wordBankId: activeWordBankId } : {})
  };

  const { data: questions } = useListQuizQuestions(filter);

  useEffect(() => {
    if (questions) {
      store.set('/data/listQuizQuestions', questions);
    }
  }, [questions]);

  return <Renderer spec={spec} registry={componentRegistry} />;
}
