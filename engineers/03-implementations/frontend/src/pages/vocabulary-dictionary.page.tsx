import React, { useEffect } from 'react';
import { Renderer } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import spec from '@/schemas/vocabulary-dictionary.render-schema.json';
import { useListWordBanks } from '@/hooks/use-list-word-banks';
import { store } from '@/store';

export default function VocabularyDictionaryPage() {
  const [activeFilter, setActiveFilter] = React.useState<any>(undefined);
  const { data: wordBanks, refetch } = useListWordBanks(activeFilter);

  React.useEffect(() => {
    store.set('/actions/triggerSearch', () => {
      const searchField = store.get('/form/search-field');
      const letterSelect = store.get('/form/letter-select');
      const difficultySelect = store.get('/form/difficulty-select');

      const filter: any = {};
      if (searchField) filter.word = searchField;
      if (letterSelect && letterSelect !== "Any") filter.startingLetter = letterSelect;
      if (difficultySelect && difficultySelect !== "Any") filter.difficultyLevel = parseInt(difficultySelect as string, 10);
      
      const newFilter = Object.keys(filter).length > 0 ? filter : undefined;
      setActiveFilter(newFilter);
      setTimeout(() => refetch(), 0);
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [store]);

  useEffect(() => {
    if (wordBanks) {
      store.set('/data/listWordBanks', wordBanks);
    }
  }, [wordBanks, store]);

  return <Renderer spec={spec} registry={componentRegistry} />;
}
