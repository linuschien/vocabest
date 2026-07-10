import React, { useEffect } from 'react';
import { Renderer, useStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import spec from '@/schemas/vocabulary-dictionary.render-schema.json';
import { useListWordBanks } from '@/hooks/use-list-word-banks';

export default function VocabularyDictionaryPage() {
  const store = useStateStore();
  const searchField = store.get('/form/search-field');
  const letterSelect = store.get('/form/letter-select');
  const difficultySelect = store.get('/form/difficulty-select');

  const lastSearchTriggered = store.get('/data/lastSearchTriggered');

  // Build filter object based on form values when search is triggered
  const filterInput = React.useMemo(() => {
    const filter: any = {};
    if (searchField) filter.word = searchField;
    if (letterSelect) filter.startingLetter = letterSelect;
    if (difficultySelect) filter.difficultyLevel = parseInt(difficultySelect as string, 10);
    return Object.keys(filter).length > 0 ? filter : undefined;
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lastSearchTriggered]);

  const { data: wordBanks } = useListWordBanks(filterInput);

  useEffect(() => {
    if (wordBanks) {
      store.set('/data/listWordBanks', wordBanks);
    }
  }, [wordBanks, store]);

  return <Renderer spec={spec} registry={componentRegistry} />;
}
