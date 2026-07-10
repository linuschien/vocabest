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

  // Build filter object based on form values
  const filterInput = React.useMemo(() => {
    const filter: any = {};
    if (searchField) filter.word = searchField;
    if (letterSelect) filter.startsWith = letterSelect;
    if (difficultySelect) filter.difficultyLevel = difficultySelect;
    return Object.keys(filter).length > 0 ? filter : undefined;
  }, [searchField, letterSelect, difficultySelect]);

  const { data: wordBanks } = useListWordBanks(filterInput);

  useEffect(() => {
    if (wordBanks) {
      store.set('/data/listWordBanks', wordBanks);
    }
  }, [wordBanks, store]);

  return <Renderer spec={spec} registry={componentRegistry} />;
}
